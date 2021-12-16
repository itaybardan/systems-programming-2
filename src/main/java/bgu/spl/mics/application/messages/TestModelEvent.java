package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

public class TestModelEvent implements Event<String> {
    private final Model model;
    private Future<String> future;

    public TestModelEvent(Model model) {
        this.model = model;
        future = new Future<>();
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
}
