/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.script;
import java.util.ArrayList;

/**
 *
 * @author Luca
 */
public class ExploredSymbol {
    
    public String name;
    public ArrayList<Double> values;
    public double first;
    public double last;
    public int points;
    public boolean logScale;

    public ExploredSymbol(String name, ArrayList<Double> values) {
        this.name = name;
        this.values = values;
        if (values != null) {
            this.points = values.size();
            this.first = values.get(0);
            this.last = values.get(values.size()-1);
            this.logScale = false;
        }
    }

    public ExploredSymbol(String name, double first, double last, int points, boolean logScale) {
        this.name = name;
        this.first = first;
        this.last = last;
        this.points = points;
        this.values = null;
        this.logScale = logScale;
    }
    
    
    
}
