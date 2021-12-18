package bgu.spl.mics;

import bgu.spl.mics.application.messages.events.TestModelEvent;
import bgu.spl.mics.application.messages.events.TrainModelEvent;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.application.services.TimeService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.ImmutablePair;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



class ExampleGPU extends MicroService{


    /**
     * @param name the micro-service name (used mainly for debugging purposes -
     *             does not have to be unique)
     */
    public ExampleGPU(String name) {
        super(name);

    }

    @Override
    protected void initialize() {

        //Setting up Callbacks
        Callback<TrainModelEvent> trainModelEventCallback = (TrainModelEvent e) -> {

            System.out.println(name + " on train model " +e.getModel().getName());
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            complete(e, e.getModel());
        };
        Callback<TestModelEvent> testModelEventCallback = (TestModelEvent e) -> {

            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.out.println(name + " on test model " +e.getModel().getName());
            Random random = new Random();
            double randNum = random.nextDouble();
            ModelStatus modelStatus = ModelStatus.Bad;
            if(randNum >= 0.6) modelStatus = ModelStatus.Good;
            complete(e, modelStatus); //got here

        };

        subscribeEvent(TrainModelEvent.class, trainModelEventCallback);
        subscribeEvent(TestModelEvent.class, testModelEventCallback);

    }
}

//Mainly copied from CSMRunner

public class MainTest {

    public static void main(String[] args) {
        InputInfo inputInfo = parseJsonInputFile();
        ImmutablePair<ArrayList<MicroService>, TimeService> microServicesPair = createMicroServices(inputInfo);
        initMicroServices(microServicesPair.getLeft(), microServicesPair.getRight());


        //TODO add json write output here.

    }

    private static ImmutablePair<ArrayList<MicroService>, TimeService> createMicroServices(InputInfo inputInfo) {
        ArrayList<MicroService> microServices = new ArrayList<>();


        for (ConferenceInformation conferenceInformation : inputInfo.conferences) {
            microServices.add(new ConferenceService(conferenceInformation.getName(), conferenceInformation));
        }

        for (Student student : inputInfo.students) {
            microServices.add(new StudentService(student));
        }

//TODO DELETE
        ExampleGPU gpu1 = new ExampleGPU("gpu1");
        ExampleGPU gpu2 = new ExampleGPU("gpu2");

        microServices.add(gpu1);
        microServices.add(gpu2);
//TODO:READD

//        for (int i = 0; i < inputInfo.cpus.size(); i++) {
//            CPU cpu = inputInfo.cpus.get(i);
//            microServices.add(new CPUService(String.valueOf(i), cpu));
//        }
//
//        for (int i = 0; i < inputInfo.gpus.size(); i++) {
//            GPU gpu = inputInfo.gpus.get(i);
//            microServices.add(new GPUService(String.valueOf(i), gpu));
//        }



        System.out.println(inputInfo.tickTime + " " + inputInfo.duration);
        TimeService timeService = new TimeService("time-service", inputInfo.tickTime, inputInfo.duration);

        return new ImmutablePair<>(microServices, timeService);
    }

    private static void initMicroServices(ArrayList<? extends MicroService> microServices, TimeService ts) {
        ExecutorService fixedPool = Executors.newFixedThreadPool(microServices.size()+1);

        fixedPool.execute(ts);
        for (MicroService m : microServices){
            fixedPool.execute(m);
        }

        fixedPool.shutdown();

    }

    private static InputInfo parseJsonInputFile() {
        InputStream inputFileStream = MainTest.class.getClassLoader().getResourceAsStream("example_input.json");
        assert inputFileStream != null;
        Reader reader = new InputStreamReader(inputFileStream);
        JsonElement rootElement = JsonParser.parseReader(reader);
        JsonObject rootObject = rootElement.getAsJsonObject();

        int ticks = rootObject.get("TickTime").getAsInt();
        int duration = rootObject.get("Duration").getAsInt();
        ArrayList<GPU> gpus = new ArrayList<>();
        for (JsonElement gpuInfo : rootObject.get("GPUS").getAsJsonArray()) {
            gpus.add(new GPU(gpuInfo.getAsString()));
        }

        ArrayList<CPU> cpus = new ArrayList<>();
        for (JsonElement cpuInfo : rootObject.get("CPUS").getAsJsonArray()) {
            cpus.add(new CPU(cpuInfo.getAsInt()));
        }

        ArrayList<ConferenceInformation> conferences = new ArrayList<>();
        for (JsonElement conferenceInfo : rootObject.get("Conferences").getAsJsonArray()) {
            JsonObject conferenceInfoObject = conferenceInfo.getAsJsonObject();
            conferences.add(new ConferenceInformation(conferenceInfoObject.get("name").getAsString(),
                    conferenceInfoObject.get("date").getAsInt()));
        }

        ArrayList<Student> students = new ArrayList<>();

        for (JsonElement studentInfo : rootObject.get("Students").getAsJsonArray()) {
            JsonObject studentInfoObject = studentInfo.getAsJsonObject();
            ArrayList<Model> models = new ArrayList<>();
            for (JsonElement modelInfo : studentInfoObject.get("models").getAsJsonArray()) {
                JsonObject modelInfoObject = modelInfo.getAsJsonObject();
                models.add(new Model(modelInfoObject.get("name").getAsString(),
                        modelInfoObject.get("type").getAsString(), modelInfoObject.get("size").getAsInt()));
            }
            students.add(new Student(studentInfoObject.get("name").getAsString(), studentInfoObject.get("department").getAsString(),
                    studentInfoObject.get("status").getAsString(), models));

        }


        return new MainTest.InputInfo(students, conferences, duration, ticks, gpus, cpus);
    }

    private static class InputInfo {
        private final ArrayList<Student> students;
        private final ArrayList<ConferenceInformation> conferences;
        private final int duration;
        private final int tickTime;
        private final ArrayList<GPU> gpus;
        private final ArrayList<CPU> cpus;

        public InputInfo(ArrayList<Student> students, ArrayList<ConferenceInformation> conferences, int duration, int tickTime, ArrayList<GPU> gpus, ArrayList<CPU> cpus) {
            this.students = students;
            this.conferences = conferences;
            this.duration = duration;
            this.tickTime = tickTime;
            this.cpus = cpus;
            this.gpus = gpus;
        }
    }
}
