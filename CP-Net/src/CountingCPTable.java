import java.util.ArrayList;

public class CountingCPTable extends CPTable<Boolean> {

    protected DomainOrdering<Boolean>[] malList;
    public static Boolean[] b1 = {true,false};
    public static Boolean[] b2 = {false,true};

    public CountingCPTable(PreferenceVariable<Boolean>[] parents, DomainOrdering<Boolean>[] malList){
        super(parents);
        this.malList = malList;
    }

    public CountingCPTable(PreferenceVariable<Boolean>[] parents, DomainOrdering<Boolean>[] malList, DomainOrdering<Boolean> initial_ordering){
        super(parents);
        this.malList = malList;
        DomainOrdering<Boolean>[] temp_ordering = new DomainOrdering[1];
        temp_ordering[0] = initial_ordering;
        this.ordering = temp_ordering;
    }

    public CountingCPTable(DomainOrdering<Boolean> initial_ordering){
        super(initial_ordering);
        this.malList = null;
    }

//    //Version that looks at both the parent present and the parent absent, defaults to benign
//    @Override
//    public DomainOrdering<Boolean> getOrdering(ArrayList<Boolean> values){
//        if(parents.length == 0) return ordering[0];
//        if(values.size() != parents.length) return null;
//        for(int i=0;i<values.size();i++){
//            if(!parents[i].inPossibleValues(values.get(i))) return null;
//        }
//        int total = 0;
//        for(int i=0;i<values.size();i++){
//            DomainOrdering<Boolean> p = malList[i];
//            if(values.get(i).equals(p.getTop())){
//                total += 1;
//            }
//            else{
//                total -= 1;
//            }
//        }
//        if(total > 0){
//            return new DomainOrdering<Boolean>(b1);
//        }
//        return new DomainOrdering<Boolean>(b2);
//    }

    //considers both scenarios, defaults to unconditional ordering
    @Override
    public DomainOrdering<Boolean> getOrdering(ArrayList<Boolean> values){
        if(parents.length == 0) return ordering[0];
        if(values.size() != parents.length) return null;
        for(int i=0;i<values.size();i++){
            if(!parents[i].inPossibleValues(values.get(i))) return null;
        }
        int total = 0;
        for(int i=0;i<values.size();i++){
            DomainOrdering<Boolean> p = malList[i];
            if(values.get(i).equals(p.getTop())){
                total += 1;
            }
            else{
                total -= 1;
            }
        }
        if(total > 0){
            return new DomainOrdering<Boolean>(b1);
        }
        if(total < 0) {
            return new DomainOrdering<Boolean>(b2);
        }
        return ordering[0];
    }

    //version that only counts a vote when the parent is present
//    @Override
//    public DomainOrdering<Boolean> getOrdering(ArrayList<Boolean> values){
//        if(parents.length == 0) return ordering[0];
//        if(values.size() != parents.length) return null;
//        for(int i=0;i<values.size();i++){
//            if(!parents[i].inPossibleValues(values.get(i))) return null;
//        }
//        int total = 0;
//        for(int i=0;i<values.size();i++){
//            if (values.get(i)) {
//                DomainOrdering<Boolean> p = malList[i];
//                if(p.getTop()){
//                    total += 1;
//                }
//                else{
//                    total -= 1;
//                }
//            }
//
//        }
//        if(total > 0){
//            return new DomainOrdering<Boolean>(b1);
//        }
//        else if(total < 0){
//            return new DomainOrdering<Boolean>(b2);
//        }
//        return ordering[0];
//    }

    @Override
    public boolean compareValues(Boolean v1, Boolean v2, ArrayList<Boolean> parentValues){
        DomainOrdering<Boolean> o = getOrdering(parentValues);
        //should change to throw exception here
        if(o == null) return false;
        return o.compareValues(v1, v2);
    }

}
