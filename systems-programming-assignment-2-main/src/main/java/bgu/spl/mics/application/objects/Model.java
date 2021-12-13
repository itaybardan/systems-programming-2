package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
enum ModelType{
    Images, Tabular, Text
}

public class Model {

    private String name;
    private ModelType type;
    private int size;



    private ModelStatus status;

    public  Model(String _name, ModelType _type, int _size){
        name = _name;
        type = _type;
        size = _size;
        status = ModelStatus.Undecided;
    }
    public String getName() {
        return name;
    }

    public ModelType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public ModelStatus getStatus() {
        return status;
    }

    public void setStatus(ModelStatus _status){
        if(status == ModelStatus.Undecided)
            status = _status;
    }

}
