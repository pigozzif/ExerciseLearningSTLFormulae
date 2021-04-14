/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;

import org.sbml.jsbml.ASTNode;
import eggloop.flow.simhya.simhya.matheval.function.FunctionDefinition;
import eggloop.flow.simhya.simhya.matheval.operator.*;


/**
 *
 * @author Luca
 */
public abstract class FunctionNode extends ExpressionNode {
    protected FunctionDefinition function;
    
    
    public FunctionNode(FunctionDefinition function, int numberOfChildren) {
        super(numberOfChildren);
        this.function = function;
        random = function.isRandomFunction();
    }

    String getExpressionString(boolean fullNumberRepresentation) {
        String s = "";
        s += function.getName() + "(";
        for (int i=0;i<super.numberOfChildren;i++)
            s += (i>0 ? "," : "") + children[i].getExpressionString(false);
        s += ")";
        return s;
    }


    abstract void computeValue();
    abstract void computeValue(SymbolArray varReference);
    abstract void computeValue(int evaluationCode);
    abstract void computeValue(SymbolArray varReference, int evaluationCode);
    abstract void computeValueLocal(SymbolArray localVariables);
    abstract void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables);
    abstract void computeValueLocal(SymbolArray localVariables, int evaluationCode);
    abstract void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode);
    
    
    void computeContinuousChangeStatus() {
        this.canChangeContinuously = false;
        for (ExpressionNode n : this.children) {
            n.computeContinuousChangeStatus();
            this.canChangeContinuously = this.canChangeContinuously || n.canChangeContinuously;
        }
    }
    void computeContinuousChangeStatus(SymbolArray varReference) {
        this.canChangeContinuously = false;
        for (ExpressionNode n : this.children) {
            n.computeContinuousChangeStatus(varReference);
            this.canChangeContinuously = this.canChangeContinuously || n.canChangeContinuously;
        }
    }
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        this.canChangeContinuously = false;
        for (ExpressionNode n : this.children) {
            n.computeContinuousChangeStatusLocal(localVariables);
            this.canChangeContinuously = this.canChangeContinuously || n.canChangeContinuously;
        }
    }
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        this.canChangeContinuously = false;
        for (ExpressionNode n : this.children) {
            n.computeContinuousChangeStatusLocal(localVariables,globalVariables);
            this.canChangeContinuously = this.canChangeContinuously || n.canChangeContinuously;
        }
    }
    
    

    @Override
    ASTNode convertToJSBML() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    String toJavaCode() {
        String s;
        s = function.toJavaCode() + "(";
        for (int i=0;i<this.numberOfChildren;i++)
            s+= (i > 0 ? "," : "") + 
                    ( children[i].isLogicalExpression() ? 
                      "(" + children[i].toJavaCode() + "? 1.0 : 0.0)"  : 
                      children[i].toJavaCode() );
        s += ")";
        return s;
    }

    @Override
    String toMatlabCode() {
        String s;
        String[] c = new String[this.numberOfChildren];
        for (int i=0;i<this.numberOfChildren;i++) {
                c[i] = children[i].toMatlabCode();
        }
        s = function.toMatlabCode(c);
        return s;
    }

    @Override
    boolean isDifferentiable(String x) {
        if (!function.isDifferentiable(x, numberOfChildren))
            return false;
        boolean diff = true;
        for (int i=0;i<this.numberOfChildren;i++)
            diff = diff && children[i].isDifferentiable(x);
        return diff;
    }

    @Override
    ExpressionNode differentiate(String x, Evaluator eval) {
        if (!this.isDifferentiable(x)) return null;
        ExpressionNode dif = null,df,ds,da,p,s;
        //use chain rule: df(g0(x),...,gk(x))/dx = sum_j df(X0,..,XK)/dXj*dgj(x)/dx 
        //differentiate w.r.t arg0
        
        for (int i =0;i<numberOfChildren;i++) {
            df = function.differentiate(i);
            da = children[i].differentiate(x, eval);
            if (df.isZero() || da.isZero()) 
                p = new NumericNode(0);
            else {
                ds = df;
                for (int j=0;j<numberOfChildren;j++) {
                    Expression exp = new Expression(children[j],eval, false);
                    ds = ds.substitute("X"+j, exp, eval);
                }
                if (ds.isNumericConstant() && da.isNumericConstant()) {
                    p = new NumericNode(ds.value*da.value,false);
                    p = eval.checkNodeDefinition(p);
                }
                else if (ds.isOne())
                    p = da;
                else if (da.isOne())
                    p = ds;
                else {
                    p = new BinaryOperatorNode(new Multiply());
                    p.addChild(ds);
                    p.addChild(da);
                    p = eval.checkNodeDefinition(p);
                }
            }
            if (i==0 || dif.isZero()) dif = p;
            else {
                if (p.isZero()) ;
                if (p instanceof UnaryOperatorNode && 
                        ((UnaryOperatorNode)p).operator.getType() == OperatorType.MINUS ) {
                   //puch minus to binary operator 
                   s = new BinaryOperatorNode(new Minus());
                   s.addChild(dif);
                   s.addChild(p.children[0]);
                   s = eval.checkNodeDefinition(s);
                   dif = s;

               } else {
                   s = new BinaryOperatorNode(new Plus());
                   s.addChild(dif);
                   s.addChild(p);
                   s = eval.checkNodeDefinition(s);
                   dif = s;
               }
            }
        }
        return dif;
    }
    
    
    
    
    
}
