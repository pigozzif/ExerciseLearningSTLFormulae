/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval.function;

import org.sbml.jsbml.ASTNode.Type;
import com.eggloop.flow.simhya.simhya.matheval.*;
import com.eggloop.flow.simhya.simhya.matheval.operator.Minus;

/**
 *
 * @author Luca
 */
public class Cos extends FunctionDefinition {

    public Cos() {
        super.name = "cos";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.cos(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_COS;
    }

    
    @Override
    public String toJavaCode() {
        return "Math.cos";
    }
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,n1,n2; 
        //cos'(x) = -sin(x)
        n2 = new BoundVariableNode(0,"X0",name);
        n1 = new UnaryFunctionNode(new Sin());
        n1.addChild(n2);
        n = new UnaryOperatorNode(new Minus());
        n.addChild(n1);
        return n;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        return true;
    }
    
    @Override
    public String toMatlabCode() {
        return "cos";
    }
}
