package bgu.spl.mics.example.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;

public class ExampleEvent implements Event<String>{

    private String senderName;
    private Future<String> future;

    public ExampleEvent(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }

    public Future<String> getFuture() {
        return this.future;
    }

    public void setFuture(Future<String> newFuture) {
        ;
    }
}