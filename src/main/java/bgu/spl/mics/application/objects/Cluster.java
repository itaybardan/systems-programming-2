package bgu.spl.mics.application.objects;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
    public final Queue<DataBatch> unProcessedDataBatches;
    public final Queue<DataBatch> processedDataBatches;
    private ArrayList<GPU> gpus;
    private ArrayList<CPU> cpus;

    public Cluster() {
        this.unProcessedDataBatches = new LinkedList<>();
        this.processedDataBatches = new LinkedList<>();
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
