/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.hype;

/**
 *
 * @author Luca
 */
public class Variable {
    String name;
    String initialValue;
   

    public Variable(String name, String value) {
        this.name = name;
        this.initialValue = value;
    }

    @Override
    public String toString() {
        return name + " = " + initialValue + ";";
    }

}
