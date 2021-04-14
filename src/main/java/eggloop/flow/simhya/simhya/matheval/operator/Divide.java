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
public class Divide extends OperatorDefinition {

    public Divide() {
        super("/");
    }

    public double compute(double x) {
        throw new UnsupportedOperationException("Operator " + symbol + " is binary");
    }
    public double compute(double x, double y) {
        return x/y;
    }

    @Override
    public Type getSBMLType() {
        return Type.DIVIDE;
    }
    
    

}
