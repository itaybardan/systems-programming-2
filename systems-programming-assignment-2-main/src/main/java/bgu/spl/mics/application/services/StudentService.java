package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.broadcasts.PublishConferenceBroadcast;
import bgu.spl.mics.application.events.TrainModelEvent;
import bgu.spl.mics.application.events.TestModelEvent;
import bgu.spl.mics.application.events.PublishResultsEvent;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.ModelStatus;
import bgu.spl.mics.application.objects.Student;


/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    private Student student;


    public StudentService(Student _student) {
        super(_student.getName()); //Service and object will share the same name
        student = _student;
    }

    @Override
    protected void initialize() {


        messageBus.register(this);


        //Setting up Callbacks
        Callback<PublishConferenceBroadcast> publishConferenceCallback = (PublishConferenceBroadcast b) -> {
            student.incrementPublifications(b.getPublishes(student.getName()));
            student.incrementPapersRead(b.getPapersRead(student.getName()));
        };

        Callback<TrainModelEvent> trainModelCallback = (TrainModelEvent e) -> {
            Model model = e.getModel();
            TestModelEvent testModelEvent = new TestModelEvent(model);
            testModelEvent.setFuture(sendEvent(testModelEvent));
            tasks.add(testModelEvent);

        };

        Callback<TestModelEvent> testModelCallback = (TestModelEvent e) -> {
            Model model = e.getModel();
            model.setStatus(e.getFuture().get());

            if (model.getStatus() == ModelStatus.Success) {
                PublishResultsEvent publishResultsEvent = new PublishResultsEvent(student);
                Future<String> f = sendEvent(publishResultsEvent);
                tasks.add(publishResultsEvent);
            }
        };

        Callback<PublishResultsEvent> publishResultsCallback = (PublishResultsEvent e) -> {

        };

        //Subscribing to messages_callback and messageBus
        messages_callbacks.put(TrainModelEvent.class, trainModelCallback);
        messages_callbacks.put(TestModelEvent.class, testModelCallback);
        messages_callbacks.put(PublishResultsEvent.class, publishResultsCallback);
        subscribeBroadcast(PublishConferenceBroadcast.class, publishConferenceCallback);

        //Sending TrainModel for all models
        for (Model model : student.getModels()) {
            TrainModelEvent trainModelEvent = new TrainModelEvent(model);
            trainModelEvent.setFuture(sendEvent(trainModelEvent));
            tasks.add(trainModelEvent);

        }


    }

}

