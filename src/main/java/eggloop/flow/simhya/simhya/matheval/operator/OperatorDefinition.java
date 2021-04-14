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
public abstract class OperatorDefinition {
    String symbol;
    OperatorType type;

    public OperatorDefinition(String name) {
        this.symbol = name;
        type = OperatorType.getType(symbol);
    }
    
    public OperatorType getType() {
        return this.type;
    }

    public String getStringRepresentation() {
        return symbol;
    }

    public abstract double compute(double x);
    public abstract double compute(double x, double y);
    
    public abstract Type getSBMLType();
    
    public boolean isStrictInequality() {
        return false;
    }
    
    public boolean isLogical() {
        return false;
    }
    
    public boolean isRelational() {
        return false;
    }
    
    public String toJavaCode() {
        return symbol;
    }
    
    public String toMatlabCode() {
        return symbol;
    }
    
    public boolean functionInJava( ) {
        return false;
    }
}
