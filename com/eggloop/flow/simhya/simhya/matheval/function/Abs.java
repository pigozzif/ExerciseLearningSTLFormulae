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
public class Abs extends FunctionDefinition {

    public Abs() {
        super.name = "abs";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.abs(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_ABS;
    }
    
    @Override
    public String toJavaCode() {
        return "Math.abs";
    }
    
    
    @Override
    public String toMatlabCode() {
        return "abs";
    }
   
    
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,v0,v1,n1; 
        //!!! abs'(x) = if(x>=0,1,-1); 
        v0 = new BoundVariableNode(0,"X0",name);
        n1 = new BinaryOperatorNode(new GreaterOrEqual());
        n1.addChild(v0);
        n1.addChild(new NumericNode(0));
        n = new IfNode();
        n.addChild(n1);
        n.addChild(new NumericNode(1));
        n.addChild(new NumericNode(-1));
        return n;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        return true;
    }

}
