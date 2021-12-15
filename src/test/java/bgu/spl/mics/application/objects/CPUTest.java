package bgu.spl.mics.application.objects;

import static org.junit.Assert.assertTrue;

import bgu.spl.mics.application.objects.CPU;
import org.junit.Before;
import org.junit.Test;

public class CPUTest {

    private CPU cpu;

    @Before
    public void setUp() {
        this.cpu = new CPU(0, Cluster.getInstance());
    }

    @Test
    public void testNumberOfCores() {
        assertTrue(this.cpu.getCores() > 0);
    }
}