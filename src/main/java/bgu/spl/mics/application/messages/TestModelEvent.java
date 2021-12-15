package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event<String> {
    private Model model;
    private Student student;
    private Future<String> future;

    public TestModelEvent(Model model, Student student, Future<String> future){
        this.model = model;
        this.student = student;
        this.future = future;
    }

    @Override
    public Future<String> getFuture() {
        return this.future;
    }

    @Override
    public void setFuture(Future<String> future) {
        this.future = future;
    }

    public Student getStudent() {
        return this.student;
    }

    public Model getModel() {
        return this.model;
    }
}