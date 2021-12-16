package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

public class TrainModelEvent implements Event<String> {
    public final Model model;
    public final Future<String> future;

    public TrainModelEvent(Model model) {
        this.model = model;
        future = new Future<>();
    }

    @Override
    public Future<String> getFuture() {
        return future;
    }

    @Override
    public void setFuture(Future<String> future) {
        this.future = future;
    }
}
