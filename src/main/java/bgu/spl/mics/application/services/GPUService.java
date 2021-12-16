package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.ModelStatus;
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
            if (testModelMessage.getStatus() == Student.Degree.PhD) {
                if (Math.random() <= 0.8) {
                    this.complete(testModelMessage, ModelStatus.Good);
                } else {
                    this.complete(testModelMessage, ModelStatus.Bad);
                }
            } else if (testModelMessage.getStatus() == Student.Degree.MSc) {
                if (Math.random() <= 0.6) {
                    this.complete(testModelMessage, ModelStatus.Good);
                } else {
                    this.complete(testModelMessage, ModelStatus.Bad);
                }
            }
        });

        this.subscribeEvent(TrainModelEvent.class, trainModelMessage -> {
            gpu.trainModel(trainModelMessage.model);
        });
    }
}
