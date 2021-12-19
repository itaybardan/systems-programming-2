package bgu.spl.mics.application.objects;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

public class CPUTest {

    private CPU cpu;

    @Before
    public void setUp() {
        this.cpu = new CPU(1);
    }

    @Test
    public void testNumberOfCores() {
        assertTrue(this.cpu.cores > 0);
    }

    @Test
    public void startProcessDataBatch() {
        this.cpu.dataBatch = new DataBatch(0, Data.DataType.text, 1000);
        this.cpu.startProcessDataBatch();
        this.cpu.startProcessDataBatch();
        assertTrue(this.cpu.isProcessing);
        assertTrue(this.cpu.startProcessTick >= 0);
    }

    @Test
    public void finishProcessing() {
        this.cpu.dataBatch = new DataBatch(0, Data.DataType.images, 1000);
        this.cpu.startProcessDataBatch();
        int cpuTimeUsedBeforeFinish = Cluster.getInstance().statistics.cpuTimeUsed.get();
        int startProcessTick = this.cpu.startProcessTick;
        this.cpu.finishProcessing();
        assertFalse(this.cpu.isProcessing);
        assertEquals(Cluster.getInstance().statistics.cpuTimeUsed.get(), cpuTimeUsedBeforeFinish + this.cpu.ticks - startProcessTick);
        assertNull(this.cpu.dataBatch);
    }

    @Test
    public void getNewDataBatch() {
        if (this.cpu.getNewDataBatch()) {
            assertNotNull(this.cpu.dataBatch);
        }
        else {
            assertNull(this.cpu.dataBatch);
        }
    }

    @Test
    public void sendReadyDataBatch() {
        this.cpu.dataBatch = new DataBatch(0, Data.DataType.images, 1000);
        GPU gpu1 = new GPU("GTX1080");
        Cluster.getInstance().dataBatchToGpu.put(this.cpu.dataBatch, gpu1);
        Cluster.getInstance().gpuToProcessedDataBatches.put(gpu1, new CopyOnWriteArrayList<>());
        this.cpu.startProcessDataBatch();
        DataBatch dataBatchBeforeFinish = this.cpu.dataBatch;
        int processedDataBatchesBefore = Cluster.getInstance().statistics.processedDataBatches.get();
        this.cpu.sendReadyDataBatch();
        this.cpu.finishProcessing();
        assertTrue(Cluster.getInstance().gpuToProcessedDataBatches.get(gpu1).contains(dataBatchBeforeFinish));
        assertEquals(Cluster.getInstance().statistics.processedDataBatches.get(), processedDataBatchesBefore + 1);
        assertFalse(Cluster.getInstance().dataBatchToGpu.containsKey(dataBatchBeforeFinish));
    }
}