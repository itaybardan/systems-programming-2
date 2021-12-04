package bgu.spl.mics;


import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


class ExampleMicroService extends  MicroService {
    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public ExampleMicroService(String name) {
        super(name);
    }

    @Override
    protected void initialize() {

    }
}

public class MessageBusTest {


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


        assertEquals(true, messageBus.isEventSubsEmpty());


        messageBus.subscribeEvent(event.getClass(), microService);
        assertEquals(false, messageBus.isEventSubsEmpty());

    }

    @Test
    public void subscribeBroadcastTest() {

        assertEquals(true, messageBus.isBroadcastSubsEmpty());

        messageBus.subscribeBroadcast(broadcast.getClass(), microService);
        assertEquals(false, messageBus.isBroadcastSubsEmpty());

    }

    @Test
    public void completeTest() { //TODO Implement this shitty method


    }

    @Test
    public void sendBroadcastTest() {

        assertFalse(messageBus.isSubscribedBroadcasts(microService));
        messageBus.subscribeBroadcast(ExampleBroadcast.class, microService);
        assertTrue(messageBus.isSubscribedBroadcasts(microService));

    }

    @Test
    public void sendBEventTest() {

        assertFalse(messageBus.isSubscribedEvents(microService));
        messageBus.subscribeEvent(ExampleEvent.class, microService);
        assertTrue(messageBus.isSubscribedEvents(microService));

    }

    @Test
    public void registerTest() {


        assertFalse(messageBus.isRegistered(microService));
        messageBus.register(microService);
        assertTrue(messageBus.isRegistered(microService));

    }

    @Test
    public void unregisterTest() { //TODO need to delete from all maps on IMPL in order to work.

        messageBus.unregister(microService); //messageBus was only initialized and is empty, and therefore should do nothing
        messageBus.register(microService); // Insert a microservice
        messageBus.unregister(microService);
        assertFalse(messageBus.isRegistered(microService));
    }

    @Test
    public void awaitMessageTest() {
        messageBus = MessageBusImpl.getInstance();


        StudentService not_exists = new StudentService("not-exists");
        Event<String> test;
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(not_exists));
        messageBus.register(microService);


        Thread thread = new Thread(() -> {
            try {
                messageBus.awaitMessage(microService);
            } catch (Exception e) {
                assertEquals(InterruptedException.class, e.getClass());
            }
        });
        thread.start(); //No events, the thread will be on wait()
        thread.interrupt();


        messageBus.subscribeEvent(ExampleEvent.class, microService); //This part is still on loop but should work
        messageBus.sendEvent(event);
        try {
            assertEquals(event, messageBus.awaitMessage(microService));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
