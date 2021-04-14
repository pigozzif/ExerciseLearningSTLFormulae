/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;

import java.util.ArrayList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Species;

/**
 *
 * @author Luca
 */
public abstract class ExpressionNode {
    ExpressionNode[] children;
    double value;
    int numberOfChildren;
    private int childrenAdded;
    int evaluationCode;
    boolean random;
    boolean canChangeContinuously;

    
    public ExpressionNode(int numberOfChildren) {
        children = new ExpressionNode[numberOfChildren];
        value = 0.0;
        this.numberOfChildren = numberOfChildren;
        childrenAdded = 0;
        evaluationCode = 0;
        random = false;
        canChangeContinuously = false;
        
    }
    /**
     * Adds a children node
     * @param node
     */
    public void addChild(ExpressionNode node) {
        if (childrenAdded >= numberOfChildren)
            throw new EvalException("Cannot add another children.");
        children[childrenAdded++] = node;
        random = random || node.random;
    }
    /**
     * Adds a child node in the specified position
     * @param node
     * @param position
     */
    public void addChild(ExpressionNode node, int position) {
        if (position >= numberOfChildren)
            throw new EvalException("Child position " + position + " out of bounds");
        children[position] = node;
    }

    public boolean isLogicalExpression() {
        return false;
    }
    
    public boolean isStrictInequality() {
        return false;
    }

    public String getExpressionString() {
        return getExpressionString(false);
    }


    public void getListOfVariables(ArrayList<Integer> list) {
        for (int i=0;i<this.numberOfChildren;i++)
            children[i].getListOfVariables(list);
    }

    public boolean isNumericConstant() {
        return false;
    }
    
    public boolean isZero() {
        return false;
    }
    
    public boolean isOne() {
        return false;
    }

    /**
     * this function checks whether the expression of this node can be converted
     * to a numerical one.
     * It firsts checks if it is a logical expression, and then, if it is
     * a relational expression, it checks whether it is a <,<=,>=,> one.
     *
     * @return true if the expression rooted in thsi node can be converted
     * from a logical expression to a numerical one.
     */
    public boolean canBeConvertedToNumericalExpression() {
        return false;
    }

    /**
     * Converts a logical expression into a numerical one, where a negative
     * value means that the logical expression is false, and a positive
     * value means that the logical expression in true.
     * This numerical form can be used for event detection purposes.
     * Throws an exception is the expression cannot be converted. Should be called
     * only after {@link #canBeConvertedToNumericalExpression() } method has returned
     * true.
     * @param eval an evaluator
     * @return a new expression node, containing the expression.
     *
     */
    public ExpressionNode convertToNumericalExpression(Evaluator eval) {
        throw new EvalException("Cannot convert into numerical form " +
                "a non-logical expression");
    }
    
    /**
     * Copy the content of the current node to the passed node
     * @param node the node to have its variables replaced.
     */
    void copyContent(ExpressionNode node) {
        node.children = java.util.Arrays.copyOf(children, numberOfChildren);
        node.childrenAdded = this.childrenAdded;
        node.evaluationCode = this.evaluationCode;
        node.numberOfChildren = this.numberOfChildren;
        node.random = this.random;
        node.value = this.value;
        node.canChangeContinuously = this.canChangeContinuously;
    }

