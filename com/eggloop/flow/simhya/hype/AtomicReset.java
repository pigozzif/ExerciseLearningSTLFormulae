/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.hype;

import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.matheval.Expression;


/**
 *
 * @author Luca
 */
public class AtomicReset {
    String variable;
    boolean local;
    Expression reset;
    String resetString;


    
    public AtomicReset(String variable, boolean local, Expression reset) {
        this.variable = variable;
        this.local = local;
        this.reset = reset;
    }
    
    public AtomicReset(String variable, boolean local, String reset) {
        this.variable = variable;
        this.local = local;
        this.resetString = reset;
    }

    @Override
    public String toString() {
        if (local)
            throw new HypeException("Trying to convert to String a local atomic reset! Instantiate local variables first");
        String s = "";
        s += variable + " = " + reset.toString();
        return s;
    }

    public String getVariable() {
    	return this.variable;
    }
    
    public boolean getLocal() {
    	return this.local;
    }
    
    public Expression getExpression() {
    	return this.reset;
    }
    
    public AtomicReset copyAtomicReset(HypeModel model, int index) {
    	String strVariable = this.variable.replace("#", "_" + index);
    	String var = this.variable.replace("#", "");
    	
		if(!model.variables.containsKey(strVariable)) {
    		model.addVariable(strVariable, model.getVariableValue(var).initialValue);
    	}
    	
        boolean bLocal = this.local;
        String strReset = "";
        if(this.reset != null) {
        	strReset = this.reset.getStrExpr();
        	strReset = strReset.replace("#", "_" + index);
        }else {
        	strReset = resetString.replace("#", "_" + index); 
        }
       
        Expression exp = model.parseExpression(strReset);
        AtomicReset ret = new AtomicReset(strVariable, bLocal, exp);
    	return ret;
    }
    
    public AtomicReset copyAtomicReset(HypeModel model, int i, int j) {
    	//String strVariable = this.variable.replace("#", "_" + index);
    	//String var = this.variable.replace("#", "");
    	String strVariable = this.variable.replace("$$", "_" + j +"_" + i);
    	strVariable = strVariable.replace("$", "_" + i +"_" + j);
    	strVariable = strVariable.replace("##", "_" + j);
    	strVariable = strVariable.replace("#", "_" + i);
    	//System.out.println(strVariable);
    	
    	String initVar = this.variable.replace("$", "");
    	initVar = initVar.replace("#", "");
    	if(!model.variables.containsKey(strVariable)) {
    		model.addVariable(strVariable, model.getVariableValue(initVar).initialValue);
    	}
        boolean bLocal = this.local;
        
        String strReset = "";
        if(this.reset != null) {
        	strReset = this.reset.getStrExpr();
        	strReset = strReset.replace("$$", "_" + j +"_" + i);
        	strReset = strReset.replace("$", "_" + i +"_" + j);
        	strReset = strReset.replace("##", "_" + j);
        	strReset = strReset.replace("#", "_" + i);
        	
        }else {
        	strReset = resetString.replace("$$", "_" + j +"_" + i);
        	strReset = strReset.replace("$", "_" + i +"_" + j);
        	strReset = strReset.replace("##", "_" + j);
        	strReset = strReset.replace("#", "_" + i);
        }
        
        /*for(String key: model.variables.keySet()) {
        	if(strReset.indexOf(key) > 0 && model.variables.containsKey(key+"_"+index)) {
        		strReset = strReset.replaceAll(key, key+"_"+index);
        	}
        }*/
        
        //System.out.println(strReset);
        Expression exp = model.parseExpression(strReset);
        AtomicReset ret = new AtomicReset(strVariable, bLocal, exp);
    	return ret;
    }
    
    public AtomicReset copyAtomicReset(HypeModel model, int i, int j, int k) {
    	//String strVariable = this.variable.replace("#", "_" + index);
    	//String var = this.variable.replace("#", "");
    	String strVariable = this.variable.replace("#$", "_" + i + "_" + k);
    	strVariable = strVariable.replace("$#", "_" + j + "_" + k);
       
    	strVariable = strVariable.replace("$$", "_" + j + "_" + i);
    	strVariable = strVariable.replace("$", "_" + i + "_" + j);
    	strVariable = strVariable.replace("##", "_" + j);
    	strVariable = strVariable.replace("#", "_" + i);
    	
    	String initVar = this.variable.replace("#","");
    	initVar = initVar.replace("$","");
    	if(!model.variables.containsKey(strVariable)) {
    		model.addVariable(strVariable, model.getVariableValue(initVar).initialValue);
    	}
        boolean bLocal = this.local;
        String strReset = "";
        if(this.reset != null) {
        	strReset = this.reset.getStrExpr();  	
        }else {
        	strReset = resetString;
        }
        
        strReset = strReset.replace("#$", "_" + i + "_" + k);
        strReset = strReset.replace("$#", "_" + j + "_" + k);
        strReset = strReset.replace("$$", "_" + j + "_" + i);
        strReset = strReset.replace("$", "_" + i + "_" + j);
    	strReset = strReset.replace("##", "_" + j);
    	strReset = strReset.replace("#", "_" + i);
    	//System.out.println(strReset);
        //strReset = strReset.replace("#", "_" + index);
        //System.out.println(strReset);
        Expression exp = model.parseExpression(strReset);
        AtomicReset ret = new AtomicReset(strVariable, bLocal, exp);
    	return ret;
    }
    
    /**
     * substitutes local variables to a parameterized atomic reset!
     * @param localVariables
     * @param globalSymbols
     * @param isConstant an arraylist containing true if the globalsymbol is a constant!
     * @return
     */
    public AtomicReset substitute(ArrayList<String> localVariables, ArrayList<String> globalSymbols, ArrayList<Boolean> isConstant) {
        String newVariable;
        Expression newReset;
        if (!local) 
            return this;
        if (localVariables.contains(this.variable)) {
            int i = localVariables.indexOf(variable);
            if (isConstant.get(i))
                 throw new HypeException("Trying to instantiate the reset variable " + variable + " by a parameter: " + globalSymbols.get(i));
            newVariable = globalSymbols.get(i);
        } else
            newVariable = variable;
        newReset = reset.substitute(localVariables, globalSymbols);
        return new AtomicReset(newVariable,false,newReset);
    }



}
