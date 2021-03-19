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
public class Uniform extends FunctionDefinition {

    public Uniform() {
        super.name = "uniform";
        super.arity = -1;
        super.minimumArity = 0;
        super.maximumArity = 2;
        super.type = FunctionType.STATIC_ARBITRARY;
        super.randomFunction = true;
    }

    @Override
    public double compute() {
        return RandomGenerator.nextUniform();
    }

    @Override
    public double compute(double x) {
        return RandomGenerator.nextUniform(x);
    }



    @Override
    public double compute(double x1, double x2) {
        return RandomGenerator.nextUniform(x1,x2);
    }

    @Override
    public Type getSBMLType() {
       throw new UnsupportedOperationException("uniform is unsupported in JSBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toJavaCode() {
        return "RandomGenerator.nextUniform";
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
        return "rand";
    }

    @Override
    public String toMatlabCode(String[] args) {
        String s = "";
        if (args.length == 0) {
            s = this.toMatlabCode() + "()";
        } else if (args.length == 1) {
            s = args[0] + " * " + this.toMatlabCode() + "()"; 
        } else {
            s = args[0] + " + ( " + args[1] + " - " + args[0] + " ) * " + this.toMatlabCode() + "()";       
        }
        return s;
    }
    
}