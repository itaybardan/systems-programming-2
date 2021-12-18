package bgu.spl.mics.application.objects;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;


public class Model {

    private final String name;
    private final ModelType type;
    private final int size;
    private TestStatus testStatus;
    private final AtomicReference<ModelStatus> modelStatus;

    public Model(String name, ModelType type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;
        testStatus = TestStatus.Untested;
        modelStatus = new AtomicReference<>(ModelStatus.Undecided);
    }


    public Model(String name, String type, int size) {
        this.name = name;
        this.type = ModelType.valueOf(type.toLowerCase(Locale.ROOT));
        this.size = size;
        testStatus = TestStatus.Untested;
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

    public TestStatus getTestStatus(){ //For json
        return testStatus;
    }

    public ModelStatus getModelStatus() {
        return modelStatus.get();
    }

    public void setModelStatus(ModelStatus _status) {
        if (modelStatus.get() == ModelStatus.Undecided)
            testStatus = TestStatus.Tested;
        modelStatus.set(_status);
    }

    public enum TestStatus {
        Untested, Tested
    }
}
