package bgu.spl.mics.application.objects;


import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConferenceInformation {
    private final String name;
    private final int date;

    private final AtomicInteger amount;
    private final CopyOnWriteArrayList<Model> publishes;

    public ConferenceInformation(String _name, int _date) {
        name = _name;
        date = _date;
        amount = new AtomicInteger(0);
        publishes = new CopyOnWriteArrayList<>();
    }

    public int getDate() {
        return date;
    }

    public CopyOnWriteArrayList<Model> getPublishes() {
        return publishes;
    }

    public int getAmountOfPublishes() {
        return amount.get();
    }

    public void addPublish(Model model) {
        amount.addAndGet(1);
        publishes.add(model);
    }

    public String getName() {
        return this.name;
    }

}
