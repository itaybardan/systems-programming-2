package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.events.TrainModelEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    public static final Map<Type, Integer> typeToTrainTickTime = new HashMap<Type, Integer>() {{
        put(Type.RTX3090, 1);
        put(Type.RTX2080, 2);
        put(Type.GTX1080, 4);
    }};
    public static final Map<Type, Integer> typeToProcessedDataCapacity = new HashMap<Type, Integer>() {{
        put(Type.RTX3090, 32);
        put(Type.RTX2080, 16);
        put(Type.GTX1080, 8);
    }};
    private static final Logger logger = Logger.getLogger(GPU.class.getName());

    public int ticks;
    public Type type;
    private final Cluster cluster = Cluster.getInstance();
    public boolean isTrainingModel = false;
    public boolean isTrainingDataBatch = false;
    public int startTrainingTick;
    public ArrayList<DataBatch> dataBatches;
    public int availableSpots;
    public TrainModelEvent event;
    public int trainedDataBatches;

    /**
     * @inv getAvailableProcessedDataSpace() >= 0
     */
    public GPU(String type) {
        this.type = Type.valueOf(type);
        this.trainedDataBatches = 0;
    }

    enum Type {RTX3090, RTX2080, GTX1080}

    public void increaseTicks() {
        this.ticks++;
    }

    public void startTrainingModel(TrainModelEvent event) {
        logger.info(String.format("GPU with type %s is starting training model: %s",
                this.type.toString(), event.model.getName()));
        this.availableSpots = GPU.typeToProcessedDataCapacity.get(this.type);
        this.isTrainingModel = true;
        this.trainedDataBatches = 0;
        this.event = event;
        Data data = new Data(Data.DataType.valueOf(event.model.getType().toString()), event.model.getSize());
        this.dataBatches = new ArrayList<>();
        for (int i = 0; i < data.getSize(); i += 1000) {
            dataBatches.add(new DataBatch(i, data.type, data.size));
        }

        while (this.availableSpots > 0 && !dataBatches.isEmpty()) {
            DataBatch dataBatchToSend = dataBatches.remove(0);
            this.cluster.dataBatchToGpu.put(dataBatchToSend, this);
            this.cluster.unprocessedDataBatches.add(dataBatchToSend);
            this.cluster.gpuToProcessedDataBatches.putIfAbsent(this, new CopyOnWriteArrayList<>());
            this.availableSpots--;
        }
    }

    public void finishTrainingModel() {
        logger.info(String.format("GPU with type %s finished training model: %s", this.type.toString(),
                this.event.model.getName()));
        this.isTrainingModel = false;
        this.cluster.statistics.trainedModelsNames.add(this.event.model.getName());
    }

    public void startTrainingDataBatch() {
        if (!this.cluster.gpuToProcessedDataBatches.get(this).isEmpty()) {
            this.cluster.gpuToProcessedDataBatches.get(this).remove(0);
            this.startTrainingTick = this.ticks;
            this.isTrainingDataBatch = true;
        }
    }

    public void finishTrainingDataBatch() {
        this.cluster.statistics.gpuTimeUsed.getAndAdd(this.ticks - this.startTrainingTick);
        this.isTrainingDataBatch = false;
        this.trainedDataBatches++;
        this.availableSpots++;
        if (!this.dataBatches.isEmpty()) {
            DataBatch dataBatchToSend = this.dataBatches.remove(0);
            this.cluster.dataBatchToGpu.put(dataBatchToSend, this);
            this.cluster.unprocessedDataBatches.add(dataBatchToSend);
        }
    }

}
