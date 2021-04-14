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
public class BinaryFunctionNode extends FunctionNode {

    public BinaryFunctionNode(FunctionDefinition function) {
        super(function,2);
    }
    @Override
    void computeValue() {
        children[0].computeValue();
        children[1].computeValue();
        super.value = function.compute(children[0].value,children[1].value);
    }

    @Override
    void computeValue(SymbolArray varReference) {
        children[0].computeValue(varReference);
        children[1].computeValue(varReference);
        super.value = function.compute(children[0].value,children[1].value);
    }

    @Override
    void computeValue(int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(evaluationCode);
            children[1].computeValue(evaluationCode);
            super.value = function.compute(children[0].value,children[1].value);
        }
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(varReference,evaluationCode);
            children[1].computeValue(varReference,evaluationCode);
            super.value = function.compute(children[0].value,children[1].value);
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        children[0].computeValueLocal(localVariables);
        children[1].computeValueLocal(localVariables);
        super.value = function.compute(children[0].value,children[1].value);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        children[0].computeValueLocal(localVariables,globalVariables);
        children[1].computeValueLocal(localVariables,globalVariables);
        super.value = function.compute(children[0].value,children[1].value);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables,evaluationCode);
            children[1].computeValueLocal(localVariables,evaluationCode);
            super.value = function.compute(children[0].value,children[1].value);
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables,globalVariables,evaluationCode);
            children[1].computeValueLocal(localVariables,globalVariables,evaluationCode);
            super.value = function.compute(children[0].value,children[1].value);
        }
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        BinaryFunctionNode newNode = new BinaryFunctionNode(this.function);
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n1,n2,n;
        n1 = children[0].convertToJSBML();
        n2 = children[1].convertToJSBML();
        n = new ASTNode(function.getSBMLType());
        n.addChild(n1);
        n.addChild(n2);
        return n;
    }

    
    
    
    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        BinaryFunctionNode node = new BinaryFunctionNode(this.function);
        for (int i =0; i<this.numberOfChildren;i++)
            node.addChild(children[i].clone(newEval));
        return newEval.checkNodeDefinition(node);
    }

}
