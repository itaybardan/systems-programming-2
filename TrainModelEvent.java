package bgu.spl.mics.application.events;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;

public class TrainModelEvent implements Event<String> {
    public TrainModelEvent(){
    }
    @Override
    public Future<String> getFuture() {
        return null;
    }

    @Override
    public void setFuture(Future<String> future) {

    }
}
