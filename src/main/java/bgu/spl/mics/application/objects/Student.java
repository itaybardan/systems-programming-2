package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Comparator;


/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    private final String name;
    private final String department;
    private final Degree status;
    private final ArrayList<Model> models;

    public int getPublications() {
        return publications;
    }

    public void setPublications(int publications) {
        this.publications = publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public void setPapersRead(int papersRead) {
        this.papersRead = papersRead;
    }

    private int publications;
    private int papersRead;

    public Student(String name, String department, String status, ArrayList<Model> models) {
        this.name = name;
        this.department = department;
        this.status = searchEnum(Degree.class, status);
        this.models = models;
        this.models.sort(Comparator.comparingInt(Model::getSize));
        this.publications = 0;
        this.papersRead = 0;


    }

    public Student(String name, String department, Degree status, ArrayList<Model> models) {
        this.name = name;
        this.department = department;
        this.status = status;
        this.models = models;
    }

    public static <T extends Enum<?>> T searchEnum(Class<T> enumeration, String search) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }

    public ArrayList<Model> getModels() {
        return models;
    }

    public String getName() {
        return name;
    }

    public void incrementPublications(int amount) {
        publications = publications + amount;
    }

    public void incrementPapersRead(int amount) { //The amount excluding this student's publications will be calculated in service
        papersRead = papersRead + amount;
    }

    public Degree getStatus() {
        return this.status;
    }

    public String getDepartment() {
        return department;
    }

    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

}
