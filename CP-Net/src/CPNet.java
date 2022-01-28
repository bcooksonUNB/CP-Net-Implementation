import java.util.ArrayList;
import java.util.Arrays;

public class CPNet<T> {

    private PreferenceVariable<T>[] nodes;
    private CPTable<T>[] cptTables;
    private AdjTable adjTable;

    public CPNet(PreferenceVariable<T>[] variables, CPTable<T>[] initial_tables){
        nodes = variables;
        cptTables = initial_tables;
        adjTable = new AdjTable(new ArrayList<PreferenceVariable>(Arrays.asList(variables)),nodes.length);
    }

    public boolean setConnections(PreferenceVariable child, PreferenceVariable[] parents, DomainOrdering<T>[] table){
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
        cptTables[nodeIndex] = new CPTable<T>(parents);
        cptTables[nodeIndex].updateTableValues(table);
        return true;
    }

//    public boolean setConnections(PreferenceVariable child, PreferenceVariable[] parents, double[] manCalcs, T testValue){
//        //add in check for cycles
//        if(!isNodeInNetwork(child)) return false;
//        for(PreferenceVariable parent : parents) {
//            if(!isNodeInNetwork(parent)) return false;
//        }
//        for(PreferenceVariable parent : parents) {
//            adjTable.addConnection(parent,child);
//        }
//        int nodeIndex = getNodeIndex(child);
//        if(nodeIndex == -1) return false;
//        cptTables[nodeIndex] = new CPTable<T>(parents, manCalcs, testValue);
//        return true;
//    }

    public DomainOrdering<T> getTableValue(PreferenceVariable node, ArrayList<T> values){
        CPTable<T> table = getTable(node);
        if(table == null) return null;
        return table.getOrdering(values);
    }

    public int getNodeIndex(PreferenceVariable node){
        for(int i=0;i<nodes.length;i++){
            if(node.equals(nodes[i])) return i;
        }
        return -1;
    }

    public CPTable<T> getTable(int index){
        if(index < 0 || index >= cptTables.length) return null;
        return cptTables[index];
    }

    public CPTable<T> getTable(PreferenceVariable node){
        int index = getNodeIndex(node);
        if(index == -1) return null;
        return getTable(index);
    }

    public boolean isNodeInNetwork(PreferenceVariable node){
        for(PreferenceVariable otherNode : nodes){
            if(node.equals(otherNode)){
                return true;
            }
        }
        return false;
    }

    public PreferenceVariable<T>[] getTopNodes(){
        ArrayList<PreferenceVariable<T>> temp_list = new ArrayList<PreferenceVariable<T>>();
        boolean[][] table = adjTable.getTable();
        for(int i=0;i<table.length;i++){
            boolean parentFlag = false;
            for(int j=0;j<table.length;j++){
                if(table[j][i]) {
                    parentFlag = true;
                    break;
                }
            }
            if(!parentFlag){
                temp_list.add(nodes[i]);
            }
        }
        PreferenceVariable<T>[] ret_list = new PreferenceVariable[temp_list.size()];
        return temp_list.toArray(ret_list);
    }

    public PreferenceVariable<T>[] getChildren(PreferenceVariable node){
        ArrayList<PreferenceVariable<T>> temp_list = new ArrayList<PreferenceVariable<T>>();
        if(!this.isNodeInNetwork(node)) return null;
        int index = this.getNodeIndex(node);
        for(int i=0;i<this.adjTable.getTable()[index].length;i++){
            if(this.adjTable.getTable()[index][i]){
                temp_list.add(nodes[i]);
            }
        }
        PreferenceVariable<T>[] ret_list = new PreferenceVariable[temp_list.size()];
        return temp_list.toArray(ret_list);
    }

