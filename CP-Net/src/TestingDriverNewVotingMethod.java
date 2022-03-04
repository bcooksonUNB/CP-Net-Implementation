import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class TestingDriverNewVotingMethod {

    static Boolean[] b1 = {true,false};
    static Boolean[] b2 = {false,true};
    static double[] support_list = {0.7,0.6,0.5,0.4,0.3,0.2,0.1};
    static double[] conditional_support_list = {0.8,0.7,0.6,0.5,0.4,0.3,0.2,0.1};
    static int max_length = 1;

    public static void main(String[] args) throws Exception{
        for(double support : support_list) {
            for (double conditional_support : conditional_support_list) {
                String mainOutputFolder = "../Output/s" + Integer.toString((int) (support * 100)) + "c" + Integer.toString((int) (conditional_support * 100)) + "l" + max_length + "_cycle4_reversemethod_v2";
                FileWriter myWriter = new FileWriter(mainOutputFolder + "_output.txt");
                myWriter.write("Unconditional Support, Conditional Support, Max Length, Node Count, Average Connection, Parentless, Childless, Diff Count, Diff Score, Accuracy, Percision, Recall, Average Indifference\n");
                for (int folderNum = 0; folderNum < 5; folderNum++) {
                    FileWriter logWriter = new FileWriter(mainOutputFolder + "_partition_" + folderNum + "_log.txt");
                    String outputFolder = mainOutputFolder + "_partition_" + folderNum + "/";
                    ArrayList<EventPattern> patterns = new ArrayList<EventPattern>();
                    ArrayList<HashMap<String, Boolean>> condMaps = new ArrayList();

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

                    EventCPNet net = new EventCPNet(pattern_list, initial_tables);

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
                        DomainOrdering<Boolean> tempOrdering;
                        if(pattern_list[i].getBaseMal()){
                            tempOrdering = new DomainOrdering<>(b1);
                        }
                        else{
                            tempOrdering = new DomainOrdering<>(b2);
                        }
                        net.setCountingConnections(pattern_list[i], parents, domainMalList, tempOrdering);
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

                    HashMap<Outcome<Boolean>, Integer[]> mal_compare = new HashMap<>();
                    HashMap<Outcome<Boolean>, Integer[]> ben_compare = new HashMap<>();
                    int count = 0;
                    for (EventOutcome o : outcomes) {
                        Integer[] mal_list = {0, 0, 0, 0};
                        Integer[] ben_list = {0, 0, 0, 0};
                        //System.out.println((outcomes.length-count) + " Remaining");
                        int tCount = 0;
                        for (EventOutcome to : trainingOutcomes) {
                            if (to.getEventName().contains("mal")) {
                                boolean[] test = net.getOrderingQuery(o, to);
                                if (test[1] && !test[0]) {
                                    mal_list[0] += 1;
                                } else {
                                    if (test[0] && !test[1]) {
                                        mal_list[1] += 1;
                                    } else {
                                        mal_list[2] += 1;
                                    }
                                }
                                if (o.equals(to)) {
                                    mal_list[3] += 1;
                                }
                            } else {
                                boolean[] test = net.getOrderingQuery(o, to);
                                if (test[1] && !test[0]) {
                                    ben_list[0] += 1;
                                } else {
                                    if (test[0] && !test[1]) {
                                        ben_list[1] += 1;
                                    } else {
                                        ben_list[2] += 1;
                                    }
                                }
                                if (o.equals(to)) {
                                    ben_list[3] += 1;
                                }
                            }
                            tCount += 1;
                        }
                        mal_compare.put(o, mal_list);
                        ben_compare.put(o, ben_list);
                        count += 1;
                    }


                    ArrayList<EventOutcome> mal_predictions = new ArrayList<>();
                    ArrayList<EventOutcome> ben_predictions = new ArrayList<>();
                    ArrayList<EventOutcome> mal_confident = new ArrayList<>();
                    ArrayList<EventOutcome> ben_confident = new ArrayList<>();
                    double total_diff = 0;
                    int diff_count = 0;
                    int tp = 0;
                    int mal_count = 0;
                    int[] total_mals = new int[4];
                    int[] total_bens = new int[4];
                    ArrayList<EventOutcome> ordered_list = new ArrayList<>();
                    HashMap<EventOutcome, Double> scoreMap = new HashMap<>();
                    for (EventOutcome o : outcomes) {
                        total_mals[0] += mal_compare.get(o)[0];
                        total_mals[1] += mal_compare.get(o)[1];
                        total_mals[2] += mal_compare.get(o)[2];
                        total_mals[3] += mal_compare.get(o)[3];

                        total_bens[0] += ben_compare.get(o)[0];
                        total_bens[1] += ben_compare.get(o)[1];
                        total_bens[2] += ben_compare.get(o)[2];
                        total_bens[3] += ben_compare.get(o)[3];

                        double score = 0;
                        logWriter.write(o.getEventName());
                        logWriter.write("\n");
                        logWriter.write("Mal Beat: " + mal_compare.get(o)[0] + " (" + ((float) mal_compare.get(o)[0] / 256) + ")");
                        logWriter.write("\n");
                        score += 1*((float) mal_compare.get(o)[0] / 256);
                        logWriter.write("Mal Lost: " + mal_compare.get(o)[1] + " (" + ((float) mal_compare.get(o)[1] / 256) + ")");
                        logWriter.write("\n");
                        score -= ((float) mal_compare.get(o)[1] / 256);
                        logWriter.write("Mal Indifferent: " + mal_compare.get(o)[2] + " (" + ((float) mal_compare.get(o)[2] / 256) + ")");
                        logWriter.write("\n");
                        logWriter.write("Mal Equals Exact: " + mal_compare.get(o)[3] + " (" + ((float) mal_compare.get(o)[3] / 256) + ")");
                        logWriter.write("\n");
                        score += 1*((float) mal_compare.get(o)[3] / 256);
                        logWriter.write("Ben Beat: " + ben_compare.get(o)[0] + " (" + ((float) ben_compare.get(o)[0] / 55) + ")");
                        logWriter.write("\n");
                        score += ((float) ben_compare.get(o)[0] / 55);
                        logWriter.write("Ben Lost: " + ben_compare.get(o)[1] + " (" + ((float) ben_compare.get(o)[1] / 55) + ")");
                        logWriter.write("\n");
                        score -= 1*((float) ben_compare.get(o)[1] / 55);
                        logWriter.write("Ben Indifferent: " + ben_compare.get(o)[2] + " (" + ((float) ben_compare.get(o)[2] / 55) + ")");
                        logWriter.write("\n");
                        logWriter.write("Ben Equals Exact: " + ben_compare.get(o)[3] + " (" + ((float) ben_compare.get(o)[3] / 55) + ")");
                        logWriter.write("\n");
                        score -= 1*((float) ben_compare.get(o)[3] / 55);
                        logWriter.write("\n");
                        if (score > 0) {
                            mal_predictions.add(o);
                            if (o.getEventName().contains("ben")) {
                                total_diff += Math.abs(score);
                                diff_count += 1;
                            } else {
                                tp += 1;
                            }
                        } else if (score <= 0) {
                            ben_predictions.add(o);
                            if (o.getEventName().contains("mal")) {
                                total_diff += Math.abs(score);
                                diff_count += 1;
                            }
                        }
                        if (o.getEventName().contains("mal")) {
                            mal_count += 1;
                        }
                        scoreMap.put(o, score);
                    }

                    ArrayList<EventOutcome> tempList = new ArrayList<>();
                    for (EventOutcome o : outcomes) {
                        tempList.add(o);
                    }

                    while (tempList.size() > 0) {
                        double minVal = 100;
                        EventOutcome minOutcome = null;
                        for (EventOutcome o : tempList) {
                            if (scoreMap.get(o) < minVal) {
                                minVal = scoreMap.get(o);
                                minOutcome = o;
                            }
                        }
                        ordered_list.add(minOutcome);
                        tempList.remove(minOutcome);
                    }

                    logWriter.write("Ordered List");
                    logWriter.write("\n");
                    for (EventOutcome o : ordered_list) {
                        logWriter.write(o.getEventName() + " " + scoreMap.get(o));
                        logWriter.write("\n");
                    }
                    double average_indifference = 0;
                    average_indifference += (((float) total_mals[2] / outcomes.length) / (float) (total_mals[0] / outcomes.length + total_mals[1] / outcomes.length + total_mals[2] / outcomes.length)) * ((float) (total_mals[0] / outcomes.length + total_mals[1] / outcomes.length + total_mals[2] / outcomes.length)) / (trainingOutcomes.length);
                    average_indifference += (((float) total_bens[2] / outcomes.length) / (float) (total_bens[0] / outcomes.length + total_bens[1] / outcomes.length + total_bens[2] / outcomes.length)) * ((float) (total_bens[0] / outcomes.length + total_bens[1] / outcomes.length + total_bens[2] / outcomes.length)) / trainingOutcomes.length;

                    for (int j = 0; j < net.nodes.length; j++) {
                        if (net.getChildren(net.nodes[j]).length == 0) {
                            childless += 1;
                        }
                    }
                    for (int j = 0; j < net.nodes.length; j++) {
                        if (net.getParents(net.nodes[j]).length == 0) {
                            parentless += 1;
                        }
                    }

                    logWriter.write("CP-Net Length: " + pattern_list.length);
                    logWriter.write("\n");
                    logWriter.write("Connections Count: " + (float) connection_count / pattern_list.length);
                    logWriter.write("\n");
                    logWriter.write("Childless: " + parentless);
                    logWriter.write("\n");
                    logWriter.write("Parentless: " + childless);
                    logWriter.write("\n");
                    logWriter.write("Diff Count: " + diff_count);
                    logWriter.write("\n");
                    logWriter.write("Diff Score: " + total_diff);
                    logWriter.write("\n");
                    logWriter.write("Accruacy: " + (float) (outcomes.length - diff_count) / outcomes.length);
                    logWriter.write("\n");
                    logWriter.write("Percision: " + (float) tp / mal_predictions.size());
                    logWriter.write("\n");
                    logWriter.write("Recall: " + (float) tp / mal_count);
                    logWriter.write("\n");
                    logWriter.write("Indifference: " + average_indifference);
                    logWriter.write("\n");

                    String outputString = "";
                    outputString += Double.toString(support) + ",";
                    outputString += Double.toString(conditional_support) + ",";
                    outputString += Integer.toString(max_length) + ",";
                    outputString += Integer.toString(pattern_list.length) + ",";
                    outputString += Double.toString((float) connection_count / pattern_list.length) + ",";
                    outputString += Integer.toString(parentless) + ",";
                    outputString += Integer.toString(childless) + ",";
                    outputString += Integer.toString(diff_count) + ",";
                    outputString += Double.toString(total_diff) + ",";
                    outputString += Double.toString((float) (outcomes.length - diff_count) / outcomes.length) + ",";
                    outputString += Double.toString((float) tp / mal_predictions.size()) + ",";
                    outputString += Double.toString((float) tp / mal_count) + ",";
                    outputString += Double.toString(average_indifference) + "\n";
                    myWriter.write(outputString);
                    logWriter.close();
                    System.out.println("Done " + folderNum + " For " + mainOutputFolder);
                }
                myWriter.close();
            }
        }
    }
}
