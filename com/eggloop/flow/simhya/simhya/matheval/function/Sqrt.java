/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval.function;

import com.eggloop.flow.simhya.simhya.matheval.*;
import org.sbml.jsbml.ASTNode.Type;
import com.eggloop.flow.simhya.simhya.matheval.operator.*;

/**
 *
 * @author Luca
 */
public class Sqrt extends FunctionDefinition {

    public Sqrt() {
        super.name = "sqrt";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.sqrt(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_ROOT;
    }
    
    @Override
    public String toJavaCode() {
        return "Math.sqrt";
    }
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,v,f,n1,n2; 
        //sqrt'(x) = 1/2*sqrt(x)
        v = new BoundVariableNode(0,"X0",name);
        f = new UnaryFunctionNode(new Sqrt());
        f.addChild(v);
        n2 = new BinaryOperatorNode(new Multiply());
        n2.addChild(new NumericNode(2));
        n2.addChild(f);
        n = new BinaryOperatorNode(new Divide());
        n.addChild(new NumericNode(1));
        n.addChild(n2);
        return n;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        return true;
    }
    
    @Override
    public String toMatlabCode() {
        return "sqrt";
    }
    
}
