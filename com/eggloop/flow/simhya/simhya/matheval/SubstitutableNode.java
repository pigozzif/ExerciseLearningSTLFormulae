/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.matheval;

import java.util.List;

/**
 * @author luca
 */
public abstract class SubstitutableNode extends ExpressionNode {
    String name;


    public SubstitutableNode(int numberOfChildren) {
        super(numberOfChildren);
    }

    @Override
    ExpressionNode substitute(String toSubst, Expression exp, Evaluator eval) {
        if (name.equals(toSubst))
            return eval.checkNodeDefinition(exp.getRoot());
        else return this;
    }

    @Override
    ExpressionNode substitute(List<String> toSubst, List<Expression> exp, Evaluator eval) {
        if (toSubst.contains(name)) {
            int i = toSubst.indexOf(name);
            return eval.checkNodeDefinition(exp.get(i).getRoot());
        } else return this;
    }

    @Override
    boolean containsSymbol(String symbol) {
        return symbol.equals(name);
    }


}
