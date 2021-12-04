package bgu.spl.mics;

import bgu.spl.mics.example.ServiceCreator;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 * "Basic queries can be implemented" - from forum.
 */


public class MessageBusImpl implements MessageBus {

    private static MessageBusImpl instance;
    private HashMap<Class<? extends Event>, LinkedList<MicroService>> event_subscribers = new HashMap<>();
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
    public boolean isEventSubsEmpty() {
        return event_subscribers.isEmpty();
    }
    public boolean isBroadcastSubsEmpty() {
        return broadcast_subscribers.isEmpty();
    }
    public boolean isRegistered(MicroService m){
        return messagesQueue.containsKey(m);
    }

    public boolean isSubscribedEvents(MicroService m){
        Set keys = event_subscribers.keySet();

        for (Iterator i = keys.iterator(); i.hasNext();)
        {
            Class<? extends Event> key = (Class<? extends Event>) i.next();

            LinkedList<MicroService> list = event_subscribers.get(key); //Get each and every list and check if m is in there.
            if(list.contains(m)) return true;
        }
        return false;
    }

    public boolean isSubscribedBroadcasts(MicroService m){
        Set keys = broadcast_subscribers.keySet();

        for (Iterator i = keys.iterator(); i.hasNext();)
        {
            Class<? extends Event> key = (Class<? extends Event>) i.next();

            LinkedList<MicroService> list = broadcast_subscribers.get(key); //Get each and every list and check if m is in there.
            if(list.contains(m)) return true;
        }
        return false;
    }




    //Methods
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        LinkedList<MicroService> temp_array;
        if(!event_subscribers.containsKey(type)) { //The event is not registered yet in the map.
            temp_array = new LinkedList<>();
            event_subscribers.put(type, temp_array);

        }
        else
            temp_array=event_subscribers.get(type); //The event exists and is already initialized in the map.

        temp_array.add(m);
    }


    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        LinkedList<MicroService> temp_array;
        if(!broadcast_subscribers.containsKey(type)) { //The broadcast is not registered yet in the map.
            temp_array = new LinkedList<>();
            broadcast_subscribers.put(type, temp_array);

        }
        else
            temp_array=broadcast_subscribers.get(type); //The broadcast exists and is already initialized in the map.

        temp_array.add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) { // needs to somehow connect with the MS that send the event.
        //TODO: Fuck yo bitch ass mother duk

    }

    @Override
    public void sendBroadcast(Broadcast b) {


    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void register(MicroService m) {
        messagesQueue.put(m, new LinkedList<>());
    }

    @Override
    public void unregister(MicroService m) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized Message awaitMessage(MicroService m) throws InterruptedException {
        if(!messagesQueue.containsKey(m)) throw new IllegalStateException(); // this microservice does not exist

        if(messagesQueue.get(m).size() != 0) { return messagesQueue.get(m).remove(1); }
        else while(messagesQueue.get(m).size() == 0) //no avaialable message
            try {
                wait();
            }
            catch (InterruptedException e ){ throw e;}

        return messagesQueue.get(m).remove(1); //TODO implement it thread - safely
    }


}
