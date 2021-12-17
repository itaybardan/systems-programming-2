package bgu.spl.mics.application.messages.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

public class TrainModelEvent implements Event<Model> {
    public final Model model;
    private Future<Model> future;

    public TrainModelEvent(Model _model) {
        model = _model;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public Future<Model> getFuture() {
        return future;
    }

    @Override
    public void setFuture(Future<Model> future) {
        this.future = future;
    }
    }
