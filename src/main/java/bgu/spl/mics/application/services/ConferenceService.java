package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.ConferenceInformation;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    private ConferenceInformation conferenceInformation;
    public ConferenceService(String name, ConferenceInformation conferenceInformation) {
        super(name);
        this.conferenceInformation = conferenceInformation;

    }

    @Override
    protected void initialize() {
        // TODO Implement this

    }
}
