package com;

import static org.junit.Assert.assertEquals;

import bgu.spl.mics.application.objects.CPU;
import org.junit.Before;
import org.junit.Test;

public class CPUTest {

    @Before
    public void setUp() throws Exception {
        CPU cpu = new CPU();
    }

    @Test
    public void testMultiply() {
        assertEquals(0, 0);
    }

    @Test
    public void testMultiplyWithZero() {
        assertEquals(0, 0);
        assertEquals(0, 0);
    }
}