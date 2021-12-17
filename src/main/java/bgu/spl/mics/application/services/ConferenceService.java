package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.PublishResultsEvent;
import bgu.spl.mics.application.objects.ConferenceInformation;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private ConferenceInformation conference;
    private AtomicInteger currentTime;

    public ConferenceService(String name, ConferenceInformation _conference) { //startTime will be calculated in advance in main, by previousConference's conferenceDate
        super(name);
        conference = _conference;
        currentTime = new AtomicInteger(1);
    }

    @Override
    protected void initialize() {

        //Setting up Callbacks
        Callback<PublishResultsEvent> publishResultsCallback = (PublishResultsEvent e) -> conference.addPublish(e.getModel());
        Callback<TickBroadcast> tickCallback = (TickBroadcast b) ->{
            currentTime.set(b.getTick());
            if(currentTime.get() >= conference.getDate()){
                sendBroadcast(new PublishConferenceBroadcast(conference.getAmountOfPublishes(), conference.getPublishes()));
                terminate();
            }
        };

        //Subscribing to necessary events and broadcasts
        subscribeEvent(PublishResultsEvent.class, publishResultsCallback);
        subscribeBroadcast(TickBroadcast.class, tickCallback);


    }

}

