package bgu.spl.mics;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class FutureTest {

    Future<Object> future;

    Object object; // Will reference the current object held in future

    Object wantedObject; //Will reference the wanted result


    Thread resolve_thread;
    Thread thread_second;

    int timeout_test;
    TimeUnit timeUnit_test;


    @Before
    public void setUp() {
        wantedObject = "something";
        timeout_test = 100;
        timeUnit_test = TimeUnit.MILLISECONDS;
        resolve_thread = new Thread(() -> future.resolve(wantedObject));
        thread_second = new Thread(() -> object = future.get(1, timeUnit_test));

    }

    @Test
    public void TestGet() { //TODO: implement a different approach, maybe nest a new runnable inside the thread in order to catch the "IllegalMonitorStateException" exception.
        object = null;
        future = new Future<>();
        Runnable runnable = () -> object = future.get(); //no time limit
        Thread thread = new Thread(runnable);

        thread.start();

        Object temp = object;
        assertNull(temp);

        resolve_thread.start();
        try {
            thread.join();
            resolve_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(wantedObject, object);

    }

    @Test
    public void TestGetEmpty() {
        future = new Future<>();

        thread_second.start();

        try {
            thread_second.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertNull(object);


        resolve_thread.start();
        Thread thread1 = new Thread(() -> object = future.get(10, timeUnit_test));
        thread1.start();
        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(wantedObject, object);
    }


    @Test
    public void TestResolve() {
        object = null;
        future = new Future<>();
        Thread thread = new Thread(() -> future.resolve(null));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread_second.start();
        try {
            thread_second.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        assertNull(object);

        Thread thread_second_V2 = new Thread(() -> future.resolve(wantedObject));
        thread_second_V2.start();
        try {
            thread_second_V2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(wantedObject, future.get());
    }

    @Test
    public void TestIsDOne() {
        future = new Future<>();
        assertFalse(future.isDone());
        future.resolve(wantedObject);
        assertTrue(future.isDone());
    }
}