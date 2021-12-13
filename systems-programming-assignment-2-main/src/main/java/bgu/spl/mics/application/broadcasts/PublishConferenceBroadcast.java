package bgu.spl.mics.application.broadcasts;

import bgu.spl.mics.Broadcast;

import java.util.Map;


public class PublishConferenceBroadcast implements Broadcast {
    private int publishesAmount;
    private Map<String, Integer> authorsOfPublishes;

    PublishConferenceBroadcast(int amm, Map<String, Integer> names){
        publishesAmount = amm;
        authorsOfPublishes = names;
    }

    public int getPapersRead(String name){
        if(!authorsOfPublishes.containsKey(name)) return publishesAmount;
        return publishesAmount - authorsOfPublishes.get(name);
    }
    public int getPublishes(String name){
        if(!authorsOfPublishes.containsKey(name)) return 0;
        return authorsOfPublishes.get(name);
    }

}
