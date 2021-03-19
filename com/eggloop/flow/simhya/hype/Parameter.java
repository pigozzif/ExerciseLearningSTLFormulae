/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.hype;

/**
 *
 * @author Luca
 */
public class Parameter {
    String name;
    String initialValue;

    public Parameter(String name, String value) {
        this.name = name;
        this.initialValue = value;
    }

     @Override
    public String toString() {
        return "param "  + name + " = " + initialValue + ";";
    }
}
