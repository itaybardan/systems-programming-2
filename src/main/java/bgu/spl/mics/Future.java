package bgu.spl.mics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * <p>
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */

public class Future<T> {

    private T result = null;

    /**
     * This should be the only public constructor in this class.
     */
    public Future() {

    }

    /**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     *
     * @return return the result of type T if it is available, if not wait until it is available.
     */
    public synchronized T get() {
        while (result == null) {
            try {
                wait(); //Will wait indefinitely until either notify() is called. will also release monitor key in the meantime
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result; //it;s still not updated
    }


    /**
     * Resolves the result of this Future object.
     *
     * @pre this.result = null;
     * @post this.result != null;
     */
    public synchronized void resolve(T _result) {
        if (this.result == null && _result != null) {
            result = _result;
            notifyAll();
        }
    }


    /**
     * @return true if this object has been resolved, false otherwise
     */
    public boolean isDone() {
        return result != null;
    }

    /**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     *
     * @param timeout the maximal amount of time units to wait for the result.
     * @param unit    the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not,
     * wait for {@code timeout} TimeUnits {@code unit}. If time has
     * elapsed, return null.
     */
    public synchronized T get(long timeout, TimeUnit unit) {
        if (result != null) {
            return result;
        }

        try {
            wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
        } catch (Exception e) {
            System.out.println(Thread.currentThread().getId());
            return result; //it;s still not updated
        }
        //System.out.println(Thread.currentThread().getId());
        return null; //Waited  the needed time, but there's still not result.
    }

}
