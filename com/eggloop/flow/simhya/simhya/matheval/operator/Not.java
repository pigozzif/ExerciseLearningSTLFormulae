/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval.operator;

import org.sbml.jsbml.ASTNode.Type;

/**
 *
 * @author Luca
 */
public class Not extends OperatorDefinition {

    public Not() {
        super("!");
    }

    public double compute(double x) {
        if ((x == 1.0))
            return 0.0;
        else return 1.0;
    }
    public double compute(double x, double y) {
        throw new UnsupportedOperationException("Operator " + symbol + " is unary");
    }

    @Override
    public Type getSBMLType() {
        return Type.LOGICAL_NOT;
    }
    
    @Override
    public boolean isLogical() {
        return true;
    }

    @Override
    public String toMatlabCode() {
        return "~";
    }
    
    
    
}
