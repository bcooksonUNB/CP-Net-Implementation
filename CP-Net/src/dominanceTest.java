import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class dominanceTest {
    static Boolean[] b1 = {true,false};
    static Boolean[] b2 = {false,true};
    public static void main(String args[]) throws Exception{
        ArrayList<EventPattern> patterns = new ArrayList<EventPattern>();
        ArrayList<HashMap<String, Boolean>> condMaps = new ArrayList();
        String outputFolder = "../Output/CycleTestChiSquare/s60c60l1_partition_0/";

        int connection_count = 0;
        int parentless = 0;
        int childless = 0;

        File f = new File(outputFolder + "cp_net_input.csv");
        Scanner scanner = new Scanner(f);
        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine().replace("\n", "");
            String[] lineList = nextLine.split(",");
            String name = lineList[0];
            boolean baseMal = lineList[1].equals("malicious") ? true : false;
            HashMap<String, Boolean> condMap = new HashMap<>();
            for (int i = 2; i < lineList.length; i += 2) {
                String condName = lineList[i];
                boolean condMal = lineList[i + 1].equals("malicious") ? true : false;
                condMap.put(condName, condMal);
                connection_count += 1;
            }
            patterns.add(new EventPattern(name, baseMal));
            condMaps.add(condMap);
        }

        CountingCPTable[] initial_tables = new CountingCPTable[patterns.size()];
        for (int i = 0; i < patterns.size(); i++) {

            if (patterns.get(i).getBaseMal()) {
                initial_tables[i] = new CountingCPTable(new DomainOrdering<Boolean>(b1));
            } else {
                initial_tables[i] = new CountingCPTable(new DomainOrdering<Boolean>(b2));
            }
        }
        EventPattern[] pattern_list = new EventPattern[patterns.size()];
        pattern_list = patterns.toArray(pattern_list);

        CPNet<Boolean> net = new EventCPNet(pattern_list, initial_tables);

        for (int i = 0; i < pattern_list.length; i++) {
            //System.out.println(pattern_list.length-i);
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
            if (parents.length == 0) {
                continue;
            }
            DomainOrdering<Boolean>[] domainMalList = new DomainOrdering[malList.length];
            for (int j = 0; j < malList.length; j++) {
                if (malList[j]) {
                    domainMalList[j] = new DomainOrdering<Boolean>(b1);
                } else {
                    domainMalList[j] = new DomainOrdering<Boolean>(b2);
                }
            }
            net.setConnections(pattern_list[i], parents, domainMalList);
        }

        System.out.println(net);
        ArrayList<Boolean> o1Vals = new ArrayList<Boolean>();
        o1Vals.add(true);
        o1Vals.add(true);
        o1Vals.add(true);
        o1Vals.add(true);
        o1Vals.add(true);
        o1Vals.add(true);
        o1Vals.add(true);
        o1Vals.add(true);
        EventOutcome o1 = new EventOutcome(pattern_list, o1Vals, "Event1");

        ArrayList<Boolean> o2Vals = new ArrayList<>();
        o2Vals.add(false);
        o2Vals.add(false);
        o2Vals.add(false);
        o2Vals.add(false);
        o2Vals.add(true);
        o2Vals.add(true);
        o2Vals.add(false);
        o2Vals.add(false);
        EventOutcome o2 = new EventOutcome(pattern_list, o2Vals, "Event2");

        ArrayList<Boolean> testVals = new ArrayList<>();
        testVals.add(true);
        testVals.add(true);
        testVals.add(true);
        testVals.add(true);
        testVals.add(true);
        testVals.add(true);
        testVals.add(true);
        testVals.add(false);
        EventOutcome test = new EventOutcome(pattern_list, testVals, "TestEvent");

        boolean dom = net.dominanceQuery(o1,o2);
        ArrayList<Outcome<Boolean>> outcomes =  net.getImprovingFlipsForOutcome(o2);
        for(Outcome<Boolean> o : outcomes){
            System.out.println(o.equals(test));
            System.out.print(o);
            System.out.println();
        }
    }
}