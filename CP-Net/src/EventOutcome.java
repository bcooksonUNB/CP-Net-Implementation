import java.util.ArrayList;

public class EventOutcome extends Outcome<Boolean>{

    private String eventName;

    public EventOutcome(EventPattern[] variables, ArrayList<Boolean> values, String name){
        super(variables,values);
        eventName = name;
    }

    public String getEventName(){ return eventName; }
}
