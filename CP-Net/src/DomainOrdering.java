public class DomainOrdering<T> {

    private T[] variables;

    public DomainOrdering(T[] variables){
        this.variables = variables;
    }

    public String toString(){
        if(variables.length == 0) return "Empty Ordering";
        String ret_string = variables[0].toString();
        for(int i=1;i<variables.length;i++){
            ret_string += " > " + variables[i].toString();
        }
        return ret_string;
    }

    public int getPosition(T v){
        for(int i=0;i<variables.length;i++){
            if(variables[i].equals(v)) return i;
        }
        return -1;
    }

    public boolean isValueInDomain(T v){
        for(T var : variables){
            if(var.equals(v)) return true;
        }
        return false;
    }

    public boolean compareValues(T v1, T v2){
        if(!isValueInDomain(v1) || !isValueInDomain(v2)) return false;
        if(getPosition(v1) < getPosition(v2)){
            return true;
        }
        return false;
    }

    public T getTop(){
        return variables[0];
    }

}
