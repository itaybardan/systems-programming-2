package bgu.spl.mics.application.objects;


import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConferenceInformation {
    private String name;
    private int date;

    private CopyOnWriteArrayList<Model> publishes;

    public ConferenceInformation(String _name, int _date){
        name = _name;
        date = _date;
        publishes = new CopyOnWriteArrayList<>();
    }

    public void addPublish(Model model){
        publishes.add(model);
    }


}
