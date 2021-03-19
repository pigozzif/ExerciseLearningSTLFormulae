/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval;

import com.eggloop.flow.simhya.simhya.matheval.operator.OperatorDefinition;
import com.eggloop.flow.simhya.simhya.matheval.operator.OperatorType;


/**
 * @author Luca
 */
public abstract class OperatorNode extends ExpressionNode {
    protected OperatorDefinition operator;


    public OperatorNode(OperatorDefinition op, int numberOfChildren) {
        super(numberOfChildren);
        this.operator = op;
    }

    String getExpressionString(boolean fullNumberRepresentation) {
        String s = "";
        switch (numberOfChildren) {
            case 1:
                s += operator.getStringRepresentation() + children[0].getExpressionString(false);
                break;
            case 2:
                s += "(" + children[0].getExpressionString(false) + " ";
                s += operator.getStringRepresentation() + " ";
                s += children[1].getExpressionString(false) + ")";
                break;
            default:
                throw new EvalException("Operator node has wrong number of children (" + numberOfChildren + ")");
        }
        return s;
    }

    @Override
    public boolean isLogicalExpression() {
        return operator.isLogical() || operator.isRelational();
    }

    @Override
    public boolean isStrictInequality() {
        return operator.isStrictInequality();
    }


    @Override
    boolean isDifferentiable(String x) {
        if (isLogicalExpression() || this.operator.getType() == OperatorType.MODULUS)
            return false;
        boolean diff = true;
        for (int i = 0; i < this.numberOfChildren; i++)
            diff = diff && children[i].isDifferentiable(x);
        return diff;
    }


    abstract void computeValue();

    abstract void computeValue(SymbolArray varReference);

    abstract void computeValue(int evaluationCode);

    abstract void computeValue(SymbolArray varReference, int evaluationCode);


}
