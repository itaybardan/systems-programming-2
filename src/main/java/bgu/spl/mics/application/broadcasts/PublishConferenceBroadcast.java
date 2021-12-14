package bgu.spl.mics.application.broadcasts;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


public class PublishConferenceBroadcast implements Broadcast {
    private final int publishesAmount;
    private CopyOnWriteArrayList<Model> publishes;

    PublishConferenceBroadcast(int amm, CopyOnWriteArrayList<Model> _publishes){
        publishesAmount = amm;
        publishes = _publishes;
    }

    public int getPapersRead(ArrayList<Model> models){
        int papersRead = publishesAmount;
        for (Model m : models){
            if (publishes.contains(m)) papersRead = papersRead - 1;
        }
        return papersRead;
    }
    public int getPublishes(ArrayList<Model> models){
        int papersRead = 0;
        for (Model m : models){
            if (publishes.contains(m)) papersRead = papersRead + 1;
        }
        return papersRead;
    }

}
