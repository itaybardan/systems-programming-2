package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Data;

import java.util.logging.Logger;

/**
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private static final Logger logger = Logger.getLogger(CPUService.class.getName());
    private final CPU cpu;

    public CPUService(String name, CPU cpu) {
        super(name);
        this.cpu = cpu;
    }

    @Override
    protected void initialize() {
        logger.info(String.format("%s CPU Service started ", this.name));

        this.subscribeBroadcast(TickBroadcast.class, tickBroadcastMessage -> {
            cpu.increaseTicks();
            if (cpu.isProcessing) {
                if (cpu.ticks - cpu.startProcessTick == Data.getTickTime(cpu.dataBatch.type) * (32 / this.cpu.cores)) {
                    cpu.sendReadyDataBatch();
                    cpu.finishProcessing();
                }
            } else {
                boolean succeed = cpu.getNewDataBatch();
                if (succeed){
                    cpu.startProcessDataBatch();
                }
            }
        });
    }
}
