/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.hype;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author Luca
 */
public class Subcomponent {
    String name;
    String variable;
    String influence;
    int flowNumber;
    ArrayList<Flow> flowList;
    HashMap<String,Integer> eventToFlow;
    int initialFlow;
    boolean processed;

    /**
     * Initializes an empty subcomponent
     * @param name the name of the subcomponent
     * @param influence the influence specific of the subcomponent
     * @param variable the variable associated with the influence
     */
    public Subcomponent(String name, String influence, String variable) {
        this.name = name;
        this.influence = influence;
        this.variable = variable;
        this.flowNumber = 0;
        this.flowList = new ArrayList<Flow>();
        this.eventToFlow = new HashMap<String,Integer>();
        this.initialFlow = -1;
        this.processed = false;
    }

    public String getName() {
        return name;
    }
    
    public Subcomponent copySubComponent(HypeModel model, int index) {
    	Subcomponent subComp = new Subcomponent(this.name+"_"+index, this.influence+"_"+index,
    			this.variable+"_"+index);
    	if(!model.isVariableDefined(subComp.variable)) {
    		model.addVariable(subComp.variable, model.getVariableValue(this.variable).initialValue);
    	}
    	subComp.flowNumber = this.flowNumber;
    	for(Flow flow: this.flowList) {
    		Flow f = flow.copyFlow(model, "_"+index);
    		//f.print();
    		subComp.addFlow(f);
    	}
    	for(String key: eventToFlow.keySet()) {
    		subComp.addEventToFlow(key+"_"+index, eventToFlow.get(key));
   // 		System.out.println("eventToFlow: "+" event: " + key + " flow: " + eventToFlow.get(key));
    	}
    	subComp.initialFlow = this.initialFlow;
    	//model.addSubcomponent(subComp);
    	return subComp;
    }
    
    public ArrayList<Subcomponent> copySubComponent(HypeModel model, int index, int population) {
    	ArrayList<Subcomponent> retList = new ArrayList<Subcomponent>();
    	for(int i=0; i<population; i++) {
    		if(i != index) {
    			String strSuffix = "_"+index + "_" + i;
    			String strName = this.name.replace("$", strSuffix);
    			Subcomponent subComp = new Subcomponent(strName, this.influence+strSuffix,
    	    			this.variable+strSuffix);
    	    	if(!model.isVariableDefined(subComp.variable)) {
    	    		//System.out.println(subComp.variable);
    	    		model.addVariable(subComp.variable, model.getVariableValue(this.variable).initialValue);
    	    	}
    	    	subComp.flowNumber = this.flowNumber;
    	    	for(Flow flow: this.flowList) {
    	    		Flow f = flow.copyFlow(model, strSuffix);
    	    		//f.print();
    	    		subComp.addFlow(f);
    	    	}
    	    	for(String key: eventToFlow.keySet()) {
    	    		subComp.addEventToFlow(key+strSuffix, eventToFlow.get(key));
    	   // 		System.out.println("eventToFlow: "+" event: " + key + " flow: " + eventToFlow.get(key));
    	    	}
    	    	subComp.initialFlow = this.initialFlow;
    	    	retList.add(subComp);
    		}
    	}
    	return retList;
    }
    
    public ArrayList<Subcomponent> copyRecursiveSubComponent(HypeModel model, int index, int population) {
    	ArrayList<Subcomponent> retList = new ArrayList<Subcomponent>();
    	for(int i=0; i<population; i++) {
    		if(i != index) {
    			for(int j=0; j<population; j++) {
    				if(j != i && j != index) {
    					String strSuffix = "_"+index + "_" + i + "_" + j;
    					String strName = this.name.replace("$$", strSuffix);
    					Subcomponent subComp = new Subcomponent(strName, this.influence+strSuffix,
    			    			this.variable+strSuffix);
    			    	if(!model.isVariableDefined(subComp.variable)) {
    			    		//System.out.println(subComp.variable);
    			    		model.addVariable(subComp.variable, model.getVariableValue(this.variable).initialValue);
    			    	}
    			    	subComp.flowNumber = this.flowNumber;
    			    	for(Flow flow: this.flowList) {
    			    		Flow f = flow.copyFlow(model, strSuffix);
    			    		//f.print();
    			    		subComp.addFlow(f);
    			    	}
    			    	for(String key: eventToFlow.keySet()) {
    			    		subComp.addEventToFlow(key+strSuffix, eventToFlow.get(key));
    			   // 		System.out.println("eventToFlow: "+" event: " + key + " flow: " + eventToFlow.get(key));
    			    	}
    			    	subComp.initialFlow = this.initialFlow;
    			    	retList.add(subComp);
    				}
				}
    		}
    	}
    	return retList;
    }
    
