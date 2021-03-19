/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.matheval.function;

import org.sbml.jsbml.ASTNode.Type;
import com.eggloop.flow.simhya.simhya.matheval.*;

/**
 *
 * @author Luca
 */
public class RandWeibull extends FunctionDefinition {

    public RandWeibull() {
        super.name = "rweibull";
        super.arity = -1;
        super.minimumArity = 2;
        super.maximumArity = 3;
        super.type = FunctionType.STATIC_ARBITRARY;
        super.randomFunction = true;
    
    }

    @Override
    public double compute(double x1, double x2) {
        return RandomGenerator.nextWeibull(x1, x2);
    }

    
    
    @Override
    public double compute(double[] args) {
        return RandomGenerator.nextWeibull(args[0],args[1],args[2]);
    }
    

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("rweibull is unsupported in JSBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toJavaCode() {
        return "RandomGenerator.nextWeibull";
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
        return "wblrnd";
    }

    @Override
    public String toMatlabCode(String[] args) {
        return this.toMatlabCode() + "( " + args[0] + ", " + args[1] +" )";
    }

    
    
}
