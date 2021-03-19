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
public class Cbrt extends FunctionDefinition {

    public Cbrt() {
        super.name = "cbrt";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.cbrt(x);
    }

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("cbrt is unsupported in JSBML --- cannot convert it to MATHML.");
    }
    
    @Override
    public String toJavaCode() {
        return "Math.cbrt";
    }
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,v,f,n1,n2,n3; 
        //cbrt'(x) = 1/3*cbrt(x)^2
        v = new BoundVariableNode(0,"X0",name);
        f = new UnaryFunctionNode(new Cbrt());
        f.addChild(v);
        n1 = new BinaryOperatorNode(new Power());
        n1.addChild(f);
        n1.addChild(new NumericNode(2));
        n2 = new BinaryOperatorNode(new Multiply());
        n2.addChild(new NumericNode(3));
        n2.addChild(n1);
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
        return "nthroot";
    }

    @Override
    public String toMatlabCode(String[] args) {
        return this.toMatlabCode() + "(" + args[0] + ",3)";
    }
    

    
    
}
