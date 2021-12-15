package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {

    public static <T extends Enum<?>> T searchEnum(Class<T> enumeration, String search) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private AtomicInteger publications = new AtomicInteger(0);
    private AtomicInteger papersRead = new AtomicInteger(0);
    private ArrayList<Model> models;


    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Degree getStatus() {
        return status;
    }

    public void setStatus(Degree status) {
        this.status = status;
    }


    public Student(String name, String department, String status, ArrayList<Model> models, int publications, int papersRead) {
        this.name = name;
        this.department = department;
        this.status = searchEnum(Degree.class, status);
        this.publications.set(publications);
        this.papersRead.set(papersRead);
        this.models = models;
    }
}
