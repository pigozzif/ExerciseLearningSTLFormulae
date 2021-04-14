/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.flat;

import java.util.ArrayList;
import java.util.HashMap;

import eggloop.flow.simhya.simhya.matheval.ExpressionSymbolArray;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.model.store.Function;
import eggloop.flow.simhya.simhya.model.store.Predicate;
import eggloop.flow.simhya.simhya.model.transition.AtomicReset;
import eggloop.flow.simhya.simhya.model.transition.Transition;
import eggloop.flow.simhya.simhya.model.store.Store;
import eggloop.flow.simhya.simhya.model.ModelException;
import eggloop.flow.simhya.simhya.model.transition.TType;
import eggloop.flow.simhya.simhya.GlobalOptions;
import org.sbml.jsbml.*;


/**
 *
 * @author Luca
 */
public class FlatModel {
    String name;
    Store store;
    
    ArrayList<String> modelVariables;
    
    
    
    Transition[] transitions;
    int[] stochasticTransitionID;
    int[] instantaneousTransitionID;
    int[] timedTransitionID;
    int[] continuousTransitionID;
    int[] hybridTransitionID;
    int[] continuouslyApproximableTransitionID;
    
    Integer[][] guardDependencyGraph;
    Integer[][] rateDependencyGraph;
    Integer[][] firingTimeDependencyGraph;
    Integer[][] guardDelayDependencyGraph;
    Integer[][] rateDelayDependencyGraph;
    Integer[][] firingTimeDelayDependencyGraph;
    boolean initialized;
    
    int numberOfTransitions;
    int numberOfStochasticTransitions;
    int numberOfInstantaneousTransitions;
    int numberOfTimedTransitions;
    int numberOfContinuousTransitions;
    int numberOfHybridTransitions;
    int numberOfContinuouslyApproximableTransitions;

    double[] initialValueOfVariables;
    double[] initialValueOfParameters;

    boolean alwaysComputeRateAfterGuard = GlobalOptions.alwaysComputeRateAfterGuard;

    HashMap<String, FlatTemplate> templates;

   
    
    public FlatModel(String name) {
        this.name = name;
        
        this.transitions = new Transition[GlobalOptions.initialArraySize];
        this.stochasticTransitionID = new int[GlobalOptions.initialArraySize];
        this.instantaneousTransitionID = new int[GlobalOptions.initialArraySize];
        this.timedTransitionID = new int[GlobalOptions.initialArraySize];
        continuousTransitionID = new int[GlobalOptions.initialArraySize];
        hybridTransitionID = new int[GlobalOptions.initialArraySize];
        continuouslyApproximableTransitionID = new int[GlobalOptions.initialArraySize];
        
        modelVariables = null;
        this.store = null;
        this.guardDependencyGraph = null;
        this.firingTimeDependencyGraph = null;
        this.rateDependencyGraph = null;
        this.guardDelayDependencyGraph = null;
        this.firingTimeDelayDependencyGraph = null;
        this.rateDelayDependencyGraph = null;
        this.initialized = false;
        this.numberOfTransitions = 0;
        numberOfStochasticTransitions = 0;
        numberOfInstantaneousTransitions = 0;
        numberOfTimedTransitions = 0;
        numberOfContinuousTransitions = 0;
        numberOfHybridTransitions = 0;
        numberOfContinuouslyApproximableTransitions = 0;
        templates = new HashMap<String, FlatTemplate>();
    }
    /**
     * add a store to the model
     * @param store
     */
    public void setStore(Store store) {
        this.store = store;
        this.initialValueOfVariables = store.getCopyOfVariablesValues();
        this.initialValueOfParameters = store.getCopyOfParametersValues();
        this.modelVariables = store.getNameOfAllVariables();
    }
    /**
     * Adds a transition to the model
     * @param t a new transition
     * @return the id of the transition
     */
    
    
    
