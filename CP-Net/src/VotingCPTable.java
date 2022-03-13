import java.util.ArrayList;

public class VotingCPTable extends CPTable<Boolean> {

    protected String[][] malList;
    public static Boolean[] b1 = {true,false};
    public static Boolean[] b2 = {false,true};

    public VotingCPTable(PreferenceVariable<Boolean>[] parents, String[][] malList){
        super(parents);
        this.malList = malList;
    }

    public VotingCPTable(PreferenceVariable<Boolean>[] parents, String[][] malList, DomainOrdering<Boolean> initial_ordering){
        super(parents);
        this.malList = malList;
        DomainOrdering<Boolean>[] temp_ordering = new DomainOrdering[1];
        temp_ordering[0] = initial_ordering;
        this.ordering = temp_ordering;
    }

    public VotingCPTable(DomainOrdering<Boolean> initial_ordering){
        super(initial_ordering);
        this.malList = null;
    }

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
            int PC = 0;
            if(malList[i][0].equals("mal")){
                PC = 1;
            }
            else if(malList[i][0].equals("ben")){
                PC = -1;
            }
            int PnC = 0;
            if(malList[i][1].equals("mal")){
                PnC = 1;
            }
            else if(malList[i][1].equals("ben")){
                PnC = -1;
            }
            int nPC = 0;
            if(malList[i][2].equals("mal")){
                nPC = 1;
            }
            else if(malList[i][2].equals("ben")){
                nPC = -1;
            }
            int nPnC = 0;
            if(malList[i][3].equals("mal")){
                nPnC = 1;
            }
            else if(malList[i][3].equals("ben")){
                nPnC = -1;
            }

            if(values.get(i)){
                if(PC == 1 && PnC == -1){
                    total += 1;
                }
                else if(PC == -1 && PnC == 1) {
                    total -= 1;
                }
            }
            if(!values.get(i)){
                if(nPC == 1 && nPnC == -1){
                    total += 1;
                }
                else if(nPC == -1 && nPnC == 1) {
                    total -= 1;
                }
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

    @Override
    public boolean compareValues(Boolean v1, Boolean v2, ArrayList<Boolean> parentValues){
        DomainOrdering<Boolean> o = getOrdering(parentValues);
        //should change to throw exception here
        if(o == null) return false;
        return o.compareValues(v1, v2);
    }

}
