package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.events.TrainModelEvent;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.*;

public class GPUTest {
    private GPU gpu;

    @Before
    public void setUp() {
        this.gpu = new GPU("RTX2080");
    }

    @Test
    public void testState() {
        if (this.gpu.isTrainingDataBatch) {
            assertTrue(this.gpu.isTrainingModel);
            assertTrue(this.gpu.startTrainingTick >= 0);
        }
        if (this.gpu.isTrainingModel) {
            assertNotNull(this.gpu.event);
        }
    }

    @Test
    public void testTrainedModels() {
        assertTrue(this.gpu.trainedDataBatches >= 0);
    }

    @Test
    public void testAvailableSpots() {
        assertTrue(this.gpu.availableSpots >= 0);
    }

    @Test
    public void testStartTrainingModel() {
        TrainModelEvent event = new TrainModelEvent(new Model("test-model", ModelType.images, 1000));
        this.gpu.startTrainingModel(event);
        assertTrue(this.gpu.isTrainingModel);
        assertEquals(this.gpu.event, event);
    }

    @Test
    public void testFinishTrainingModel() {
        TrainModelEvent event = new TrainModelEvent(new Model("test-model-2", ModelType.tabular, 20000));
        this.gpu.startTrainingModel(event);
        Model trainedModel = this.gpu.event.model;
        this.gpu.finishTrainingModel();
        assertFalse(this.gpu.isTrainingModel);
        assertTrue(Cluster.getInstance().statistics.trainedModelsNames.contains(trainedModel.getName()));
    }

    @Test
    public void testStartTrainingDataBatch() {
        TrainModelEvent event = new TrainModelEvent(new Model("test-model-2", ModelType.text, 10000));
        this.gpu.startTrainingModel(event);
        this.gpu.startTrainingDataBatch();
        assertTrue(this.gpu.startTrainingTick >= 0);
    }

    @Test
    public void testFinishTrainingDataBatch() {
        TrainModelEvent event = new TrainModelEvent(new Model("test-model-4", ModelType.tabular, 5000));
        this.gpu.startTrainingModel(event);
        int trainedDataBatchesBeforeFinish = this.gpu.trainedDataBatches;
        int gpuTimeUsedBeforeFinish = Cluster.getInstance().statistics.gpuTimeUsed.get();
        this.gpu.finishTrainingDataBatch();
        assertEquals(Cluster.getInstance().statistics.gpuTimeUsed.get(),
                gpuTimeUsedBeforeFinish + this.gpu.ticks - this.gpu.startTrainingTick);
        int trainedDataBatchesAfterFinish = this.gpu.trainedDataBatches;
        assertEquals(trainedDataBatchesBeforeFinish + 1, trainedDataBatchesAfterFinish);
        assertFalse(this.gpu.isTrainingDataBatch);
    }

    public void tearDown() {
    }
}
