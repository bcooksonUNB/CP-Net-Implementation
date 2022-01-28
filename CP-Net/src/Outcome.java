import java.util.ArrayList;

public class Outcome<T> {

    private PreferenceVariable<T>[] variables;
    private ArrayList<T> values;

    public Outcome(PreferenceVariable<T>[] variables, ArrayList<T> values){
        this.variables = variables;
        this.values = values;
    }

    public T getValue(PreferenceVariable<T> var){
        int index = -1;
        for(int i=0;i<variables.length;i++){
            if(variables[i].equals(var)){
                index = i;
                break;
            }
        }
        if(index == -1) return null;
        return values.get(index);
    }


    public String toString(){
        String ret_string = "";
        for(int i=0;i<variables.length;i++){
            ret_string += variables[i].toString() + ": " + (T)values.get(i).toString() + "\n";
        }
        return ret_string;
    }
}
