import java.util.ArrayList;

public class CountingCPTable extends CPTable<Boolean> {

    private DomainOrdering<Boolean>[] malList;
    static Boolean[] b1 = {true,false};
    static Boolean[] b2 = {false,true};

    public CountingCPTable(PreferenceVariable<Boolean>[] parents, DomainOrdering<Boolean>[] malList){
        super(parents);
        this.malList = malList;
    }

    public CountingCPTable(DomainOrdering<Boolean> initial_ordering){
        super(initial_ordering);
        this.malList = null;
    }

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
        return new DomainOrdering<Boolean>(b2);
    }

    private DomainOrdering<Boolean> getManualCount(boolean[] values){
        Boolean[] b1 = {false,true};
        Boolean[] b2 = {true,false};
        double total=0;
        for(int i=0;i<values.length;i++){
            if(values[i]){
                total += 1;
            }
        }
        if(total >= 0.8247) return new DomainOrdering(b2);
        else return new DomainOrdering(b1);
    }

}
