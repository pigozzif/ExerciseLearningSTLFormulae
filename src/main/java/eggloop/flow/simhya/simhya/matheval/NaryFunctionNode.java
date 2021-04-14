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
public class NaryFunctionNode extends FunctionNode {
    double[] arguments;

    public NaryFunctionNode(FunctionDefinition function, int numberOfChildren) {
        super(function,numberOfChildren);
        arguments = new double[super.numberOfChildren];
    }
    

   @Override
    void computeValue() {
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeValue();
            arguments[i] = children[i].value;
        }
        super.value = function.compute(arguments);
    }

    @Override
    void computeValue(SymbolArray varReference) {
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeValue(varReference);
            arguments[i] = children[i].value;
        }
        super.value = function.compute(arguments);
    }

    @Override
    void computeValue(int evaluationCode) {
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
        for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeValueLocal(localVariables);
            arguments[i] = children[i].value;
        }
        super.value = function.compute(arguments);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
         for (int i=0;i<super.numberOfChildren;i++) {
            children[i].computeValueLocal(localVariables,globalVariables);
            arguments[i] = children[i].value;
        }
        super.value = function.compute(arguments);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
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
    protected Object clone() throws CloneNotSupportedException {
        NaryFunctionNode newNode = new NaryFunctionNode(this.function,this.numberOfChildren);
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n1,n;
        n = new ASTNode(function.getSBMLType());
        for (ExpressionNode v : children) {
            n1 = v.convertToJSBML();
            n.addChild(n1);
        }
        return n;
    }
    
    
        @Override
    protected ExpressionNode clone(Evaluator newEval) {
        NaryFunctionNode node = new NaryFunctionNode(this.function,this.numberOfChildren);
        for (int i =0; i<this.numberOfChildren;i++)
            node.addChild(children[i].clone(newEval));
        return newEval.checkNodeDefinition(node);
    }
    

}
