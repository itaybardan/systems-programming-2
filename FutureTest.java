import bgu.spl.mics.Future;
import org.junit.Assert;
import org.junit.Before;
import  org.junit.After;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class FutureTest {

    Future<Object> future;

    Object object; // Will reference the current object held in future

    Object wantedObject; //Will reference the wanted result

    int timeout_test = 10;
    TimeUnit timeUnit_test = TimeUnit.MILLISECONDS;


    @Before
    public void setUp(){
        future = new Future<Object>();
        wantedObject = "something";
    }

    @Test
    public void TestGet(){ //TODO: implement a different approach, maybe nest a new runnable inside the thread in order to catch the "IllegalMonitorStateException" exception.
        Runnable runnable = () -> {object = future.get();}; //no time limit
        Thread thread = new Thread(runnable);

        thread.start();

        Object temp = object;
        assertEquals(null, temp);
        thread.interrupt();
        assertEquals(null, temp);

        future.resolve(wantedObject);
        temp = future.get();
        assertEquals(wantedObject, temp);



    }
    @Test
    public void TestGetEmpty(){

        Object temp = future.get(1, TimeUnit.MILLISECONDS);
        assertEquals(null, temp);

        future.resolve(wantedObject);
        temp = future.get();
        assertEquals(wantedObject, temp);
    }






    @Test
    public void TestResolve(){
        assertEquals(null, future.get(1, TimeUnit.MILLISECONDS));

        future.resolve(wantedObject);
        assertEquals(wantedObject, future.get());

        future.resolve("other thing");
        assertEquals(wantedObject, future.get(1, TimeUnit.MILLISECONDS));
    }
    @Test
    public void TestIsDOne(){

        assertEquals(false, future.isDone());
        future.resolve(wantedObject);
        assertEquals(true, future.isDone());
    }
}
