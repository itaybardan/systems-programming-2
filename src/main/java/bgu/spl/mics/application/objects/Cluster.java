package bgu.spl.mics.application.objects;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
    public static class Statistics {
        public CopyOnWriteArrayList<String> trainedModelsNames;
        public AtomicInteger processedDataBatches;
        public AtomicInteger cpuTimeUsed;
        public AtomicInteger gpuTimeUsed;

        public Statistics(CopyOnWriteArrayList<String> trainedModelsNames, AtomicInteger processedDataBatches, AtomicInteger cpuTimeUsed, AtomicInteger gpuTimeUsed) {
            this.trainedModelsNames = trainedModelsNames;
            this.processedDataBatches = processedDataBatches;
            this.cpuTimeUsed = cpuTimeUsed;
            this.gpuTimeUsed = gpuTimeUsed;
        }
    }

    public Statistics statistics;
    public ConcurrentHashMap<DataBatch, GPU> dataBatchToGpu;
    public PriorityBlockingQueue<DataBatch> unprocessedDataBatches;
    public ConcurrentHashMap<GPU, CopyOnWriteArrayList<DataBatch>> gpuToProcessedDataBatches;
    public final Object unprocessedDataBatchesLock = new Object();

    public Cluster() {
        this.unprocessedDataBatches = new PriorityBlockingQueue<>();
        this.dataBatchToGpu = new ConcurrentHashMap<>();
        this.gpuToProcessedDataBatches = new ConcurrentHashMap<>();
        this.statistics = new Statistics(new CopyOnWriteArrayList<>(), new AtomicInteger(0),
                new AtomicInteger(0), new AtomicInteger(0));
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Cluster getInstance() {
        return Cluster.ClusterInstanceHolder.clusterInstance;
    }

    private static class ClusterInstanceHolder {
        private static final Cluster clusterInstance = new Cluster();
    }
}
