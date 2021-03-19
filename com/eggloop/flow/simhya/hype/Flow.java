/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.hype;

import java.util.ArrayList;

/**
 *
 * @author Luca
 */
public class Flow {
    String rate;
    String function;
    boolean increment;

    
    public Flow(String rate, String function, boolean increment) {
        this.rate = rate;
        this.function = function;
        this.increment = increment;
    }

    
    public FlowTransition getTransition(String influence, int state, String variable, String guard) {
        FlowTransition t;
        String reset = variable + (increment ? " += " : " -= ") + rate;
        t = new FlowTransition(influence + "_" + state,guard,reset,function);
        return t;
    }
    
    public void print() {
    	System.out.println("flow:" + " ");
    	System.out.println("rate:" + " " + rate);
    	System.out.println("function:" + " " + function);
    	System.out.println("increment:" + " " + increment);
    }
    
    public Flow copyFlow(HypeModel model, String suffix) {
    	String strFunc = function;
    	ArrayList<String> newVariables = new ArrayList<String>();
    	//System.out.println("strFunc1: " + strFunc);
    	 for(String key: model.variables.keySet()) {
         	if(strFunc.indexOf(key) > 0) {
         		newVariables.add(key);
         	}
         }
    	 for(String var: newVariables) {
    		 strFunc = strFunc.replaceAll(var, var+suffix);
    		 if(!model.isVariableDefined(var+suffix)) {
      			model.addVariable(var+suffix, model.getVariableValue(var).initialValue);
      		}
    	 }
    	 //System.out.println("strFunc2: " + strFunc);
    	 //System.out.println("strFunc2: " + strFunc);
    	 Flow f = new Flow(rate, strFunc, increment);
    	 return f;
    }
    
}
