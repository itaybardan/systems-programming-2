package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TrainModelEvent implements Event<String> {
    public Model model;
    public Student student;
    public Future<Model> future;

    public TrainModelEvent(Model model, Student student, Future<Model> future){
        this.model = model;
        this.student = student;
        this.future = future;
    }
    @Override
    public Future<String> getFuture() {
        return null;
    }

    @Override
    public void setFuture(Future<String> future) {

    }
}
