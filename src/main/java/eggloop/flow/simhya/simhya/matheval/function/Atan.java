/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval.function;

import org.sbml.jsbml.ASTNode.Type;
import eggloop.flow.simhya.simhya.matheval.*;
import eggloop.flow.simhya.simhya.matheval.operator.*;


/**
 *
 * @author Luca
 */
public class Atan extends FunctionDefinition {

    public Atan() {
        super.name = "atan";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.atan(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_ARCTAN;
    }
    
    @Override
    public String toJavaCode() {
        return "Math.atan";
    }

    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,v,f,n1,n2; 
        //atan'(x) = 1/(1-x^2)
        v = new BoundVariableNode(0,"X0",name);
        n1 = new BinaryOperatorNode(new Power());
        n1.addChild(v);
        n1.addChild(new NumericNode(2));
        n2 = new BinaryOperatorNode(new Minus());
        n2.addChild(new NumericNode(1));
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
        return "atan";
    }
    
}
