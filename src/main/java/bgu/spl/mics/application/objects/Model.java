package bgu.spl.mics.application.objects;

import java.util.Locale;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
enum ModelType{
    images, tabular, text
}

public class Model {
    private String name;
    private ModelType type;
    private int size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Model(String name, String type, int size) {
        this.name = name;
        this.type = ModelType.valueOf(type.toLowerCase(Locale.ROOT));
        this.size = size;
    }
}
