package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private int name;
    private String department;
    private Degree status;
    private AtomicInteger publications; //these values need to be updated instantly.
    private AtomicInteger papersRead; //these values need to be updated instantly.
    private ArrayList<Model> models;

    public Student(int _name, String _department, Degree _status, ArrayList<Model> _models) {
        name = _name;
        department = _department;
        status = _status;
        publications = new AtomicInteger(0);
        papersRead = new AtomicInteger(0); // All papers publicized except papers that belong to this Student.
        models = _models;
    }

    public void incrementPublifications(){
        publications.set(publications.get()+1);
    }
    public void incrementPapersRead(int amount){ //The amount excluding this student's publications will be calculated in service
        papersRead.set(papersRead.get()+amount);
    }

}
