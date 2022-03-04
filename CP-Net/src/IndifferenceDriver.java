import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class IndifferenceDriver {
    static Boolean[] b1 = {true,false};
    static Boolean[] b2 = {false,true};
    static double support = 0.05;
    static double[] conditional_support_list = {0.8,0.75,0.7,0.65,0.6,0.55,0.5,0.45,0.4,0.35,0.3,0.25,0.2,0.15,0.1,0.05};
    static int max_length = 1;

    public static void main(String[] args) throws Exception{
        for(double conditional_support : conditional_support_list) {
            String mainOutputFolder = "../Output/s" + Integer.toString((int) (support * 100)) + "c" + Integer.toString((int) (conditional_support * 100)) + "l" + max_length;
            double finalWorse = 0;
            double finalBetter = 0;
            double finalIndifferent = 0;
            int parentless = 0;
            int size = 0;
            System.out.println("Starting " + mainOutputFolder);
            for (int folderNum = 0; folderNum < 5; folderNum++) {
                FileWriter logWriter = new FileWriter(mainOutputFolder + "_partition_" + folderNum + "_log.txt");
                String outputFolder = mainOutputFolder + "_partition_" + folderNum + "/";
                ArrayList<EventPattern> patterns = new ArrayList<EventPattern>();
                ArrayList<HashMap<String, Boolean>> condMaps = new ArrayList();

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

                File trainingFolder = new File(outputFolder + "testingdata/training");
                File[] trainingFiles = trainingFolder.listFiles();
                EventOutcome[] trainingOutcomes = new EventOutcome[trainingFiles.length];
                int trainingCounter = 0;
                for (File fi : trainingFiles) {
                    String fileName = fi.getName();
                    scanner = new Scanner(fi);
                    HashMap<String, Boolean> presentMap = new HashMap();
                    while (scanner.hasNextLine()) {
                        String nextLine = scanner.nextLine().replace("\n", "");
                        String[] lineList = nextLine.split(",");
                        String event = lineList[0];
                        boolean present = lineList[1].equals("True");
                        presentMap.put(event, present);
                    }
                    ArrayList<Boolean> valueList = new ArrayList<>();
                    for (EventPattern pat : pattern_list) {
                        valueList.add(presentMap.get(pat.getName()));
                    }
                    trainingOutcomes[trainingCounter] = new EventOutcome(pattern_list, valueList, fileName);
                    trainingCounter += 1;
                }

                File folder = new File(outputFolder + "testingdata/testing");
                File[] files = folder.listFiles();
                EventOutcome[] outcomes = new EventOutcome[files.length];
                int counter = 0;
                for (File fi : files) {
                    String fileName = fi.getName();
                    scanner = new Scanner(fi);
                    HashMap<String, Boolean> presentMap = new HashMap();
                    while (scanner.hasNextLine()) {
                        String nextLine = scanner.nextLine().replace("\n", "");
                        String[] lineList = nextLine.split(",");
                        String event = lineList[0];
                        boolean present = lineList[1].equals("True");
                        presentMap.put(event, present);
                    }
                    ArrayList<Boolean> valueList = new ArrayList<>();
                    for (EventPattern pat : pattern_list) {
                        valueList.add(presentMap.get(pat.getName()));
                    }
                    outcomes[counter] = new EventOutcome(pattern_list, valueList, fileName);
                    counter += 1;
                }

                Outcome<Boolean>[] allOutcomes = net.getRandomOutcomes(100);
                int total_worse = 0;
                int total_better = 0;
                int total_indifferent = 0;
                for (int i = 0; i < allOutcomes.length; i++) {
                    Outcome<Boolean> o = allOutcomes[i];
                    int worseCount = 0;
                    int betterCount = 0;
                    int inCount = 0;
                    for (int j = 0; j < allOutcomes.length; j++) {
                        Outcome<Boolean> o2 = allOutcomes[j];
                        boolean test[] = net.getOrderingQuery(o, o2);
                        if (test[1] && !test[0]) {
                            betterCount += 1;
                        } else {
                            if (test[0] && !test[1]) {
                                worseCount += 1;
                            } else {
                                inCount += 1;
                            }
                        }
                    }
                    //if (i % 50 == 0)
                        //System.out.println(allOutcomes.length - i + " remaining");
                    total_worse += worseCount;
                    total_better += betterCount;
                    total_indifferent += inCount;
                }
                finalWorse += (double) total_worse / allOutcomes.length;
                finalBetter += (double) total_better / allOutcomes.length;
                finalIndifferent += (double) total_indifferent / allOutcomes.length;
                for(int j=0;j<net.nodes.length;j++){
                    if(net.getParents(net.nodes[j]).length == 0){
                        parentless += 1;
                    }
                }
                size += net.nodes.length;
            }

            System.out.println((double)size/5);
            System.out.println(finalWorse/5 + " " + finalBetter/5 + " " + finalIndifferent/5);
            System.out.println(parentless/5 + " " + finalIndifferent/(finalWorse+finalBetter+finalIndifferent));
        }
    }
}
