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
}
