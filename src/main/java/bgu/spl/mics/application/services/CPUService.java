package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private final CPU cpu;

    public CPUService(String name, CPU cpu) {
        super(name);
        this.cpu = cpu;
    }

    @Override
    protected void initialize() {
        this.subscribeBroadcast(TickBroadcast.class, tickBroadcastMessage -> this.cpu.increaseTicks());
    }
}
