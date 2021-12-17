package bgu.spl.mics.application.objects;

import org.junit.Test;

import static junit.framework.TestCase.*;

public class GPUTest {
    private GPU gpu;

    public void setUp() {
        this.gpu = new GPU("RTX2080");
    }

    public void tearDown() {
    }

    @Test
    public void testSendDataBatchToCluster() {
        this.gpu.sendDataBatchToCluster(new DataBatch(0, Data.DataType.images));
        assertEquals(0, this.gpu.getAvailableProcessedDataSpace());
    }

    @Test
    public void testTrainDataBatchModelComputingTime() {
        assertNotNull(this.gpu.getProcessedData().peek());
        long startTickTime = this.gpu.ticks;
        long computingTime = GPU.typeToTrainTickTime.get(this.gpu.type);
        long endTickTime = this.gpu.ticks;
        assertEquals(endTickTime - startTickTime, computingTime);
    }

    @Test
    public void testAvailablePlaceIsNonNegative() {
        assertTrue(this.gpu.getAvailableProcessedDataSpace() >= 0);
    }
}
