package bgu.spl.mics;


import bgu.spl.mics.application.messages.broadcasts.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TerminateBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.PublishResultsEvent;
import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;

import java.util.Map;
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


    private final Map<Class<? extends Event>, LinkedBlockingQueue<MicroService>> event_subscribers; //READ BELOW
    //this hashmap will contain all the event classes, and for each, a pair of:all the subscribed microservices for that event class,
    //and the counter needed for the round-robin implementation.

    private final Map<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> broadcast_subscribers;

    private final ConcurrentHashMap<Message, Future> message_future;

    private final ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> messagesQueue;
    private final Object lock = new Object();

    private static class MessageBusHolder {
        private static final MessageBusImpl messageBusInstance = new MessageBusImpl();

    }

    private MessageBusImpl() {
        event_subscribers = new ConcurrentHashMap<>();
        broadcast_subscribers = new ConcurrentHashMap<>();
        message_future = new ConcurrentHashMap<>();
        messagesQueue = new ConcurrentHashMap<>();
        InitBroadcasts();
        InitEvents();

    }

    public void InitBroadcasts() {
        broadcast_subscribers.put(TerminateBroadcast.class, new CopyOnWriteArrayList<>());
        broadcast_subscribers.put(TickBroadcast.class, new CopyOnWriteArrayList<>());
        broadcast_subscribers.put(PublishConferenceBroadcast.class, new CopyOnWriteArrayList<>());
    }

    public void InitEvents() {
        event_subscribers.put(TrainModelEvent.class, new LinkedBlockingQueue<>());
        event_subscribers.put(TestModelEvent.class, new LinkedBlockingQueue<>());
        event_subscribers.put(PublishResultsEvent.class, new LinkedBlockingQueue<>());
    }


    public static MessageBusImpl getInstance() {
        return MessageBusHolder.messageBusInstance;
    }


    //Basic Queries
    public boolean isEventSubsEmpty(Class<? extends Event> type) {
        if (!event_subscribers.containsKey(type)) return true;
        return event_subscribers.get(type).isEmpty();
    }

    public boolean isBroadcastSubsEmpty(Class<? extends Broadcast> type) {
        if (broadcast_subscribers.containsKey(type)) return true;
        return broadcast_subscribers.get(type).isEmpty();
    }

    public boolean isRegistered(MicroService m) {
        return messagesQueue.containsKey(m);
    }


    public Boolean isMessageQueueEmpty(MicroService m) { //Will be used by micro services which have other tasks they need to work on, not via the buss

        if (!messagesQueue.containsKey(m)) return true;
        return messagesQueue.get(m).size() == 0;
    }


    //Methods
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        if (m == null || type == null) return;
        if (event_subscribers.containsKey(type)) { //The event is registered in the map
            event_subscribers.get(type).add(m);
        }
    }


    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        if (m == null || type == null) return;
        if (broadcast_subscribers.containsKey(type)) { //The event is registered in the map
            broadcast_subscribers.get(type).add(m);
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        message_future.remove(e).resolve(result);

    }

    @Override
    public void sendBroadcast(Broadcast b) {
        Class<? extends Broadcast> type = b.getClass();

        if (!broadcast_subscribers.containsKey(type) || broadcast_subscribers.get(type).size() == 0) return;
        if (type == TerminateBroadcast.class) {
            for (MicroService m : broadcast_subscribers.get(type)) {
                messagesQueue.get(m).clear();
                messagesQueue.get(m).add(b);
                m.notifyMicroService();
            }

        } else {
            for (MicroService m : broadcast_subscribers.get(type)) {
                messagesQueue.get(m).add(b);
                m.notifyMicroService();
            }
        }


    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        Class<? extends Event> type = e.getClass();

        if (!event_subscribers.containsKey(type) || event_subscribers.get(type).isEmpty()) {
            return null;
        }

        synchronized (event_subscribers.get(type)) {
            MicroService eventHandler = event_subscribers.get(type).poll();
            if (eventHandler == null) return null;
            Future<T> future = new Future<>();
            message_future.put(e, future);
            messagesQueue.get(eventHandler).add(e);


            try {
                event_subscribers.get(type).put(eventHandler);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            eventHandler.notifyMicroService();

            return future;
        }
    }

    @Override
    public void register(MicroService m) {
        messagesQueue.put(m, new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(MicroService m) {

        for (Class<? extends Message> type : m.getMessagesCallbacks()) {
            if (type == null) continue;

            if (event_subscribers.containsKey(type)) { // meaning this micro service is registered to the event subscribers of said event.
                event_subscribers.get(type).remove(m);
            }

            if (broadcast_subscribers.containsKey(type)) { // meaning this microservice is registered to the broadcast subs of said broadcast.
                broadcast_subscribers.get(type).remove(m);
            }

            messagesQueue.remove(m); //Finally, remove the message queue of the microservice.
        }

    }


    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {

        if (!isRegistered(m)) throw new IllegalStateException(); // this microservice does not exist

        synchronized (m) {
            while (messagesQueue.get(m).isEmpty()) {

                m.wait(); // will be notified when it gets a message. no need to be in a while loop since only this method can remove from m's queue
            }

            return messagesQueue.get(m).poll();

        }
    }


    public boolean isEventProcessed(Event event) { //Only for test
        for (MicroService m : messagesQueue.keySet()) {
            if (messagesQueue.get(m).contains(event)) return true;
        }
        return false;
    }


    public boolean isBroadcastProcessed(Broadcast broadcast) { //Only for test
        for (MicroService m : messagesQueue.keySet()) {
            if (messagesQueue.get(m).contains(broadcast)) return true;
        }

        return false;
    }

}