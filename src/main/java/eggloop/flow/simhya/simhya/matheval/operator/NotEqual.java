/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval.operator;

import org.sbml.jsbml.ASTNode.Type;

/**
 *
 * @author Luca
 */
public class NotEqual extends OperatorDefinition {

    public NotEqual() {
        super("!=");
    }

    public double compute(double x) {
        throw new UnsupportedOperationException("Operator " + symbol + " is binary");
    }
    public double compute(double x, double y) {
        if (x != y)
            return 1.0;
        else return 0.0;
    }

    @Override
    public Type getSBMLType() {
        return Type.RELATIONAL_NEQ;
    }
    
    @Override
    public boolean isRelational() {
        return true;
    }

    @Override
    public String toMatlabCode() {
        return "~=";
    }

    
    
}
