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
public class Exp extends FunctionDefinition {

    public Exp() {
        super.name = "exp";
        super.arity = 1;
        super.minimumArity = 0;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_UNARY;
        super.randomFunction = false;
    }

    @Override
    public double compute(double x) {
        return Math.exp(x);
    }

    @Override
    public Type getSBMLType() {
        return Type.FUNCTION_EXP;
    }

    @Override
    public String toJavaCode() {
        return "Math.exp";
    }
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= arity)
            throw new RuntimeException("Function " + this.name + " has " + this.arity + " arguments");
        ExpressionNode n,v,f,n1; 
        //exp'(x) = exp(x)
        v = new BoundVariableNode(0,"X0",name);
        f = new UnaryFunctionNode(new Exp());
        f.addChild(v);
        return f;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        return true;
    }
    
    @Override
    public String toMatlabCode() {
        return "exp";
    }
    
}
