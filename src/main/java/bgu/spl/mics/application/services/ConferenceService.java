package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.PublishResultsEvent;
import bgu.spl.mics.application.objects.ConferenceInformation;

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
    private int ticks;

    public ConferenceService(String name, ConferenceInformation _conference) { //startTime will be calculated in advance in main, by previousConference's conferenceDate
        super(name);
        conference = _conference;
        ticks = 0;
    }

    @Override
    protected void initialize() {
        subscribeEvent(PublishResultsEvent.class,
                publishResultsEvent -> conference.addPublish(publishResultsEvent.getModel()));

        subscribeBroadcast(TickBroadcast.class, TickBroadcastMessage -> {
            this.ticks += 1;
            if (this.ticks >= conference.getDate()) {
                sendBroadcast(new PublishConferenceBroadcast(conference.getAmountOfPublishes(), conference.getPublishes()));
                terminate();
            }
        });
    }
}

