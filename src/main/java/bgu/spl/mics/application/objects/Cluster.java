package bgu.spl.mics.application.objects;


import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
    public ConcurrentHashMap<DataBatch, GPU> dataBatchToGpu;
    public CopyOnWriteArrayList<DataBatch> unprocessedDataBatches;
    public ConcurrentHashMap<GPU, CopyOnWriteArrayList<DataBatch>> gpuToProcessedDataBatches;

    public Cluster() {
        this.unprocessedDataBatches = new CopyOnWriteArrayList<>();
        this.dataBatchToGpu = new ConcurrentHashMap<>();
        this.gpuToProcessedDataBatches = new ConcurrentHashMap<>();
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
