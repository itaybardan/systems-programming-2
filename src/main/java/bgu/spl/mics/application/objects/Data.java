package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    private final DataType type;
    private final int processed;
    private final int size;
    public Data(DataType type, int size) {
        this.type = type;
        this.processed = 0;
        this.size = size;
    }

    public int getTickTime() {
        switch (this.type) {
            case images:
                return 4;
            case tabular:
                return 2;
            case text:
                return 1;
            default:
                return 0;
        }
    }

    public int getSize() {
        return this.size;
    }

    /**
     * Enum representing the Data type.
     */
    enum DataType {
        images, text, tabular
    }
}
