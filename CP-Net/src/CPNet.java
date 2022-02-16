import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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

    public boolean setCountingConnections(PreferenceVariable child, PreferenceVariable[] parents, DomainOrdering<Boolean>[] values){
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
        cptTables[nodeIndex] = (CPTable<T>)(new CountingCPTable(parents, values));
        return true;
    }

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
            System.out.println(nodes.length);
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
        //HashMap<Integer, Outcome<T>> orderings = new HashMap();
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

    public boolean dominanceQuery(Outcome<T> o1, Outcome<T> o2){
        if(o1.equals(o2)) return false;
        Outcome<T> tempOutcome = o2.copyOutcome();
        ArrayList<Outcome<T>> flips = getImprovingFlipsForOutcome(tempOutcome);
        ArrayList<Outcome<T>> queue = new ArrayList<>();
        HashSet<Outcome<T>> used = new HashSet<>();
        //System.out.println("___Outcome___\n" + o2);
        //System.out.println("___Flips_____\n" + Arrays.asList(flips) + "---------");
        used.add(tempOutcome);
        for(Outcome<T> f : flips){
            if(!used.contains(f)){
                queue.add(f);
                used.add(f);
            }
        }
        while(queue.size() > 0){
            Outcome<T> currentOutcome = queue.remove(0);
            if (currentOutcome.equals(o1)) return true;
            flips = getImprovingFlipsForOutcome(currentOutcome);
            //System.out.println("___Outcome___\n" + currentOutcome);
            //System.out.println("___Flips_____\n" + Arrays.asList(flips) + "---------");
            for(Outcome<T> f : flips){
                if(!used.contains(f)){
                    queue.add(f);
                    used.add(f);
                }
            }
            //queue.addAll(Arrays.asList(flips));
            //System.out.println(used.size());
            //queue.removeAll(used);
        }
        return false;
    }

    public Outcome<T>[] dominanceSort(Outcome<T>[] outcomes){
        ArrayList<Outcome<T>> sort_list = new ArrayList<>();
        sort_list.add(outcomes[0]);
        for(int i=1;i<outcomes.length;i++){
            boolean flag = false;
            int n = sort_list.size();
            for(int j=0;j<n;j++){
                if(!dominanceQuery(outcomes[i],sort_list.get(j)) && dominanceQuery(sort_list.get(j), outcomes[i])){
                    flag = true;
                    sort_list.add(j, outcomes[i]);
                    break;
                }
            }
            if(!flag){
                sort_list.add(outcomes[i]);
            }
        }
        int counter = 0;
        for(Outcome<T> o : sort_list){
            outcomes[counter] = o;
            counter += 1;
        }
        return outcomes;
    }

    private ArrayList<Outcome<T>> getImprovingFlipsForOutcome(Outcome<T> o){
        ArrayList<Outcome<T>> temp_list = new ArrayList<>();
        PreferenceVariable<T>[] vars = o.getVariables();

        for(int i=0;i<o.size();i++){
            for(int j=0;j<vars[i].getValues().length;j++){
                if(!vars[i].getValues()[j].equals(o.getValue(vars[i]))){
                    ArrayList<T> valueList = new ArrayList<>();
                    for(int k=0;k<o.size();k++){
                        if(k != i){
                            valueList.add(o.getValue(vars[k]));
                        }
                        else{
                            valueList.add(vars[i].getValues()[j]);
                        }
                    }
                    if(isFlipImproved(o, vars[i], vars[i].getValues()[j])){
                        Outcome<T> flippedOutcome = new Outcome<>(vars, valueList);
                        temp_list.add(flippedOutcome);
                    }
                }
            }
        }
        Outcome<T>[] ret_list = new Outcome[temp_list.size()];
        ret_list = temp_list.toArray(ret_list);
        return temp_list;
    }

    private boolean isFlipImproved(Outcome<T> o, PreferenceVariable<T> var, T newVal){
        PreferenceVariable<T>[] parents = getParents(var);
        CPTable<T> t = getTable(var);
        ArrayList<T> parentValues = new ArrayList<>();
        for(PreferenceVariable p : parents){
            parentValues.add((T)o.getValue(p));
        }
        DomainOrdering<T> order = t.getOrdering(parentValues);
        return order.compareValues(newVal, o.getValue(var));
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
