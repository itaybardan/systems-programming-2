package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */

public class Future<T> {

	T result;
	private boolean isResolved;
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		isResolved=false;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public T get() {
		//TODO: implement this.
		if(result != null) return result;
		try{
			wait(); //Will wait indefinitely until either notify() is called.
		}
		catch (InterruptedException e) {
			return result;
		}

		return null;

	}
	/**
     * Resolves the result of this Future object.
	 *
	 * @pre result = null;
	 * @post vehicles.size() == vehicles.size()@pre + 1
	 * @post this.result != null;
	 *
     */
	public void resolve (T result) {
		if(this.result == null)
		this.result = result;
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
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public synchronized T get(long timeout, TimeUnit unit) {
		//TODO: implement this.
		if(result != null){
			return result;
		}

		try{
			wait(unit.toMillis(timeout)); //Will wait the appropriate amount of time TODO: check if works properly.
		}
		catch (InterruptedException e) {
			if(result != null){
				return result;
			}
		}
		return null; //Waited  the needed time, but there's still not result.
	}

}
