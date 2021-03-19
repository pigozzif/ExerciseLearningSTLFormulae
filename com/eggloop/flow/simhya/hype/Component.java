/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.hype;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Luca
 */
public class Component {
    String name;
    ArrayList<Component> componentList;
    ArrayList<Subcomponent> subcomponentList;
    private ArrayList<Subcomponent> groundSubcomponentList;

    public Component(String name) {
        this.name = name;
        this.componentList = new ArrayList<Component>();
        this.subcomponentList = new ArrayList<Subcomponent>();
        this.groundSubcomponentList = null;
    }
    
    public void addComponent(Component c) {
        if (!this.componentList.contains(c))
            this.componentList.add(c);
    }
    
    public void addSubcomponent(Subcomponent c) {
        if (!this.subcomponentList.contains(c))
            this.subcomponentList.add(c);
    }
    
    /**
     * @param num how many number of components need to be copied
     * */
    public ArrayList<Component> CopyComponent(HypeModel model, int num) {
    	ArrayList<Component> retList = new ArrayList<Component>();
    	for(int i=0; i<num; i++) {
    		Component comp = new Component(this.name+"_"+i);
    		for(Subcomponent subComp: subcomponentList) {
    			if(subComp.getName().contains("$$")) {
    				ArrayList<Subcomponent> subList = subComp.copyRecursiveSubComponent(model, i, num);
    				for(Subcomponent s: subList) {
    					comp.addSubcomponent(s);
    				}
    			} else if(subComp.getName().contains("$")) {
    				ArrayList<Subcomponent> subList = subComp.copySubComponent(model, i, num);
    				for(Subcomponent s: subList) {
    					comp.addSubcomponent(s);
    				}
    			}else {
    				comp.addSubcomponent(subComp.copySubComponent(model, i));
    			}
    			
    		}
    		//System.out.println(comp.name);
    		model.addComponent(comp);
    		retList.add(comp);
    	}
    	return retList;
    }
    
    public void print() {
    	for(int i=0;subcomponentList!=null && i<subcomponentList.size();i++) {
    		System.out.println("subcomponentList:");
    		subcomponentList.get(i).print();
    	}
    	for(int i=0; groundSubcomponentList!=null&&i<groundSubcomponentList.size();i++) {
    		System.out.println("groundSubcomponentList:");
    		groundSubcomponentList.get(i).print();
    	}
    }
    
    /**
     * Generates a list of all distinct subcomponents contained by the component
     * @return a list of Subcomponents
     */
    private ArrayList<Subcomponent> generateGroundTerm() {
        ArrayList<Subcomponent> list = new ArrayList<Subcomponent> ();
        list.addAll(this.subcomponentList);
        for (Component c : this.componentList) {
            ArrayList<Subcomponent> l = c.generateGroundTerm();
            for (Subcomponent s : l)
                if (!list.contains(s))
                    list.add(s);
        }
        return list;
    }
    
    
    /**
     * Generates the list of state variables!
     * @return 
     */
    public ArrayList<Variable> generateStateVariables() {
        ArrayList<Variable> list = new ArrayList<Variable>();
        if (groundSubcomponentList == null)
            groundSubcomponentList = this.generateGroundTerm();
        for (Subcomponent s : groundSubcomponentList) {
            list.add(s.generateStateVariable());
        }
        return list;
    }
    
    
    /**
     * generates the list of all flow transitions of all subcomponents of the component
     * @return 
     */
    public ArrayList<FlowTransition> generateFlowTransitions() {
        ArrayList<FlowTransition> list = new ArrayList<FlowTransition>();
        if (groundSubcomponentList == null)
            groundSubcomponentList = this.generateGroundTerm();
        for (Subcomponent s : groundSubcomponentList)
            list.addAll(s.generateFlowTransitions());
        return list;
    }
    
    
    /**
     * Generates a mapping from events to a map between flow state variables and their state,
     * to add them to the resets.
     * @param events the list of event names.
     * @return 
     */
    public HashMap<String,HashMap<String,Integer>> generateEventToFlowStateMapping(Set<String> events) {
        HashMap<String,HashMap<String,Integer>> eventToFlowState = new HashMap<String,HashMap<String,Integer>>();
        for (String e : events) {
            HashMap<String,Integer> map = new HashMap<String,Integer>();
            eventToFlowState.put(e, map);
        }
        if (groundSubcomponentList == null)
            groundSubcomponentList = this.generateGroundTerm();
        for (Subcomponent s : groundSubcomponentList)
            s.generateEventToFlowStateMapping(eventToFlowState);
        return eventToFlowState;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof Component ) {
            return this.name.equals(((Component)obj).name);
        } else return false;
    }

}
