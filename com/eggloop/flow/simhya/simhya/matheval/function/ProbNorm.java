/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval.function;

import org.sbml.jsbml.ASTNode.Type;
import umontreal.iro.lecuyer.probdist.NormalDistQuick;
import com.eggloop.flow.simhya.simhya.matheval.*;
import com.eggloop.flow.simhya.simhya.matheval.operator.*;

/**
 * computes the cumulative probability function of a normal univariate.
 * It accepts 1 argument, assuming standardized normal distribution, or 3 arguments,
 * mean, variance and x, respectively.
 * @author luca
 */
public class ProbNorm extends FunctionDefinition {

    public ProbNorm() {
        super.name = "pnorm";
        super.arity = -1;
        super.minimumArity = 1;
        super.maximumArity = 3;
        super.type = FunctionType.STATIC_ARBITRARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return NormalDistQuick.cdf01(x);
    }

    @Override
    public double compute(double[] args) {
        if (args.length != 3)
            throw new EvalException("Function " + this.name + " accepts 1 or three arguments");
        
//        System.out.println("mu " + args[0] + "; sigma " + args[1] + "; x " + args[2]);

        return NormalDistQuick.cdf(args[0], args[1], args[2]);
    }

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("pnorm is unsupported in JSBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toJavaCode() {
        return "RandomGenerator.normalDist";
    }

    
    @Override
    public ExpressionNode differentiate(int arg) {
        return null;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        return false;
    }

    @Override
    public String toMatlabCode() {
        return "normcdf";
    }
    

}
