/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine.ode;

import org.apache.commons.math.ode.DerivativeException;
import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import java.util.HashSet;
import eggloop.flow.simhya.simhya.simengine.Stoichiometry;

/**
 * This is the ODE function for a model in which all transitions are
 * continuously approximable but possibly guarded.
 * WARNING: there is no guarantee of error bounds as the solvers are not
 * for piecewise smooth systems.
 * @author luca
 */
public class GuardedOdeFunction  extends OdeFunction {
    SymbolArray vars;
    boolean [] enabled;
    HashSet<Integer> evaluatedTransitions;

    public GuardedOdeFunction(FlatModel model) {
        super(model);
        //sets up the SymboArray dats structure for evaluation of flows
        vars = SymbolArray.getNewFastEvalSymbolArray(this.dimension);
        enabled = new boolean[model.getNumberOfTransitions()];
        for (int i=0;i<this.continuousTransitions;i++) {
            int j = this.transitions[i];
            enabled[j] = guard[j].evaluate() || guard[j].changeContinuously();
        }
        this.evaluatedTransitions = new HashSet<Integer>();
        for (int i=0;i<this.continuousTransitions;i++) {
            int j = this.transitions[i];
            this.evaluatedTransitions.add(j);
        }
    }

    public void updateGuardStatus(int firedTransition, double t, double[] y) {
        Integer[] depGuards = model.getListOfUpdatedGuards(firedTransition);
        vars.setValuesReference(y);
        for (Integer i : depGuards)
            if (this.evaluatedTransitions.contains(i))
                 enabled[i] = (useCache ? guard[i].evaluateCache(vars) : guard[i].evaluate(vars)) 
                         || guard[i].changeContinuously(vars);
    }

    
    
    public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
        //resets derivatives to zero
        java.util.Arrays.fill(yDot, 0.0);
        //assign correct value to variables
        vars.setValuesReference(y);
        for (int i=0;i<this.continuousTransitions;i++) {
            int j = this.transitions[i];
            if (enabled[j]) {
                boolean guardStatus = useCache ? guard[j].evaluateCache(vars) :
                                                guard[j].evaluate(vars);
                if (guardStatus) {
                    double rateValue = useCache ? rate[j].computeCache(vars) :
                                          rate[j].compute(vars);
                    for (Stoichiometry s : stoich[j])
                        yDot[s.variable] += s.coefficient * rateValue;
                }
            }
        }
        //sanity check
        for (int i=0;i<yDot.length;i++) {
            if (Double.isInfinite(yDot[i]) || Double.isNaN(yDot[i]))
                throw new DerivativeException("Variable " + i + " has infinite or NaN derivative");
        }
    }
}
