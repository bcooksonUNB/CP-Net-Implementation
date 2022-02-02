import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Driver3 {

    static Boolean[] b1 = {true,false};
    static Boolean[] b2 = {false,true};

    public static void main(String[] args) throws Exception{
        ArrayList<EventPattern> patterns = new ArrayList<EventPattern>();
        ArrayList<HashMap<String,Boolean>> condMaps = new ArrayList();

        File f = new File("../Output/support40length1/cp_net_input.csv");
        Scanner scanner = new Scanner(f);
        while(scanner.hasNextLine()){
            String nextLine = scanner.nextLine().replace("\n","");
            String[] lineList = nextLine.split(",");
            String name = lineList[0];
            boolean baseMal = lineList[1].equals("malicious") ? true : false;
            HashMap<String,Boolean> condMap = new HashMap<>();
            for(int i=2;i<lineList.length;i+=2){
                String condName = lineList[i];
                boolean condMal = lineList[i+1].equals("malicious") ? true : false;
                condMap.put(condName,condMal);
            }
            patterns.add(new EventPattern(name,baseMal));
            condMaps.add(condMap);
        }

        CPTable<Boolean>[] initial_tables = new CPTable[patterns.size()];
        for(int i=0;i<patterns.size();i++){
            if(patterns.get(i).getBaseMal()){
                initial_tables[i] = new CPTable<Boolean>(new DomainOrdering<Boolean>(b1));
            }
            else{
                initial_tables[i] = new CPTable<Boolean>(new DomainOrdering<Boolean>(b2));
            }
        }
        EventPattern[] pattern_list = new EventPattern[patterns.size()];
        pattern_list = patterns.toArray(pattern_list);

        CPNet<Boolean> net = new CPNet<Boolean>(pattern_list, initial_tables);

        for(int i=0;i<pattern_list.length;i++){
            //System.out.println(pattern_list[i] + " is done (" + Integer.toString(pattern_list.length-i) + " remaining.)");
            boolean[] malList = new boolean[condMaps.get(i).keySet().size()];
            EventPattern[] parents = new EventPattern[condMaps.get(i).keySet().size()];
            int counter = 0;
            for (String condName : condMaps.get(i).keySet()) {
                for (EventPattern p : pattern_list) {
                    if (p.getName().equals(condName)) {
                        parents[counter] = p;
                        malList[counter] = condMaps.get(i).get(condName);
                        counter += 1;
                        break;
                    }
                }
            }

            if(parents.length == 0){
                continue;
            }
            boolean[] flagList = new boolean[parents.length];
            int[] countList = new int[parents.length];
            for (int j = 0; j < flagList.length; j++) {
                flagList[j] = false;
                countList[j] = 0;
            }
            DomainOrdering<Boolean>[] orderList = new DomainOrdering[(int) Math.pow(2, parents.length)];
            for (int j = 0; j < Math.pow(2, parents.length); j++) {
                for (int a = 0; a < parents.length; a++) {
                    if (countList[a] == Math.pow(2, a)) {
                        flagList[a] = true;
                        countList[a] -= 1;
                    }
                    else if(countList[a] == 0){
                        flagList[a] = false;
                        countList[a] += 1;
                    }
                    else if(flagList[a]) {
                        countList[a] -= 1;
                    }
                    else if(!flagList[a]){
                        countList[a] += 1;
                    }
                }

                int mal_count = 0;
                for (int a = 0; a < parents.length; a++) {
                    if (flagList[a]) {
                        mal_count += malList[a] ? 1 : -1;
                    } else {
                        mal_count += malList[a] ? -1 : 1;
                    }
                }

                if(mal_count > 0){
                    orderList[j] = new DomainOrdering<Boolean>(b1);
                }
                else{
                    orderList[j] = new DomainOrdering<Boolean>(b2);
                }
            }
            net.setConnections(pattern_list[i], parents, orderList);
        }

//        EventPattern p = null;
//        for(EventPattern pat : pattern_list)
//            if(pat.getName().equals("('RegOpenKeyW';)")){
//                p = pat;
//                break;
//            }
//        net.printCPTable(p);
        Outcome<Boolean>[] outcomes = net.getAllOutcomes();
        outcomes = net.orderingQuerySort(outcomes);
        System.out.println("----10 Most Benign----");
        for(int i=0;i<10;i++){
            System.out.println(outcomes[i]);
        }
        System.out.println("---------------\n");
        System.out.println("----10 Most Malicious----");
        for(int i=outcomes.length-1;i>= outcomes.length-10;i--){
            System.out.println(outcomes[i]);
        }
    }
}
