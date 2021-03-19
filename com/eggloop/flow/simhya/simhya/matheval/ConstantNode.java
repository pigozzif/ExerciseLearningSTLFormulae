/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval;

import org.sbml.jsbml.ASTNode;

/**
 *
 * @author Luca
 */
public class ConstantNode extends SubstitutableNode {
    private int constIndex;
    private SymbolArray constReference;


   public ConstantNode(int constIndex, SymbolArray constReference) {
        super(0);
        if (!constReference.checkSymbolIndex(constIndex))
            throw new EvalException("Variable index is wrong");
        this.constReference = constReference;
        this.constIndex = constIndex;
        name = constReference.getName(constIndex);
    }


    @Override
    String getExpressionString(boolean fullNumberRepresentation) {
        return this.name;
    }

    @Override
    void computeValue() {
        super.value = constReference.values[constIndex];
    }

    @Override
    void computeValue(SymbolArray varReference) {
        super.value = constReference.values[constIndex];
    }

    @Override
    void computeValue(int evaluationCode) {
        super.value = constReference.values[constIndex];
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        super.value = constReference.values[constIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        super.value = constReference.values[constIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        super.value = constReference.values[constIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        super.value = constReference.values[constIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        super.value = constReference.values[constIndex];
    }
    
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        ConstantNode newNode = new ConstantNode(this.constIndex,this.constReference);
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n = new ASTNode(ASTNode.Type.NAME);
        n.setVariable(new org.sbml.jsbml.Parameter(this.name));
        return n;
    }

    @Override
    void computeContinuousChangeStatus() {
        this.canChangeContinuously = false;
    }

    @Override
    void computeContinuousChangeStatus(SymbolArray varReference) {
        this.canChangeContinuously = false;
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        this.canChangeContinuously = false;
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        this.canChangeContinuously = false;
    }

    @Override
    boolean containsConstant(String constant) {
        return constant.equals(name);
    }
    
    @Override
    String toJavaCode() {
        return "parameter[" + this.constIndex + "]";
    }
    
    

    @Override
    String toMatlabCode() {
        return "par(" + (this.constIndex+1) + ")";
    }

    
    @Override
    ExpressionNode differentiate(String x, Evaluator eval) {
        //we admit differentiation w.r.t costants
        if (this.name.equals(x)) {
            return new NumericNode(1);
        } else
            return new NumericNode(0);
    }
    
     @Override
    boolean isDifferentiable(String x) {
        return true;
    }

    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        ExpressionNode node = new ConstantNode(this.constIndex,newEval.getConstantReference());
        node = newEval.checkNodeDefinition(node);
        return node;
    }
     
     
     
    
}
