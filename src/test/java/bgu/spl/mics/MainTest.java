package bgu.spl.mics;

import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.application.services.TimeService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


class ExampleGPU extends MicroService {


    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public ExampleGPU(String name) {
        super(name);
    }

    @Override
    protected void initialize() {

        //Setting up Callbacks
        Callback<TrainModelEvent> trainModelEventCallback = (TrainModelEvent e) -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.out.println("gpu on train");
            complete(e, e.getModel());
        };
        Callback<TestModelEvent> testModelEventCallback = (TestModelEvent e) -> {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.out.println("gpu on test");
            Random random = new Random();
            double randNum = random.nextDouble();
            ModelStatus modelStatus = ModelStatus.Bad;
            if (randNum >= 0.6) modelStatus = ModelStatus.Good;
            complete(e, modelStatus);

        };

        subscribeEvent(TrainModelEvent.class, trainModelEventCallback);
        subscribeEvent(TestModelEvent.class, testModelEventCallback);

    }
}


public class MainTest {

    final int ticks = 1;
    final int duration = 600;
    ExampleMicroService exampleMicroService;
    StudentService studentService;
    ConferenceService conferenceService1, conferenceService2;
    ExampleGPU exampleGPU;
    TimeService timeService;
    MessageBusImpl messageBus;


    @Before
    public void beforeTest() {

        ArrayList<Model> models = new ArrayList<Model>();
        Model m1 = new Model("test1", ModelType.images, 10000);
        Model m2 = new Model("test2", ModelType.tabular, 20000);
        Model m3 = new Model("test3", ModelType.text, 30000);
        models.add(m1);
        models.add(m2);
        models.add(m3);


        Student student1 = new Student("stud1", "dep", Student.Degree.MSc, models);
        ConferenceInformation conf1 = new ConferenceInformation("conf1", 50);
        ConferenceInformation conf2 = new ConferenceInformation("conf1", 450);

        exampleMicroService = new ExampleMicroService("exm");
        exampleGPU = new ExampleGPU("gpu");
        studentService = new StudentService(student1);
        int confCounter = 1;
        conferenceService1 = new ConferenceService("conf1", conf1, confCounter);
        confCounter = conf1.getDate();
        conferenceService2 = new ConferenceService("conf2", conf2, confCounter);


        messageBus = MessageBusImpl.getInstance();
    }

    @Test
    public void runTest() {

        ExecutorService fixedPool = Executors.newFixedThreadPool(5);
        timeService = new TimeService("time-service-test", ticks, duration);
        fixedPool.execute(timeService);
        fixedPool.execute(exampleGPU);
        fixedPool.execute(conferenceService1);
        fixedPool.execute(conferenceService2);

        Thread thread = new Thread(() -> { // Need to have some sort of delay between setup of gpus and student services
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fixedPool.execute(studentService);


        fixedPool.shutdown();
        try {
            fixedPool.awaitTermination(800, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


}