    public int addTransition(Transition t) {
        if (this.initialized)
            throw new ModelException("Cannot add a transition to an initialized model");
        if (t == null)
            throw new ModelException("Null transition added to the model");
        int id = this.numberOfTransitions;
        if (id == this.transitions.length)
            transitions = java.util.Arrays.copyOf(transitions, 2*transitions.length);
        this.transitions[id] = t;
        if (t.getType() == TType.STOCHASTIC) {
            if (this.numberOfStochasticTransitions == this.stochasticTransitionID.length)
                this.stochasticTransitionID = java.util.Arrays.copyOf(stochasticTransitionID, 2*stochasticTransitionID.length);
            this.stochasticTransitionID[this.numberOfStochasticTransitions++]=id;
            if (t.isContinuouslyApproximable()) {
                if (this.numberOfContinuouslyApproximableTransitions == this.continuouslyApproximableTransitionID.length)
                    this.continuouslyApproximableTransitionID = java.util.Arrays.copyOf(continuouslyApproximableTransitionID, 2*continuouslyApproximableTransitionID.length);
                this.continuouslyApproximableTransitionID[this.numberOfContinuouslyApproximableTransitions++]=id;
            }
        }
        else if(t.getType() == TType.CONTINUOUS) {
            if (!t.isContinuouslyApproximable())
                throw new ModelException("Transition " + t.getEvent() + " is not continuously approximable");
            if (this.numberOfContinuousTransitions == this.continuousTransitionID.length)
                this.continuousTransitionID = java.util.Arrays.copyOf(continuousTransitionID, 2*continuousTransitionID.length);
            this.continuousTransitionID[this.numberOfContinuousTransitions++]=id;
            if (this.numberOfContinuouslyApproximableTransitions == this.continuouslyApproximableTransitionID.length)
                this.continuouslyApproximableTransitionID = java.util.Arrays.copyOf(continuouslyApproximableTransitionID, 2*continuouslyApproximableTransitionID.length);
            this.continuouslyApproximableTransitionID[this.numberOfContinuouslyApproximableTransitions++]=id;
        }
        else if(t.getType() == TType.HYBRID) {
            if (!t.isContinuouslyApproximable())
                throw new ModelException("Transition " + t.getEvent() + " is not continuously approximable");
            if (this.numberOfHybridTransitions == this.hybridTransitionID.length)
                this.hybridTransitionID = java.util.Arrays.copyOf(hybridTransitionID, 2*hybridTransitionID.length);
            this.hybridTransitionID[this.numberOfHybridTransitions++]=id;
            if (this.numberOfContinuouslyApproximableTransitions == this.continuouslyApproximableTransitionID.length)
                this.continuouslyApproximableTransitionID = java.util.Arrays.copyOf(continuouslyApproximableTransitionID, 2*continuouslyApproximableTransitionID.length);
            this.continuouslyApproximableTransitionID[this.numberOfContinuouslyApproximableTransitions++]=id;
        }
        else if(t.getType() == TType.INSTANTANEOUS) {
            if (this.numberOfInstantaneousTransitions == this.instantaneousTransitionID.length)
                this.instantaneousTransitionID = java.util.Arrays.copyOf(instantaneousTransitionID, 2*instantaneousTransitionID.length);
            this.instantaneousTransitionID[this.numberOfInstantaneousTransitions++]=id;
        }
        else if(t.getType() == TType.TIMED) {
             if (this.numberOfTimedTransitions == this.timedTransitionID.length)
                this.timedTransitionID = java.util.Arrays.copyOf(timedTransitionID, 2*timedTransitionID.length);
            this.timedTransitionID[this.numberOfTimedTransitions++]=id;
        }
        numberOfTransitions++;    
        return id;
    }
    /**
     *
     * @return true if the model has been initialized by calling  generateDependencyGraphs();
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    public int getNumberOfTransitions() {
        return this.numberOfTransitions;
    }

    /**
     *
     * @return the number of stoch transitions
     */
    public int getNumberOfStochasticTransitions(boolean force) {
        if (force)
            return this.numberOfStochasticTransitions + this.numberOfContinuousTransitions +
                    this.numberOfHybridTransitions;
        else
            return this.numberOfStochasticTransitions;
    }
    /**
     *
     * @return the list of IDs of stochastic transitions
     */
    public int[] getListOfStochasticTransitionID(boolean force) {
        if (force) {
            int [] v = new int[getNumberOfStochasticTransitions(force)];
            int j = 0;
            for (int i=0;i<this.numberOfStochasticTransitions;i++)
                v[j+i] = stochasticTransitionID[i];
            j += numberOfStochasticTransitions;
            for (int i=0;i<this.numberOfContinuousTransitions;i++)
                v[j+i] = continuousTransitionID[i];
            j += numberOfContinuousTransitions;
            for (int i=0;i<this.numberOfHybridTransitions;i++)
                v[j+i] = hybridTransitionID[i];
            return v;
        } else
            return this.stochasticTransitionID;
    }

    public int[] getListOfContinuousTransitionID(boolean force) {
        if (force)
            return this.continuouslyApproximableTransitionID;
        else
            return this.continuousTransitionID;
    }

    public int[] getListOfHybridTransitionID() {
        return this.hybridTransitionID;
    }

    public int getNumberOfContinuousTransitions(boolean force) {
        if (force)
            return this.numberOfContinuouslyApproximableTransitions;
        else
            return this.numberOfContinuousTransitions;
    }

    public int getNumberOfHybridTransitions() {
        return this.numberOfHybridTransitions;
    }


    /**
     *
     * @return the number of instantaneous transitions
     */
    public int getNumberOfInstantaneousTransitions() {
        return this.numberOfInstantaneousTransitions;
    }
    /**
     *
     * @return the list of IDs of instantaneous transitions
     */
    public int[] getListOfInstantaneousTransitionsID() {
        return this.instantaneousTransitionID;
    }
    /**
     *
     * @return the number of timed transitions
     */
    public int getNumberOfTimedTransitions() {
        return this.numberOfTimedTransitions;
    }
    /**
     *
     * @return the list of IDs of timed transitions;
     */
    public int[] getListOfTimedTransitionsID() {
        return this.timedTransitionID;
    }
    /**
     * Evaluates the guard of the indicated transition
     * @param transitionID the ID of the transition
     * @return true if the transition is active
     */
    public boolean evaluateGuard(int transitionID) {
        return transitions[transitionID].evaluateGuard();
        
    }
    /**
     * Return the current activation status of the specified transition
     * @param transitionID
     * @return true if the transition is active
     */
    public boolean getCurrentGuardStatus(int transitionID) {
        return transitions[transitionID].isActive();
    }
    /**
     * computes the rate of the indicated transition
     * @param transitionID
     * @return
     */
    public double computeRate(int transitionID) {
        return transitions[transitionID].computeRate();
    }
    /**
     * computes the next firing time of a timed transition. raises an exception if
     * the transition is not timed
     * @param transitionID
     * @param time the current simulation time
     * @ the retuned value is guaranteed to be >= time.
     */
    public double computeNextFiringTime(int transitionID, double time) {
        return transitions[transitionID].getNextFiringTime(time);
    }
    /**
     * Executes a given transition, modyfing the store.
     * @param transitionID
     */
    public void execute(int transitionID) {
        this.transitions[transitionID].execute(store);
    }
    /**
     *
     * @param transitionID
     * @return true if the specified transition is stopping
     */
    public boolean isStopping(int transitionID) {
        return this.transitions[transitionID].isStopping();
    }
    /**
     *
     * @param transitionID
     * @return true if the specified transition is delayed and is stopping after
     * a delay
     */
    public boolean isStoppingAfterDelay(int transitionID) {
        return this.transitions[transitionID].isDelayed() && this.transitions[transitionID].isStoppingAfterDelay();
    }
    /**
     *
     * @param transitionID
     * @return the type of a given transition
     */
    public TType getTransitionType(int transitionID) {
        return this.transitions[transitionID].getType();
    }
    /**
     *
     * @param transitionID
     * @return the name of a transition (the event name_
     */
    public String getTransitionName(int transitionID) {
        return this.transitions[transitionID].getEvent();
    }


