package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import com.google.gson.*;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) {
        InputStream inputFileStream = CRMSRunner.class.getClassLoader().getResourceAsStream("example_input.json");
        assert inputFileStream != null;
        Reader reader = new InputStreamReader(inputFileStream);
        JsonElement rootElement = JsonParser.parseReader(reader);
        JsonObject rootObject = rootElement.getAsJsonObject();

        int ticks = rootObject.get("ticks").getAsInt();
        int duration = rootObject.get("duration").getAsInt();
        ArrayList<GPU> gpus = new ArrayList<>();
        for (JsonElement studentInfo : rootObject.get("Students").getAsJsonArray()) {
            gpus.add(new GPU());
        }

        ArrayList<CPU> cpus = new ArrayList<>();
        for (JsonElement studentInfo : rootObject.get("Students").getAsJsonArray()) {
            cpus.add(new CPU(0, new Cluster()));
        }

        ArrayList<ConfrenceInformation> conferences = new ArrayList<>();
        for (JsonElement studentInfo : rootObject.get("Students").getAsJsonArray()) {
            conferences.add(new ConfrenceInformation());
        }

        ArrayList<Student> students = new ArrayList<>();
        for (JsonElement studentInfo : rootObject.get("Students").getAsJsonArray()) {
            JsonObject studentInfoObject = studentInfo.getAsJsonObject();
            students.add(new Student(studentInfoObject.get("name").getAsString(), studentInfoObject.get("department").getAsString(),
                    studentInfoObject.get("status").getAsString(), 0, 0));
        }
    }
}
