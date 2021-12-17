package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    public static int size = 1000;
    public Data.DataType type;
    public int startIndex;

    public DataBatch(int startIndex, Data.DataType dataType) {
        this.startIndex = startIndex;
        this.type = dataType;

    }
}
