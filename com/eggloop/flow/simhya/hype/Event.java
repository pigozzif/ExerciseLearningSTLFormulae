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
public class Event {
    String name;
    boolean stochastic;
    String guard;
    ResetList reset;
    String rate;
    String flowReset = null;

    public Event(String name, String guard, ResetList reset) {
        this.name = name;
        this.guard = guard;
        this.reset = reset;
        this.stochastic = false;
        this.rate = "";
    }

    public Event(String name, String guard, ResetList reset, String rate) {
        this.name = name;
        this.guard = guard;
        this.reset = reset;
        this.rate = rate;
        this.stochastic = true;
    }
    
    public void setFlowReset(HashMap<String,Integer> varToState) {
        this.flowReset = "";
        for (String s : varToState.keySet())
            flowReset += (!flowReset.isEmpty() ? "; " : "") + s + " = " + varToState.get(s);
    }
    
    public Event copyEvent(HypeModel model, int index) {
    	String strName = this.name + "_" + index;
        boolean bStochastic = this.stochastic;;
        String strGuard = guard.toString();
        ArrayList<String> newVars = new ArrayList<String>(); 
        for(String key: model.variables.keySet()) {
        	int begin = 0;
        	do{	
        		begin = strGuard.indexOf(key, begin);
        		if(begin < 0) {
        			break;
        		}else {
        			//System.out.println(strGuard.charAt(begin+key.length()));
        			if(strGuard.charAt(begin+key.length()) == '#') {
            			if(!model.variables.containsKey(key+"_"+index)){
            				newVars.add(key);
            			}
            		}
        			begin = begin + key.length();
        		} 
        	}while(true);
        	/*if(strGuard.indexOf(key) > 0 && model.variables.containsKey(key+"_"+index)) {
        		strGuard = strGuard.replaceAll(key, key+"_"+index);
        	}*/
        }
        for(String key: newVars) {
        	model.addVariable(key+"_"+index, model.getVariableValue(key).initialValue);
        }
        
        strGuard = strGuard.replace("#", "_"+index);
        String strRate = this.rate;
        String strFlowReset = this.flowReset;
        Event e = null;
        if(bStochastic) {
        	e = new Event(strName, strGuard, null, strRate);
        }else {
        	e = new Event(strName, strGuard, null);
        }
        if(reset!=null) {
        	e.reset = this.reset.copyResetList(model, index);
        }
        
        e.flowReset = strFlowReset;
        //System.out.println(strGuard);
        model.symbols.addEvent(strName);
        model.eventMap.put(strName, e);
    	return e;
    }
    
    /**
     * @param replacement replace "#,$" in the definition of event
     * */
    public Event copyEvent(HypeModel model, int i,int j) {
    	String suffix = "_" + i +"_" + j;
    	String strName = this.name + suffix;
        boolean bStochastic = this.stochastic;;
        String strGuard = guard.toString();
        
        //System.out.println("before: " + strGuard);
        /*for(String key: model.variables.keySet()) {
			if(strGuard.indexOf(key)>=0&&model.variables.containsKey(key+"_"+index)) {
				strGuard = strGuard.replace(key,key+"_"+index);
				strGuard = strGuard.replace(key+"_"+index+"$$", key+"$$");
				strGuard = strGuard.replace(key+"_"+index+"$", key+"$");
				strGuard = strGuard.replace(key+"_"+index+"#", key+"#");
			}       	
        }*/
        
        //strGuard = strGuard.replace("$", replacement);
        
        strGuard = strGuard.replace("$$", "_" + j +"_" + i);
        strGuard = strGuard.replace("$", suffix);
        strGuard = strGuard.replace("##", "_" + j);
        strGuard = strGuard.replace("#", "_"+i);
        
        //System.out.println("after: " + strGuard);
        
        String strRate = this.rate;
        String strFlowReset = this.flowReset;
        Event e = null;
        if(bStochastic) {
        	e = new Event(strName, strGuard, null, strRate);
        }else {
        	e = new Event(strName, strGuard, null);
        }
        if(reset!=null) {
        	e.reset = this.reset.copyResetListWithInteraction(model, i, j);
        }
        
        e.flowReset = strFlowReset;
        model.symbols.addEvent(strName);
        model.eventMap.put(strName, e);
    	return e;
    }
    
    public Event copyRecursiveEvent(HypeModel model, int i, int j, int k) {
    	String suffix = "_"+i+"_"+j+"_"+k;
    	String strName = this.name + suffix;
        boolean bStochastic = this.stochastic;;
        String strGuard = guard.toString();
        
        //System.out.println("before: " + strGuard);
        /*for(String key: model.variables.keySet()) {
			if(strGuard.indexOf(key)>=0&&model.variables.containsKey(key+"_"+index)) {
				strGuard = strGuard.replace(key,key+"_"+index);
				strGuard = strGuard.replace(key+"_"+index+"$$", key+"$$");
				strGuard = strGuard.replace(key+"_"+index+"$", key+"$");
				strGuard = strGuard.replace(key+"_"+index+"#", key+"#");
			}       	
        }*/
        
        //strGuard = strGuard.replace("$", replacement);
        
        strGuard = strGuard.replace("#$", "_" + i + "_" + k);
        strGuard = strGuard.replace("$#", "_" + j + "_" + k);
       
        strGuard = strGuard.replace("$$", "_" + j + "_" + i);
        strGuard = strGuard.replace("$", "_" + i + "_" + j);
        strGuard = strGuard.replace("##", "_" + j);
        strGuard = strGuard.replace("#", "_" + i);
        
        //System.out.println("after: " + strGuard);
        
        String strRate = this.rate;
        String strFlowReset = this.flowReset;
        Event e = null;
        if(bStochastic) {
        	e = new Event(strName, strGuard, null, strRate);
        }else {
        	e = new Event(strName, strGuard, null);
        }
        if(reset!=null) {
        	e.reset = this.reset.copyResetListWithRecursiveInteraction(model, i, j, k);
        }
        
        e.flowReset = strFlowReset;
        model.symbols.addEvent(strName);
        model.eventMap.put(strName, e);
    	return e;
    }
    
    
    public EventTransition getTransition(GuardResetPair p) {
        if (this.flowReset == null)
            throw new HypeException("Flow state reset not set for this event!");
        String g = p.guard + (!this.guard.isEmpty() ? " && ( " + this.guard + " )" : "");
        String r = p.reset;
        //System.out.println("here: "+r);
        if (!r.isEmpty() && !flowReset.isEmpty())
            r += "; ";
        r += flowReset;
        String r1 = this.reset.toString();
        if (!r.isEmpty() && !r1.isEmpty())
            r += "; ";
        r += r1;
        EventTransition t;
        if (this.stochastic)
            t = new EventTransition(name,g,r,rate);
        else
            t = new EventTransition(name,g,r);
       return t;
    }
    
}
