package bgu.spl.mics.application.objects;


import java.util.LinkedList;
import java.util.Queue;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    /**
     * @inv getCores() > 0
     * @inv getUnprocessedDataBatchSize() >= 0
     * @inv getProcessedDataBatchSize() >= 0
     */

    private final int cores;
    private LinkedList<DataBatch> unprocessedDataBatch;
    private LinkedList<Data> processedData;
    private Cluster cluster;
    private int ticks;

    public CPU(int cores, Cluster cluster) {
        this.cores = cores;
        this.cluster = cluster;
    }

    /**
     * @return number of cores
     * @post @pre(getCores()) == @post(getCores())
     */
    public int getCores() {
        return this.cores;
    }

    /**
     * @param unprocessedDataBatch the batch data coming from the cluster
     */
    public void setUnprocessedDataBatch(LinkedList<DataBatch> unprocessedDataBatch) {
        this.unprocessedDataBatch = unprocessedDataBatch;
    }

    /**
     * pop batch data from
     *
     * @pre getDataBatchSize() > 0
     * @post @pre(getDataBatchSize()) == @post(getDataBatchSize()) + 1
     * @post @pre(tickTime) - @post(tickTime) > (32 / getCores()) * @pre(result).tickTime
     */
    public void processData() {

    }

    /**
     * @return unprocessedDataBatch size
     */
    public int getUnprocessedDataBatchSize() {
        return this.unprocessedDataBatch.size();
    }

    public int getProcessedDataBatchSize() {
        return this.processedData.size();
    }


    /**
     * @post getProcessedDataSize() == 0
     * @post getUnprocessedDataSize() == 0
     */
    public void sendDataBackToCluster() {

    }

    public Queue<DataBatch> getUnprocessedDataBatch() {
        return unprocessedDataBatch;
    }

    public Queue<Data> getProcessedData() {
        return processedData;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }
}

