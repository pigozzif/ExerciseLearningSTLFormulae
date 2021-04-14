/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.hype;

/**
 *
 * @author Luca
 */
public class ExpressionVariable {
    String name;
    String expression;

    public ExpressionVariable(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

     @Override
    public String toString() {
        return "expression "  + name + " = " + expression + ";";
    }
}
