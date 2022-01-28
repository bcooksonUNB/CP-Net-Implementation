public class PreferenceVariable<T> {

    protected String name;
    protected T[] possibleValues;

    public PreferenceVariable(String name){
        this.name = name;
    }

    public PreferenceVariable(String name, T[] possibleValues){
        this.name = name;
        this.possibleValues = possibleValues;
    }

    public boolean equals(PreferenceVariable other){
        if(this.name.equals(other.getName())) return true;
        return false;
    }

    public String getName(){
        return name;
    }

    public T[] getValues(){
        return possibleValues;
    }

    public void setValues(T[] newValues){
        possibleValues = newValues;
    }

    public boolean inPossibleValues(T value){
        for(T v : possibleValues){
            if(v.equals(value)) return true;
        }
        return false;
    }

    public int getValueIndex(T value){
        for(int i=0;i<possibleValues.length;i++){
            if(possibleValues[i].equals(value)) return i;
        }
        return -1;
    }

    public String toString(){
        String ret_string = name + " (";
        for(T val : getValues()) ret_string += val.toString() + ",";
        ret_string += ")";
        return ret_string;
    }
}
