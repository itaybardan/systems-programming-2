package bgu.spl.mics.application.events;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.ModelStatus;

public class TestModelEvent implements Event<ModelStatus> {
    private final Model model;

    public TestModelEvent(Model _model){
        model = _model;
    }

    public Model getModel(){
        return model;
    }
    @Override
    public Future<ModelStatus> getFuture() {
        return null;
    }

    @Override
    public void setFuture(Future<ModelStatus> future) {} //Future is either
}