    abstract String getExpressionString(boolean fullNumberRepresentation);
    abstract void computeValue();
    abstract void computeValue(SymbolArray varReference);
    abstract void computeValue(int evaluationCode);
    abstract void computeValue(SymbolArray varReference, int evaluationCode);
    abstract void computeValueLocal(SymbolArray localVariables);
    abstract void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables);
    abstract void computeValueLocal(SymbolArray localVariables, int evaluationCode);
    abstract void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode);

    
    /**
     * Checks if the function or the predicate can change their value continuously
     * due to the evolution of continuous variables (the continuoity status of a variable
     * is defined in the {@link SymbolArray} object).
     * It relies on the computation of the current value. If the value on the node is
     * not the current one, it may give incorrect results.
     * 
     */
    abstract void computeContinuousChangeStatus();
    abstract void computeContinuousChangeStatus(SymbolArray varReference);
    abstract void computeContinuousChangeStatusLocal(SymbolArray localVariables);
    abstract void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables);
    
    
    public void setOwner(String owner) {
        for (ExpressionNode n : children)
            n.setOwner(owner);
    }
    
    
    void updateLocalVariableIds(SymbolArray localVars) {
        for (ExpressionNode n : children)
            n.updateLocalVariableIds(localVars);
    }
    
    /**
     * Substitues any constant or variable with the specified name with the given 
     * expression
     * @param toSubst the name of the symbol to replace
     * @param exp the expression to substitute to the symbol
     * @param eval the evaluator, needed to avoid duplication of nodes
     * @return a new expression node, if a substitution occurred, or the old
     * node, if nothig has been replaced in the expression rooted in the node.
     */
    ExpressionNode substitute(String toSubst, Expression exp, Evaluator eval) {
        ExpressionNode[] newChildren = new ExpressionNode[children.length];
        for (int i=0;i<children.length;i++)
            newChildren[i] = children[i].substitute(toSubst, exp, eval);
        return this.checkSubstitutedChildren(newChildren, eval);
    }
    
    
    /**
     * Substitues any constant or variable in the list with the specified name with the corresponding 
     * expression in the expression list
     * @param toSubst the name of the symbol to replace
     * @param exp the expression  to substitute to the symbol
     * @param eval the evaluator, needed to avoid duplication of nodes
     * @return a new expression node, if a substitution occurred, or the old
     * node, if nothig has been replaced in the expression rooted in the node.
     */
    ExpressionNode substitute(List<String> toSubst, List<Expression> exp, Evaluator eval) {
        if (toSubst.size() != exp.size())
            throw new EvalException("Names and expression list sizes mismatch");
        ExpressionNode[] newChildren = new ExpressionNode[children.length];
        for (int i=0;i<children.length;i++)
            newChildren[i] = children[i].substitute(toSubst, exp, eval);
        return this.checkSubstitutedChildren(newChildren, eval);
    }
    
    
    private ExpressionNode checkSubstitutedChildren(ExpressionNode[] newChildren, Evaluator eval) {
        //if no subsitution has been done, pointers have not changed.
        boolean newNodeNeeded = false;
        for (int i=0;i<children.length;i++)
            newNodeNeeded = newNodeNeeded || (children[i] != newChildren[i]);
        if (newNodeNeeded) {
            ExpressionNode newNode;
            try {
                newNode = (ExpressionNode)this.clone();
                newNode.children = newChildren;
                newNode = eval.checkNodeDefinition(newNode);
                return newNode;
            } catch (Exception e) {
                throw new EvalException("Error while substituting variables in expression " 
                        + this.getExpressionString());
            }
        } else 
            return this;
    }
    
    /**
     * Converts the espression rooted in the node to MATHML representation
     * using ASTNode of JSBML Library.
     */ 
    abstract ASTNode convertToJSBML();

//    /**
//     * Converts the espression rooted in the node to MATHML representation
//     * using ASTNode of JSBML Library.
//     * Use this version of the function if you want to force 
//     * symbols to be matched explicitly.
//     * @param variables an array with variable objects
//     * @param parameters an array with parameters
//     * @param expressionVars an array with expression variables
//     * @return 
//     */
//    abstract ASTNode convertToJSBML(Species[] variables, Parameter[] parameters, Parameter[] expressionVars);    
 
    
    /**
     * Checks if the expression starting with this node contains a symbol with the given name.
     * @param symbol
     * @return 
     */
    boolean containsSymbol(String symbol) {
        boolean answer = false;
        for (ExpressionNode n : children) {
            answer = answer || n.containsSymbol(symbol);
        }
        return answer;
    }
    
        /**
     * Checks if the expression starting with this node contains a variable with the given name.
     * @param variable
     * @return 
     */
    boolean containsGlobalVariable(String variable) {
        boolean answer = false;
        for (ExpressionNode n : children) {
            answer = answer || n.containsGlobalVariable(variable);
        }
        return answer;
    }
    
        /**
     * Checks if the expression starting with this node contains a constant with the given name.
     * @param constant
     * @return 
     */
    boolean containsConstant(String constant) {
        boolean answer = false;
        for (ExpressionNode n : children) {
            answer = answer || n.containsConstant(constant);
        }
        return answer;
    }
    
        /**
     * Checks if the expression starting with this node contains a symbol with the given name.
     * @param expVar
     * @return 
     */
    boolean containsExpressionVariable(String expVar) {
        boolean answer = false;
        for (ExpressionNode n : children) {
            answer = answer || n.containsExpressionVariable(expVar);
        }
        return answer;
    }
    
        /**
     * Checks if the expression starting with this node contains a bound variable with the given name.
     * @param boundVar
     * @return 
     */
    boolean containsBoundVariable(String boundVar) {
        boolean answer = false;
        for (ExpressionNode n : children) {
            answer = answer || n.containsBoundVariable(boundVar);
        }
        return answer;
    }
    
    abstract String toJavaCode();
    
    abstract String toMatlabCode();
    
    /**
     * returns the node of the derivative w.r.t x
     * @param x
     * @return 
     */
    abstract ExpressionNode differentiate(String x, Evaluator eval);   
    
    
    abstract boolean isDifferentiable(String x);

    
    protected abstract ExpressionNode clone(Evaluator newEval);
    
}
