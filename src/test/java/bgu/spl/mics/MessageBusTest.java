package bgu.spl.mics;

import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.example.services.ExampleMicroService;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertThrows;


public class MessageBusTest {
    private MessageBusImpl messageBus;
    private ExampleEvent event;
    private ExampleBroadcast broadcast;
    private ExampleMicroService microService;

    @Before
    public void setUp() {
        this.messageBus = MessageBusImpl.getInstance();
        this.event = new ExampleEvent("test_event");
        this.broadcast = new ExampleBroadcast("test_broadcast");
        this.microService = new ExampleMicroService("test_micro_service");
    }

    @Test
    public void subscribeEventTest() {

        assertTrue(messageBus.isEventSubsEmpty(event.getClass()));
        messageBus.register(microService);

        messageBus.subscribeEvent(event.getClass(), microService);
        assertFalse(messageBus.isEventSubsEmpty(event.getClass()));

        messageBus.unregister(microService);
        assertTrue(messageBus.isEventSubsEmpty(event.getClass()));
    }

    @Test
    public void subscribeBroadcastTest() {

        assertTrue(messageBus.isBroadcastSubsEmpty(broadcast.getClass()));
        messageBus.register(microService);
        messageBus.subscribeBroadcast(broadcast.getClass(), microService);
        assertFalse(messageBus.isBroadcastSubsEmpty(broadcast.getClass()));

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

        Thread thread = new Thread(() -> {
            microService.initialize();
            wasNotifiedByBroadcast.set(true);
        });
        thread.start();

        System.out.println(thread.isInterrupted());
        Thread thread1 = new Thread(() -> messageBus.sendBroadcast(broadcast));
        thread1.start();


        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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


        assertFalse(messageBus.isRegistered(microService));
        messageBus.register(microService);
        assertTrue(messageBus.isRegistered(microService));

        messageBus.unregister(microService);

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


        StudentService not_exists = new StudentService("not-exists");
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


        messageBus.unregister(microService);
    }
}
