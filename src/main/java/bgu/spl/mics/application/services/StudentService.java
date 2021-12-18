package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.broadcasts.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.broadcasts.TickBroadcast;
import bgu.spl.mics.application.messages.events.PublishResultsEvent;
import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.ModelStatus;
import bgu.spl.mics.application.objects.Student;

import java.util.logging.Logger;

import static bgu.spl.mics.application.services.State.*;

enum State {
    WaitingForTrainToFinish, WaitingForTestToFinish, DoneTrainingModels
}

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
    private static final Logger logger = Logger.getLogger(StudentService.class.getName());

    public State state;
    private final Student student;
    private int currentModelIndex;
    public Future<Model> trainFuture;
    public Future<ModelStatus> testFuture;


    public StudentService(Student student) {
        super(student.getName()); //Service and object will share the same name
        this.student = student;
        currentModelIndex = 0;
    }

    @Override
    protected void initialize() {
        logger.info(String.format("%s Student Service started ", this.name));

        this.subscribeBroadcast(TickBroadcast.class, tickBroadcastMessage -> {
            if (this.state == WaitingForTrainToFinish) {
                if (this.trainFuture.isDone()) {
                    this.testFuture = sendEvent(new TestModelEvent(this.trainFuture.get(), this.student.getStatus()));
                    this.state = WaitingForTestToFinish;
                }
            } else if (this.state == WaitingForTestToFinish) {
                if (this.testFuture.isDone()) {
                    this.student.getModels().get(currentModelIndex).setModelStatus(this.testFuture.get());
                    if (this.testFuture.get() == ModelStatus.Good) {
                        sendEvent(new PublishResultsEvent(this.trainFuture.get()));
                    }
                    this.currentModelIndex++;
                    if (currentModelIndex < this.student.getModels().size()) {
                        this.trainFuture = this.sendEvent(new TrainModelEvent(this.student.getModels().get(this.currentModelIndex)));
                        this.state = WaitingForTrainToFinish;
                    } else {
                        this.state = DoneTrainingModels;
                    }
                }
            }
        });

        subscribeBroadcast(PublishConferenceBroadcast.class, PublishConferenceBroadcastMessage -> {
            student.incrementPublications(PublishConferenceBroadcastMessage.getPublishes(student.getModels()));
            student.incrementPapersRead(PublishConferenceBroadcastMessage.getPapersRead(student.getModels()));
        });

        if (!this.student.getModels().isEmpty()) {
            this.trainFuture = this.sendEvent(new TrainModelEvent(this.student.getModels().get(this.currentModelIndex)));
            this.state = WaitingForTrainToFinish;
        } else {
            this.state = DoneTrainingModels;
        }
    }
}
