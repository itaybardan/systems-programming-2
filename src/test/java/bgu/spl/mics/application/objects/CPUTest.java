package bgu.spl.mics.application.objects;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CPUTest {

    private CPU cpu;

    @Before
    public void setUp() {
        this.cpu = new CPU(0);
    }

    @Test
    public void testNumberOfCores() {
        assertTrue(this.cpu.cores > 0);
    }
}