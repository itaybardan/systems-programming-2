package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Student;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private final GPU gpu;
    public ConcurrentLinkedQueue<TrainModelEvent> trainModelTasks;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
    }

    @Override
    protected void initialize() {
        this.subscribeBroadcast(TickBroadcast.class, tickBroadcastMessage -> {
            this.gpu.increaseTicks();
            if (gpu.isTrainingModel) {
                if (gpu.isTrainingDataBatch && gpu.ticks - gpu.startTrainingTick == GPU.typeToTrainTickTime.get(gpu.type)) {
                    gpu.finishTrainingDataBatch();
                    if (gpu.dataBatches.isEmpty()) {
                        gpu.finishTrainingModel();
                        this.complete(gpu.event, gpu.event.model);
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
            if (testModelMessage.getStudentDegree() == Student.Degree.PhD) {
                if (Math.random() <= 0.8) {
                    this.complete(testModelMessage, "Good");
                } else {
                    this.complete(testModelMessage, "Bad");
                }
            } else if (testModelMessage.getStudentDegree() == Student.Degree.MSc) {
                if (Math.random() <= 0.6) {
                    this.complete(testModelMessage, "Good");
                } else {
                    this.complete(testModelMessage, "Bad");
                }
            }
        });

        this.subscribeEvent(TrainModelEvent.class, trainModelMessage -> {
            trainModelTasks.add(trainModelMessage);
        });
    }
}
