package bgu.spl.mics.application.events;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Student;

public class PublishResultsEvent implements Event<String> {
    private Student _student;

    public PublishResultsEvent(Student student){ //Conference will receive this event and increment needed values to the student
        super();
        _student = student;
    }

    public Student getStudent(){
        return _student;
    }

    @Override
    public Future<String> getFuture() {
        return null;
    }

    @Override
    public void setFuture(Future<String> future) {

    }
}