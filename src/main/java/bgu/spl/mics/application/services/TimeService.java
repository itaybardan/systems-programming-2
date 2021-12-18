package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.TerminateBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TimeService is the global system timer There is only one instance of this microservice.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other microservices about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {

    private Timer timer;
    private final int programDuration;
    private final int tickLength;


    public TimeService(String name, int _tick_length, int _programDuration) {
        super(name);
        tickLength = _tick_length;
        programDuration = _programDuration;
    }


    @Override
    protected void initialize() {
        timer = new Timer();
        timer.schedule(new BroadcastTickTask(), 0, tickLength);
    }

    public class BroadcastTickTask extends TimerTask {
        AtomicInteger currentTime = new AtomicInteger(0);

        public void run() {
            if (currentTime.get() < programDuration) {
                sendBroadcast(new TickBroadcast(currentTime.get()));
            } else {
                sendBroadcast(new TerminateBroadcast());
                terminate();
                timer.cancel();
            }
            currentTime.addAndGet(1);
        }
    }
}
