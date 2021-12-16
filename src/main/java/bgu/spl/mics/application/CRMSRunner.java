package bgu.spl.mics.application;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) {
        InputInfo inputInfo = parseJsonInputFile();
        ImmutablePair<ArrayList<MicroService>, TimeService> microServicesPair = createMicroServices(inputInfo);
        initMicroServices(microServicesPair.getLeft(), microServicesPair.getRight());
    }

    private static ImmutablePair<ArrayList<MicroService>, TimeService> createMicroServices(InputInfo inputInfo) {
        ArrayList<MicroService> microServices = new ArrayList<>();
        for (Student student : inputInfo.students) {
            microServices.add(new StudentService(student));
        }

        for (ConferenceInformation conferenceInformation : inputInfo.conferences) {
            // TODO: what did you meant here guy? what is the start time and why do we need it? i set it to 0 meantime.
            microServices.add(new ConferenceService(conferenceInformation.getName(), conferenceInformation, 0));
        }

        for (int i = 0; i < inputInfo.cpus.size(); i++) {
            CPU cpu = inputInfo.cpus.get(i);
            microServices.add(new CPUService(String.valueOf(i), cpu));
        }

        for (int i = 0; i < inputInfo.gpus.size(); i++) {
            GPU gpu = inputInfo.gpus.get(i);
            microServices.add(new GPUService(String.valueOf(i), gpu));
        }

        TimeService timeService = new TimeService("time-service", inputInfo.duration, inputInfo.tickTime);

        return new ImmutablePair<>(microServices, timeService);
    }

    private static void initMicroServices(ArrayList<? extends MicroService> microServices, TimeService ts) {
        // create threads out of every ms
        ArrayList<Thread> threads = new ArrayList<>();
        for (MicroService ms : microServices) {
            threads.add(new Thread(ms));
        }

        // start the threads
        Thread timeServiceThread = new Thread(ts);
        for (Thread thread : threads) {
            thread.start();
        }
        timeServiceThread.start();


        // wait for time service to finish
        try {
            timeServiceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // wait for the ms threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static InputInfo parseJsonInputFile() {
        InputStream inputFileStream = CRMSRunner.class.getClassLoader().getResourceAsStream("example_input.json");
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
            cpus.add(new CPU(cpuInfo.getAsInt(), new Cluster()));
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

        return new CRMSRunner.InputInfo(students, conferences, duration, ticks, gpus, cpus);
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
