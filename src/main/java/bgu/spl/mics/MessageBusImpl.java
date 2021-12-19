package bgu.spl.mics;


import bgu.spl.mics.application.messages.broadcasts.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminateBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.PublishResultsEvent;
import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.StudentService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 * "Basic queries can be implemented" - from forum.
 */


public class MessageBusImpl implements MessageBus {
    private final ConcurrentHashMap<Class<? extends Event>, LinkedBlockingQueue<MicroService>> eventSubscribers;
    private final ConcurrentHashMap<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> broadcastSubscribers;
    private final ConcurrentHashMap<Message, Future> messageToFuture;
    private final ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> messagesQueue;

    private static class MessageBusHolder {
        private static final MessageBusImpl messageBusInstance = new MessageBusImpl();
    }

    private MessageBusImpl() {
        eventSubscribers = new ConcurrentHashMap<>();
        broadcastSubscribers = new ConcurrentHashMap<>();
        messageToFuture = new ConcurrentHashMap<>();
        messagesQueue = new ConcurrentHashMap<>();
        initBroadcasts();
        initEvents();
    }

    public void initBroadcasts() {
        broadcastSubscribers.put(TerminateBroadcast.class, new CopyOnWriteArrayList<>());
        broadcastSubscribers.put(TickBroadcast.class, new CopyOnWriteArrayList<>());
        broadcastSubscribers.put(PublishConferenceBroadcast.class, new CopyOnWriteArrayList<>());
    }

    public void initEvents() {
        eventSubscribers.put(TrainModelEvent.class, new LinkedBlockingQueue<>());
        eventSubscribers.put(TestModelEvent.class, new LinkedBlockingQueue<>());
        eventSubscribers.put(PublishResultsEvent.class, new LinkedBlockingQueue<>());
    }

    public static MessageBusImpl getInstance() {
        return MessageBusHolder.messageBusInstance;
    }


    public int ammOfSubs(Class<? extends Event> type){ //Used by StudentService
        if(!eventSubscribers.containsKey(type)){
            return 0;
        }
        return eventSubscribers.get(type).size();
    }

    public boolean isEventSubsEmpty(Class<? extends Event> type) {
        if (!eventSubscribers.containsKey(type)) {
            return true;
        }
        return eventSubscribers.get(type).isEmpty();
    }

    public boolean isBroadcastSubsEmpty(Class<? extends Broadcast> type) {
        if (!broadcastSubscribers.containsKey(type)) return true;
        return broadcastSubscribers.get(type).isEmpty();
    }

    public boolean isRegistered(MicroService m) {
        return messagesQueue.containsKey(m);
    }


    /**
     * Will be used by microservices which have other tasks they need to work on, not via the bus
     */
    public Boolean isMessageQueueEmpty(MicroService m) {
        if (!messagesQueue.containsKey(m)) return true;
        return messagesQueue.get(m).isEmpty();
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        if (m == null || type == null) return;
        //The event is registered in the map
        if (eventSubscribers.containsKey(type)) {
            eventSubscribers.get(type).add(m);
        }
    }


    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        if (m == null || type == null) return;
        // The event is registered in the map
        if (broadcastSubscribers.containsKey(type)) {
            broadcastSubscribers.get(type).add(m);
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        messageToFuture.remove(e).resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        Class<? extends Broadcast> type = b.getClass();

        if (!broadcastSubscribers.containsKey(type) || broadcastSubscribers.get(type).isEmpty()) return;
        if (type == TerminateBroadcast.class) {
            for (MicroService m : broadcastSubscribers.get(type)) {
                messagesQueue.get(m).clear();
                messagesQueue.get(m).add(b);
                m.notifyMicroService();
            }

        } else {
            for (MicroService m : broadcastSubscribers.get(type)) {
                if(messagesQueue.get(m) != null) { //Avoiding getting to a null array that exists mid unregistration
                    messagesQueue.get(m).add(b);
                    m.notifyMicroService();
                }
            }
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        Class<? extends Event> type = e.getClass();

        if (!eventSubscribers.containsKey(type) || eventSubscribers.get(type).isEmpty()) {
            return null;
        }

        MicroService eventHandler = eventSubscribers.get(type).poll();
        if (eventHandler == null) {
            return null;
        }
        Future<T> future = new Future<>();
        messageToFuture.put(e, future);
        messagesQueue.get(eventHandler).add(e);
        try {
            eventSubscribers.get(type).put(eventHandler);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        eventHandler.notifyMicroService();
        return future;
    }

    @Override
    public void register(MicroService m) {
        messagesQueue.put(m, new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(MicroService m) {

        for (Class<? extends Message> type : m.getMessagesCallbacks()) {
            if (type == null) continue;

            // meaning this microservice is registered to the event subscribers of said event.
            if (eventSubscribers.containsKey(type)) {
                eventSubscribers.get(type).remove(m);
            }

            // meaning this microservice is registered to the broadcast subs of said broadcast.
            if (broadcastSubscribers.containsKey(type)) {
                broadcastSubscribers.get(type).remove(m);
            }

            //Finally, remove the message queue of the microservice.
            messagesQueue.remove(m);
        }
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {

        if (!isRegistered(m)) throw new IllegalStateException(); // this microservice does not exist

        synchronized (m.lock) {
            while (messagesQueue.get(m).isEmpty()) {
                // will be notified when it gets a message. no need to be in a while loop since only this method can remove from m's queue
                m.lock.wait();
            }
            return messagesQueue.get(m).poll();
        }
    }


    public boolean isEventProcessed(Event event, MicroService m) { //Only for test
        return messagesQueue.containsKey(m) && messagesQueue.get(m).contains(event);
    }


    public boolean isBroadcastProcessed(Broadcast broadcast, MicroService m) { //Only for test
        return messagesQueue.containsKey(m) && messagesQueue.get(m).contains(broadcast);
    }
}
