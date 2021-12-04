package bgu.spl.mics;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


@RunWith(JUnitParamsRunner.class)
public class FutureTest {
    private Future<Object> future;


    @Before
    public void setUp() throws Exception {
        this.future = new Future<Object>();
    }

    @After
    public void tearDown() throws Exception {
    }

    @SuppressWarnings("unused")
    private Object[] waitTimeUnitsParameters() {
        return new Object[]{1000, 10, 2, 100};
    }

    @Test
    @Parameters(method = "waitTimeUnitsParameters")
    public void testGetTimeoutIsNotSurpass(long waitTimeUnits) {
        long startTime = System.nanoTime();
        this.future.get(waitTimeUnits, TimeUnit.NANOSECONDS);
        long endTime = System.nanoTime();
        assertTrue(endTime - startTime > waitTimeUnits);

    }

    @Test
    public void testResolve() {
        Object result = new Object();
        this.future.resolve(result);
        assertTrue(this.future.isDone());
        assertNotNull(this.future.get());
    }

    @SuppressWarnings("unused")
    private Object[] parametersForTestIsDone() {
        return new Object[]{new Object(), "hello", "world"};
    }

    @Test
    @Parameters(method = "parametersForTestIsDone")
    public void testIsDone(Object result) {
        this.future = new Future<>();
        assertFalse(this.future.isDone());
        this.future.resolve(result);
        assertTrue(this.future.isDone());
        assertNotNull(this.future.get());
    }

    @Test
    @Parameters(method = "waitTimeUnitsParameters")
    public void testGet(final long waitTimeUnits) {
        long startTime = System.nanoTime();
        this.future = new Future<Object>();
        final Object result = new Object();
        Thread resolver = new Thread() {
            private final long _waitTimeUnits = waitTimeUnits;
            private final Object _result = result;

            public void run() {
                try {
                    TimeUnit.NANOSECONDS.sleep(this._waitTimeUnits);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                future.resolve(this._result);
            }
        };
        resolver.start();
        future.get();
        long endTime = System.nanoTime();
        assertTrue(endTime - startTime > waitTimeUnits);
        assertTrue(future.isDone());
    }
}
