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
public class Asin extends FunctionDefinition {

    public Asin() {
        super.name = "asin";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.asin(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_ARCSIN;
    }
    
    @Override
    public String toJavaCode() {
        return "Math.asin";
    }
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,v,f,n1,n2,n3; 
        //asin'(x) = 1/sqrt(x^2+1)
        v = new BoundVariableNode(0,"X0",name);
        n1 = new BinaryOperatorNode(new Power());
        n1.addChild(v);
        n1.addChild(new NumericNode(2));
        n2 = new BinaryOperatorNode(new Plus());
        n2.addChild(new NumericNode(1));
        n2.addChild(n1);
        f = new UnaryFunctionNode(new Sqrt());
        f.addChild(n2);
        n = new BinaryOperatorNode(new Divide());
        n.addChild(new NumericNode(1));
        n.addChild(f);
        return n;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        return true;
    }

    
    @Override
    public String toMatlabCode() {
        return "asin";
    }
}