    public PreferenceVariable<T>[] getParents(PreferenceVariable node){
        ArrayList<PreferenceVariable<T>> temp_list = new ArrayList<PreferenceVariable<T>>();
        if(!this.isNodeInNetwork(node)) return null;
        int index = this.getNodeIndex(node);
        for(int i=0;i<this.adjTable.getTable().length;i++){
            if(this.adjTable.getTable()[i][index]){
                temp_list.add(nodes[i]);
            }
        }
        PreferenceVariable<T>[] ret_list = new PreferenceVariable[temp_list.size()];
        return temp_list.toArray(ret_list);
    }

    public Outcome<T> getOptimalSolution(){
        ArrayList<T> ret_list = new ArrayList<>();
        boolean[] doneFlags = new boolean[nodes.length];
        for(int i=0;i<nodes.length;i++){
            doneFlags[i] = false;
            ret_list.add(null);
        }
        int doneCount = 0;
        ArrayList<PreferenceVariable> doneList = new ArrayList<PreferenceVariable>();

        while(doneCount < nodes.length){
            for(int i=0;i<nodes.length;i++){
                if(!doneFlags[i]){
                    PreferenceVariable[] parents = getParents(nodes[i]);
                    boolean flag = true;
                    for(PreferenceVariable p : parents){
                        if(!doneList.contains(p)){
                            flag = false;
                        }
                    }
                    if(flag){
                        T[] values = (T[])(new Object[parents.length]);
                        int counter = 0;
                        for(PreferenceVariable p : parents){
                            int index = getNodeIndex(p);
                            values[counter] = ret_list.get(index);
                            counter++;

                        }
                        ArrayList<T> valuesList = new ArrayList<>(Arrays.asList(values));
                        T value = cptTables[i].getOrdering(valuesList).getTop();
                        ret_list.set(i,value);
                        doneList.add(nodes[i]);
                        doneFlags[i] = true;
                        doneCount += 1;
                    }
                }
            }
        }
        Outcome<T> solution = new Outcome<T>(nodes,ret_list);
        return solution;
    }

    public boolean[] getOrderingQuery(Outcome<T> o1, Outcome<T> o2){
        boolean[] results = new boolean[2];
        results[0] = compareOutcomes(o1,o2);
        results[1] = compareOutcomes(o2,o1);
        return results;
    }

