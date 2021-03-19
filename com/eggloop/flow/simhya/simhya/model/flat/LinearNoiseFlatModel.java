/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.model.flat;
import java.util.ArrayList;

import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;
import com.eggloop.flow.simhya.simhya.model.ModelException;
import com.eggloop.flow.simhya.simhya.simengine.Stoichiometry;
import com.eggloop.flow.simhya.simhya.model.store.Function;
import com.eggloop.flow.simhya.simhya.model.transition.*;


/**
 *
 * @author Luca
 */
public class LinearNoiseFlatModel extends FlatModel {
    /*
     * Introduci liste di liste di derivata + elenco di coppie stoich - come funzione. Implementa derivate in funzione
     * E implementa il fatto che derivata zero diventa costante zero e che posso controllarlo.
     */
    
    Function[][] jacobianFunctions;
    Stoichiometry[][] jacobianStoichiometry;
    int dimension;
    
    
    
    LinearNoiseFlatModel(String name) {
        super(name);
        this.jacobianFunctions = null;
        this.jacobianStoichiometry = null;
        this.dimension = 0;
    }
    
    void initializeJacobian() {
        this.dimension = this.modelVariables.size();
        jacobianFunctions = new Function[numberOfContinuouslyApproximableTransitions][this.modelVariables.size()];
        jacobianStoichiometry = new Stoichiometry[numberOfContinuouslyApproximableTransitions][];
        for (int i=0;i<this.numberOfContinuouslyApproximableTransitions;i++) {
            Transition t = this.transitions[this.continuouslyApproximableTransitionID[i]];
            for (int j=0;j<this.modelVariables.size();j++) {
                String x = this.modelVariables.get(j);
                jacobianFunctions[i][j] = t.getRateFunction().differentiate(x);
            }
            AtomicReset[] res = t.getResetList();    
            jacobianStoichiometry[i] = new Stoichiometry[res.length];
            for (int j=0;j<res.length;j++) {
                int k = res[j].getUpdatedVariables().get(0);
                jacobianStoichiometry[i][j] = new Stoichiometry(k,res[j].getConstantIncrement());
            }
        }
        //System.out.println(printJacobian());
    }
    
    
    public String printJacobian() {
        String s = "";
        int n = numberOfContinuouslyApproximableTransitions;
        int m = modelVariables.size();
        for (int i=0;i<m;i++) {
            for (int j=0;j<m;j++) {
                String w =  "";
                for (int k=0;k<n;k++) {
                    for (int h=0;h<jacobianStoichiometry[k].length;h++)
                        if (jacobianStoichiometry[k][h].variable == i && !jacobianFunctions[k][j].isConstantZero())
                            w += (w.equals("") ? "" : " + " ) + "(" +
                                    jacobianStoichiometry[k][h].coefficient + ")*( " +
                                    jacobianFunctions[k][j].toModelLanguage() + ")";
                }
                if (w.equals("")) w = "0";
                s += String.format("%50s", w);
            }
            s += "\n";
        }
        return s;
    }
    
    public Function[][] getJacobianFunctions() {
        return this.jacobianFunctions;
    } 
    
    public Stoichiometry[][] getJacobianStoichiometry() {
        return this.jacobianStoichiometry;
    }

    @Override
    public boolean isLinearNoiseApproximation() {
        return true;
    }
    
   @Override 
   public String exportToMatlab(String filename) {
        //pre checks. Cannot support delayed stochastic transitions
        if (this.containsDelayedTransitions())
            throw new ModelException("Cannot export to Matlab a model with stochastically delayed transitions");
        if (this.containsInstantaneousTransitions() || this.containsTimedTransitions())
            throw new ModelException("Cannot export to Matlab a model containing instantaneous or timed transitions");
        if (this.containsNonContinuouslyApproximableStochasticTransitions())
            throw new ModelException("Cannot export to SBML a model containing stochastic transitions not continuously approximable.");
        
        String s = "% m file generated from linear noise model " + this.name + "\n";
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
        String[][] S = new String[this.dimension][this.numberOfContinuouslyApproximableTransitions];        
        for (int i=0;i<this.dimension;i++) {
            for (int j=0;j<this.numberOfContinuouslyApproximableTransitions;j++) {
                S[i][j] = "0";
            }
        }
        s += "\nn = " + dimension + ";\n";
        s += "m = " + numberOfContinuouslyApproximableTransitions + ";\n";
        s += "code = @(i,j)(n*i - (i-1)*(i-2)/2 + j - i + 1);\n";
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
        for (int i=0;i<this.dimension;i++) {
            s += "\t";
            for (int j=0;j<this.numberOfContinuouslyApproximableTransitions;j++) {
                s += (j > 0  ? ", "  : "") + S[i][j];
            }
            s += (i<this.dimension-1 ? ";\n" : " ];\n");
        }        
        s += "\n";
        //construct Jacobian Matrix
        s += "Jf = zeros(m,n);\n";
        for (int i=1;i<=numberOfContinuouslyApproximableTransitions;i++) {
            for (int j=1;j<=dimension;j++) {
                s += "Jf(" + i + "," + j +") = " + this.jacobianFunctions[i-1][j-1].toMatlabCode() + ";\n";
            }
        }
        s += "\nJ = S*Jf;\n\n";
        //construct covariance matrix
        s += "C = zeros(" + dimension + ");\n";
        s += "for i=1:n\n";
        s += "\tfor j=i:n\n";
        s += "\t\tC(i,j) = var(code(i,j));\n";
        s += "\t\tif (i ~= j)\n\t\tC(j,i) = var(code(i,j));\n\t\tend;\n";
        s += "\tend;\nend;\n";
        //compute derivative function
        s += "dx = S*f;\n";
        s += "dC = J*C + C*J' + S*diag(f)*S';\n";
        //add dC to dx
        s += "for i=1:n\n";
        s += "\tfor j=i:n\n";
        s += "\tdx(code(i,j)) = dC(i,j);\n";
        s += "\tend;\nend;\n";
        s += "end\n";
        s += "\n\n%solving ode\n";
        s += "delta = tf/points;\n";
        s += "tspan = 0:delta:tf;\n";
        s += "[T,Y] = ode45(@odefun,tspan,var);\n";
        s += "\nend\n";
        return s;
    }
    
   
   private int  code(int n, int i, int j) {
       //add + 1 for Matlab! 
        return n + n*i - i*(i-1)/2 + j - i + 1;
    }
    
    
}
