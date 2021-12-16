package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.broadcasts.PublishConferenceBroadcast;
import bgu.spl.mics.application.broadcasts.TickBroadcast;
import bgu.spl.mics.application.events.PublishResultsEvent;
import bgu.spl.mics.application.objects.ConferenceInformation;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private final ConferenceInformation conference;
    private final int startTime;
    private final AtomicInteger currentTime;

    public ConferenceService(String name, ConferenceInformation _conference, int startTime) { //startTime will be calculated in advance in main, by previousConference's conferenceDate
        super(name);
        conference = _conference;
        this.startTime = startTime; // = prevConference.conferenceDate / tickTime || 1 if it's the first conference, startTime <= program duration
        currentTime = new AtomicInteger(1);
    }

    @Override
    protected void initialize() {

        synchronized (this) {
            try {
                Thread.sleep(startTime);
            } catch (InterruptedException e) {
                //terminate();
            }
        }

        messageBus.register(this);


        //Setting up Callbacks
        Callback<PublishResultsEvent> publishResultsCallback = (PublishResultsEvent e) -> conference.addPublish(e.getModel());
        Callback<TickBroadcast> tickCallback = (TickBroadcast b) -> {
            currentTime.set(b.time);
            if (currentTime.get() >= conference.getDate()) {
                sendBroadcast(new PublishConferenceBroadcast(conference.getAmountOfPublishes(), conference.getPublishes()));
                terminate();
            }
        };

        //Subscribing to necessary events and broadcasts
        subscribeEvent(PublishResultsEvent.class, publishResultsCallback);
        subscribeBroadcast(TickBroadcast.class, tickCallback);


    }

}

