/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval;

import org.sbml.jsbml.ASTNode;
import com.eggloop.flow.simhya.simhya.matheval.function.FunctionDefinition;

/**
 * @author Luca
 */
public class UnaryFunctionNode extends FunctionNode {

    public UnaryFunctionNode(FunctionDefinition function) {
        super(function, 1);
    }

    @Override
    void computeValue() {
        children[0].computeValue();
        super.value = function.compute(children[0].value);
    }

    @Override
    void computeValue(SymbolArray varReference) {
        children[0].computeValue(varReference);
        super.value = function.compute(children[0].value);
    }

    @Override
    void computeValue(int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(evaluationCode);
            super.value = function.compute(children[0].value);
        }
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(varReference, evaluationCode);
            super.value = function.compute(children[0].value);
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        children[0].computeValueLocal(localVariables);
        super.value = function.compute(children[0].value);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        children[0].computeValueLocal(localVariables, globalVariables);
        super.value = function.compute(children[0].value);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables, evaluationCode);
            super.value = function.compute(children[0].value);
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables, globalVariables, evaluationCode);
            super.value = function.compute(children[0].value);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        UnaryFunctionNode newNode = new UnaryFunctionNode(this.function);
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n1, n;
        n1 = children[0].convertToJSBML();
        n = new ASTNode(function.getSBMLType());
        n.addChild(n1);
        return n;
    }


    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        UnaryFunctionNode node = new UnaryFunctionNode(this.function);
        for (int i = 0; i < this.numberOfChildren; i++)
            node.addChild(children[i].clone(newEval));
        return newEval.checkNodeDefinition(node);
    }


}
