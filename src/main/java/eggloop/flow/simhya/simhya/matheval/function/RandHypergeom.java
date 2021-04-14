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
public class RandHypergeom extends FunctionDefinition {

    public RandHypergeom() {
        super.name = "rhyperg";
        super.arity = 3;
        super.minimumArity = 3;
        super.maximumArity = 3;
        super.type = FunctionType.STATIC_NARY;
        super.randomFunction = true;
    
    }

    @Override
    public double compute(double[] args) {
        return RandomGenerator.nextHyperGeometric(args[0],args[1],args[2]);
    }
    

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("rhyperg is unsupported in JSBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toJavaCode() {
        return "RandomGenerator.nextHyperGeometric";
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
        return "hygernd";
    }

    
    
    
}
