package bgu.spl.mics;


import bgu.spl.mics.application.broadcasts.TerminateBroadcast;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 * "Basic queries can be implemented" - from forum.
 */


public class MessageBusImpl implements MessageBus {
    private final ConcurrentHashMap<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> broadcastSubscribers;
    private final ConcurrentHashMap<MicroService, CopyOnWriteArrayList<Message>> microServicesMessages;
    private final ConcurrentHashMap<Class<? extends Event<?>>, MutablePair<CopyOnWriteArrayList<MicroService>, AtomicInteger>> eventSubscribers;

    private static class MessageBusHolder {
        private static final MessageBusImpl messageBusInstance = new MessageBusImpl();
    }

    private MessageBusImpl() {
        this.broadcastSubscribers = new ConcurrentHashMap<>();
        this.microServicesMessages = new ConcurrentHashMap<>();
        this.eventSubscribers = new ConcurrentHashMap<>();
    }

    public static MessageBusImpl getInstance() {
        return MessageBusHolder.messageBusInstance;
    }

    public boolean isEventSubscribersEmpty(Class<? extends Event<?>> event) {
        MutablePair<CopyOnWriteArrayList<MicroService>, AtomicInteger> result = eventSubscribers.getOrDefault(event,
                new MutablePair<>(new CopyOnWriteArrayList<>(), new AtomicInteger(0)));
        return result.left.isEmpty();
    }

    @Override
    public boolean isEventSubsEmpty(Class<? extends Event> type) {
        return false;
    }

    public boolean isBroadcastSubsEmpty(Class<? extends Broadcast> event) {
        if (broadcastSubscribers.get(event) == null) return true;
        return broadcastSubscribers.get(event).isEmpty();
    }

    public boolean isRegistered(MicroService m) {
        return microServicesMessages.containsKey(m);
    }


    public boolean isEventProcessed(Event event) {
        for (MicroService m : microServicesMessages.keySet()) {
            if (microServicesMessages.get(m).contains(event)) return true;
        }
        return false;
    }

    public boolean isBroadcastProcessed(Broadcast broadcast) {
        for (MicroService m : microServicesMessages.keySet()) {
            if (microServicesMessages.get(m).contains(broadcast)) return true;
        }

        return false;
    }

    public boolean isMessageQueueEmpty(MicroService m) { //Will be used by microservices which have other tasks they need to work on, not via the buss

        if (!microServicesMessages.containsKey(m)) return true;
        return microServicesMessages.get(m).size() == 0;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> event, MicroService m) {
        this.eventSubscribers.putIfAbsent(event, new MutablePair<>(new CopyOnWriteArrayList<>(), new AtomicInteger(0)));
        this.eventSubscribers.get(event).left.add(m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> event, MicroService m) {
        this.broadcastSubscribers.putIfAbsent(event, new CopyOnWriteArrayList<>());
        this.broadcastSubscribers.get(event).add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        // needs to somehow connect with the MS that send the event.
        e.getFuture().resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        Class<? extends Broadcast> type = b.getClass();

        if (!broadcastSubscribers.containsKey(type) || this.broadcastSubscribers.get(type).size() == 0) return;
        if (type == TerminateBroadcast.class) {
            for (MicroService m : broadcastSubscribers.get(type)) {
                this.microServicesMessages.get(m).clear();
                this.microServicesMessages.get(m).add(b);
                m.notifyMicroService();

            }

        } else {
            for (MicroService m : this.broadcastSubscribers.get(type)) {
                this.microServicesMessages.get(m).add(b);
                m.notifyMicroService();
            }
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        Class<? extends Event> type = e.getClass();

        if (!eventSubscribers.containsKey(type) || eventSubscribers.get(type).getKey().size() == 0)
            return new Future<>();

        CopyOnWriteArrayList<MicroService> microServiceList = eventSubscribers.get(type).getKey();
        int counter = eventSubscribers.get(type).getValue().get();
        microServicesMessages.get(microServiceList.get(counter)).add(e);
        microServiceList.get(counter).notifyMicroService();
        eventSubscribers.get(type).getValue().set((counter + 1) % (eventSubscribers.get(type).getKey().size())); //increase the counter

        return new Future<>();
    }

    @Override
    public void register(MicroService m) {
        this.microServicesMessages.put(m, new CopyOnWriteArrayList<>());
    }

    @Override
    public void unregister(MicroService m) {

        for (Class<? extends Message> type : m.getMessagesCallbacks()) {
            if (type == null) continue;

            // meaning this microservice is registered to the event subscribers of said event.
            if (eventSubscribers.containsKey(type) && eventSubscribers.get(type).getKey().contains(m)) {
                synchronized (eventSubscribers) {
                    eventSubscribers.get(type).getKey().remove(m);
                    int prev_counter = eventSubscribers.get(type).getValue().get();
                    //The counter's value is now illegal, for example it's 5 while there are now 5 microservices
                    if (prev_counter == eventSubscribers.get(type).getKey().size() && prev_counter != 0)
                        eventSubscribers.get(type).getValue().set(prev_counter - 1);
                }
                // meaning this microservice is registered to the broadcast subs of said broadcast.
            } else if (broadcastSubscribers.containsKey(type)) {
                broadcastSubscribers.get(type).remove(m);
            }

            microServicesMessages.remove(m); //Finally, remove the message queue of the microservice.
        }

    }


    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {

        if (!isRegistered(m)) throw new IllegalStateException(); // this microservice does not exist
        synchronized (m) {
            if (microServicesMessages.get(m).size() == 0) {

                m.wait(); // will be notified when it gets a message. no need to be in a while loop since only this method can remove from m's queue
            }
            return microServicesMessages.get(m).remove(0);

        }
    }
}
