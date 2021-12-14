package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
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
    private int currentModelIndex;


    public StudentService(Student _student) {
        super(_student.getName()); //Service and object will share the same name
        student = _student;
        currentModelIndex = 0;
    }

    @Override
    protected void initialize() {


        messageBus.register(this);


        //Setting up Callbacks
        Callback<PublishConferenceBroadcast> publishConferenceCallback = (PublishConferenceBroadcast b) -> {
            student.incrementPublifications(b.getPublishes(student.getModels()));
            student.incrementPapersRead(b.getPapersRead(student.getModels()));
        };

        Callback<TrainModelEvent> trainModelCallback = (TrainModelEvent e) -> {
            Model model = e.getModel();
            TestModelEvent testModelEvent = new TestModelEvent(model);
            testModelEvent.setFuture(sendEvent(testModelEvent));
            task = testModelEvent;

        };

        Callback<TestModelEvent> testModelCallback = (TestModelEvent e) -> {
            Model model = e.getModel();
            model.setModelStatus(e.getFuture().get()); //the Future of TestModelEvent will be of type enum ModelStatus - Good or Bad.

            if (model.getModelStatus() == ModelStatus.Good) {
                PublishResultsEvent publishResultsEvent = new PublishResultsEvent(model);
                publishResultsEvent.setFuture(sendEvent(publishResultsEvent));
            }

            if(++currentModelIndex < student.getModels().size()){ //Will send TrainModel for the next model if there is one
                task = new TrainModelEvent(student.getModels().get(currentModelIndex));
                task.setFuture(messageBus.sendEvent(task));
            }
        };

        //Setting up one's tasks.
        messages_callbacks.put(TrainModelEvent.class, trainModelCallback);
        messages_callbacks.put(TestModelEvent.class, testModelCallback);
        messages_callbacks.put(PublishConferenceBroadcast.class, publishConferenceCallback);

        //Subscribing to necessary events and broadcasts
        subscribeBroadcast(PublishConferenceBroadcast.class, publishConferenceCallback);

        //Sending the first TrainModel
        Model firstModel = student.getModels().get(0);
        if(firstModel != null) {
            TrainModelEvent trainModelEvent = new TrainModelEvent(firstModel);
            trainModelEvent.setFuture(sendEvent(trainModelEvent));
            task = trainModelEvent;
        }

    }

}

