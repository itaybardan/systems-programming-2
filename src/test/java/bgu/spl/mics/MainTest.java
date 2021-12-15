package bgu.spl.mics;

import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.ModelType;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.application.services.TimeService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MainTest {

    final int ticks = 1;
    final int duration = 300;
    bgu.spl.mics.example.ExampleMicroService exampleMicroService;
    StudentService studentService;
    ConferenceService conferenceService1, conferenceService2;
    TimeService timeService;


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
        ConferenceInformation conf2 = new ConferenceInformation("conf1", 70);

        exampleMicroService = new bgu.spl.mics.example.ExampleMicroService("exm");
        studentService = new StudentService(student1);
        int confCounter = 1;
        conferenceService1 = new ConferenceService("conf1", conf1, confCounter);
        confCounter = conf1.getDate();
        conferenceService2 = new ConferenceService("conf2", conf2, confCounter);

    }

    @Test
    public void runTest() {

        ExecutorService fixedPool = Executors.newFixedThreadPool(5);
        timeService = new TimeService("time-service-test", ticks, duration);
        fixedPool.execute(conferenceService1);
        fixedPool.execute(conferenceService2);
        fixedPool.execute(studentService);
        fixedPool.execute(timeService);

        fixedPool.shutdown();
        try {
            fixedPool.awaitTermination(800, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
