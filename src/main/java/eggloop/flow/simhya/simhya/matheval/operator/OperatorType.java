/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matheval.operator;

/**
 *
 * @author Luca
 */
public enum OperatorType {
    AND, OR, NOT, EQUAL, NOTEQUAL, GREATER, GRETEROREQUAL, LESS, LESSOREQUAL, 
    PLUS, MINUS, MULTIPLY, DIVIDE, POWER, MODULUS;
    
    static OperatorType getType(String x) {
        if (x.equals("&&")) return AND;
        else if (x.equals("||")) return OR;
        else if (x.equals("!")) return NOT;
        
        else if (x.equals("==")) return EQUAL;
        else if (x.equals("!=")) return NOTEQUAL;
        else if (x.equals(">")) return GREATER;
        else if (x.equals(">=")) return GRETEROREQUAL;
        else if (x.equals("<")) return LESS;
        else if (x.equals("<=")) return LESSOREQUAL;
        
        else if (x.equals("+")) return PLUS;
        else if (x.equals("-")) return MINUS;
        else if (x.equals("*")) return MULTIPLY;
        else if (x.equals("/")) return DIVIDE;
        else if (x.equals("^")) return POWER;
        else if (x.equals("%")) return MODULUS;
        return null;
    }
}
