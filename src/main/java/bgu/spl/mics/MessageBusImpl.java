package bgu.spl.mics;


import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Map;
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
    private final ConcurrentHashMap<Class<? extends Event<?>>, CopyOnWriteArrayList<MicroService>> eventSubscribers;

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
        CopyOnWriteArrayList<MicroService> result = eventSubscribers.getOrDefault(event, new CopyOnWriteArrayList<>());
        return result.isEmpty();
    }

    public boolean isBroadcastSubsEmpty(Class<? extends Broadcast> event) {
        if (broadcastSubscribers.get(event) == null) return true;
        return broadcastSubscribers.get(event).isEmpty();
    }

    public boolean isRegistered(MicroService m) {
        return microServicesMessages.containsKey(m);
    }


    public boolean isEventProcessed(Event<?> event) {
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


    //Methods
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> event, MicroService m) {
        MutablePair<CopyOnWriteArrayList<MicroService>, AtomicInteger> temp_pair;
        if (!eventSubscribers.containsKey(event)) { //The event is not registered yet in the map.
            temp_pair = new MutablePair<>(new CopyOnWriteArrayList<>(), new AtomicInteger(0)); // counter represents the current micro service to give an event
            temp_pair.getKey().add(m); //Add the micro service to the list that represents all the subscribed micro services for this event type
            eventSubscribers.put(event, temp_pair);

        } else {

            CopyOnWriteArrayList<MicroService> temp_array = eventSubscribers.get(event).getKey(); //The event exists and is already initialized in the map.
            temp_array.add(m);

        }
    }


    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> event, MicroService m) {
        CopyOnWriteArrayList<MicroService> temp_array;
        if (!broadcastSubscribers.containsKey(event)) { //The broadcast is not registered yet in the map.
            temp_array = new CopyOnWriteArrayList<>();
            temp_array.add(m);
            broadcastSubscribers.put(event, temp_array);

        } else {
            //The broadcast exists and is already initialized in the map.
            broadcastSubscribers.get(event).add(m);
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) { // needs to somehow connect with the MS that send the event.
        e.getFuture().resolve(result);

    }

    @Override
    public void sendBroadcast(Broadcast b) {
        for (MicroService m : broadcastSubscribers.get(b.getClass())) {
            m.notifyMicroService();
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        if (!eventSubscribers.containsKey(e.getClass()))
            return null;

        CopyOnWriteArrayList<MicroService> subscribers = this.microServicesMessages.getOrDefault(e, new Map<Event<?>, CopyOnWriteArrayList<MicroService>())
        CopyOnWriteArrayList<MicroService> microServicesList = eventSubscribers.get(event).getKey();
        int counter = eventSubscribers.get(event).getValue().get();
        microServicesMessages.get(microServicesList.get(counter)).add(e);
        eventSubscribers.get(event).getValue().set((counter + 1) % (eventSubscribers.get(event).getKey().size())); //increase the counter


        microServicesList.get(counter).notifyMicroService();

        Future<T> future = new Future(); //e.setFuture?
        return future;
    }

    @Override
    public void register(MicroService m) {
        if (microServicesMessages.containsKey(m)) return; //This microservice is already implemented, do nothing.
        microServicesMessages.put(m, new CopyOnWriteArrayList<>());
    }

    @Override
    public void unregister(MicroService m) {
        for (Class<? extends Event<?>> type : m.getEventsSubs()) {
            if (type != null && eventSubscribers.containsKey(type)) { // meaning this micro service is registered to the event subscribers of said event.
                //TODO when we initialize a micro service, we subscribe to the appropriate event and initialize it's event_sub field.
                eventSubscribers.get(type).getKey().remove(m);
                int prev_counter = eventSubscribers.get(type).getValue().get();
                System.out.println(prev_counter);
                if (prev_counter == eventSubscribers.get(type).getKey().size() && prev_counter != 0) //The counter's value is now illegal, for example it's 5 while there are now 5 MS's
                    eventSubscribers.get(type).getValue().set(prev_counter - 1);
            }
        }

        for (Class<? extends Broadcast> type : m.getBroadcastsSubs()) {
            if (type != null && broadcastSubscribers.containsKey(type) && broadcastSubscribers.get(type).contains(m)) { // meaning this micro service is registered to the broadcast subs of said broadcast.
                //TODO when we initialize a micro service, we subscribe to the appropriate broadcast and initialize it's broadcast_sub field.
                broadcastSubscribers.get(type).remove(m);
            }
        }
        microServicesMessages.remove(m); //Finally, remove the message queue of said microservice.
    }

    @Override
    public Message awaitMessage(MicroService m) { //TODO find a better way than synchronized that can use wait() func

        synchronized (m) {
            if (!microServicesMessages.containsKey(m))
                throw new IllegalStateException(); // this microservice does not exist

            if (microServicesMessages.get(m).size() == 0) {
                try {
                    wait(); // will be notified when it gets a message. no need to be in a while loop since only this method can remove from m's queue
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return microServicesMessages.get(m).remove(0);

        }
    }
}
