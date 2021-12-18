package bgu.spl.mics;

import bgu.spl.mics.application.messages.broadcasts.TerminateBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.PublishResultsEvent;
import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.ModelStatus;
import bgu.spl.mics.application.objects.ModelType;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;

import org.junit.Before;
import org.junit.Test;


import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;


class ExampleMicroService extends  MicroService {
    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */



    public ExampleMicroService(String name) {
        super(name);

        messages_callbacks.put(ExampleEvent.class, System.out::println);
        messages_callbacks.put(ExampleBroadcast.class, System.out::println);
        messages_callbacks.put(TickBroadcast.class, (Object s) -> System.out.println("time tick"));
        messages_callbacks.put(PublishResultsEvent.class, System.out::println);
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

public class MessageBusTest { //each test needs to be done separately as MessageBusImpl is a singleton


    MessageBusImpl messageBus;
    Model testModel;
    TrainModelEvent event1;

    ExampleMicroService microService;

    @Before
    public void setUp(){
        messageBus = MessageBusImpl.getInstance();
        testModel = new Model("test", ModelType.images, 10);
        event1 = new TrainModelEvent(testModel);
        microService = new ExampleMicroService("test_micro_service");
    }

    @Test
    public void subscribeEventTest() {
        assertTrue(messageBus.isEventSubsEmpty(PublishResultsEvent.class));
        messageBus.register(microService);
        messageBus.subscribeEvent(PublishResultsEvent.class, microService);
        assertFalse(messageBus.isEventSubsEmpty(PublishResultsEvent.class));
        messageBus.unregister(microService);

    }

    @Test
    public void subscribeBroadcastTest() {
        //assertTrue(messageBus.isBroadcastSubsEmpty(TerminateBroadcast.class));
        messageBus.register(microService);
        messageBus.subscribeBroadcast(TerminateBroadcast.class, microService);
        //assertFalse(messageBus.isBroadcastSubsEmpty(TerminateBroadcast.class));

        messageBus.unregister(microService);

    }

    @Test
    public void completeTest() {

        messageBus.register(microService);
        messageBus.subscribeEvent(TrainModelEvent.class, microService);


        event1.setFuture(messageBus.sendEvent(event1));
        Thread thread = new Thread( () -> messageBus.complete(event1, testModel));
        thread.start();
        try {
            thread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(event1.getFuture().isDone());

        messageBus.unregister(microService);

    }

    @Test
    public void sendBroadcastTest() {

        TickBroadcast tickBroadcast = new TickBroadcast(1);

        assertFalse(messageBus.isBroadcastProcessed(tickBroadcast, microService));

        ExampleMicroService microService1 = new ExampleMicroService("aaa");
        messageBus.subscribeBroadcast(TickBroadcast.class, microService1);
        messageBus.sendBroadcast(tickBroadcast);

        assertTrue(messageBus.isBroadcastProcessed(tickBroadcast, microService1));
        messageBus.unregister(microService1);

    }

    @Test
    public void sendEventTest() {

        PublishResultsEvent publishResultsEvent = new PublishResultsEvent(testModel);
        assertFalse(messageBus.isEventProcessed(publishResultsEvent, microService));

        ExampleMicroService microService2 = new ExampleMicroService("bbb");
        messageBus.subscribeEvent(PublishResultsEvent.class, microService2);
        messageBus.sendEvent(publishResultsEvent);

        assertTrue(messageBus.isEventProcessed(publishResultsEvent, microService2));
        messageBus.unregister(microService2);

    }

    @Test
    public void registerTest() {


        ExampleMicroService microService2 = new ExampleMicroService("test");

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


        ExampleMicroService microService1 = new ExampleMicroService("not_exists");
        messageBus.unregister(microService1);
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(microService1));

        ExampleMicroService microService2 = new ExampleMicroService("exists");
        messageBus.subscribeEvent(TestModelEvent.class, microService2); //This part is still on loop but should work
        messageBus.register(microService2);


        Thread thread = new Thread(() -> {
            try {
                messageBus.awaitMessage(microService2);
            } catch (Exception e) {
//                assertEquals(InterruptedException.class, e.getClass());
            }
        });
        thread.start(); //No events, the thread will be on wait()



        TestModelEvent testModelEvent = new TestModelEvent(testModel, Student.Degree.MSc);
        Thread thread1 = new Thread( () -> messageBus.sendEvent(testModelEvent));

        thread1.start();
        try {
            thread.join();
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(messageBus.isMessageQueueEmpty(microService1));


        messageBus.unregister(microService1);
    }
}
