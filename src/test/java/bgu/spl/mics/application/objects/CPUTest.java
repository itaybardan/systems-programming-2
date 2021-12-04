package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import static junit.framework.TestCase.*;


public class CPUTest {

    private CPU cpu;

    @Before
    public void setUp() throws Exception {
        this.cpu = new CPU(1, new Cluster());
        this.cpu.setUnprocessedDataBatch(new LinkedList<DataBatch>());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void coresGreaterThanZero() {
        assertTrue(this.cpu.getCores() > 0);
    }

    @Test
    public void unprocessedDataBatchSizeGreaterEqualsZero() {
        assertTrue(this.cpu.getUnprocessedDataBatchSize() >= 0);
    }

    @Test
    public void processedDataBatchSizeGreaterEqualsZero() {
        assertTrue(this.cpu.getProcessedDataBatchSize() >= 0);
    }

    @Test
    public void getCores() {
        int cores1 = this.cpu.getCores();
        int cores2 = this.cpu.getCores();
        assertEquals(cores1, cores2);
    }

    @Test
    public void processData() {
        long startTickTime = this.cpu.getTicks();
        DataBatch db = this.cpu.getUnprocessedDataBatch().peek();
        assertNotNull(db);
        this.cpu.processData();
        long computingTime = (long) 32 / this.cpu.getCores() * db.getData().getTickTime();
        long endTickTime = this.cpu.getTicks();
        assertEquals(endTickTime - startTickTime, computingTime);
    }

    @Test
    public void sendDataBackToCluster() {
        this.cpu.sendDataBackToCluster();
        assertEquals(0, this.cpu.getProcessedDataBatchSize());
        assertEquals(0, this.cpu.getUnprocessedDataBatchSize());
    }
}