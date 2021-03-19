/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval.function;

import org.sbml.jsbml.ASTNode.Type;
import com.eggloop.flow.simhya.simhya.matheval.*;
import com.eggloop.flow.simhya.simhya.matheval.operator.*;

/**
 *
 * @author Luca
 */
public class Sinh extends FunctionDefinition {

    public Sinh() {
        super.name = "sinh";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.sinh(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_SINH;
    }
    
    @Override
    public String toJavaCode() {
        return "Math.sinh";
    }
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,n2; 
        //cosh'(x) = sinh(x)
        n2 = new BoundVariableNode(0,"X0",name);
        n = new UnaryFunctionNode(new Cosh());
        n.addChild(n2);
        return n;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        return true;
    }
    
    @Override
    public String toMatlabCode() {
        return "sinh";
    }

}
