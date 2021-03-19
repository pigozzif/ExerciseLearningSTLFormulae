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
public class And extends OperatorDefinition {

    public And() {
        super("&&");
    }

    public double compute(double x) {
        throw new UnsupportedOperationException("Operator " + symbol + " is binary");
    }
    public double compute(double x, double y) {
        if ((x == 1.0) && (y == 1.0))
            return 1.0;
        else return 0.0;
    }

    @Override
    public Type getSBMLType() {
        return Type.LOGICAL_AND;
    }

    @Override
    public boolean isLogical() {
        return true;
    }
    
    
    
    
}
