package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {
    private static final Logger LOGGER = Logger.getLogger(TimeService.class.getName());
    private final int duration;
    private final int tickTime;
    private int currentTick;
    private final ScheduledExecutorService scheduler;


    public TimeService(String name, int duration, int tickTime) {
        super(name);
        this.duration = duration;
        this.tickTime = tickTime;
        this.currentTick = 1;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    @Override
    protected void initialize() {
        TimerTask task = new TimerTask() {
            public void run() {
                sendBroadcast(new TickBroadcast(currentTick));
                currentTick++;
                if (currentTick == duration + 1)
                    cancel();
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, this.tickTime, TimeUnit.MILLISECONDS);
        scheduler.shutdown();
        sendBroadcast(new TickBroadcast(-1)); // sends terminate signal to
        terminate();
    }
}
