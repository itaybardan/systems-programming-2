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
        this.gpu.sendDataBatchToCluster(new DataBatch(0));
        assertEquals(0, this.gpu.getAvailableProcessedDataSpace());
    }

    @Test
    public void testTrainDataBatchModelComputingTime() {
        assertNotNull(this.gpu.getProcessedData().peek());
        long startTickTime = this.gpu.getTicks();
        this.gpu.trainModel(new Model("stam-model", ModelType.images, 100000));
        long computingTime = GPU.typeToTrainTickTime.get(this.gpu.getType());
        long endTickTime = this.gpu.getTicks();
        assertEquals(endTickTime - startTickTime, computingTime);
    }

    @Test
    public void testAvailablePlaceIsNonNegative() {
        assertTrue(this.gpu.getAvailableProcessedDataSpace() >= 0);
    }
}
