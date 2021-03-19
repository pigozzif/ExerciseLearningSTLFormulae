/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.hype;
import java.util.ArrayList;

/**
 *
 * @author Luca
 */
public class Function {
    String name;
    ArrayList<String> parameters;
    int arity;
    String definition;

    public Function(String name, ArrayList<String> parameters, String definition) {
        this.name = name;
        this.parameters = parameters;
        this.arity = parameters.size();
        this.definition = definition;
    }

    @Override
    public String toString() {
        String s = "function " + this.getFullDefinition() + ";";
        return s;
    }


    public String getFullDefinition() {
        String s = "";
        s += name + "(";
        for (int i=0;i<this.parameters.size();i++)
            s += (i>0 ? "," : "") + parameters.get(i);
        s += ") = " + definition;
        return s;
    }

}
