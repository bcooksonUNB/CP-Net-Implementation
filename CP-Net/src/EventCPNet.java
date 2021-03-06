import java.util.ArrayList;
import java.util.Arrays;

public class EventCPNet extends CPNet<Boolean>{

    public EventCPNet(EventPattern[] variables, CountingCPTable[] initial_tables){
        super(variables,initial_tables);
    }

    public boolean setCountingConnections(PreferenceVariable child, PreferenceVariable[] parents, DomainOrdering<Boolean>[] mal_list, DomainOrdering<Boolean> default_ordering){
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
        cptTables[nodeIndex] = new CountingCPTable(parents,mal_list,default_ordering);
        return true;
    }
}
