package bgu.spl.mics.application.objects;


import bgu.spl.mics.application.services.TimeService;

import java.util.logging.Logger;

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
    private static final Logger logger = Logger.getLogger(CPU.class.getName());
    public final int cores;
    private final Cluster cluster = Cluster.getInstance();
    public int ticks;
    public boolean isProcessing = false;
    public int startProcessTick;
    public DataBatch dataBatch;

    public CPU(int cores) {
        this.cores = cores;
    }

    /**
     * pop batch data from
     *
     * @pre getDataBatchSize() > 0
     * @post @pre(getDataBatchSize()) == @post(getDataBatchSize()) + 1
     * @post @pre(tickTime) - @post(tickTime) == (32 / getCores()) * @pre(result).tickTime
     */
    public void startProcessDataBatch() {
        if (!this.cluster.unprocessedDataBatches.isEmpty()) {
            this.isProcessing = true;
            this.dataBatch = this.cluster.unprocessedDataBatches.remove(0);
            this.startProcessTick = this.ticks;
            logger.info(String.format("CPU start process data batch: %d", this.dataBatch.startIndex));

        }
    }

    public void increaseTicks() {
        this.ticks += 1;
    }

    public boolean getNewDataBatch() {

        synchronized (this.cluster.unprocessedDataBatchesLock) {
            if (!this.cluster.unprocessedDataBatches.isEmpty()) {
                this.dataBatch = this.cluster.unprocessedDataBatches.remove(0);
                return true;
            }
        }
        return false;
    }

    public void finishProcessing() {
        this.isProcessing = false;
        this.cluster.statistics.cpuTimeUsed.getAndAdd(this.ticks - this.startProcessTick);
        this.startProcessTick = -1;
        this.dataBatch = null;
    }

    public void sendReadyDataBatch() {
        synchronized (this.cluster.gpuToProcessedDataBatches.get(this.cluster.dataBatchToGpu.get(this.dataBatch))) {
            this.cluster.gpuToProcessedDataBatches.get(this.cluster.dataBatchToGpu.get(this.dataBatch)).add(dataBatch);
            this.cluster.statistics.processedDataBatches.getAndIncrement();
            this.cluster.gpuToProcessedDataBatches.get(this.cluster.dataBatchToGpu.get(this.dataBatch)).notify();
            this.cluster.dataBatchToGpu.remove(this.dataBatch);
        }
    }
}

