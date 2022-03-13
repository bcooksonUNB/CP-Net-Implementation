import java.util.ArrayList;
import java.util.Arrays;

public class VotingCPNet extends CPNet<Boolean>{

    public VotingCPNet(EventPattern[] variables, VotingCPTable[] initial_tables){
        super(variables,initial_tables);
    }

    public boolean setCountingConnections(PreferenceVariable child, PreferenceVariable[] parents, String[][] mal_list, DomainOrdering<Boolean> default_ordering){
        //add in check for cycles
        if(!isNodeInNetwork(child)) return false;
        for(PreferenceVariable parent : parents) {
            if(!isNodeInNetwork(parent)) return false;
        }
        for(PreferenceVariable parent : parents) {
            adjTable.addConnection(parent,child);
        }
        int nodeIndex = getNodeIndex(child);
        if(nodeIndex == -1) return false;
        cptTables[nodeIndex] = new VotingCPTable(parents,mal_list,default_ordering);
        return true;
    }
}
