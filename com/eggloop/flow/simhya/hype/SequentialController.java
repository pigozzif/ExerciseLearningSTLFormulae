/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.hype;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Luca
 */
public class SequentialController {
    String name;
    /**
     * maps event to seq. controller names!
     */
    HashMap<String,ArrayList<String>> transitions;
    HashMap<String,SequentialController> derivativeSet;
    boolean initiallyActive;
    int code;
    private boolean derivativeSetComputed;
    private String stateVariable;

    public SequentialController(String name) {
        this.name = name;
        this.initiallyActive= false;
        this.transitions = new HashMap<String,ArrayList<String>>();
        this.derivativeSet = new HashMap<String,SequentialController>();
        derivativeSetComputed = false;
        this.stateVariable = "";
    }
    
    
    /**
     * Adds a transition for the specified event to the specified sequential controller
     * @param event
     * @param seqContr 
     */
    public void addTransition(String event, String seqContr) {
        if (this.transitions.containsKey(event))
            this.transitions.get(event).add(seqContr);
        else {
            ArrayList<String> list = new ArrayList<String>();
            list.add(seqContr);
            this.transitions.put(event, list);
        }
    }
    
    
    public void computeDerivativeSet(HashMap<String,SequentialController> seqControllers) {
        for (SequentialController s : seqControllers.values())
            s.derivativeSetComputed = false;
        this.derivativeSet = this.computeDerivativeSetRec(seqControllers);
    }
    
    /**
     * recursively computes the derivative set of a sequential controller. Relies on the private 
     * variable derivativeSetComputed to avoid infinite loop.
     * @param seqControllers the list of all sequential controllers!
     * @return 
     */
    private HashMap<String,SequentialController> computeDerivativeSetRec(HashMap<String,SequentialController> seqControllers) {
        HashMap<String,SequentialController> set = new HashMap<String,SequentialController>();
        set.put(name, this);
        this.derivativeSetComputed = true;
        for (String e : this.transitions.keySet())
            for (String n : this.transitions.get(e)) {
                if (!seqControllers.containsKey(n))
                    throw new HypeException("Sequential controller " + n + " has not been defined");
                SequentialController s = seqControllers.get(n);
                if (!s.derivativeSetComputed)
                    set.putAll(s.computeDerivativeSetRec(seqControllers));
            }
        return set;
    }
    
    /**
     * Sets the sequential controller as the initial state of a seq controller
     * occurrence in the synch tree.
     * @param code the leaf code in the synch tree.
     */
    private void setAsInitialComponent(int code) {
        for (SequentialController s : this.derivativeSet.values()) {
            s.initiallyActive = false;
            s.code = code;
        }
        this.initiallyActive = true;
    }
    
    /**
     * Generates state variables for all seq controllers in the derivative set, 
     * with this as the initial state.
     * State variables equal -1 when the state is not active and 1 when the state is active
     * @param code the leaf code in the synch tree.
     * @return 
     */
    ArrayList<Variable> generateStateVariables(int code) {
        this.setAsInitialComponent(code);
        ArrayList<Variable> list = new ArrayList<Variable>();
        for (SequentialController s : this.derivativeSet.values()) {
            s.stateVariable = "contr." + s.name + "." + code;
            if (s.initiallyActive)
                list.add(new Variable(s.stateVariable,String.valueOf(1)));
            else
                list.add(new Variable(s.stateVariable,String.valueOf(-1)));
        }
        return list;
    }
    
    ArrayList<GuardResetPair> generateTransitionsForEvent(String event) {
        ArrayList<GuardResetPair> list = new ArrayList<GuardResetPair>();
        for (SequentialController s : this.derivativeSet.values()) {
            String guard = s.stateVariable + " > 0";
            if (s.transitions.containsKey(event))
               for (String targetName : s.transitions.get(event)) {
                   SequentialController target = this.derivativeSet.get(targetName);
                   String reset = "";
                   if (!s.name.equals(target.name))
                        reset = s.stateVariable + " = -1; " + target.stateVariable + " = 1";
                   GuardResetPair pair = new GuardResetPair(guard,reset);
                   list.add(pair);
               }
        }
        return list;
    }

}
