package bgu.spl.mics;


import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 * "Basic queries can be implemented" - from forum.
 */


public class MessageBusImpl implements MessageBus {

    private static MessageBusImpl instance;
    private HashMap<Class<? extends Event>, MutablePair<LinkedList<MicroService>, Integer>> event_subscribers = new HashMap<>(); //READ BELOW
    //this hashmap will contain all the event classes, and for each, a pair of:all the subscribed microservices for that event class,
    //and the counter needed for the round-robin implementation.

    private HashMap<Class<? extends Broadcast>, LinkedList<MicroService>> broadcast_subscribers = new HashMap<>();


    private HashMap<MicroService, LinkedList<Message>> messagesQueue = new HashMap<>();


    private MessageBusImpl() {
    }

    public static MessageBusImpl getInstance() {
        if (instance == null)
            instance = new MessageBusImpl();
        return instance;
    }


    //Basic Queries
    public boolean isEventSubsEmpty(Class<? extends Event> type)
    {
        if(event_subscribers.get(type) == null) return true;
        return event_subscribers.get(type).getKey().isEmpty();
    }
    public boolean isBroadcastSubsEmpty(Class<? extends Broadcast> type) {
        if(broadcast_subscribers.get(type) == null) return true;
        return broadcast_subscribers.get(type).isEmpty();
    }
    public boolean isRegistered(MicroService m){
        return messagesQueue.containsKey(m);
    }



    public boolean isEventProcessed(Event event) {
        for(MicroService m : messagesQueue.keySet()){
            if (messagesQueue.get(m).contains(event)) return true;
        }
        return false;
    }


    public boolean isBroadcastProcessed(Broadcast broadcast) {
        for(MicroService m : messagesQueue.keySet()){
            if (messagesQueue.get(m).contains(broadcast)) return true;
        }

        return false;
    }


    //Methods
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        MutablePair<LinkedList<MicroService>, Integer> temp_pair;
        if(!event_subscribers.containsKey(type)) { //The event is not registered yet in the map.
            temp_pair = new MutablePair<LinkedList<MicroService>, Integer>(new LinkedList<>(), 0); // counter represents the current micro service to give an event
            temp_pair.getKey().add(m); //Add the micro service to the list that represents all the subscribed micro services for this event type
            event_subscribers.put(type, temp_pair);

        }
        else {

            LinkedList<MicroService> temp_array = event_subscribers.get(type).getKey(); //The event exists and is already initialized in the map.
            temp_array.add(m);

        }
    }


    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        LinkedList<MicroService> temp_array;
        if(!broadcast_subscribers.containsKey(type)) { //The broadcast is not registered yet in the map.
            temp_array = new LinkedList<>();
            temp_array.add(m);
            broadcast_subscribers.put(type, temp_array);

        }
        else {
             //The broadcast exists and is already initialized in the map.
            broadcast_subscribers.get(type).add(m);
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) { // needs to somehow connect with the MS that send the event.

        e.getFuture().resolve(result);

    }

    @Override
    public synchronized void sendBroadcast(Broadcast b) {
        for(MicroService m : broadcast_subscribers.get(b.getClass())){
            m.notifyMicroService();
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        Class< ? extends Event> type = e.getClass();

        if(!event_subscribers.containsKey(type))
           return null;

        LinkedList<MicroService> microServiceslist= event_subscribers.get(type).getKey();
        int counter = event_subscribers.get(type).getValue();
        messagesQueue.get(microServiceslist.get(counter)).add(e);
        event_subscribers.get(type).setValue((counter+1) % ( event_subscribers.get(type).getKey().size()) ); //increase the counter

        Future<T> future = new Future(); //e.setFuture?
        return future;
    }

    @Override
    public void register(MicroService m) {
        if(messagesQueue.containsKey(m)) return; //This micro service is already implemented, do nothing.
        messagesQueue.put(m, new LinkedList<>());

    }

    @Override
    public void unregister(MicroService m) {
        Class type = m.getEventSub(); // Unsubscribing from the event.

        if(type != null && event_subscribers.containsKey(type)){ // meaning this micro service is registered to the event subscribers of said event.
            //TODO when we initialize a micro service, we subscribe to the appropriate event and initialize it's event_sub field.
            event_subscribers.get(type).getKey().remove(m);
            int prev_counter = event_subscribers.get(type).getValue();
            System.out.println(prev_counter);
            if(prev_counter == event_subscribers.get(type).getKey().size() && prev_counter != 0) //The counter's value is now illegal, for example it's 5 while there are now 5 MS's
            event_subscribers.get(type).setValue( prev_counter-1 );
        }

        type = m.getBroadcastSub(); // Unsubscribing from the broadcast
        if(type != null && broadcast_subscribers.containsKey(type) && broadcast_subscribers.get(type).contains(m)){ // meaning this micro service is registered to the broadcast subs of said broadcast.
            //TODO when we initialize a micro service, we subscribe to the appropriate broadcast and initialize it's broadcast_sub field.
            broadcast_subscribers.get(type).remove(m);
        }

        messagesQueue.remove(m); //Finally, remove the message queue of said micro service.


    }

    @Override
    public synchronized Message awaitMessage(MicroService m) throws InterruptedException { //TODO find a better way than synchronized that can use wait() func
        if(!messagesQueue.containsKey(m)) throw new IllegalStateException(); // this microservice does not exist

        if(messagesQueue.get(m).size() != 0) { return messagesQueue.get(m).remove(0); }
        else while(messagesQueue.get(m).size() == 0) //no avaialable message
            try {
                wait();
            }
            catch (InterruptedException e ){ throw e;}

        return messagesQueue.get(m).remove(1); //TODO implement it thread - safely
    }


//    public boolean isSubscribedEvents(MicroService m){ //TODO Maybe not needed
//        Set keys = event_subscribers.keySet();
//
//        for (Iterator i = keys.iterator(); i.hasNext();)
//        {
//            Class<? extends Event> key = (Class<? extends Event>) i.next();
//
//            MutablePair<LinkedList<MicroService>, Integer> pair = event_subscribers.get(key);
//            LinkedList<MicroService> list = pair.getKey(); //Get each and every list and check if m is in there.
//
//            if(list.contains(m)) return true;
//        }
//        return false;
//    }
//
//    public boolean isSubscribedBroadcasts(MicroService m){ //TODO Maybe not needed
//        Set keys = broadcast_subscribers.keySet();
//
//        for (Iterator i = keys.iterator(); i.hasNext();)
//        {
//            Class<? extends Event> key = (Class<? extends Event>) i.next();
//
//            LinkedList<MicroService> list = broadcast_subscribers.get(key); //Get each and every list and check if m is in there.
//            if(list.contains(m)) return true;
//        }
//        return false;
//    }

}
