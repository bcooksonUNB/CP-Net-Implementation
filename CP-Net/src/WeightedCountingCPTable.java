import java.util.ArrayList;

public class WeightedCountingCPTable extends CountingCPTable{

    public WeightedCountingCPTable(PreferenceVariable<Boolean>[] parents, DomainOrdering<Boolean>[] malList, double bias){
        super(parents, malList);
    }

    public WeightedCountingCPTable(DomainOrdering<Boolean> initial_ordering){
        super(initial_ordering);
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
}
