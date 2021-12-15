package bgu.spl.mics.application.objects;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class GPUTest {
    private GPU gpu;

    public void setUp() throws Exception {
        this.gpu = new GPU();
    }

    public void tearDown() throws Exception {
    }

    @Test
    public void testSendDataBatchToCluster() {
        this.gpu.sendDataBatchToCluster();
        assertEquals(0, this.gpu.getAvailableProcessedDataSpace());
    }

    @Test
    public void testTrainDataBatchModelComputingTime() {
        assertNotNull(this.gpu.getProcessedData().peek());
        long startTickTime = this.gpu.getTicks();
        this.gpu.trainModel();
        long computingTime = GPU.typeToTrainTickTime.get(this.gpu.getType());
        long endTickTime = this.gpu.getTicks();
        assertEquals(endTickTime - startTickTime, computingTime);
    }

    @Test
    public void testAvailablePlaceIsNonNegative() {
        assertTrue(this.gpu.getAvailableProcessedDataSpace() >= 0);
    }
}