    public void finalizeInitialization() {
        store.finalizeInitialization();
        transitions = (Transition[]) java.util.Arrays.copyOf(transitions, numberOfTransitions);
        stochasticTransitionID = java.util.Arrays.copyOf(stochasticTransitionID, numberOfStochasticTransitions);
        instantaneousTransitionID = java.util.Arrays.copyOf(instantaneousTransitionID, numberOfInstantaneousTransitions);
        timedTransitionID = java.util.Arrays.copyOf(timedTransitionID, numberOfTimedTransitions);
        this.continuousTransitionID = java.util.Arrays.copyOf(continuousTransitionID, numberOfContinuousTransitions);
        this.hybridTransitionID = java.util.Arrays.copyOf(hybridTransitionID, this.numberOfHybridTransitions);
        this.continuouslyApproximableTransitionID =  java.util.Arrays.copyOf(continuouslyApproximableTransitionID, this.numberOfContinuouslyApproximableTransitions);
        this.generateDependencyGraphs();
        this.generateDependencyGraphsAfterDelay();
        this.initialized = true;
    }


    /**
     * Constructs the dependency graphs;
     */
    private void generateDependencyGraphs() {
        //initialization
        ArrayList<ArrayList<Integer>> varGuard,varRate,varTime,transUpdate,timeDG,guardDG,rateDG;
        ArrayList<Integer> vars,trans;
        varGuard = new ArrayList();
        varRate = new ArrayList();
        varTime = new ArrayList();
        transUpdate = new ArrayList();
        timeDG = new ArrayList();
        guardDG = new ArrayList();
        rateDG = new ArrayList();
        this.firingTimeDependencyGraph = new Integer[this.numberOfTransitions][];
        this.guardDependencyGraph = new Integer[this.numberOfTransitions][];
        this.rateDependencyGraph = new Integer[this.numberOfTransitions][];
        for (int i=0;i<this.store.getNumberOfVariables();i++) {
            varGuard.add(new ArrayList());
            varRate.add(new ArrayList());
            varTime.add(new ArrayList());
        }
        
        for (int i=0;i<this.numberOfTransitions;i++) {
            timeDG.add(new ArrayList());
            guardDG.add(new ArrayList());
            rateDG.add(new ArrayList());
        }
        //filling basic info
        for (int id=0;id<this.numberOfTransitions;id++) {
            vars = this.transitions[id].getGuardVariables();
            for (Integer v : vars)
                varGuard.get(v).add(id);
            vars = this.transitions[id].getRateVariables();
            for (Integer v : vars)
                varRate.get(v).add(id);
            vars = this.transitions[id].getNextFiringTimeVariables();
            for (Integer v : vars)
                varTime.get(v).add(id);
            vars = this.transitions[id].getUpdatedVariables();
            transUpdate.add(vars);

//            System.out.println("Transition " + id);
//            System.out.println("updated vars: " + transUpdate.get(id).toString());
        }


//        for (int i=0;i<this.store.getNumberOfVariables();i++) {
//            System.out.println("Transitions depending on var " + i);
//            System.out.println("guards: " + varGuard.get(i).toString());
//            System.out.println("rates: " + varRate.get(i).toString());
//            System.out.println("firing times: " + varTime.get(i).toString());
//        }


        //construct dependency graphs. for each transition id, and for each updated variable
        //add to the dep graph all the transitions depending on that var.
        for (int id=0;id<this.numberOfTransitions;id++) {
            vars = transUpdate.get(id);
            for (Integer v : vars) {
                trans = varGuard.get(v);
                for (Integer t : trans)
                    if (!guardDG.get(id).contains(t))
                        guardDG.get(id).add(t);
                trans = varRate.get(v);
                for (Integer t : trans)
                    if (alwaysComputeRateAfterGuard) {
                        if (!guardDG.get(id).contains(t) && !rateDG.get(id).contains(t))
                            rateDG.get(id).add(t);
                    }
                    else if (!rateDG.get(id).contains(t))
                        rateDG.get(id).add(t);
                trans = varTime.get(v);
                for (Integer t : trans)
                    if (alwaysComputeRateAfterGuard) {
                        if (!guardDG.get(id).contains(t) && !timeDG.get(id).contains(t))
                            timeDG.get(id).add(t);
                    } else if (!timeDG.get(id).contains(t))
                            timeDG.get(id).add(t);
            }
//            System.out.println("Dependency graph for transition " + id);
//            System.out.println("guards: " + guardDG.get(id).toString());
//            System.out.println("rates: " + rateDG.get(id).toString());
//            System.out.println("firing times: " + timeDG.get(id).toString());
        }
        for (int id=0;id<this.numberOfTransitions;id++) {
            this.guardDependencyGraph[id] = guardDG.get(id).toArray(new Integer[0]);
            this.rateDependencyGraph[id] = rateDG.get(id).toArray(new Integer[0]);
            this.firingTimeDependencyGraph[id] = timeDG.get(id).toArray(new Integer[0]);
        }
    }
     /**
     * Constructs the dependency graphs for delayed updates;
     */
    private void generateDependencyGraphsAfterDelay() {
        //initialization
        ArrayList<ArrayList<Integer>> varGuard,varRate,varTime,transUpdate,timeDG,guardDG,rateDG;
        ArrayList<Integer> vars,trans;
        varGuard = new ArrayList();
        varRate = new ArrayList();
        varTime = new ArrayList();
        timeDG = new ArrayList();
        guardDG = new ArrayList();
        rateDG = new ArrayList();
        this.firingTimeDelayDependencyGraph = new Integer[this.numberOfTransitions][];
        this.guardDelayDependencyGraph = new Integer[this.numberOfTransitions][];
        this.rateDelayDependencyGraph = new Integer[this.numberOfTransitions][];
        for (int i=0;i<this.store.getNumberOfVariables();i++) {
            varGuard.add(new ArrayList());
            varRate.add(new ArrayList());
            varTime.add(new ArrayList());
        }
        transUpdate = new ArrayList();
        for (int i=0;i<this.numberOfTransitions;i++) {
            timeDG.add(new ArrayList());
            guardDG.add(new ArrayList());
            rateDG.add(new ArrayList());
        }
        //filling basic info
        for (int id=0;id<this.numberOfTransitions;id++) {
            vars = this.transitions[id].getGuardVariables();
            for (Integer v : vars)
                varGuard.get(v).add(id);
            vars = this.transitions[id].getRateVariables();
            for (Integer v : vars)
                varRate.get(v).add(id);
            vars = this.transitions[id].getNextFiringTimeVariables();
            for (Integer v : vars)
                varTime.get(v).add(id);
            vars = this.transitions[id].getUpdatedVariablesAfterDelay();
            transUpdate.add(vars);
        }
        //construct dependency graphs. for each transition id, and for each updated variable
        //add to the dep graph all the transitions depending on that var.
        for (int id=0;id<this.numberOfTransitions;id++) {
            vars = transUpdate.get(id);
            for (Integer v : vars) {
                trans = varGuard.get(v);
                for (Integer t : trans)
                    if (!guardDG.get(id).contains(t))
                        guardDG.get(id).add(t);
                trans = varRate.get(v);
                for (Integer t : trans)
                    if (alwaysComputeRateAfterGuard) {
                        if (!guardDG.get(id).contains(t) && !rateDG.get(id).contains(t))
                            rateDG.get(id).add(t);
                    }
                    else if (!rateDG.get(id).contains(t))
                        rateDG.get(id).add(t);
                trans = varTime.get(v);
                for (Integer t : trans)
                    if (alwaysComputeRateAfterGuard) {
                        if (!guardDG.get(id).contains(t) && !timeDG.get(id).contains(t))
                            timeDG.get(id).add(t);
                    } else if (!timeDG.get(id).contains(t))
                            timeDG.get(id).add(t);
            }
        }
        for (int id=0;id<this.numberOfTransitions;id++) {
            this.guardDelayDependencyGraph[id] = guardDG.get(id).toArray(new Integer[0]);
            this.rateDelayDependencyGraph[id] = rateDG.get(id).toArray(new Integer[0]);
            this.firingTimeDelayDependencyGraph[id] = timeDG.get(id).toArray(new Integer[0]);
        }
    }


