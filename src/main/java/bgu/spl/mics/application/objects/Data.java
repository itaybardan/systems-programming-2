package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;
    private int size;

    public int getTickTime() {
        switch (this.type) {
            case Images:
                return 4;
            case Tabular:
                return 2;
            case Text:
                return 1;
            default:
                return 0;
        }

    }
}
