package bgu.spl.mics;


import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;


class ExampleMicroService extends  MicroService {
    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */


    public ExampleMicroService(String name) {
        super(name);
        event_subs = new ArrayList<>();
        event_subs.add(ExampleEvent.class);
        broadcast_subs = new ArrayList<>();
        broadcast_subs.add(ExampleBroadcast.class);

    }

    @Override
    public synchronized void notifyMicroService() {
        notify();
    }

    @Override
    protected synchronized void initialize() {//Only synchronized for this test!
        try {
            wait();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class MessageBusTest { //each test needs to be done seperately as MessageBusImpl is a singleton


    MessageBusImpl messageBus;
    ExampleEvent event;
    ExampleBroadcast broadcast;
    ExampleMicroService microService;

    @Before
    public void setUp(){
        messageBus = MessageBusImpl.getInstance();
        event = new ExampleEvent("test_event");
        broadcast = new ExampleBroadcast("test_broadcast");
        microService = new ExampleMicroService("test_micro_service");
    }

    @Test
    public void subscribeEventTest() {

        assertEquals(true, messageBus.isEventSubscribersEmpty(event.getClass()));
        messageBus.register(microService);

        messageBus.subscribeEvent(event.getClass(), microService);
        assertEquals(false, messageBus.isEventSubscribersEmpty(event.getClass()));

        messageBus.unregister(microService);
        assertEquals(true, messageBus.isEventSubscribersEmpty(event.getClass()));
    }

    @Test
    public void subscribeBroadcastTest() {

        assertEquals(true, messageBus.isBroadcastSubsEmpty(broadcast.getClass()));
        messageBus.register(microService);
        messageBus.subscribeBroadcast(broadcast.getClass(), microService);
        assertEquals(false, messageBus.isBroadcastSubsEmpty(broadcast.getClass()));

        messageBus.unregister(microService);

    }

    @Test
    public void completeTest() {
        ExampleEvent event = new ExampleEvent("zazzi bazazzi");
        messageBus.register(microService);
        messageBus.subscribeEvent(event.getClass(), microService);

        event.setFuture(messageBus.sendEvent(event));
        messageBus.complete(event, "mazazzi");
        assertTrue(event.getFuture().isDone());

        messageBus.unregister(microService);

    }

    @Test
    public void sendBroadcastTest() {

        assertFalse(messageBus.isBroadcastProcessed(broadcast));

        messageBus.register(microService);
        messageBus.subscribeBroadcast(ExampleBroadcast.class, microService);

        AtomicBoolean wasNotifiedByBroadcast = new AtomicBoolean(false);

        Thread thread = new Thread( () -> {
            microService.initialize();
            wasNotifiedByBroadcast.set(true);
        });
        thread.start();

        System.out.println(thread.isInterrupted());
        Thread thread1 = new Thread( () -> messageBus.sendBroadcast(broadcast));
        thread1.start();


        try{
            thread.join();
        }catch (Exception e){}

        assertTrue(wasNotifiedByBroadcast.get());

        messageBus.unregister(microService);

    }

    @Test
    public void sendBEventTest() {
        assertFalse(messageBus.isEventProcessed(event));

        messageBus.register(microService);
        messageBus.subscribeEvent(ExampleEvent.class, microService);
        messageBus.sendEvent(event);

        assertTrue(messageBus.isEventProcessed(event));

    }

    @Test
    public void registerTest() {

        ExampleMicroService microService2 = new ExampleMicroService("test");
        assertFalse(messageBus.isRegistered(microService));

        messageBus.register(microService);
        messageBus.register(microService2);
        assertTrue(messageBus.isRegistered(microService));
        assertTrue(messageBus.isRegistered(microService2));

        messageBus.unregister(microService);
        assertFalse(messageBus.isRegistered(microService));
        assertTrue(messageBus.isRegistered(microService2));

        messageBus.unregister(microService2);
        assertFalse(messageBus.isRegistered(microService2));

    }

    @Test
    public void unregisterTest() {


        messageBus.unregister(microService); //messageBus was only initialized and is empty, and therefore should do nothing
        messageBus.register(microService); // Insert a microservice
        messageBus.unregister(microService);
        assertFalse(messageBus.isRegistered(microService));


    }

    @Test
    public void awaitMessageTest() {
        messageBus = MessageBusImpl.getInstance();


       ExampleMicroService not_exists = new ExampleMicroService("not_exists");
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(not_exists));
        messageBus.register(microService);


        Thread thread = new Thread(() -> {
            try {
               messageBus.awaitMessage(microService);
            } catch (Exception e) {
//                assertEquals(InterruptedException.class, e.getClass());
            }
        });
        thread.start(); //No events, the thread will be on wait()




        messageBus.subscribeEvent(ExampleEvent.class, microService); //This part is still on loop but should work
        messageBus.sendEvent(event);
        try {
            assertEquals(event, messageBus.awaitMessage(microService));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        messageBus.unregister(microService);
    }
}