    /**
     *
     * @param transitionID
     * @return the list of transitions whose guard is modified by the execution of the
     * transition with specified ID
     */
    public Integer[] getListOfUpdatedGuards(int transitionID) {
        //if (!this.initialized)
        //    throw new ModelException("Model has not been initialized");
        return this.guardDependencyGraph[transitionID];
    }
    /**
     *
     * @param transitionID
     * @return the list of transitions whose rate is modified by the execution of the
     * transition with specified ID
     */
    public Integer[] getListOfUpdatedRates(int transitionID) {
        //if (!this.initialized)
        //    throw new ModelException("Model has not been initialized");
        return this.rateDependencyGraph[transitionID];
    }
    /**
     *
     * @param transitionID
     * @return the list of transitions whose firing time is modified by the execution of the
     * transition with specified ID
     */
    public Integer[] getListOfUpdatedFiringTimes(int transitionID) {
        //if (!this.initialized)
        //    throw new ModelException("Model has not been initialized");
        return this.firingTimeDependencyGraph[transitionID];
    }

    public Integer[] getListOfUpdatedFiringTimesAfterDelay(int transitionID) {
       return this.firingTimeDelayDependencyGraph[transitionID];
    }

    public Integer[] getListOfUpdatedGuardsAfterDelay(int transitionID) {
        return this.guardDelayDependencyGraph[transitionID];
    }

    public Integer[] getListOfUpdatedRatesAfterDelay(int transitionID) {
        return this.rateDelayDependencyGraph[transitionID];
    }




   /**
    * Raises an exception if a variable is not in the store
    * @param list a list of variable names
    * @return the ids of a list of variable names. Used for printing purposes
    */
    public ArrayList<Integer> getIdOfVariables(ArrayList<String> list) {
        ArrayList<Integer> ids = new ArrayList();
        for (String s : list)
            ids.add(store.getVariableID(s));
        return ids;
    }
    /**
     *
     * @return an array with the value of all variables of the store
     */
    public double[] getVariablesValues() {
        return store.getVariablesValues();
    }


  
    /**
     * 
     * @return the name of the model
     */
    public String getName() {
        return this.name;
    }
    /**
     * Changes the value of a variable. For batch purposes
     * @param var the name of the variable to modify.
     * @param value the new value
     */
    public void setValueOfVariable(String var, double value) {
        this.store.setVariableValue(this.store.getVariableID(var), value);
    }
     /**
     * Changes the value of a parameter. For batch purposes
     * @param param the name of the parameter to modify.
     * @param value the new value
     */
    public void setValueOfParameter(String param, double value) {
        this.store.setParameterValue(store.getParameterID(param), value);
    }

    /**
     * Changes irreversibly the value of a parameter
     * @param param
     * @param value
     */
    public void changeInitialValueOfParameter(String param, double value) {
        if (!store.getParametersReference().containsSymbol(param))
            throw new ModelException("Parameter " + param + " is not defined in the model.");
        if (store.getParametersReference().hasExpressionForInitialValue(param))
            throw new ModelException("Parameter " + param + " has a value defined by an expression and cannot be changed.");
        this.setValueOfParameter(param, value);
        initialValueOfParameters[store.getParameterID(param)] = value;
        this.computeInitialValues();
    }

    /**
     * Changes irreversibly the initial value of a variable
     * @param var
     * @param value
     */
    public void changeInitialValueOfVariable(String var, double value) {
        if (!store.getVariablesReference().containsSymbol(var))
            throw new ModelException("Variable " + var + " is not defined in the model.");
        if (store.getVariablesReference().hasExpressionForInitialValue(var))
            throw new ModelException("Variable " + var + " has a value defined by an expression and cannot be changed.");
        setValueOfVariable(var, value);
        initialValueOfVariables[store.getVariableID(var)] = value;
        this.computeInitialValues();
    }


    /**
     * Resets the model to its initial state
     */
    public void resetToInitialState() {
        this.resetToBasicInitialState();
        this.computeInitialValues();
    }

    public void computeInitialValues() {
        this.store.getParametersReference().evaluateAndStoreInitialValueExpressions();
        this.store.getVariablesReference().evaluateAndStoreInitialValueExpressions();
        //possibly recompute again parameters? be careful here, if you evaluate an expression
        //depending on a parameter or a variable, which has not been evaluated, then 
        //you get an error. Think how to improve this!!!
    }

    public void resetToBasicInitialState() {
        this.store.setAllVariableValues(initialValueOfVariables);
        this.store.setAllParameterValues(initialValueOfParameters);
    }

