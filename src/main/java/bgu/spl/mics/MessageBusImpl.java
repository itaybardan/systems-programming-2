package bgu.spl.mics;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.MutablePair;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
    public static MessageBusImpl messageBusImpl = new MessageBusImpl();
    private HashMap<Class<? extends Event>, MutablePair<LinkedList<MicroService>, Integer>> event_subscribers = new HashMap<>();
    private HashMap<Class<? extends Broadcast>, LinkedList<MicroService>> broadcast_subscribers = new HashMap<>();
    private HashMap<MicroService, LinkedList<Message>> messagesQueue = new HashMap<>();

    /**
     * @param type The type to subscribe to,
     * @param m    The subscribing micro-service.
     * @param <T>  the type the event holds
     */
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        MutablePair<LinkedList<MicroService>, Integer> temp_pair;
        if (!event_subscribers.containsKey(type)) { //The event is not registered yet in the map.
            temp_pair = new MutablePair<LinkedList<MicroService>, Integer>(new LinkedList<>(), 0); // counter represents the current micro service to give an event
            temp_pair.getKey().add(m); //Add the micro service to the list that represents all the subscribed micro services for this event type
            event_subscribers.put(type, temp_pair);

        } else {

            LinkedList<MicroService> temp_array = event_subscribers.get(type).getKey(); //The event exists and is already initialized in the map.
            temp_array.add(m);

        }
    }

    /**
     * @param type The type to subscribe to.
     * @param m    The subscribing micro-service.
     */
    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        LinkedList<MicroService> temp_array;
        if (!broadcast_subscribers.containsKey(type)) { //The broadcast is not registered yet in the map.
            temp_array = new LinkedList<>();
            temp_array.add(m);
            broadcast_subscribers.put(type, temp_array);

        } else {
            //The broadcast exists and is already initialized in the map.
            broadcast_subscribers.get(type).add(m);
        }
    }

    /**
     * @param e      The completed event.
     * @param result The resolved result of the completed event.
     * @param <T>    the type the event holds
     */
    @Override
    public <T> void complete(Event<T> e, T result) { // needs to somehow connect with the MS that send the event.

        e.getFuture().resolve(result);

    }

    /**
     * @param b The message to added to the queues.
     */
    @Override
    public synchronized void sendBroadcast(Broadcast b) {
        for (MicroService m : broadcast_subscribers.get(b.getClass())) {
            m.notifyMicroService();
        }
    }

    /**
     * @param e   The event to add to the queue.
     * @param <T>
     * @return
     */
    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        Class<? extends Event> type = e.getClass();

        if (!event_subscribers.containsKey(type))
            return null;

        LinkedList<MicroService> microServiceslist = event_subscribers.get(type).getKey();
        int counter = event_subscribers.get(type).getValue();
        messagesQueue.get(microServiceslist.get(counter)).add(e);
        event_subscribers.get(type).setValue((counter + 1) % (event_subscribers.get(type).getKey().size())); //increase the counter

        Future<T> future = new Future(); //e.setFuture?
        return future;
    }

    /**
     * @param m the micro-service to create a queue for.
     */
    @Override
    public void register(MicroService m) {
        if (messagesQueue.containsKey(m)) return; //This microservice is already implemented, do nothing.
        messagesQueue.put(m, new LinkedList<>());

    }

    /**
     * @param m the micro-service to unregister.
     */
    @Override
    public void unregister(MicroService m) {
        Class type = m.getEventSub(); // Unsubscribing from the event.

        if (type != null && event_subscribers.containsKey(type)) { // meaning this micro service is registered to the event subscribers of said event.
            //TODO when we initialize a micro service, we subscribe to the appropriate event and initialize it's event_sub field.
            event_subscribers.get(type).getKey().remove(m);
            int prev_counter = event_subscribers.get(type).getValue();
            System.out.println(prev_counter);
            if (prev_counter == event_subscribers.get(type).getKey().size() && prev_counter != 0) //The counter's value is now illegal, for example it's 5 while there are now 5 MS's
                event_subscribers.get(type).setValue(prev_counter - 1);
        }

        type = m.getBroadcastSub(); // Unsubscribing from the broadcast
        if (type != null && broadcast_subscribers.containsKey(type)) { // meaning this micro service is registered to the broadcast subs of said broadcast.
            //TODO when we initialize a micro service, we subscribe to the appropriate broadcast and initialize it's broadcast_sub field.
            broadcast_subscribers.get(type).remove(m);
        }

        messagesQueue.remove(m); //Finally, remove the message queue of said microservice.
    }

    /**
     * @param m The micro-service requesting to take a message from its message
     *          queue.
     * @return the resolved message
     * @throws InterruptedException if await raise some exception
     */
    @Override
    public synchronized Message awaitMessage(MicroService m) throws InterruptedException { //TODO find a better way than synchronized that can use wait() func
        if (!messagesQueue.containsKey(m)) throw new IllegalStateException(); // this microservice does not exist

        if (messagesQueue.get(m).size() != 0) {
            return messagesQueue.get(m).remove(0);
        } else while (messagesQueue.get(m).size() == 0) //no available message
            wait();

        return messagesQueue.get(m).remove(1); //TODO implement it thread - safely
    }

    public static MessageBusImpl getInstance() {
        return MessageBusImpl.messageBusImpl;
    }

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
        for (MicroService m : this.messagesQueue.keySet()) {
            if (this.messagesQueue.get(m).contains(broadcast)) return true;
        }

        return false;
    }
}
