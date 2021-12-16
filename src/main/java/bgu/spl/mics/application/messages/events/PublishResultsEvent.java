package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent implements Event<String> {
    private Model model;

    public PublishResultsEvent(Model _model) { //Conference will receive this event and increment needed values to the student
        super();
        model = _model;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public Future<String> getFuture() {
        return null;
    }

    @Override
    public void setFuture(Future<String> future) {

    }
}
