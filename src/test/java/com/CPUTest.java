package com;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import bgu.spl.mics.application.objects.CPU;
import org.junit.Before;
import org.junit.Test;

public class CPUTest {

    private CPU cpu;

    @Before
    public void setUp() throws Exception {
        this.cpu = new CPU(1);
    }

    @Test
    public void testNumberOfCores() {
        assertTrue(this.cpu.getCores() > 0);
    }
}