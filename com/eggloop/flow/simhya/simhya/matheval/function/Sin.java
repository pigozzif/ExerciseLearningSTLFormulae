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
public class Sin extends FunctionDefinition {

    public Sin() {
        super.name = "sin";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.sin(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_SIN;
    }
    
    @Override
    public String toJavaCode() {
        return "Math.sin";
    }
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,n1; 
        n1 = new BoundVariableNode(0,"X0",name);
        n = new UnaryFunctionNode(new Cos());
        n.addChild(n1);
        return n;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        return true;
    }
    
    @Override
    public String toMatlabCode() {
        return "sin";
    }

}
