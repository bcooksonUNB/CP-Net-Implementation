public class CPDriver {

    public static void main(String[] args){
        Boolean[] b1 = {true,false};
        Boolean[] b2 = {false,true};

        String[] jacketVals = {"Black","White"};
        String[] pantsVals = {"Black","White"};
        String[] shirtVals = {"Red","White"};

        String[] shirtOrder2 = {"White","Red"};

        PreferenceVariable<String> jacket = new PreferenceVariable<String>("Jacket",jacketVals);
        PreferenceVariable<String> pants = new PreferenceVariable<String>("Pants",pantsVals);
        PreferenceVariable<String> shirt = new PreferenceVariable<String>("Shirt",shirtVals);
        PreferenceVariable<String>[] input_variables = new PreferenceVariable[3];
        input_variables[0] = jacket;
        input_variables[1] = pants;
        input_variables[2] = shirt;

        CPTable<String>[] input_tables = new CPTable[3];
        input_tables[0] = new CPTable<String>(new DomainOrdering<String>(jacketVals));
        input_tables[1] = new CPTable<String>(new DomainOrdering<String>(pantsVals));
        input_tables[2] = new CPTable<String>(new DomainOrdering<String>(shirtVals));

        CPNet<String> net = new CPNet<String>(input_variables,input_tables);

        PreferenceVariable<String>[] shirtParents = new PreferenceVariable[2];
        shirtParents[0] = jacket;
        shirtParents[1] = pants;
        DomainOrdering<String>[] shirtTable = new DomainOrdering[4];
        shirtTable[0] = new DomainOrdering<>(shirtVals);
        shirtTable[1] = new DomainOrdering<>(shirtOrder2);
        shirtTable[2] = new DomainOrdering<>(shirtOrder2);
        shirtTable[3] = new DomainOrdering<>(shirtVals);
        net.setConnections(shirt,shirtParents,shirtTable);

        Outcome<String> opt = net.getOptimalSolution();

        String[] o1Vals = {"Black","White","Red"};
        Outcome<String> o1 = net.createOutcome(o1Vals);
        String[] o2Vals = {"White","Black","White"};
        Outcome<String> o2 = net.createOutcome(o2Vals);

        boolean[] out = net.getOrderingQuery(o1,o2);
        System.out.println(out[0] + " " + out[1]);

        Outcome<String>[] outcomes = net.getAllOutcomes();
        outcomes = net.orderingQuerySort(outcomes);
        for(Outcome o : outcomes) System.out.println(o);
    }

}
