package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.events.TrainModelEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public int ticks;
    public Type type;
    private Cluster cluster = Cluster.getInstance();
    public LinkedList<DataBatch> processedData;
    public boolean isTrainingModel = false;
    public boolean isTrainingDataBatch = false;
    public int startTrainingTick;
    public Data data;
    public ArrayList<DataBatch> dataBatches;
    public int availableSpots;
    public TrainModelEvent event;

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
        this.processedData = new LinkedList<>();
        this.availableSpots = GPU.typeToProcessedDataCapacity.get(this.type) - this.processedData.size();
        this.isTrainingModel = true;
        this.event = event;
        Data data = new Data(Data.DataType.valueOf(event.model.getType().toString()), event.model.getSize());
        this.dataBatches = new ArrayList<>();
        for (int i = 0; i < data.getSize(); i += 1000) {
            dataBatches.add(new DataBatch(i));
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
        this.isTrainingDataBatch = false;
        this.availableSpots++;

    }

    public void startTrainingDataBatch() {
        while (this.cluster.gpuToProcessedDataBatches.get(this).isEmpty()) {
            synchronized (this.cluster.gpuToProcessedDataBatches.get(this)) {
                try {
                    this.cluster.gpuToProcessedDataBatches.get(this).wait();
                    this.processedData.add(this.cluster.gpuToProcessedDataBatches.get(this).remove(0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void finishTrainingModel() {
        this.isTrainingModel = false;
        this.event = null;
        this.dataBatches = null;
        this.availableSpots = -1;
    }

    enum Type {RTX3090, RTX2080, GTX1080}
}