    public boolean containsExplorableParameter(String name) {
        return this.store.getParametersReference().containsSymbol(name)
                && !this.store.getParametersReference().hasExpressionForInitialValue(name);
    }

    public boolean containsExplorableVariable(String name) {
        return this.store.getVariablesReference().containsSymbol(name)
                && !this.store.getVariablesReference().hasExpressionForInitialValue(name);
    }

   





    public Predicate[] getTransitionDelayedGuards() {
        Predicate[] p = new Predicate[this.numberOfTransitions];
        for (int i=0;i<numberOfTransitions;i++)
            p[i] = transitions[i].getDelayedGuardPredicate();
        return p;
    }

    public AtomicReset[][] getTransitionDelayedResets() {
        AtomicReset[][] r = new AtomicReset[this.numberOfTransitions][];
        for (int i=0;i<numberOfTransitions;i++)
            r[i] = transitions[i].getDelayedResetList();
        return r;
    }

    public Function[] getTransitionDelays() {
        Function[] f = new Function[this.numberOfTransitions];
        for (int i=0;i<numberOfTransitions;i++)
            f[i] = transitions[i].getDelayFunction();
        return f;
    }

    public String[] getTransitionEvents() {
       String[] e = new String[this.numberOfTransitions];
       for (int i=0;i<numberOfTransitions;i++)
            e[i] = transitions[i].getEvent();
       return e;
    }

    public Predicate[] getTransitionGuards() {
        Predicate[] p = new Predicate[this.numberOfTransitions];
        for (int i=0;i<numberOfTransitions;i++)
            p[i] = transitions[i].getGuardPredicate();
        return p;
    }

    public Function[] getTransitionRates() {
        Function[] f = new Function[this.numberOfTransitions];
        for (int i=0;i<numberOfTransitions;i++)
            f[i] = transitions[i].getRateFunction();
        return f;
    }

    public AtomicReset[][] getTransitionResets() {
        AtomicReset[][] r = new AtomicReset[this.numberOfTransitions][];
        for (int i=0;i<numberOfTransitions;i++)
            r[i] = transitions[i].getResetList();
        return r;
    }

    public Function[] getTransitionTimedActivations() {
        Function[] f = new Function[this.numberOfTransitions];
        for (int i=0;i<numberOfTransitions;i++)
            f[i] = transitions[i].getTimedActivationFunction();
        return f;
    }

    public TType[] getTransitionTypes() {
        TType[] t = new TType[this.numberOfTransitions];
        for (int i=0;i<numberOfTransitions;i++)
            t[i] = transitions[i].getType();
        return t;
    }

    public boolean[] getTransitionStoppingStatus() {
        boolean [] s = new boolean[this.numberOfTransitions];
        for (int i=0;i<numberOfTransitions;i++)
            s[i] = transitions[i].isStopping();
        return s;
    }

    public boolean[] getTransitionStoppingStatusAfterDelay() {
        boolean [] s = new boolean[this.numberOfTransitions];
        for (int i=0;i<numberOfTransitions;i++)
            s[i] = transitions[i].isStoppingAfterDelay();
        return s;
    }

    public Store getStore() {
        return this.store;
    }

    public void changeVariableValue(String name, double newValue) {
        store.setVariableValue(store.getVariableID(name), newValue);
    }
    public void changeParameterValue(String name, double newValue) {
        store.setParameterValue(store.getParameterID(name), newValue);
    }

    public Transition getTransition(int transitionID) {
        if (transitionID < 0 || transitionID >= this.numberOfTransitions)
            throw new IndexOutOfBoundsException("Transition " + transitionID + " " +
                    "does not exist");
        return transitions[transitionID];
    }




    public String toModelLanguage() {
        //We have to reset the model to initial state to properly save
        //initial values of variables.
        this.resetToInitialState();
        ArrayList<String> names;
        String s = "";
        s += "model " + this.name + " {\n\n";
        names = store.getNameOfAllVariables();
        for (String n : names)
            s += "\t" + n + " = " + store.getVariableValue(store.getVariableID(n)) + ";\n";
        names = store.getNameOfAllParameters();
        for (String n : names)
            s += "\t" + "param " + n + " = " + store.getParameterValue(store.getParameterID(n)) + ";\n";
        s += "\n";
        names = store.getNameOfAllExpressionVariables();
        for (String n : names)
            s += "\t" + "expression " + n + " = " + store.getExpressionVariablesReference().getExpression(store.getExpressionVariableID(n)).toString() + ";\n";
        s += "\n";
        names = store.getFunctionDefinitions();
        for (String n : names)
            s += "\t" + "function " + removeInitialAndFinalSpaces(n) + ";\n";
        s += "\n";
        for (Transition t : this.transitions)
            s += "\t" + t.toModelLanguage(store);
        s += "}\n";
        return s;
    }

    private String removeInitialAndFinalSpaces(String s) {
        char[] c = new char[s.length()];
        s.getChars(0, s.length(), c, 0);
        int b=0,e=c.length-1;
        while (c[b] == ' ' || c[b] == '\t')
            b++;
        while (c[e] == ' ' || c[e] == '\t')
            e--;
        int l = e - b + 1;
        return String.copyValueOf(c, b, l);
    }

    public void saveToFile(String filename) {
        java.io.PrintWriter p;

        try {
            p = new java.io.PrintWriter(filename);
            String s = toModelLanguage();
            p.print(s);
            p.close();
        }
        catch(java.io.IOException e) {
            System.err.println("Error while saving model to file: " + e);
        }
    }

    public boolean containsDelayedTransitions() {
        for (Transition t : this.transitions)
            if (t.isDelayed())
                return true;
        return false;
    }

    public boolean containsInstantaneousTransitions() {
        for (Transition t : this.transitions)
            if (t.getType() == TType.INSTANTANEOUS)
                return true;
        return false;
    }

    public boolean containsTimedTransitions() {
        for (Transition t : this.transitions)
            if (t.getType() == TType.TIMED)
                return true;
        return false;
    }

    public boolean containsGuardedTransitions() {
        for (Transition t : this.transitions)
            if (!t.getGuardPredicate().isTautology())
                return true;
        return false;
    }

