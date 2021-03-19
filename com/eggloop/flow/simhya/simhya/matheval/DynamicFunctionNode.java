/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval;
import org.sbml.jsbml.ASTNode;
import com.eggloop.flow.simhya.simhya.matheval.function.*;
import com.eggloop.flow.simhya.simhya.matheval.operator.*;
/**
 *
 * @author Luca
 */
public class DynamicFunctionNode extends FunctionNode {

    public DynamicFunctionNode(FunctionDefinition function, int numberOfChildren) {
        super(function,numberOfChildren);
    }

    @Override
    public boolean canBeConvertedToNumericalExpression() {
        return function.canBeConvertedToNumericalFunction();
    }

    @Override
    public ExpressionNode convertToNumericalExpression(Evaluator eval) {
        FunctionDefinition f = function.convertToNumericalFunction(eval);
        ExpressionNode n = new DynamicFunctionNode(f,numberOfChildren);
        for (int i=0;i<numberOfChildren;i++)
            n.addChild(children[i]);
        n = eval.checkNodeDefinition(n);
        return n;
    }



   

   @Override
    void computeValue() {
        double[] arguments = new double[super.numberOfChildren];
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeValue();
            arguments[i] = children[i].value;
        }
        super.value = function.compute(arguments);
    }

    @Override
    void computeValue(SymbolArray varReference) {
        double[] arguments = new double[super.numberOfChildren];
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeValue(varReference);
            arguments[i] = children[i].value;
        }
        super.value = function.compute(arguments);
    }

    @Override
    void computeValue(int evaluationCode) {
        double[] arguments = new double[super.numberOfChildren];
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            for (int i=0;i<super.numberOfChildren;i++) {
                children[i].computeValue(evaluationCode);
                arguments[i] = children[i].value;
            }
            super.value = function.compute(arguments);
        }
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        double[] arguments = new double[super.numberOfChildren];
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            for (int i=0;i<super.numberOfChildren;i++) {
                children[i].computeValue(varReference,evaluationCode);
                arguments[i] = children[i].value;
            }
            super.value = function.compute(arguments);
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        double[] arguments = new double[super.numberOfChildren];
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeValueLocal(localVariables);
            arguments[i] = children[i].value;
        }
        super.value = function.compute(arguments);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        double[] arguments = new double[super.numberOfChildren];
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeValueLocal(localVariables,globalVariables);
            arguments[i] = children[i].value;
        }
        super.value = function.compute(arguments);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        double[] arguments = new double[super.numberOfChildren];
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            for (int i=0;i<super.numberOfChildren;i++) {
                children[i].computeValueLocal(localVariables,evaluationCode);
                arguments[i] = children[i].value;
            }
            super.value = function.compute(arguments);
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        double[] arguments = new double[super.numberOfChildren];
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            for (int i=0;i<super.numberOfChildren;i++) {
                children[i].computeValueLocal(localVariables,globalVariables,evaluationCode);
                arguments[i] = children[i].value;
            }
            super.value = function.compute(arguments);
        }
    }
    
    

    @Override
    public boolean isLogicalExpression() {
        return super.function.isLogicalFunction();
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        DynamicFunctionNode newNode = new DynamicFunctionNode(this.function,this.numberOfChildren);
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        throw new UnsupportedOperationException("MATHML conversion of user defined functions is not supported");
    }
    
    @Override
    void computeContinuousChangeStatus() {
        boolean[] arguments = new boolean[super.numberOfChildren];
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeContinuousChangeStatus();
            arguments[i] = children[i].canChangeContinuously;
        }
        super.canChangeContinuously = function.computeContinuousChangeStatus(arguments);
    }

    @Override
    void computeContinuousChangeStatus(SymbolArray varReference) {
        boolean[] arguments = new boolean[super.numberOfChildren];
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeContinuousChangeStatus(varReference);
            arguments[i] = children[i].canChangeContinuously;
        }
        super.canChangeContinuously = function.computeContinuousChangeStatus(arguments);
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        boolean[] arguments = new boolean[super.numberOfChildren];
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeContinuousChangeStatusLocal(localVariables);
            arguments[i] = children[i].canChangeContinuously;
        }
        super.canChangeContinuously = function.computeContinuousChangeStatus(arguments);
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        boolean[] arguments = new boolean[super.numberOfChildren];
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeContinuousChangeStatusLocal(localVariables,globalVariables);
            arguments[i] = children[i].canChangeContinuously;
        }
        super.canChangeContinuously = function.computeContinuousChangeStatus(arguments);
    }

    
    
    @Override
    ExpressionNode differentiate(String x, Evaluator eval) {
        if (! (function instanceof DynamicFunction))
            throw new RuntimeException("Dynamic function node has no dynamic function attached to it!");
        DynamicFunction f = (DynamicFunction)this.function;
        if (!this.isDifferentiable(x)) return null;
        boolean totalDerivativeNeeded = true;
        ExpressionNode dif = null,df,ds,da,p,s;
        //use chain rule: df(g0(x),...,gk(x))/dx = sum_j df(X0,..,XK)/dXj*dgj(x)/dx 
        //differentiate w.r.t arg0
        
        for (int i =0;i<numberOfChildren;i++) {
            String arg = f.getLocalSymbolName(i);
            //we need to take the  derivative w.r.t x to properly compute the 
            //total derivative only if it is not a local parameter
            //of the function
            totalDerivativeNeeded = totalDerivativeNeeded && !arg.equals(x);
            df = f.differentiate(i);
            da = children[i].differentiate(x, eval);
            if (df.isZero() || da.isZero()) 
                p = new NumericNode(0);
            else {
                ds = df;
                for (int j=0;j<numberOfChildren;j++) {
                    String a = f.getLocalSymbolName(j);
                    Expression exp = new Expression(children[j],eval, false);
                    ds = ds.substitute(a, exp, eval);
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
            if (totalDerivativeNeeded) {
                df = f.differentiate(x);
                if (!df.isZero()) {
                   ds = df; 
                   for (int j=0;j<numberOfChildren;j++) {
                        String a = f.getLocalSymbolName(j);
                        Expression exp = new Expression(children[j],eval, false);
                        ds = ds.substitute(a, exp, eval);
                    } 
                   if (dif.isZero()) {
                       dif = ds;
                   }else if (ds instanceof UnaryOperatorNode && 
                        ((UnaryOperatorNode)ds).operator.getType() == OperatorType.MINUS ) {
                       //puch minus to binary operator 
                       s = new BinaryOperatorNode(new Minus());
                       s.addChild(dif);
                       s.addChild(ds.children[0]);
                       s = eval.checkNodeDefinition(s);
                       dif = s;

                   } else {
                       s = new BinaryOperatorNode(new Plus());
                       s.addChild(dif);
                       s.addChild(ds);
                       s = eval.checkNodeDefinition(s);
                       dif = s;
                   }
                }
            } 
        }
        return dif;
        
        
    }

    @Override
    boolean isDifferentiable(String x) {
        if (!this.function.isDifferentiable(x, null))
            return false;
        for (int i=0;i<this.numberOfChildren;i++)
            if(!children[i].isDifferentiable(x))
                return false;
        return true;
    }
    
    
    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        DynamicFunctionNode node = new DynamicFunctionNode(newEval.getFunctionDefinition(this.function.getName()),this.numberOfChildren);
        for (int i =0; i<this.numberOfChildren;i++)
            node.addChild(children[i].clone(newEval));
        return newEval.checkNodeDefinition(node);
    }

}
