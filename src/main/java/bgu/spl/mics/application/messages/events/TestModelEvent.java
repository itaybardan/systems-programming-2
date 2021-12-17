package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.ModelStatus;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event<ModelStatus> {
    private Future<ModelStatus> future;
    private Model model;
    private final Student.Degree degree;

    public TestModelEvent(Model _model, Student.Degree _degree){
        model = _model;
        future = new Future<>();
        degree = _degree;
    }

    public Student.Degree getStatus(){
        return degree;
    }

    public Model getModel(){
        return model;
    }
    @Override
    public Future<ModelStatus> getFuture() {
        return future;
    }

    @Override
    public void setFuture(Future<ModelStatus> _future) { future=_future;} //Future is either
}