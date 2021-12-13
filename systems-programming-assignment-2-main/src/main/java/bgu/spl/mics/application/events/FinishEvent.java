package bgu.spl.mics.application.events;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;

public class FinishEvent implements Event<Event> {
    Event<Event> finishedEvent;
    Future<Event> future;
    public FinishEvent(Event<Event> e){
        finishedEvent=e;
    }

    @Override
    public Future<Event> getFuture() {
        return future;
    }

    @Override
    public void setFuture(Future<Event> _future) {
        future = _future;
    }
}
