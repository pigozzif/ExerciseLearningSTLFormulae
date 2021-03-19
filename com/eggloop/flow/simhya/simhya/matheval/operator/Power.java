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
public class Power extends OperatorDefinition {

    public Power() {
        super("^");
    }
    public double compute(double x) {
        throw new UnsupportedOperationException("Operator " + symbol + " is binary");
    }
    public double compute(double x, double y) {
        return Math.pow(x, y);
    }

    @Override
    public Type getSBMLType() {
        return Type.POWER;
    }

    @Override
    public boolean functionInJava() {
        return true;
    }

    @Override
    public String toJavaCode() {
        return "Math.pow";
    }
    
    

}