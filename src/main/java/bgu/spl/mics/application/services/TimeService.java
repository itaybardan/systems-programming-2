package bgu.spl.mics.application.services;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.TerminateBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this microservice.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other microservices about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {

    private Timer timer;
    private final int programDuration;
    private final int tick_length;
    private AtomicInteger currentTime;


    public TimeService(String name, int _tick_length, int _programDuration) {
        super(name);
        tick_length = _tick_length;
        programDuration = _programDuration;
    }


    @Override
    protected void initialize() {
        timer = new Timer();
        currentTime = new AtomicInteger(0);

        //starting count time
        timer.schedule(timerTask(() -> {
                    if (currentTime.get() < programDuration) {
                        sendBroadcast(new TickBroadcast(currentTime.get()));
                    } else {
                        sendBroadcast(new TerminateBroadcast());
                        terminate();
                        timer.cancel();
                    }
                    currentTime.addAndGet(tick_length);
                }
        ), 0, tick_length);


    }

    private static TimerTask timerTask(Runnable runnable) {
        return new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }
}
