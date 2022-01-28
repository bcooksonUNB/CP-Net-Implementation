public class EventPattern extends PreferenceVariable<Boolean>{

    private double baseProbMal;

    EventPattern(String name){
        super(name);
        Boolean[] possibleValues = {false,true};
        this.setValues(possibleValues);
    }

    EventPattern(String name, double baseProbMal){
        super(name);
        Boolean[] possibleValues = {false,true};
        this.setValues(possibleValues);
        this.baseProbMal = baseProbMal;
    }

    public double getBaseProbMal() {
        return baseProbMal;
    }
}
