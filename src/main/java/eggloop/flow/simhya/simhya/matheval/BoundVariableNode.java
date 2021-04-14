/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matheval;

import java.util.ArrayList;
import eggloop.flow.simhya.simhya.utils.RandomGenerator;


/**
 *
 * @author luca
 */
public class BoundVariableNode extends VariableNode {
    String owner;
    int uniqueId;
    

    public BoundVariableNode(int varIndex, String name, int uniqueId) {
        super(varIndex, name);
        //owner here is just a random number to avoid name clashes between local variables
        //and global variables.
        this.uniqueId =  uniqueId;
        this.owner = "";
    }
    
    public BoundVariableNode(int varIndex, String name, String owner) {
        super(varIndex, name);
        this.owner = owner;
        this.uniqueId = 0;
    }
    
     @Override
    public void getListOfVariables(ArrayList<Integer> list) {
         
    }

    
     
     

    @Override
    void computeValue() {
        throw new EvalException("Unsupported operation by local variable!");
    }

    @Override
    void computeValue(SymbolArray varReference) {
        throw new EvalException("Unsupported operation by local variable!");
    }

    @Override
    void computeValue(int evaluationCode) {
        throw new EvalException("Unsupported operation by local variable!");
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        throw new EvalException("Unsupported operation by local variable!");
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        value = localVariables.values[varIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        value = localVariables.values[varIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        value = localVariables.values[varIndex];
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        value = localVariables.values[varIndex];
    }
    
    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }
     
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        BoundVariableNode newNode = new BoundVariableNode(this.varIndex,this.name,this.uniqueId);
        newNode.varReference = this.varReference;
        newNode.owner = this.owner;
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    void updateLocalVariableIds(SymbolArray localVars) {
        this.varIndex = localVars.getSymbolId(name);
    }

    @Override
    String getExpressionString(boolean fullNumberRepresentation) {
        if (!owner.isEmpty())
            return this.name + "$" + this.owner;
        else
            return this.name + "$" + this.uniqueId;
    }
    
    
    @Override
    void computeContinuousChangeStatus() {
        throw new EvalException("Unsupported operation by local variable!");
    }

    @Override
    void computeContinuousChangeStatus(SymbolArray varReference) {
        throw new EvalException("Unsupported operation by local variable!");
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        this.canChangeContinuously = localVariables.getContinuousEvolutionStatus(varIndex);
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        this.canChangeContinuously = localVariables.getContinuousEvolutionStatus(varIndex);
    }

    @Override
    boolean containsGlobalVariable(String variable) {
        return false;
    }

    @Override
    boolean containsBoundVariable(String boundVar) {
        return boundVar.equals(name);
    }

    @Override
    String toJavaCode() {
        return "args[" + this.varIndex + "]";
    }
    

    @Override
    String toMatlabCode() {
        return "arg_" + (this.varIndex+1);
    }
    
    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        BoundVariableNode node = new BoundVariableNode(this.varIndex,this.name,this.owner);
        node.uniqueId = this.uniqueId;
        return newEval.checkNodeDefinition(node);
    }
    

}
