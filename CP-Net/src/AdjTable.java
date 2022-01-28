import java.util.ArrayList;

public class AdjTable {

    private ArrayList<PreferenceVariable> nodeIDList;
    private boolean[][] adjTable;


    public AdjTable(ArrayList<PreferenceVariable> nodeIDList, int nodeCount){
        this.nodeIDList = nodeIDList;
        adjTable = new boolean[nodeCount][nodeCount];
        for(int i=0;i<nodeCount;i++){
            for(int j=0;j<nodeCount;j++){
                adjTable[i][j] = false;
            }
        }
    }

    public boolean addConnection(PreferenceVariable parent, PreferenceVariable child){
        int parentIndex = nodeIDList.indexOf(parent);
        int childIndex = nodeIDList.indexOf(child);
        if(adjTable[parentIndex][childIndex]) return false;
        adjTable[parentIndex][childIndex] = true;
        return true;
    }

    public boolean[][] getTable(){
        return adjTable;
    }
}

