package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.ModelStatus;
import bgu.spl.mics.application.objects.Student;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private static final Logger logger = Logger.getLogger(GPUService.class.getName());
    private final GPU gpu;
    public ConcurrentLinkedQueue<TrainModelEvent> trainModelTasks;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        this.trainModelTasks = new ConcurrentLinkedQueue<>();
    }

    @Override
    protected void initialize() {
        logger.info(String.format("%s GPU service started", this.name));
        this.subscribeBroadcast(TickBroadcast.class, tickBroadcastMessage -> {
            this.gpu.increaseTicks();
            if (gpu.isTrainingModel) {
                if (gpu.isTrainingDataBatch) {
                    if (gpu.ticks - gpu.startTrainingTick == GPU.typeToTrainTickTime.get(gpu.type)) {
                        gpu.finishTrainingDataBatch();
                        if (gpu.trainedDataBatches == gpu.event.model.getSize() / 1000) {
                            gpu.finishTrainingModel();
                            this.complete(gpu.event, gpu.event.model);
                        }
                    }
                } else {
                    gpu.startTrainingDataBatch();
                }
            } else {
                if (!this.trainModelTasks.isEmpty()) {
                    this.gpu.startTrainingModel(this.trainModelTasks.poll());
                }
            }
        });

        this.subscribeEvent(TestModelEvent.class, testModelMessage -> {
            double probability = testModelMessage.getStatus() == Student.Degree.PhD ? 0.8 : 0.6;
            if (Math.random() <= probability) {
                this.complete(testModelMessage, ModelStatus.Good);
            } else {
                this.complete(testModelMessage, ModelStatus.Bad);
            }
        });

        this.subscribeEvent(TrainModelEvent.class, trainModelMessage -> trainModelTasks.add(trainModelMessage));
    }
}
