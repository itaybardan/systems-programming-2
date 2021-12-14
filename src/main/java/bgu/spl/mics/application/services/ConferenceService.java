package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.broadcasts.PublishConferenceBroadcast;
import bgu.spl.mics.application.events.PublishResultsEvent;
import bgu.spl.mics.application.objects.ConferenceInformation;
import bgu.spl.mics.application.objects.Model;

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
    private int countDown;

    public ConferenceService(String name, ConferenceInformation _conference, int _countDown) {
        super(name);
        conference = _conference;
        countDown = _countDown;
        // TODO Implement this
    }

    @Override
    protected void initialize() {


        messageBus.register(this);


        //Setting up Callbacks
        Callback<PublishResultsEvent> publishResultsCallback = ( PublishResultsEvent e) -> {
            conference.addPublish(e.getModel());
        };

        Callback<PublishConferenceBroadcast> publishConferenceCallback = (PublishConferenceBroadcast b) -> {
            terminate();
        };

        messages_callbacks.put(PublishResultsEvent.class, publishResultsCallback);

        //Setting up one's tasks.

        //Subscribing to necessary events and broadcasts


        //Sending Event for all models

    }

}

