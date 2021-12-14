package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
enum ModelType{
    Images, Tabular, Text
}

public class Model {

    private final String name;
    private final ModelType type;
    private final int size;

    public enum TestStatus{
        Untested, Tested
    }

    private AtomicReference<TestStatus> testStatus;
    private AtomicReference<ModelStatus> modelStatus;


    public  Model(String _name, ModelType _type, int _size){
        name = _name;
        type = _type;
        size = _size;
        testStatus = new AtomicReference<>(TestStatus.Untested);
        modelStatus = new AtomicReference<>(ModelStatus.Undecided);
    }
    public String getName() {
        return name;
    }

    public ModelType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public ModelStatus getModelStatus() {
        return modelStatus.get();
    }

    public void setModelStatus(ModelStatus _status){
        if(modelStatus.get() == ModelStatus.Undecided)
            testStatus.set(TestStatus.Tested);
            modelStatus.set(_status);
    }
}
