package bgu.spl.mics.example.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;

public class ExampleEvent implements Event<String> {

    private final String senderName; //will be added

    private Future<String> future;

    public ExampleEvent(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }


    public Future<String> getFuture() {
        return future;
    }

    public void setFuture(Future<String> future) {
        this.future = future;
    } //GUY ADDED
}