    public boolean containsContinuousGuardedTransitions(boolean force) {
        if (force) {
            for (Integer j : this.continuouslyApproximableTransitionID)
                if (!transitions[j].getGuardPredicate().isTautology())
                    return true;
        }
        else {
            for (Integer j : this.continuousTransitionID)
                if (!transitions[j].getGuardPredicate().isTautology())
                    return true;
        }
        return false;
    }

    public boolean containsStochasticGuardedTransitions(boolean force) {
        for (Integer j : this.stochasticTransitionID)
            if (!transitions[j].getGuardPredicate().isTautology())
                return true;
        if (force) {
            for (Integer j : this.hybridTransitionID)
                if (!transitions[j].getGuardPredicate().isTautology())
                    return true;
            for (Integer j : this.continuousTransitionID)
                if (!transitions[j].getGuardPredicate().isTautology())
                    return true;
        }
        return false;
    }



    

    public boolean containsNonContinuouslyApproximableStochasticTransitions() {
        return (this.getNumberOfStochasticTransitions(true) != this.numberOfContinuouslyApproximableTransitions);
    }

    public boolean containsOnlyContinuousTransitions(boolean force) {
        if (force)
            return (this.numberOfTransitions == this.numberOfContinuouslyApproximableTransitions);
        else
            return (this.numberOfTransitions == this.numberOfContinuousTransitions);
    }

    public boolean containsOnlyStochasticTransitions(boolean forceStochastic) {
        if (forceStochastic)
            return (this.numberOfTransitions == this.getNumberOfStochasticTransitions(true));
        else
            return (this.numberOfTransitions == this.numberOfStochasticTransitions);
    }



    public int getAverageNumberOfTransitionsToUpdate() {
        int sum = 0;
        for (int i=0;i<this.numberOfTransitions;i++)
            sum += this.rateDependencyGraph[i].length + this.guardDependencyGraph[i].length;
        return sum/numberOfTransitions + (sum % numberOfTransitions > 0 ? 1 : 0);
    }

    public void recomputeDependencyGraphs(boolean computeRateAfterGuard) {
        this.alwaysComputeRateAfterGuard = computeRateAfterGuard;
        this.generateDependencyGraphs();
        this.generateDependencyGraphsAfterDelay();
        this.alwaysComputeRateAfterGuard = GlobalOptions.alwaysComputeRateAfterGuard;
    }

      public void recomputeDependencyGraphs() {
        this.generateDependencyGraphs();
        this.generateDependencyGraphsAfterDelay();
    }

    public boolean isTemplate(String  name) {
        return this.templates.containsKey(name);
    }

    public void addTemplate(String name, FlatTemplate template) {
        if (this.templates.containsKey(name))
            throw new ModelException("Template " + name + " already defined");
        this.templates.put(name, template);
    }

    public FlatTemplate getTemplateDefinition(String name) {
        if (!this.templates.containsKey(name))
            throw new ModelException("Template " + name + " has not been defined");
        return this.templates.get(name);
    }
    

    /**
     * Exports the model to a SBML model of level 2v4
     * @return 
     */
    public SBMLDocument exportToSBML() {
        return exportToSBML(2,4);
    }
    
    //nella definizione di reazioni, devi trovare tutte le variabili
    //che sono usate in kinetic law, e rendere modifier tutte quelle che non sono 
    //reactant o product.
    //usa una lista per salvare variabili reactant product, ecc, 
    //poi prendi tutte var della kinetic function
    //e se qualcuna non e' buona rendila modifier.
    
    
    
