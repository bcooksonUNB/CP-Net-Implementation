import java.util.ArrayList;

public class CPTable<T> {

    protected PreferenceVariable<T>[] parents;
    protected DomainOrdering<T>[] ordering;
    private double[] manualCalc;
    private boolean isManualCalc = false;
    private T testValue;

    public CPTable(PreferenceVariable<T>[] indList){
        parents = indList;
        //updateTableSize(parents);
    }

    public CPTable(DomainOrdering<T> single_ordering){
        parents = new PreferenceVariable[0];
        updateTableSize(parents);
        DomainOrdering[] temp_ordering = {single_ordering};
        updateTableValues(temp_ordering);
    }

//    public CPTable(PreferenceVariable<T>[] indList, double[] probs, T testValue){
//        parents = indList;
//        manualCalc = probs;
//        isManualCalc = true;
//        this.testValue = testValue;
//    }

    public DomainOrdering<T> getOrdering(ArrayList<T> values){
        //if(isManualCalc) return getManualCount(values);
        if(values.size() != parents.length) return null;
        for(int i=0;i<values.size();i++){
            if(!parents[i].inPossibleValues(values.get(i))) return null;
        }
        int index = 0;
        int multiplier = 1;
        for(int i=0;i<values.size();i++){
            index += parents[i].getValueIndex(values.get(i))*multiplier;
            multiplier *= parents[i].getValues().length;
        }
        return ordering[index];
    }

    public boolean compareValues(T v1, T v2, ArrayList<T> parentValues){
        DomainOrdering<T> o = getOrdering(parentValues);
        //should change to throw exception here
        if(o == null) return false;
        return o.compareValues(v1, v2);
    }

//    private DomainOrdering<T> getManualCount(T[] values){
//        Boolean[] b1 = {false,true};
//        Boolean[] b2 = {true,false};
//        double count=0;
//        double total=0;
//        for(int i=0;i<values.length;i++){
//            if(values[i] == testValue){
//                count += 1;
//                total += manualCalc[i];
//            }
//        }
//        total = total/count;
//        if(total >= 0.8247) return new DomainOrdering(b2);
//        else return new DomainOrdering(b1);
//    }


    public void updateTable(PreferenceVariable[] newParents, DomainOrdering<T>[] newValues){
        updateTableSize(newParents);
        updateTableValues(newValues);
    }

    public void updateTableSize(PreferenceVariable[] newParents){
        int tableSize = 1;
        for(PreferenceVariable p : newParents){
            tableSize *= p.getValues().length;
        }
        ordering = new DomainOrdering[tableSize];
    }

    public boolean updateTableValues(DomainOrdering<T>[] newValues){
        if(newValues.length != ordering.length) return false;
        for(int i=0;i<newValues.length;i++) ordering[i] = newValues[i];
        return true;
    }

    public String toString(){
        String ret_string = "";
        for(int i=0;i<parents.length;i++){
            if(i != parents.length-1){
                ret_string += parents[i].toString() + "\t|";
            }
            else{
                ret_string += parents[i].toString() + "|\n";
            }
        }
        int listLength = ordering.length;
        if(listLength == 1){
            ret_string += ordering[0].toString();
            return ret_string;
        }
        int multStart = listLength/parents[parents.length-1].getValues().length;
        for(int i=0;i<ordering.length;i++){
            int multiplier = multStart;
            ArrayList<T> valueList = new ArrayList<>();
            for(int j=parents.length-1;j>=0;j--){
                int index = (i/multiplier)%parents[j].getValues().length;
                valueList.add(0,parents[j].getValues()[index]);
                multiplier /= parents[j].getValues().length;
            }
            String row_string = "";
            for(int j=0;j<valueList.size();j++){
                row_string += valueList.get(j).toString() + "\t|";
            }
            row_string += ordering[i].toString() + "|\n";
            ret_string += row_string;
        }
        return  ret_string;
    }

}
