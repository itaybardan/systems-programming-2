package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Student;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private final GPU gpu;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
    }

    @Override
    protected void initialize() {
        this.subscribeBroadcast(TickBroadcast.class, tickBroadcastMessage -> {
            this.gpu.increaseTicks();
        });

        this.subscribeEvent(TestModelEvent.class, testModelMessage -> {
            if (testModelMessage.getStudent().getStatus() == Student.Degree.PhD) {
                if (Math.random() <= 0.8) {
                    this.complete(testModelMessage, "Good");
                }
                else {
                    this.complete(testModelMessage, "Bad");
                }
            }
            else if (testModelMessage.getStudent().getStatus() == Student.Degree.MSc) {
                if (Math.random() <= 0.6) {
                    this.complete(testModelMessage, "Good");
                }
                else {
                    this.complete(testModelMessage, "Bad");
                }
            }
        });

        this.subscribeEvent(TrainModelEvent.class, trainModelMessage -> {
            gpu.trainModel(trainModelMessage.model);
        });
    }
}
