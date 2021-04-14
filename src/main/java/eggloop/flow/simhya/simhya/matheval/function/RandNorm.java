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
public class RandNorm extends FunctionDefinition {

    public RandNorm() {
        super.name = "rnorm";
        super.arity = -1;
        super.minimumArity = 0;
        super.maximumArity = 2;
        super.type = FunctionType.STATIC_ARBITRARY;
        super.randomFunction = true;
    }

    @Override
    public double compute() {
        return RandomGenerator.nextNormal();
    }

    @Override
    public double compute(double x) {
        return RandomGenerator.nextNormal(x);
    }


    @Override
    public double compute(double x1, double x2) {
        return RandomGenerator.nextNormal(x1, x2);
    }

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("rnorm is unsupported in JSBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toJavaCode() {
        return "RandomGenerator.nextNormal";
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
        return "normrnd";
    }

    @Override
    public String toMatlabCode(String[] args) {
        String s = this.toMatlabCode() + "( ";
        if (args.length > 0) s += args[0]; else s+= "0";
        s+= ", ";
        if (args.length > 1) s += args[1]; else s+= "1";
        s += " )";
        return s;
    }
    
    
    

}

