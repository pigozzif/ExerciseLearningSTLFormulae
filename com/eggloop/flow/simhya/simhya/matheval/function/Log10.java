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
public class Log10 extends FunctionDefinition {

    public Log10() {
        super.name = "log10";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.log10(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_LOG;
    }

    @Override
    public String toJavaCode() {
        return "Math.log10";
    }

    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,v,f,n1; 
        //log10'(x) = 1/ (log(10)*x)
        v = new BoundVariableNode(0,"X0",name);
        n1 = new BinaryOperatorNode(new Multiply());
        n1.addChild(new NumericNode(Math.log(10),false));
        n1.addChild(v);
        n = new BinaryOperatorNode(new Divide());
        n.addChild(new NumericNode(1));
        n.addChild(n1);
        return n;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        return true;
    }
    
    @Override
    public String toMatlabCode() {
        return "log10";
    }

    
}
