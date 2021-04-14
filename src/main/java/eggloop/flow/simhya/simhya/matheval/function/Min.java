
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
public class Min extends FunctionDefinition {

    public Min() {
        super.name = "min";
        super.arity = -1;
        super.minimumArity = 2;
        super.maximumArity = Integer.MAX_VALUE;
        super.type = FunctionType.STATIC_ARBITRARY;
        super.randomFunction = false;
    }

    

    @Override
    public double compute(double x1, double x2) {
        return Math.min(x1, x2);
    }

    @Override
    public double compute(double[] args) {
        return MiscFunctions.max(args);
    }

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("Min is unsupported in JSBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toJavaCode() {
        return "MiscFunctions.min";
    }
    
    @Override
    public ExpressionNode differentiate(int arg) {
        if (arg >= 2)
            throw new RuntimeException("Function " + this.name + " cannot be differentiated with more than 2 arguments");
        ExpressionNode n,v0,v1,n1; 
        //!!! d min(x,y)/dx = if(x<=y,1,0); d min(x,y)/dy = if(x>y,1,0)
        v0 = new BoundVariableNode(0,"X0",name);
        v1 = new BoundVariableNode(1,"X1",name);
        if (arg == 0)
            n1 = new BinaryOperatorNode(new LessOrEqual());
        else if (arg == 1)
            n1 = new BinaryOperatorNode(new Greater());
        else return null;
        n1.addChild(v0);
        n1.addChild(v1);
        n = new IfNode();
        n.addChild(n1);
        n.addChild(new NumericNode(1));
        n.addChild(new NumericNode(0));
        return n;
    }

    @Override
    public boolean isDifferentiable(String x, Integer args) {
        if (args != 2)
            return false;
        else return true;
    }
    
    @Override
    public String toMatlabCode() {
        return "min";
    }

    @Override
    public String toMatlabCode(String[] args) {
        String s = this.toMatlabCode() + "([ ";
        for (int i=0;i<args.length;i++) {
            s += (i>0 ? ", " : "") + args[i];
        }
        s += " ])";
        return s;
    }

    

   

}