    public boolean compareOutcomes(Outcome<T> o1, Outcome<T> o2){
        //Step 1: Preform top down traversal to get all the variables who have the same values assigned to
        //all their ancenstors in both outcomes
        //Go through the CP-Net DAG topologically to determine this
        ArrayList<PreferenceVariable<T>> finalList = new ArrayList<PreferenceVariable<T>>();
        ArrayList<PreferenceVariable<T>> ancestorSharers = new ArrayList<PreferenceVariable<T>>();
        ArrayList<PreferenceVariable<T>> queue = new ArrayList<PreferenceVariable<T>>();
        for(PreferenceVariable<T> p : getTopNodes()) queue.add(p);
        while(queue.size() != 0){
            PreferenceVariable<T> popped = queue.get(0);
            queue.remove(0);
            if(o1.getValue(popped).equals(o2.getValue(popped))) {
                ancestorSharers.add(popped);
            }
            finalList.add(popped);
            PreferenceVariable<T>[] poppedChildren = getChildren(popped);
            for (PreferenceVariable<T> p : poppedChildren){
                PreferenceVariable<T>[] pParents = getParents(p);
                boolean flag = true;
                for(PreferenceVariable<T> parent : pParents){
                    if(!ancestorSharers.contains(parent)){
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    queue.add(p);
                }
            }
        }

        //step 2: check assignments of all different ancestor sharers to see if they differ.
        //if they do, see which of those values is prefered given their parents, return the result of
        //the comparison accordingly
        for(PreferenceVariable<T> p : finalList){
            if(!o1.getValue(p).equals(o2.getValue(p))){
                int index = getNodeIndex(p);
                T v1 = o1.getValue(p);
                T v2 = o2.getValue(p);
                PreferenceVariable<T>[] parents = getParents(p);
                ArrayList<T> parVals = new ArrayList<>();
                for(PreferenceVariable<T> parent : parents){
                    parVals.add(o1.getValue(parent));
                }
                if(!cptTables[index].compareValues(v1, v2, parVals)) return true;
            }
        }
        return false;
    }

    public Outcome<T>[] orderingQuerySort(Outcome<T>[] outcomes){
        //orders by insertion sort
        int n = outcomes.length;
        for(int i=1;i<n;i++){
            Outcome<T> o = outcomes[i];
            int j = i-1;
            while(j >= 0 && getOrderingQuery(o, outcomes[j])[0]){
                outcomes[j+1] = outcomes[j];
                j = j-1;
            }
            outcomes[j+1] = o;
        }
        return outcomes;
    }

    public Outcome<T> createOutcome(T[] values){
        if(values.length != nodes.length) return null;
        ArrayList<T> vals = new ArrayList<>();
        vals.addAll(Arrays.asList(values));
        return new Outcome<T>(nodes, vals);
    }

    public Outcome<T>[] getAllOutcomes(){
        int listLength = 1;
        for(int i=0;i<nodes.length;i++){
            listLength *= nodes[i].getValues().length;
        }
        Outcome<T>[] outcomeList = new Outcome[listLength];
        int multStart = listLength/nodes[nodes.length-1].getValues().length;
        for(int i=0;i<listLength;i++){
            int multiplier = multStart;
            ArrayList<T> valueList = new ArrayList<>();
            for(int j=nodes.length-1;j>=0;j--){
                int index = (i/multiplier)%nodes[j].getValues().length;
                valueList.add(0,nodes[j].getValues()[index]);
                multiplier /= nodes[j].getValues().length;
            }
            outcomeList[i] = new Outcome<T>(nodes, valueList);
        }
        return outcomeList;
    }

    public PreferenceVariable<T> getNodeByName(String name){
        for(PreferenceVariable<T> p : nodes){
            if(p.getName().equals(name)) return p;
        }
        return null;
    }

    public void printCPTable(PreferenceVariable p){
        int index = getNodeIndex(p);
        System.out.println(cptTables[index]);
    }

    public String toString(){
        String ret_string = "";
        for(int i=0;i<nodes.length;i++){
            PreferenceVariable<T> node = nodes[i];
            String temp_string = "";
            temp_string += node.getName() + ". Children: ";
            boolean flag = false;
            for(int j=0;j<this.adjTable.getTable()[i].length;j++){
                if(this.adjTable.getTable()[i][j]){
                    flag = true;
                    temp_string += nodes[j].getName() + ", ";
                }
            }
            if(!flag){
                temp_string += "None";
            }
            temp_string += "\n";
            ret_string += temp_string;
        }
        return ret_string;
    }

    public void printTreeForm(){
        PreferenceVariable<T>[] tops = getTopNodes();
        ArrayList<PreferenceVariable<T>> queue = new ArrayList<>();
        ArrayList<PreferenceVariable<T>> seen = new ArrayList<>();
        for(PreferenceVariable<T> p: tops) queue.add(p);
        while(queue.size() != 0){
            PreferenceVariable<T> current = queue.get(0);
            queue.remove(0);
            System.out.println(current);
            System.out.print("Children: ");
            PreferenceVariable<T>[] currentChildren = getChildren(current);
            for(PreferenceVariable<T> p : currentChildren){
                System.out.print(p);
                if(!seen.contains(p)){
                    seen.add(p);
                    queue.add(p);
                }
            }
            System.out.println();
        }
    }

    public void printReverseTreeForm(){
        for(PreferenceVariable p : nodes){
            System.out.println(p);
            System.out.print("Parents: ");
            for(PreferenceVariable par : getParents(p)) System.out.print(par + ", ");
            System.out.println();
        }
    }

}