    /**
     * Exports the model to a SBML model
     * @param level the SBML level
     * @param version the SBML version
     * @return An SBMLDocument file
     */
    public SBMLDocument exportToSBML(int level, int version) {
        if (level != 2 && level != 3)
            throw new ModelException("Can export only to SBML level 2 or level 3");
        if (level == 3 && version != 1)
            throw new ModelException("Can export only to SBML level 3 version 1");
        if (level == 2 && ( version < 1 || version > 4))
            throw new ModelException("Can export only to SBML level 2 versions 1 to 4");
        //pre checks. Cannot support delayed stochastic transitions
        if (this.containsDelayedTransitions())
            throw new ModelException("Cannot export to SBML a model with stochasticaly delayed transitions");
        if (!store.getFunctionDefinitions().isEmpty())
            throw new ModelException("Cannot export to SBML a model containing user-defined functions");
        if (this.containsNonContinuouslyApproximableStochasticTransitions())
            throw new ModelException("Cannot export to SBML a model containing stochastic transitions not continuously approximable.");
        
        SBMLDocument sbmlDoc = new org.sbml.jsbml.SBMLDocument(level, version);
        sbmlDoc.createModel(name);
        org.sbml.jsbml.Model model = sbmlDoc.getModel();
        model.setName(name);
        Compartment comp = new Compartment("main");
        comp.setName("main");
        comp.setConstant(true);
        comp.setValue(1.0);
        comp.setUnits(Unit.Kind.DIMENSIONLESS);
        model.addCompartment(comp);
        
        //adding species, storing them in an array
        int n = store.getNumberOfVariables();
        Species[] species = new Species[n];
        SymbolArray symbols = store.getVariablesReference();
        for (int i=0;i<n;i++) {
            Species s = model.createSpecies(symbols.getName(i));
            //s.setUnits(Unit.Kind.DIMENSIONLESS);
            s.setSubstanceUnits(Unit.Kind.DIMENSIONLESS);
            s.setCompartment(comp);
            if (symbols.hasExpressionForInitialValue(i)) {
                ASTNode m = symbols.getInitialValueExpression(i).convertToJSBML();
                InitialAssignment ass = model.createInitialAssignment();
                ass.setMath(m);
                ass.setVariable(s);
            } else
                s.setInitialAmount(symbols.getValue(i));
            species[i] = s;
        }
        
        //adding parameters, storing them in an array
        n = store.getNumberOfParameters();
        Parameter[] params = new Parameter[n];
        symbols = store.getParametersReference();
        for (int i=0;i<n;i++) {
            Parameter p = model.createParameter(symbols.getName(i));
            p.setUnits(Unit.Kind.DIMENSIONLESS);
            p.setConstant(true);
            if (symbols.hasExpressionForInitialValue(i)) {
                ASTNode m = symbols.getInitialValueExpression(i).convertToJSBML();
                InitialAssignment ass = model.createInitialAssignment();
                ass.setMath(m);
                ass.setVariable(p);
            } else
                p.setValue(symbols.getValue(i));
            params[i] = p;
            
        }        
        
        //adding expressions shorthands
        n = store.getNumberOfExpressionVariables();
        Parameter[] exprs = new Parameter[n];
        ExpressionSymbolArray esymbols = store.getExpressionVariablesReference();
        for (int i=0;i<n;i++) {
            Parameter p = model.createParameter(esymbols.getName(i));
            p.setUnits(Unit.Kind.DIMENSIONLESS);
            p.setConstant(false);
            ASTNode m = esymbols.getExpression(i).convertToJSBML();
            AssignmentRule r = model.createAssignmentRule();
            r.setMath(m);
            r.setVariable(p);
            //p.setValue(esymbols.getValue(i));          
            exprs[i] = p;
        }
        
        int counter  = 1;
        
        //defining reactions from continuously approximable transitions
        int[] trans =  this.getListOfContinuousTransitionID(true);
        for (Integer i : trans) {
            Transition t = transitions[i];
            ASTNode m, m1, m2;
            m1 = t.getRateFunction().convertToJSBML();
            //defining a piecewise function if there is a guard
            if (!t.getGuardPredicate().isTautology()) {
                m2 = t.getGuardPredicate().convertToJSBML();
                ASTNode m0 = new ASTNode(ASTNode.Type.INTEGER);
                m0.setValue(0);
                m = new ASTNode(ASTNode.Type.FUNCTION_PIECEWISE);
                m.addChild(m1);
                m.addChild(m2);
                m.addChild(m0);
            } else m = m1;
            KineticLaw k = new KineticLaw();
            k.setMath(m);
            Reaction reac = model.createReaction(t.getEvent() + "_" + (counter++));
            reac.setKineticLaw(k);
            reac.setReversible(false);
            ArrayList<Integer> addedSpecies = new ArrayList<Integer>();
            for (AtomicReset ar : t.getResetList()) {
                int j = ar.getUpdatedVariables().get(0);
                double st = ar.getConstantIncrement();
                boolean product = st > 0;
                boolean reactant = st < 0;
                if (product) {
                    SpeciesReference ref = reac.createProduct(species[j]);
                    ref.setStoichiometry(st);
                } else if (reactant) {
                    SpeciesReference ref = reac.createReactant(species[j]);
                    ref.setStoichiometry(-st);
                } else {
                    reac.createModifier(species[j]);
                }
                addedSpecies.add(j);
            }
            //adds as modifiers all species in the kinetic rate but not added yet.
            ArrayList<Integer> allKineticSpecies = new ArrayList<Integer>();
            allKineticSpecies.addAll(t.getRateVariables());
            allKineticSpecies.addAll(t.getGuardVariables());
            for (Integer j : allKineticSpecies) {
                if (!addedSpecies.contains(j)) {
                    reac.createModifier(species[j]);
                    addedSpecies.add(j);
                }
            }
        }
        
        
        //define untimed events
        trans =  this.getListOfInstantaneousTransitionsID();
        for (Integer i : trans) {
            Transition t = transitions[i];
            Event ev = model.createEvent(t.getEvent() + "_" + (counter++));
            ASTNode m = t.getGuardPredicate().convertToJSBML();
            Trigger tr = ev.createTrigger();
            tr.setMath(m);
            if (level == 3) {
                m = t.getRateFunction().convertToJSBML();
                ev.createPriority(m);
            }
            for (AtomicReset ar : t.getResetList()) {
                int j = ar.getUpdatedVariables().get(0);
                m = ar.convertToJSBML();
                ev.createEventAssignment(species[j], m);
            }
        }

        //define timed events
        trans =  this.getListOfTimedTransitionsID();
        for (Integer i : trans) {
            Transition t = transitions[i];
            Event ev = model.createEvent(t.getEvent() + "_" + (counter++));
            //create time trigger
            ASTNode m1 = t.getGuardPredicate().convertToJSBML();
            ASTNode m2 = t.getTimedActivationFunction().convertToJSBML();
            ASTNode mt = new ASTNode(ASTNode.Type.NAME_TIME);
            ASTNode m0 = ASTNode.geq(mt, m2);
            ASTNode m = new ASTNode(ASTNode.Type.LOGICAL_AND);
            m.addChild(m0);
            m.addChild(m1);
            Trigger tr = ev.createTrigger();
            tr.setMath(m);
            //create priority 1
            if (level == 3) {
                m = new ASTNode(ASTNode.Type.INTEGER);
                m.setValue(1);
                ev.createPriority(m);
            }
            for (AtomicReset ar : t.getResetList()) {
                int j = ar.getUpdatedVariables().get(0);
                m = ar.convertToJSBML();
                ev.createEventAssignment(species[j], m);
            }
        }
        return sbmlDoc;  
    }
    
    
    
    /**
     * Exports the model to SBML l2v4 and saves it to a file
     * @param filename  the name of the file to export to
     */
    public void exportToSBML(String filename) {
        this.exportToSBML(2, 4, filename);
    }
    
    /**
     * Exports the model to SBML and saves it to a file
     * @param level the SBML level
     * @param version the SBML version
     * @param filename  the name of the file to export to
     */
    public void exportToSBML(int level, int version, String filename) {
        try {
            SBMLWriter wr = new SBMLWriter();
            SBMLDocument doc = this.exportToSBML(level, version);
            java.io.File file = new java.io.File(filename);
            wr.write(doc, file);
        } catch (Exception e) {
            throw new ModelException("Cannot export model to SBML: " + e);
        }
    }    
    
    
    /**
     * Converts the model to a SBML model and returns its String representation
     * @param level the SBML level
     * @param version the SBML version
     * @return An SBMLDocument file
     */
    public String convertToSBML(int level, int version) {
        SBMLDocument doc = this.exportToSBML(level, version);
        String s = doc.toString();
        return s;
    }
    
    
    public ArrayList<String> getOriginalModelVariables() {
        return this.modelVariables;
    }
    
