package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.events.TrainModelEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
    public LinkedList<DataBatch> processedData;
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
    }


    /**
     * @post getAvailableProcessedDataSpace() == 0
     */
    public void sendDataBatchToCluster(DataBatch dataBatch) {

    }

    public int getAvailableProcessedDataSpace() {
        return GPU.typeToProcessedDataCapacity.get(this.type) - this.processedData.size();
    }

    public LinkedList<DataBatch> getProcessedData() {
        return this.processedData;
    }

    public void increaseTicks() {
        this.ticks++;
    }

    public void startTrainingModel(TrainModelEvent event) {
        logger.info(String.format("GPU with type %s is starting training model: %s",
                this.type.toString(), event.model.getName()));
        this.processedData = new LinkedList<>();
        this.availableSpots = GPU.typeToProcessedDataCapacity.get(this.type) - this.processedData.size();
        this.isTrainingModel = true;
        this.trainedDataBatches = 0;
        this.event = event;
        Data data = new Data(Data.DataType.valueOf(event.model.getType().toString()), event.model.getSize());
        this.dataBatches = new ArrayList<>();
        for (int i = 0; i < data.getSize(); i += 1000) {
            dataBatches.add(new DataBatch(i, data.type));
        }

        while (this.availableSpots > 0 && !dataBatches.isEmpty()) {
            DataBatch dataBatchToSend = dataBatches.remove(0);
            this.cluster.dataBatchToGpu.put(dataBatchToSend, this);
            this.cluster.unprocessedDataBatches.add(dataBatchToSend);
            this.cluster.gpuToProcessedDataBatches.putIfAbsent(this, new CopyOnWriteArrayList<>());
            this.availableSpots--;
        }
    }

    public void finishTrainingDataBatch() {
        logger.info(String.format("GPU with type %s finished training data batch", this.type.toString()));
        this.cluster.statistics.gpuTimeUsed.getAndAdd(this.ticks - this.startTrainingTick);
        this.isTrainingDataBatch = false;
        this.trainedDataBatches++;
        if (!this.dataBatches.isEmpty()){
            this.sendDataBatchToCluster(this.dataBatches.remove(0));
        }
    }

    public void startTrainingDataBatch() {
        logger.info(String.format("GPU with type %s starting to train data batch", this.type.toString()));
        while (this.cluster.gpuToProcessedDataBatches.get(this).isEmpty()) {
            synchronized (this.cluster.gpuToProcessedDataBatches.get(this)) {
                try {
                    this.cluster.gpuToProcessedDataBatches.get(this).wait();
                    this.processedData.add(this.cluster.gpuToProcessedDataBatches.get(this).remove(0));
                    this.startTrainingTick = this.ticks;
                    this.isTrainingDataBatch = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void finishTrainingModel() {
        logger.info(String.format("GPU with type %s finished training model: %s", this.type.toString(),
                this.event.model.getName()));
        this.isTrainingModel = false;
        this.cluster.statistics.trainedModelsNames.add(this.event.model.getName());
    }

    enum Type {RTX3090, RTX2080, GTX1080}
}
