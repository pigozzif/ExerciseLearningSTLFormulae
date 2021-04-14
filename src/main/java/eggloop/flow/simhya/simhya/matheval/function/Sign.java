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
public class Sign extends FunctionDefinition {

    public Sign() {
        super.name = "sign";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.signum(x);
    }

    @Override
    public Type getSBMLType() {
           throw new UnsupportedOperationException("sign is unsupported in JSBML --- cannot convert it to MATHML."); 
    }
    
    @Override
    public String toJavaCode() {
        return "Math.signum";
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
        return "sign";
    }
}