    /**
     * Clones the model, creating a new initializes copy of it!
     */
    @Override
    public FlatModel clone() {
        Store newStore = this.store.clone();
        FlatModel newModel = new FlatModel(this.name);
        for (Transition t : this.transitions) {
            Transition tnew = t.clone(newStore);
            newModel.addTransition(t);
        }
        newModel.setStore(newStore);
        newModel.initialValueOfVariables = this.initialValueOfVariables;
        newModel.initialValueOfParameters = this.initialValueOfParameters;
        newModel.finalizeInitialization();
        return newModel;
    }
    
    
    /**
     * Return true if the model is set up for linear noise approximation.     
     * @return t
     */
    public boolean isLinearNoiseApproximation() {
        return false;
    }
    
    
       
    
    /**
     * this function prepares the model for linear noise approximation.
     * Notice that the modification is irreversible. A better strategy is to clone the 
     * model first, this has to be implemented yet.
     * 
     * We basically add variance and covariance variables.
     */
    public FlatModel generateLinearNoiseApproximation() {
        Store newStore = this.store.clone();
        LinearNoiseFlatModel newModel = new LinearNoiseFlatModel(this.name);
        int n = store.getNumberOfVariables();
        int c = 0;
        for (int i=0;i<n;i++) {
            String x = this.modelVariables.get(i);
            for (int j=i;j<n;j++) {
                String y = this.modelVariables.get(j);
                String var = (i==j ? "var."+x : "cov."+x+"."+y);
                newStore.addVariable(var, 0.0);
                c++;
            }
        }
        newStore.finalizeVariableInitialization();
        for (Transition t : this.transitions) {
            Transition tnew = t.clone(newStore);
            newModel.addTransition(tnew);
        }
        newModel.setStore(newStore);
        initialValueOfVariables = java.util.Arrays.copyOf(initialValueOfVariables, n + c);
        newModel.initialValueOfParameters = this.initialValueOfParameters;
        newModel.modelVariables = (ArrayList<String>)this.modelVariables.clone();
        newModel.finalizeInitialization();
        newModel.initializeJacobian();
        return newModel;        
    }

    
    public String exportToMatlab(String filename) {
        //pre checks. Cannot support delayed stochastic transitions
        if (this.containsDelayedTransitions())
            throw new ModelException("Cannot export to Matlab a model with stochastically delayed transitions");
        if (this.containsInstantaneousTransitions() || this.containsTimedTransitions())
            throw new ModelException("Cannot export to Matlab a model containing instantaneous or timed transitions");
        if (this.containsNonContinuouslyApproximableStochasticTransitions())
            throw new ModelException("Cannot export to SBML a model containing stochastic transitions not continuously approximable.");
        
        String s = "% m file generated from model " + this.name + "\n";
        s += "function [T,Y] = " + filename + "(tf,points)\n";
        s += "if(nargin==0)\n\ttf=100; points=1000;\nelseif (nargin == 1)\n\tpoints = 1000;\nend;";
        s += "\n\n%model parameters\n";
        SymbolArray params = this.store.getParametersReference();
        SymbolArray vars = this.store.getVariablesReference();
        for (int i=0;i<params.getNumberOfSymbols();i++) {
            s += "par("+ (i+1) + ") = " + 
                    ( params.hasExpressionForInitialValue(i) ? 
                      params.getInitialValueExpression(i).toMatlabCode() :
                      params.getValue(i)) +
                    ";  % " + params.getName(i) + "\n";
        }
        s += "\n\n%model variables\n";
        for (int i=0;i<vars.getNumberOfSymbols();i++) {
            s += "var("+ (i+1) + ") = " + 
                    ( vars.hasExpressionForInitialValue(i) ? 
                      vars.getInitialValueExpression(i).toMatlabCode() :
                      vars.getValue(i)) +
                    ";  % " + vars.getName(i) + "\n";
        }
        
        s += "\n\n%defined function\n";
        s += store.functionDefinitionsToMatlabCode(); 
        
        s += "\n\n%ode function\n";
        s += "function dx = odefun(t,var)\n";
        s += store.expressionDefinitionsToMatlabCode();
        String[][] S = new String[this.store.getNumberOfVariables()][this.numberOfContinuouslyApproximableTransitions];        
        for (int i=0;i<this.store.getNumberOfVariables();i++) {
            for (int j=0;j<this.numberOfContinuouslyApproximableTransitions;j++) {
                S[i][j] = "0";
            }
        }      
        int[] tID = continuouslyApproximableTransitionID;
        s += "\nf = [ ";
        for (int j=0;j<tID.length;j++) {
            Transition t = this.getTransition(tID[j]);
            String f = "\t" + ( t.getGuardPredicate().isTautology() ? "" : t.getGuardPredicate().toMatlabCode() + " * ");
            f += t.getRateFunction().toMatlabCode() +  (j<tID.length-1 ? ";\n" : "];\n");
            s += f;
            AtomicReset[] r = t.getResetList();
            for (int i=0;i<r.length;i++) {
                ArrayList<Integer> l = r[i].getUpdatedVariables();
                for (Integer k : l) {
                    S[k][j] = r[i].toMatlabCode();
                }
            }
        }
        s+= "\nS = [ ";
        for (int i=0;i<this.store.getNumberOfVariables();i++) {
            s += "\t";
            for (int j=0;j<this.numberOfContinuouslyApproximableTransitions;j++) {
                s += (j > 0  ? ", "  : "") + S[i][j];
            }
            s += (i<this.store.getNumberOfVariables()-1 ? ";\n" : " ];\n");
        }        
        s += "\n";
        s += "dx = S*f;\n";
        s += "end\n";
        s += "\n\n%solving ode\n";
        s += "delta = tf/points;\n";
        s += "tspan = 0:delta:tf;\n";
        s += "[T,Y] = ode45(@odefun,tspan,var);\n";
        s += "\nend\n";
        return s;
    }
    
    /**
     * Constructs the linear noise approximation and exports it as a matlab function
     * @param filename
     * @return 
     */
    public String exportToMatlabLinearNoise(String filename) {
        FlatModel lnmodel = this.generateLinearNoiseApproximation();    
        return lnmodel.exportToMatlab(filename);
    }
    
}
