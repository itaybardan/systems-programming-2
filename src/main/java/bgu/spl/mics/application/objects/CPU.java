package bgu.spl.mics.application.objects;


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
        }
    }

    public void increaseTicks() {
        this.ticks += 1;
    }

    public void getNewDataBatch() {
        // TODO: add lock here
        synchronized (this.cluster.unprocessedDataBatches) {
            if (!this.cluster.unprocessedDataBatches.isEmpty()) {
                this.dataBatch = this.cluster.unprocessedDataBatches.remove(0);
            }
        }
    }

    public void finishProcessing() {
        this.isProcessing = false;
        this.startProcessTick = -1;
        this.dataBatch = null;
    }

    public void sendReadyDataBatch() {
        synchronized (this.cluster.gpuToProcessedDataBatches.get(this.cluster.dataBatchToGpu.get(this.dataBatch))) {
            this.cluster.gpuToProcessedDataBatches.get(this.cluster.dataBatchToGpu.get(this.dataBatch)).add(dataBatch);
            this.cluster.dataBatchToGpu.remove(this.dataBatch);
            this.cluster.gpuToProcessedDataBatches.get(this.cluster.dataBatchToGpu.get(this.dataBatch)).notify();
        }
    }
}

