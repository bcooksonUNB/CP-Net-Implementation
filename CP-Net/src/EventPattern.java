public class EventPattern extends PreferenceVariable<Boolean>{

    private boolean baseMal;

    EventPattern(String name){
        super(name);
        Boolean[] possibleValues = {false,true};
        this.setValues(possibleValues);
    }

    EventPattern(String name, boolean baseMal){
        super(name);
        Boolean[] possibleValues = {false,true};
        this.setValues(possibleValues);
        this.baseMal = baseMal;
    }

    public boolean getBaseMal() {
        return baseMal;
    }
}
