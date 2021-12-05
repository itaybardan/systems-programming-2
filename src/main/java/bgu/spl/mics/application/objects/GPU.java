package bgu.spl.mics.application.objects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * @inv getAvailableProcessedDataSpace() >= 0
     */

    enum Type {RTX3090, RTX2080, GTX1080}

    public static Map<Type, Integer> typeToTrainTickTime = new HashMap<Type, Integer>() {{
        put(Type.RTX3090, 1);
        put(Type.RTX2080, 2);
        put(Type.GTX1080, 4);
    }};
    public static Map<Type, Integer> typeToProcessedDataCapacity = new HashMap<Type, Integer>() {{
        put(Type.RTX3090, 32);
        put(Type.RTX2080, 16);
        put(Type.GTX1080, 8);
    }};
    private Type type;
    private Model model;
    private Cluster cluster;
    private LinkedList<DataBatch> processedData;
    private int ticks;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * @post getAvailableProcessedDataSpace() == 0
     */
    public void sendDataBatchToCluster() {

    }

    /**
     * @pre getProcessedDataSize() > 0
     * @post @post(getTicks()) - @pre(getTicks()) == GPU.typeToTrainTickTime.get(this.type)
     */
    public void trainDataBatchModel() {

    }


    public int getAvailableProcessedDataSpace() {
        return GPU.typeToProcessedDataCapacity.get(this.type) - this.processedData.size();
    }

    public int getTicks() {
        return this.ticks;
    }

    public int getProcessedDataSize() {
        return this.processedData.size();
    }

    public LinkedList<DataBatch> getProcessedData() {
        return this.processedData;
    }

    public void addProcessedDataBatch(DataBatch db) {
        this.processedData.add(db);
    }

}
