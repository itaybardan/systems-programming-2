package bgu.spl.mics;


import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;
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

    private static MessageBusImpl instance;
    private Map<Class<? extends Event<?>>, MutablePair<CopyOnWriteArrayList<MicroService>, AtomicInteger>> event_subscribers; //READ BELOW
    //this hashmap will contain all the event classes, and for each, a pair of:all the subscribed microservices for that event class,
    //and the counter needed for the round-robin implementation.

    private Map<Class<? extends Broadcast>, CopyOnWriteArrayList<MicroService>> broadcast_subscribers;

    private Map<MicroService, CopyOnWriteArrayList<Message>> messagesQueue;

    private static class MessageBusHolder {
        private static MessageBusImpl messageBusInstance = new MessageBusImpl();

    }

    private MessageBusImpl() {
        event_subscribers = new ConcurrentHashMap<>();
        broadcast_subscribers = new ConcurrentHashMap<>();
        messagesQueue = new ConcurrentHashMap<>();
    }


    public static MessageBusImpl getInstance() {
        return MessageBusHolder.messageBusInstance;
    }


    //Basic Queries
    public boolean isEventSubsEmpty(Class<? extends Event> type) {
        if (event_subscribers.get(type) == null) return true;
        return event_subscribers.get(type).getKey().isEmpty();
    }

    public boolean isBroadcastSubsEmpty(Class<? extends Broadcast> type) {
        if (broadcast_subscribers.get(type) == null) return true;
        return broadcast_subscribers.get(type).isEmpty();
    }

    public boolean isRegistered(MicroService m) {
        return messagesQueue.containsKey(m);
    }


    public boolean isEventProcessed(Event event) {
        for (MicroService m : messagesQueue.keySet()) {
            if (messagesQueue.get(m).contains(event)) return true;
        }
        return false;
    }


    public boolean isBroadcastProcessed(Broadcast broadcast) {
        for (MicroService m : messagesQueue.keySet()) {
            if (messagesQueue.get(m).contains(broadcast)) return true;
        }

        return false;
    }


    //Methods
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        MutablePair<CopyOnWriteArrayList<MicroService>, AtomicInteger> temp_pair;
        if (!event_subscribers.containsKey(type)) { //The event is not registered yet in the map.
            temp_pair = new MutablePair<>(new CopyOnWriteArrayList<>(), new AtomicInteger(0)); // counter represents the current micro service to give an event
            temp_pair.getKey().add(m); //Add the micro service to the list that represents all the subscribed micro services for this event type
            event_subscribers.put(type, temp_pair);

        } else {

            CopyOnWriteArrayList<MicroService> temp_array = event_subscribers.get(type).getKey(); //The event exists and is already initialized in the map.
            temp_array.add(m);

        }
    }


    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        CopyOnWriteArrayList<MicroService> temp_array;
        if (!broadcast_subscribers.containsKey(type)) { //The broadcast is not registered yet in the map.
            temp_array = new CopyOnWriteArrayList<>();
            temp_array.add(m);
            broadcast_subscribers.put(type, temp_array);

        } else {
            //The broadcast exists and is already initialized in the map.
            broadcast_subscribers.get(type).add(m);
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) { // needs to somehow connect with the MS that send the event.
        e.getFuture().resolve(result);

    }

    @Override
    public void sendBroadcast(Broadcast b) {
        for (MicroService m : broadcast_subscribers.get(b.getClass())) {
            m.notifyMicroService();
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        Class<? extends Event> type = e.getClass();

        if (!event_subscribers.containsKey(type))
            return null;

        CopyOnWriteArrayList<MicroService> microServiceslist = event_subscribers.get(type).getKey();
        int counter = event_subscribers.get(type).getValue().get();
        messagesQueue.get(microServiceslist.get(counter)).add(e);
        event_subscribers.get(type).getValue().set((counter + 1) % (event_subscribers.get(type).getKey().size())); //increase the counter


        microServiceslist.get(counter).notifyMicroService();

        Future<T> future = new Future(); //e.setFuture?
        return future;
    }

    @Override
    public void register(MicroService m) {
        if (messagesQueue.containsKey(m)) return; //This micro service is already implemented, do nothing.
        messagesQueue.put(m, new CopyOnWriteArrayList<>());

    }

    @Override
    public void unregister(MicroService m) {

        for (Class<? extends Event> type : m.getEventsSubs()) {

            if (type != null && event_subscribers.containsKey(type)) { // meaning this micro service is registered to the event subscribers of said event.
                //TODO when we initialize a micro service, we subscribe to the appropriate event and initialize it's event_sub field.
                event_subscribers.get(type).getKey().remove(m);
                int prev_counter = event_subscribers.get(type).getValue().get();
                System.out.println(prev_counter);
                if (prev_counter == event_subscribers.get(type).getKey().size() && prev_counter != 0) //The counter's value is now illegal, for example it's 5 while there are now 5 MS's
                    event_subscribers.get(type).getValue().set(prev_counter - 1);
            }
        }
        for (Class<? extends Broadcast> type : m.getBroadcastsSubs()) {
            if (type != null && broadcast_subscribers.containsKey(type) && broadcast_subscribers.get(type).contains(m)) { // meaning this micro service is registered to the broadcast subs of said broadcast.
                //TODO when we initialize a micro service, we subscribe to the appropriate broadcast and initialize it's broadcast_sub field.
                broadcast_subscribers.get(type).remove(m);
            }
        }
        messagesQueue.remove(m); //Finally, remove the message queue of said microservice.


    }

    @Override
    public Message awaitMessage(MicroService m) { //TODO find a better way than synchronized that can use wait() func

        synchronized (m) {
            if (!messagesQueue.containsKey(m)) throw new IllegalStateException(); // this microservice does not exist

            if (messagesQueue.get(m).size() == 0) {
                try {
                    wait(); // will be notified when it gets a message. no need to be in a while loop since only this method can remove from m's queue
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return messagesQueue.get(m).remove(0);

        }
    }
}
