/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matheval.function;

import org.sbml.jsbml.ASTNode.Type;
import eggloop.flow.simhya.simhya.matheval.*;

/**
 *
 * @author Luca
 */
public class RandBeta extends FunctionDefinition {

    public RandBeta() {
        super.name = "rbeta";
        super.arity = 2;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_BINARY;
        super.randomFunction = true;
    
    }
    

    @Override
    public double compute(double x1, double x2) {
        return RandomGenerator.nextBeta(x1, x2);
    }

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("rbeta is unsupported in JSBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toJavaCode() {
        return "RandomGenerator.nextBeta";
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
        return "betarnd";
    }
    
    
    
}
