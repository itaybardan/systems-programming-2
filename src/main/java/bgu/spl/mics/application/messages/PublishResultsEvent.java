package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;

public class PublishResultsEvent implements Event<String> {
    public PublishResultsEvent(){
        super();

    }
    @Override
    public Future<String> getFuture() {
        return null;
    }

    @Override
    public void setFuture(Future<String> future) {

    }
}