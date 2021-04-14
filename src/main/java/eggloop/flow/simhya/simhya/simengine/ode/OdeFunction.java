/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine.ode;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.model.transition.AtomicReset;
import eggloop.flow.simhya.simhya.model.store.Function;
import eggloop.flow.simhya.simhya.model.store.Predicate;
import eggloop.flow.simhya.simhya.simengine.GuardChecker;
import eggloop.flow.simhya.simhya.simengine.Stoichiometry;

/**
 *
 * @author luca
 */
public abstract class OdeFunction implements FirstOrderDifferentialEquations, GuardChecker {
    FlatModel model;

    Function [] rate;
    Predicate [] guard;
    Stoichiometry[][] stoich;
    boolean [] hasNonTrivialFlow;
    int[] transitions;
    int dimension;
    int continuousTransitions;
    int totalTransitions;

    boolean useCache;
    
    OdeFunction() {
        return;
    }

    public OdeFunction(FlatModel model) {
        this.model = model;
        this.dimension = model.getStore().getVariablesReference().getNumberOfSymbols();
        this.continuousTransitions = model.getNumberOfContinuousTransitions(true);
        this.totalTransitions = model.getNumberOfTransitions();
        rate = model.getTransitionRates();
        guard = model.getTransitionGuards();
        stoich = new Stoichiometry[totalTransitions][];
        transitions = model.getListOfContinuousTransitionID(true);
        hasNonTrivialFlow = new boolean[dimension];
        java.util.Arrays.fill(hasNonTrivialFlow, false);
        AtomicReset[][] resets = model.getTransitionResets();
        for (Integer j : transitions) {
            stoich[j] = new Stoichiometry[resets[j].length];
            for (int i=0;i<resets[j].length;i++) {
                int k = resets[j][i].getUpdatedVariables().get(0);
                stoich[j][i] = new Stoichiometry(k,
                        resets[j][i].getConstantIncrement());
                hasNonTrivialFlow[k] = true;
            }
        }
        this.model.getStore().getVariablesReference().setContinuousEvolutionReference(hasNonTrivialFlow);
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

   
    
    
   

    public int getDimension() {
        return this.dimension;
    }


    public boolean evolvesContinuously(Predicate p) {
        boolean answer = false;
        for (Integer j : p.getVariableList()) {
            answer = answer || hasNonTrivialFlow[j];
        }
        return answer;
    }

    public boolean evolvesContinuously(Function f) {
        boolean answer = false;
        for (Integer j : f.getVariableList()) {
            answer = answer || hasNonTrivialFlow[j];
        }
        return answer;
    }


}

