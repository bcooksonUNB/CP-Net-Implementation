//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Scanner;
//
//public class Driver2 {
//
//    static Boolean[] b1 = {true,false};
//    static Boolean[] b2 = {false,true};
//
//    public static void main(String[] args) throws Exception{
//        ArrayList<EventPattern> patterns = new ArrayList<EventPattern>();
//        ArrayList<HashMap<String,Double>> condMaps = new ArrayList<HashMap<String,Double>>();
//
//        File f = new File("../Output/cond_output6.csv");
//        Scanner scanner = new Scanner(f);
//        while(scanner.hasNextLine()){
//            String nextLine = scanner.nextLine().replace("\n","");
//            String[] lineList = nextLine.split(",");
//            String name = lineList[0];
//            double baseProb = Double.parseDouble(lineList[1]);
//            HashMap<String,Double> condMap = new HashMap<>();
//            for(int i=2;i<lineList.length;i+=2){
//                String condName = lineList[i];
//                Double condProb = Double.parseDouble(lineList[i+1]);
//                condMap.put(condName,condProb);
//            }
//            patterns.add(new EventPattern(name));
//            condMaps.add(condMap);
//        }
//
//        CPTable<Boolean>[] initial_tables = new CPTable[patterns.size()];
//        for(int i=0;i<patterns.size();i++){
//            if(patterns.get(i).getBaseProbMal() >= 0.8247){
//                initial_tables[i] = new CPTable<Boolean>(new DomainOrdering<Boolean>(b1));
//            }
//            else{
//                initial_tables[i] = new CPTable<Boolean>(new DomainOrdering<Boolean>(b2));
//            }
//        }
//        EventPattern[] pattern_list = new EventPattern[patterns.size()];
//        pattern_list = patterns.toArray(pattern_list);
//
//        CPNet<Boolean> net = new CPNet<Boolean>(pattern_list, initial_tables);
//
//        for(int i=0;i<pattern_list.length;i++){
//            System.out.println(pattern_list[i] + " is done (" + Integer.toString(pattern_list.length-i) + " remaining.)");
//            if(condMaps.size() != 0){
//                double[] probList = new double[condMaps.get(i).keySet().size()];
//                EventPattern[] parents = new EventPattern[condMaps.get(i).keySet().size()];
//                int counter = 0;
//                for(String condName : condMaps.get(i).keySet()){
//                    for(EventPattern p : pattern_list){
//                        if(p.getName().equals(condName)){
//                            parents[counter] = p;
//                            counter += 1;
//                            break;
//                        }
//                    }
//                }
//
//                if(parents.length <= 100) {
//                    boolean[] flagList = new boolean[parents.length];
//                    int[] countList = new int[parents.length];
//                    for (int j = 0; j < flagList.length; j++) {
//                        flagList[j] = false;
//                        countList[j] = 0;
//                    }
//                    DomainOrdering<Boolean>[] orderList = new DomainOrdering[(int) Math.pow(2, parents.length)];
//                    for (int j = 0; j < Math.pow(2, parents.length); j++) {
//                        for (int a = 0; a < parents.length; a++) {
//                            if (countList[a] == Math.pow(2, a)) {
//                                flagList[a] = true;
//                                countList[a] = 0;
//                            } else {
//                                flagList[a] = false;
//                                countList[a] += 1;
//                            }
//                        }
//                        double[] totalCond = new double[parents.length];
//                        for (int a = 0; a < parents.length; a++) {
//                            if (flagList[a]) {
//                                //stotalCond[a] = pattern_list[a].getBaseProbMal();
//                            } else {
//                                //totalCond[a] = 1 - pattern_list[a].getBaseProbMal();
//                            }
//                        }
//                        double ave = Util.average(totalCond);
//                        if (ave >= 0.5) {
//                            orderList[j] = new DomainOrdering<Boolean>(b1);
//                        } else {
//                            orderList[j] = new DomainOrdering<Boolean>(b2);
//                        }
//                    }
//                    if(parents.length > 0) {
//                        net.setConnections(pattern_list[i], parents, orderList);
//                    }
//                }
//                else{
//                    //for(PreferenceVariable p : parents) System.out.println(p);
//                    //net.setConnections(pattern_list[i], parents, probList, true);
//                }
//            }
//        }
//
//        //net.printReverseTreeForm();
//        //System.out.println(net.getOptimalSolution());
////        for(PreferenceVariable p : patterns){
////            System.out.println(p);
////            net.printCPTable(p);
////        }
//        //net.printTreeForm();
////        Outcome<Boolean>[] outcomes = net.getAllOutcomes();
//
//        ArrayList<Boolean> values1 = new ArrayList<>();
//        ArrayList<Boolean> values2 = new ArrayList<>();
//        for(int i=0;i<pattern_list.length;i++){
//            values1.add(false);
//            values2.add(true);
//        }
//        Outcome<Boolean> o1 = new Outcome<>(pattern_list,values1);
//        Outcome<Boolean> o2 = new Outcome<>(pattern_list,values2);
//        net.dominanceQuery(o1,o2);
////        outcomes = net.orderingQuerySort(outcomes);
////        for(Outcome o : outcomes) System.out.println(o);
//    }
//
//}
