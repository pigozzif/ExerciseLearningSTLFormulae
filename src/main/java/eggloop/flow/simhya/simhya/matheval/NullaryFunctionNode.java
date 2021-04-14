/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;

import org.sbml.jsbml.ASTNode;
import eggloop.flow.simhya.simhya.matheval.function.FunctionDefinition;
/**
 *
 * @author Luca
 */
public class NullaryFunctionNode extends FunctionNode {
    


    public NullaryFunctionNode(FunctionDefinition function) {
        super(function,0);
    }


    @Override
    void computeValue() {
        super.value = function.compute();
    }

    @Override
    void computeValue(SymbolArray varReference) {
        super.value = function.compute();
    }

    @Override
    void computeValue(int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            super.value = function.compute();
        }
        
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            super.value = function.compute();
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        super.value = function.compute();
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        super.value = function.compute();
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            super.value = function.compute();
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            super.value = function.compute();
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        NullaryFunctionNode newNode = new NullaryFunctionNode(this.function);
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n;
        n = new ASTNode(function.getSBMLType());
        return n;
    }
    
    @Override
    ExpressionNode differentiate(String x, Evaluator eval) {
        return null;
    }

    @Override
    boolean isDifferentiable(String x) {
        return false;
    }
    
        @Override
    protected ExpressionNode clone(Evaluator newEval) {
        NullaryFunctionNode node = new NullaryFunctionNode(this.function);
        for (int i =0; i<this.numberOfChildren;i++)
            node.addChild(children[i].clone(newEval));
        return newEval.checkNodeDefinition(node);
    }
    
    
    
}
