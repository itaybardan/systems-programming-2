package bgu.spl.mics.application.broadcasts;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    public final int time;
    public TickBroadcast(int _time){
        time = _time;
    }

}
