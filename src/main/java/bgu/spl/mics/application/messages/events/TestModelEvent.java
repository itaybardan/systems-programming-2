package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.ModelStatus;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event<ModelStatus> {
    private final Model model;
    private final Student.Degree studentDegree;
    private Future<ModelStatus> future;

    public TestModelEvent(Model model, Student.Degree studentDegree) {
        this.model = model;
        this.studentDegree = studentDegree;
        future = new Future<ModelStatus>();
    }

    @Override
    public Future<ModelStatus> getFuture() {
        return this.future;
    }

    @Override
    public void setFuture(Future<ModelStatus> future) {
        this.future = future;
    }

    public Model getModel() {
        return this.model;
    }


    public Student.Degree getStudentDegree() {
        return this.studentDegree;
    }
}
