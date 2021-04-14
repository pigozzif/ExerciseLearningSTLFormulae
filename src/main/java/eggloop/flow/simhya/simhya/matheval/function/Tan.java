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
public class Tan extends FunctionDefinition {

    public Tan() {
        super.name = "tan";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.tan(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_TAN;
    }
    
    @Override
    public String toJavaCode() {
        return "Math.tan";
    }
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,v,f,n1; 
        //tan'(x) = 1+tan(x)^2
        v = new BoundVariableNode(0,"X0",name);
        f = new UnaryFunctionNode(new Tan());
        f.addChild(v);
        n1 = new BinaryOperatorNode(new Power());
        n1.addChild(f);
        n1.addChild(new NumericNode(2));
        n = new BinaryOperatorNode(new Plus());
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
        return "tan";
    }
}
