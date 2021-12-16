package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event<String> {
    private final Model model;
    private final Student.Degree studentDegree;
    private Future<String> future;

    public TestModelEvent(Model model, Student.Degree studentDegree) {
        this.model = model;
        this.studentDegree = studentDegree;
        future = new Future<String>();
    }

    @Override
    public Future<String> getFuture() {
        return this.future;
    }

    @Override
    public void setFuture(Future<String> future) {
        this.future = future;
    }

    public Model getModel() {
        return this.model;
    }


    public Student.Degree getStudentDegree() {
        return this.studentDegree;
    }
}
