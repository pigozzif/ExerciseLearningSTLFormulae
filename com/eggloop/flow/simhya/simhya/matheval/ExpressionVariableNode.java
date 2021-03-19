/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval;
import java.util.ArrayList;
import java.util.List;
import org.sbml.jsbml.ASTNode;

/**
 *
 * @author Luca
 */
public class ExpressionVariableNode extends ExpressionNode {
    Expression expression;
    String name;


     public ExpressionVariableNode(String name,Expression exp) {
        super(0);
        this.expression = exp;
        this.name = name;
    }


    @Override
    public void getListOfVariables(ArrayList<Integer> list) {
        ArrayList<Integer> l = this.expression.getListOfVariables();
        list.addAll(l);
    }

    @Override
    String getExpressionString(boolean fullNumberRepresentation) {
        return this.name;
    }

    @Override
    void computeValue() {
        super.value = expression.computeValue();
    }

    @Override
    void computeValue(SymbolArray varReference) {
        super.value = expression.computeValue(varReference);
    }

    @Override
    void computeValue(int evaluationCode) {
        super.value = expression.computeValueCache();
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        super.value = expression.computeValueCache(varReference);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        expression.setLocalSymbolsValue(localVariables);
        super.value = expression.computeValue();
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        expression.setLocalSymbolsValue(localVariables);
        super.value = expression.computeValue(globalVariables);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        expression.setLocalSymbolsValue(localVariables);
        super.value = expression.computeValueCache();
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        expression.setLocalSymbolsValue(localVariables);
        super.value = expression.computeValueCache(globalVariables);
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        ExpressionVariableNode newNode = new ExpressionVariableNode(this.name,this.expression);
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ExpressionNode substitute(String toSubst, Expression exp, Evaluator eval) {
        Expression newExp = expression.substitute(toSubst, exp);
        if (expression != newExp) {
            return newExp.getRoot();
        } else return this;
    }

    @Override
    ExpressionNode substitute(List<String> toSubst, List<Expression> exp, Evaluator eval) {
        Expression newExp = expression.substitute(toSubst, exp);
        if (expression != newExp) {
            return newExp.getRoot();
        } else return this;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n = new ASTNode(ASTNode.Type.NAME);
        n.setVariable(new org.sbml.jsbml.Parameter(this.name));
        return n;
    }

    @Override
    void computeContinuousChangeStatus() {
        canChangeContinuously = expression.computeContinuousChangeStatus();
    }

    @Override
    void computeContinuousChangeStatus(SymbolArray varReference) {
        canChangeContinuously = expression.computeContinuousChangeStatus(varReference);
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        expression.setLocalSymbolsValue(localVariables);
        canChangeContinuously = expression.computeContinuousChangeStatus();
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        expression.setLocalSymbolsValue(localVariables);
        canChangeContinuously = expression.computeContinuousChangeStatus(globalVariables);
    }

    @Override
    boolean containsExpressionVariable(String expVar) {
        return expVar.equals(name);
    }

    @Override
    boolean containsSymbol(String symbol) {
        return symbol.equals(name) || expression.containsSymbol(symbol);
    }

    @Override
    boolean containsBoundVariable(String boundVar) {
        return expression.containsBoundVariable(boundVar);
    }

    @Override
    boolean containsConstant(String constant) {
        return expression.containsConstant(constant);
    }

    @Override
    boolean containsGlobalVariable(String variable) {
        return expression.containsGlobalVariable(variable);
    }

    @Override
    String toJavaCode() {
        return "_expr_" + this.name + "()";
    }
    
    

    @Override
    String toMatlabCode() {
        return "expr_" + this.name;
    }

    
    @Override
    /**
     * An expression variable is a logical expression iff 
     */
    public boolean isLogicalExpression() {
        return expression.isLogicalExpression();
    }
    
    
    @Override
    ExpressionNode differentiate(String x, Evaluator eval) {
        //here we admit differentiation w.r.t. an expression variable,
        //but differentiate the expression in case the name of the expr variable
        //is not x
        if (this.name.equals(x)) {
            return new NumericNode(1);
        } else
            return this.expression.differentiateNode(x);
    }
    
    
    @Override
    boolean isDifferentiable(String x) {
        if (this.name.equals(x)) 
            return true;
        else
            return this.expression.isDifferentiable(x);
    }

    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        Expression exp = newEval.getExpressionVariableReference().getExpression(newEval.getExpressionVariableReference().getSymbolId(name));
        ExpressionVariableNode node = new ExpressionVariableNode(this.name,exp);
        return newEval.checkNodeDefinition(node);
    }
    
    
    
    
}
