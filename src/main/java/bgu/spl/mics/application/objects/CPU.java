package bgu.spl.mics.application.objects;


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final Cluster cluster;
    public AtomicInteger ticks;
    private LinkedList<DataBatch> unprocessedDataBatch;
    private Queue<Data> processedData;

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
     * pop batch data from
     *
     * @pre getDataBatchSize() > 0
     * @post @pre(getDataBatchSize()) == @post(getDataBatchSize()) + 1
     * @post @pre(tickTime) - @post(tickTime) == (32 / getCores()) * @pre(result).tickTime
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

    /**
     * @param unprocessedDataBatch the batch data coming from the cluster
     */
    public void setUnprocessedDataBatch(LinkedList<DataBatch> unprocessedDataBatch) {
        this.unprocessedDataBatch = unprocessedDataBatch;
    }

    public int getTicks() {
        return ticks.get();
    }


    public void increaseTicks() {
        int currentTicks;
        do {
            currentTicks = this.ticks.get();
        } while (!this.ticks.compareAndSet(currentTicks, currentTicks + 1));
    }
}

