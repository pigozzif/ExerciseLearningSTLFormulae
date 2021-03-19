/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.model.flat;
import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.model.transition.*;
import com.eggloop.flow.simhya.simhya.model.store.*;
import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import com.eggloop.flow.simhya.simhya.matheval.Expression;
import com.eggloop.flow.simhya.simhya.model.ModelException;

/**
 *
 * @author luca
 */
public class FlatTemplate {
    String name;
    SymbolArray boundedVars;
    SymbolArray localVars;
    //this is boundedVars + localVars
    SymbolArray allLocalVars;
    int arity;
    ArrayList<Transition> transitions;
    int instances;

    public FlatTemplate(String name, SymbolArray boundedVars) {
        this.name = name;
        this.boundedVars = boundedVars;
        allLocalVars = boundedVars.clone(true);
        localVars = new SymbolArray();
        arity = boundedVars.getNumberOfSymbols();
        transitions = new ArrayList<Transition>();
        instances = 0;
    }
    
    
    public boolean isLocalVariable(String name) {
        return this.allLocalVars.containsSymbol(name);
    }
    
    
    /**
     * Adds a local variable to the template
     * @param name the variable name
     * @param value the variable initial value
     */
    public void addLocalVariable(String name, double value) {
        if (this.isLocalVariable(name))
            throw new ModelException("Local variable " + name + " already defined (either as local or template parameter)");
        this.localVars.addSymbol(name, value);
        this.allLocalVars.addSymbol(name, value);
    }
    
    /**
     * Adds a local variable defined by an expression to the template
     * @param name the variable name
     * @param value the variable initial value
     */
    public void addLocalVariable(String name, double value, Expression expression) {
        if (this.isLocalVariable(name))
            throw new ModelException("Local variable " + name + " already defined (either as local or template parameter)");
        this.localVars.addSymbol(name, value, expression);
        this.allLocalVars.addSymbol(name, value);
    }
    
    
    /**
     * returns the list of all local variables
     */
    public SymbolArray getAllLocalVariables() {
        return this.allLocalVars;
    }


    public int getArity() {
        return this.arity;
    }
    
    
    /**
     * Adds a transition to the template
     * @param t 
     */
    public void addTransition(Transition t) {
        this.transitions.add(t);
    }

    public ArrayList<Transition> generateNewInstance(Store store, ArrayList<Expression> boundedVarDefinition) {
        return generateNewInstance(store,boundedVarDefinition,name,false);
    }

    public ArrayList<Transition> generateNewInstance(Store store, ArrayList<Expression> boundedVarDefinition, String instanceName) {
        return generateNewInstance(store,boundedVarDefinition,instanceName,true);
    }

       
    ArrayList<Transition> generateNewInstance(Store store, ArrayList<Expression> boundedVarDefinition, String instanceName, boolean uniqueName) {
        if (this.arity != boundedVarDefinition.size())
            throw new ModelException("Wrong number of arguments for template " + this.name);

        ArrayList<String> replacedLocalVars;
        ArrayList<Expression> globalVarsExpression;
        ArrayList<Transition> newTransitions;
        replacedLocalVars = new ArrayList<String> ();
        globalVarsExpression = new ArrayList<Expression>();
        newTransitions = new ArrayList<Transition>();
        ArrayList<String> toSubst = this.boundedVars.getNameOfAllSymbols();
        
        //adding local variables as global variables to the store, together 
        //with initial value expression properly recomputed
        for (int i=0;i<this.localVars.getNumberOfSymbols();i++) {
            String newname = instanceName + "." + (uniqueName ? "" : this.instances + ".") + this.localVars.getName(i);
            replacedLocalVars.add(this.localVars.getName(i));
            int j = store.addVariable(newname, localVars.getValue(i));
            globalVarsExpression.add(store.generateExpressionForSymbol(newname));
            if (this.localVars.hasExpressionForInitialValue(i)) {
                Expression exp = this.localVars.getInitialValueExpression(i);
                exp = exp.substitute(toSubst, boundedVarDefinition);
                exp = exp.substitute(replacedLocalVars, globalVarsExpression);
                store.replaceInitialValueExpression(j, exp);
                store.setVariableValue(j, exp.computeValue());
            }
        }
        
        //instantiating all transitions
        ArrayList<String> vars = new ArrayList<String>();
        ArrayList<Expression> exps = new ArrayList<Expression>();
        vars.addAll(toSubst);
        vars.addAll(replacedLocalVars);
        exps.addAll(boundedVarDefinition);
        exps.addAll(globalVarsExpression);
        for (Transition t : this.transitions) {
            Transition t1 = instantiateTransition(t, vars, exps);
            newTransitions.add(t1);
        }
        if (!uniqueName)
            this.instances ++;
        return newTransitions;
    }
    
    
    private Transition instantiateTransition(Transition trans, ArrayList<String> vars, ArrayList<Expression> exps) {
        Function f,f1; 
        Predicate p,p1;
        Reset res;
        Transition t = new Transition(trans.getEvent(),trans.getType());
        //add guard
        p = trans.getGuardPredicate();
        if (p!=null) { 
            p = p.substitute(vars, exps);
            t.setGuard(new Guard(p));
        }
        //add rate
        f = trans.getRateFunction();
        if (f!=null) { 
            f = f.substitute(vars, exps);
            t.setRate(new Rate(f));
        }
        //add timed activation
        f = trans.getTimedActivationFunction();
        if (f!=null) { 
            f = f.substitute(vars, exps);
            t.setTimedActivation(new TimedActivation(f));
        }
        //add continuity condition
        p = trans.getContinuityStatusPredicate(true);
        if (p!=null)
            p = p.substitute(vars, exps);
        p1 = trans.getContinuityStatusPredicate(false);
        if (p1!=null) 
            p1 = p1.substitute(vars, exps);
        if (p != null && p1 != null)
            t.setContinuityStatus(new ContinuityStatus(p1,p));
        //RESET
        //process each atomic reset.
        res = new Reset();
        for (AtomicReset r : trans.getResetList())
            res.addAtomicReset(r.substitute(vars, exps));
        t.setReset(res);      
        //delay
        f = trans.getDelayFunction();
        if (f!=null) { 
            //if f is non null, p and reset are defined
            f = f.substitute(vars, exps);
            p = trans.getDelayedGuardPredicate();
            p = p.substitute(vars, exps);   
            //DELAY RESET
            res = new Reset();
            for (AtomicReset r : trans.getDelayedResetList())
                res.addAtomicReset(r.substitute(vars, exps));
            t.setDelay(new Delay(f), new Guard(p), res);
        }
        t.setStopping(trans.isStopping());
        t.setStoppingAfterDelay(trans.isStoppingAfterDelay());
        return t;
    }
    
    
}
