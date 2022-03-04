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

    public Outcome<T> copyOutcome(){
        return new Outcome<T>(variables, values);
    }

    public String toString(){
        String ret_string = "";
        for(int i=0;i<variables.length;i++){
            ret_string += variables[i].toString() + ": " + (T)values.get(i).toString() + "\n";
        }
        return ret_string;
    }

    public int size(){
        return variables.length;
    }

    public PreferenceVariable<T>[] getVariables(){
        PreferenceVariable<T>[] ret_list = new PreferenceVariable[variables.length];
        for(int i=0;i<size();i++){
            ret_list[i] = variables[i];
        }
        return variables;
    }

    public ArrayList<T> getValues(){
        return values;
    }

    @Override
    public boolean equals(Object o){
        if(o == this) return true;
        if (!(o instanceof Outcome)) {
            return false;
        }
        Outcome<T> o2 = (Outcome<T>)o;
        if(this.size() != o2.size()) return false;
        PreferenceVariable<T>[] vars2 = o2.getVariables();
        for(int i=0;i<size();i++){
            if(!variables[i].equals(vars2[i])) return false;
            if(!values.get(i).equals(o2.getValue(vars2[i]))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for(PreferenceVariable<T> p : variables){
            result = prime * result + p.hashCode();
        }
        for(T t : values){
            result = prime * result + t.hashCode();
        }
        return result;
    }

}