    public void print() {
    	System.out.println("subcomponent:");
    	System.out.println("name:" + " " + name);
    	System.out.println("variable:" + " " + variable);
    	System.out.println("influence:" + " " + influence);
    	System.out.println("flowNumber:" + " " + flowNumber);
    	System.out.println("flowList:");
    	for(int i=0;flowList!=null && i<flowList.size();i++) {
    		flowList.get(i).print();
    	}
    	System.out.println("eventToFlow:");
    	for(String key: eventToFlow.keySet()) {
    		System.out.println("key:" + " " + key);
    		System.out.println("value:" + " " + eventToFlow.get(key));
    	}
        System.out.println("initialFlow:" + " " + initialFlow);
    }
    
    /**
     * Adds a flow to the subcomponent, including the events that activate the flow.
     * @param rate a string with the rate of the flow
     * @param function a function with the rate function of the flow
     * @param increment a boolean, true if the rate has a positive sign
     * @param initial true if the flow is the initial flow of the subcomponent, can be set only once
     * otherwise raises an exception
     * @param events a list of events (they must be checked to be proper events) that activate the flow
     * Checks that non event has already been associated with another flow, otherwise raises an exception.
     */
    public void addFlow(String rate, String function, boolean increment, 
            boolean initial, ArrayList<String> events) {
        Flow f = new Flow(rate, function, increment);
        this.addFlow(f, initial, events);
    }
    
    
    void addFlow(Flow f, boolean initial, ArrayList<String> events) {
        int code = this.flowNumber;
        this.flowList.add(f);
        if (initial) {
            if (this.initialFlow >= 0)
                throw new HypeException("Initial flow for subcomponent " + name + " already defined");
            this.initialFlow = code;
        }
        for (String e : events) {
            if (this.eventToFlow.containsKey(e))
                throw new HypeException("Flow already assigned to event " + e + " in subcomponent " + name);
            this.eventToFlow.put(e, code);
        }
        this.flowNumber++;
    }
    
    public void addFlow(Flow f) {
    	this.flowList.add(f);
    }
    
    public void addEventToFlow(String event, int flow) {
    	this.eventToFlow.put(event, flow);
    }
    
    public Variable generateStateVariable() {
        String n = "comp." + name;
        String v = String.valueOf(this.initialFlow);
        Variable x = new Variable(n,v);
        return x;
    }
    
    
    public ArrayList<FlowTransition> generateFlowTransitions() {
        ArrayList<FlowTransition> list = new ArrayList<FlowTransition>();
        String stateVar = this.generateStateVariable().name;
        for (int i=0;i<this.flowNumber;i++) {
            String guard = stateVar + " == " + i;
            FlowTransition t = this.flowList.get(i).getTransition(influence, i, variable, guard);
            list.add(t);
        }
        return  list;
    }
    
    
    public void generateEventToFlowStateMapping(HashMap<String,HashMap<String,Integer>> eventToFlowState) {
        String stateVar = this.generateStateVariable().name;
        for (String e : this.eventToFlow.keySet())
            eventToFlowState.get(e).put(stateVar, eventToFlow.get(e));
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof Subcomponent ) {
            return this.name.equals(((Subcomponent)obj).name);
        } else return false;
    }
    
}
