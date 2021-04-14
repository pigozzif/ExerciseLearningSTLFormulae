/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;

import org.sbml.jsbml.ASTNode;

import java.util.ArrayList;

/**
 * @author Luca
 */
public class VariableNode extends SubstitutableNode {
    int varIndex;
    SymbolArray varReference;


    VariableNode(int varIndex, String name) {
        super(0);
        this.varIndex = varIndex;
        this.varReference = null;
        this.name = name;
    }


    public VariableNode(int varIndex, SymbolArray varReference) {
        super(0);
        if (!varReference.checkSymbolIndex(varIndex))
            throw new EvalException("Variable index is wrong");
        this.varIndex = varIndex;
        this.varReference = varReference;
        name = varReference.getName(varIndex);
    }


    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void getListOfVariables(ArrayList<Integer> list) {
        list.add(varIndex);
    }

    @Override
    String getExpressionString(boolean fullNumberRepresentation) {
        return this.name;
    }

    @Override
    void computeValue() {
        super.value = varReference.values[varIndex];
    }

    @Override
    void computeValue(SymbolArray varReference) {
        super.value = varReference.values[varIndex];
    }

    @Override
    void computeValue(int evaluationCode) {
        super.value = varReference.values[varIndex];
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        super.value = varReference.values[varIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        super.value = varReference.values[varIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        super.value = globalVariables.values[varIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        super.value = varReference.values[varIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        super.value = globalVariables.values[varIndex];
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        VariableNode newNode = new VariableNode(this.varIndex, this.name);
        newNode.varReference = this.varReference;
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
        this.canChangeContinuously = varReference.getContinuousEvolutionStatus(varIndex);
    }

    @Override
    void computeContinuousChangeStatus(SymbolArray varReference) {
        this.canChangeContinuously = varReference.getContinuousEvolutionStatus(varIndex);
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        this.canChangeContinuously = varReference.getContinuousEvolutionStatus(varIndex);
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        this.canChangeContinuously = globalVariables.getContinuousEvolutionStatus(varIndex);
    }

    @Override
    boolean containsGlobalVariable(String variable) {
        return variable.equals(name);
    }

    @Override
    String toJavaCode() {
        return "variable[" + this.varIndex + "]";
    }

    @Override
    String toMatlabCode() {
        return "var(" + (this.varIndex + 1) + ")";
    }


    @Override
    ExpressionNode differentiate(String x, Evaluator eval) {
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
        ExpressionNode node = new VariableNode(this.varIndex, newEval.getVariableReference());
        node = newEval.checkNodeDefinition(node);
        return node;
    }


}
