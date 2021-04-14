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
public class RandExp extends FunctionDefinition {

    public RandExp() {
        super.name = "rexp";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = true;
    }

    @Override
    public double compute(double x) {
        return RandomGenerator.nextExponential(x);
    }

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("rexp is unsupported in JSBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toJavaCode() {
        return "RandomGenerator.nextExponential";
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
        return "exprnd";
    }

    @Override
    public String toMatlabCode(String[] args) {
        return this.toMatlabCode() + "( 1/" + args[0] + " )";
    }
    
}
