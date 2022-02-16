import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Driver4 {

    static Boolean[] b1 = {true,false};
    static Boolean[] b2 = {false,true};
    static String outputFolder = "../Output/support50length1test2/";

    public static void main(String[] args) throws Exception{
        ArrayList<EventPattern> patterns = new ArrayList<EventPattern>();
        ArrayList<HashMap<String,Boolean>> condMaps = new ArrayList();

        File f = new File(outputFolder + "cp_net_input.csv");
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

        File trainingFolder = new File(outputFolder + "testingdata/training");
        File[] trainingFiles = trainingFolder.listFiles();
        EventOutcome[] trainingOutcomes = new EventOutcome[trainingFiles.length];
        int trainingCounter = 0;
        for(File fi : trainingFiles){
            String fileName = fi.getName();
            scanner = new Scanner(fi);
            HashMap<String,Boolean> presentMap = new HashMap();
            while(scanner.hasNextLine()){
                String nextLine = scanner.nextLine().replace("\n","");
                String[] lineList = nextLine.split(",");
                String event = lineList[0];
                boolean present = lineList[1].equals("True");
                presentMap.put(event,present);
            }
            ArrayList<Boolean> valueList = new ArrayList<>();
            for(EventPattern pat : pattern_list){
                valueList.add(presentMap.get(pat.getName()));
            }
            trainingOutcomes[trainingCounter] = new EventOutcome(pattern_list,valueList,fileName);
            trainingCounter += 1;
        }

        File folder = new File(outputFolder + "testingdata/testing");
        File[] files = folder.listFiles();
        EventOutcome[] outcomes = new EventOutcome[files.length];
        int counter = 0;
        for(File fi : files){
            String fileName = fi.getName();
            scanner = new Scanner(fi);
            HashMap<String,Boolean> presentMap = new HashMap();
            while(scanner.hasNextLine()){
                String nextLine = scanner.nextLine().replace("\n","");
                String[] lineList = nextLine.split(",");
                String event = lineList[0];
                boolean present = lineList[1].equals("True");
                presentMap.put(event,present);
            }
            ArrayList<Boolean> valueList = new ArrayList<>();
            for(EventPattern pat : pattern_list){
                valueList.add(presentMap.get(pat.getName()));
            }
            outcomes[counter] = new EventOutcome(pattern_list,valueList,fileName);
            counter += 1;
        }


        HashMap<Outcome<Boolean>,Integer[]> mal_compare = new HashMap<>();
        HashMap<Outcome<Boolean>,Integer[]> ben_compare = new HashMap<>();
        int count = 0;
        for(EventOutcome o : outcomes){
            Integer[] mal_list = {0,0,0,0};
            Integer[] ben_list = {0,0,0,0};
            System.out.println((outcomes.length-count) + " Remaining");
            int tCount = 0;
            for(EventOutcome to : trainingOutcomes){
                //System.out.println(o);
                //System.out.println(to);
                //System.out.println();
                System.out.println((outcomes.length-count) + " Remaining - " + (trainingOutcomes.length-tCount));
                if(to.getEventName().contains("mal")){
                    boolean test = net.dominanceQuery(o,to);
                    if(test){
                        mal_list[0] += 1;
                    }
                    else{
                        test = net.dominanceQuery(to,o);
                        if(test){
                            mal_list[1] += 1;
                        }
                        else {
                            mal_list[2] += 1;
                        }
                    }
                    if(o.equals(to)){
                        mal_list[3] += 1;
                    }
                }
                else{
                    boolean test = net.dominanceQuery(o,to);
                    if(test){
                        ben_list[0] += 1;
                    }
                    else {
                        test = net.dominanceQuery(to,o);
                        if(test){
                            ben_list[1] += 1;
                        }
                        else{
                            ben_list[2] += 1;
                        }
                    }
                    if(o.equals(to)){
                        ben_list[3] += 1;
                    }
                }
                tCount += 1;
            }
            mal_compare.put(o,mal_list);
            ben_compare.put(o,ben_list);
            count += 1;
        }


        ArrayList<EventOutcome> mal_predictions = new ArrayList<>();
        ArrayList<EventOutcome> ben_predictions = new ArrayList<>();
        ArrayList<EventOutcome> mal_confident = new ArrayList<>();
        ArrayList<EventOutcome> ben_confident = new ArrayList<>();

        ArrayList<EventOutcome> ordered_list = new ArrayList<>();
        HashMap<EventOutcome, Double> scoreMap = new HashMap<>();
        for(EventOutcome o : outcomes){
            double score = 0;
            System.out.println(o.getEventName());
            System.out.println("Mal Beat: " + mal_compare.get(o)[0] + " (" + ((float)mal_compare.get(o)[0]/256) + "%)") ;
            score += ((float)mal_compare.get(o)[0]/256);
            System.out.println("Mal Lost: " + mal_compare.get(o)[1] + " (" + ((float)mal_compare.get(o)[1]/256) + "%)");
            score -= ((float)mal_compare.get(o)[1]/256);
            System.out.println("Mal Indifferent: " + mal_compare.get(o)[2] + " (" + ((float)mal_compare.get(o)[2]/256) + "%)");
            System.out.println("Mal Equals Exact: " + mal_compare.get(o)[3] + " (" + ((float)mal_compare.get(o)[3]/256) + "%)");
            score += ((float)mal_compare.get(o)[3]/256);
            System.out.println("Ben Beat: " + ben_compare.get(o)[0] + " (" + ((float)ben_compare.get(o)[0]/55) + "%)");
            score += ((float)ben_compare.get(o)[0]/55);
            System.out.println("Ben Lost: " + ben_compare.get(o)[1] + " (" + ((float)ben_compare.get(o)[1]/55) + "%)");
            score -= ((float)ben_compare.get(o)[1]/55);
            System.out.println("Ben Indifferent: " + ben_compare.get(o)[2] + " (" + ((float)ben_compare.get(o)[2]/55) + "%)");
            System.out.println("Ben Equals Exact: " + ben_compare.get(o)[3] + " (" + ((float)ben_compare.get(o)[3]/55) + "%)");
            score -= ((float)ben_compare.get(o)[3]/55);
            System.out.println();
            if(score > 0 && score < 1.2){
                mal_predictions.add(o);
            }
            else if (score <= 0 && score > -1.2){
                ben_predictions.add(o);
            }
            else if(score >= 1.2){
                mal_confident.add(o);
            }
            else if(score <= -1.2){
                ben_confident.add(o);
            }
            scoreMap.put(o,score);
        }

        ArrayList<EventOutcome> tempList = new ArrayList<>();
        for(EventOutcome o : outcomes){
            tempList.add(o);
        }

        while(tempList.size() > 0){
            double minVal = 100;
            EventOutcome minOutcome = null;
            for(EventOutcome o : tempList){
                if(scoreMap.get(o) < minVal){
                    minVal = scoreMap.get(o);
                    minOutcome = o;
                }
            }
            ordered_list.add(minOutcome);
            tempList.remove(minOutcome);
        }

        System.out.println("Very Confident Malicious");
        for(EventOutcome o : mal_confident){
            System.out.println(o.getEventName());
        }
        System.out.println();
        System.out.println("Predicted Malicious");
        for(EventOutcome o : mal_predictions){
            System.out.println(o.getEventName());
        }
        System.out.println();
        System.out.println("Very Confident Benign");
        for(EventOutcome o : ben_confident){
            System.out.println(o.getEventName());
        }
        System.out.println();
        System.out.println("Predicted Beneign");
        for(EventOutcome o : ben_predictions){
            System.out.println(o.getEventName());
        }
        System.out.println();

        System.out.println("Ordered List");
        for(EventOutcome o : ordered_list){
            System.out.println(o.getEventName() + " " + scoreMap.get(o));
        }

//        System.out.println("Starting");
//        Outcome<Boolean>[] allOutcomes = net.getAllOutcomes();
//        int total_worse = 0;
//        int total_better = 0;
//        int total_indifferent = 0;
//        for(int i=0;i<allOutcomes.length;i++){
//            Outcome<Boolean> o = allOutcomes[i];
//            //System.out.println(o);
//            int worseCount = 0;
//            int betterCount = 0;
//            int inCount = 0;
//            for(int j=0;j<allOutcomes.length;j++){
//                Outcome<Boolean> o2 = allOutcomes[j];
//                boolean test = net.dominanceQuery(o,o2);
//                if(test){
//                    //System.out.println(o2 + ": Better");
//                    betterCount += 1;
//                }
//                else{
//                    test = net.dominanceQuery(o2,o);
//                    if(test){
//                        //System.out.println(o2 + ": Worse");
//                        worseCount += 1;
//                    }
//                    else{
//                        //System.out.println(o2 + ": Indifferent");
//                        inCount += 1;
//                    }
//                }
//            }
//            System.out.println(allOutcomes.length-i + " remaining");
//            total_worse += worseCount;
//            total_better += betterCount;
//            total_indifferent += inCount;
//            //System.out.println(worseCount + " " + betterCount + " " + inCount);
//            //System.out.println();
//        }
//
//        System.out.println((double)total_worse/allOutcomes.length + " " + (double)total_better/allOutcomes.length + " " + (double)total_indifferent/allOutcomes.length);

//        Boolean[] o1List = {true,true,true,true};
//        ArrayList<Boolean> o1L = new ArrayList<>(Arrays.asList(o1List));
//        Outcome<Boolean> o1 = new Outcome<Boolean>(pattern_list,o1L);
//
//        Boolean[] o2List = {false,false,false,false};
//        ArrayList<Boolean> o2L = new ArrayList<>(Arrays.asList(o2List));
//        Outcome<Boolean> o2 = new Outcome<Boolean>(pattern_list,o2L);
//
//        System.out.println(net.dominanceQuery(o2,o1));
        //System.out.println(o1.equals(o2));
        //System.out.println(net.getOptimalSolution());
        //outcomes = (EventOutcome[])net.dominanceSort(outcomes);
        //for(EventOutcome eo : outcomes){
        //    System.out.println(eo.getEventName());
        //System.out.println(eo);
        //}
//        EventPattern p = null;
//        for(EventPattern pat : pattern_list)
//            if(pat.getName().equals("('RegOpenKeyW';)")){
//                p = pat;
//                break;
//            }
//        net.printCPTable(p);

//        System.out.println(net.getOptimalSolution());
//        Outcome<Boolean>[] outcomes = net.getAllOutcomes();
//        outcomes = net.orderingQuerySort(outcomes);
//        System.out.println("----10 Most Benign----");
//        for(int i=0;i<10;i++){
//            System.out.println(outcomes[i]);
//        }
//        System.out.println("---------------\n");
//        System.out.println("----10 Most Malicious----");
//        for(int i=outcomes.length-1;i>= outcomes.length-10;i--){
//            System.out.println(outcomes[i]);
//        }
    }
}
