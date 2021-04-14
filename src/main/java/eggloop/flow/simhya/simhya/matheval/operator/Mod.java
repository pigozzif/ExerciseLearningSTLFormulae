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
public class Mod extends OperatorDefinition {

    public Mod() {
        super("%");
    }

    public double compute(double x) {
        throw new UnsupportedOperationException("Operator " + symbol + " is binary");
    }
    public double compute(double x, double y) {
        return x % y;
    }

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("modulus % is unsupported in JSBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toMatlabCode() {
        return "mod";
    }
    
    

}
