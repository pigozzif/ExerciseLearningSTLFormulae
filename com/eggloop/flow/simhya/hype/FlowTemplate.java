/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.hype;
import com.eggloop.flow.simhya.simhya.matheval.Expression;
import java.util.ArrayList;

/**
 *
 * @author luca
 */
public class FlowTemplate {
    Expression rate;
    Expression function;
    boolean increment;

    
    public FlowTemplate(Expression rate, Expression function, boolean increment) {
        this.rate = rate;
        this.function = function;
        this.increment = increment;
    }
    
    
    Flow instantiate(ArrayList<String> templateParams, ArrayList<String> replacingVars) {
        String r = rate.substitute(templateParams, replacingVars).toString();
        String f = function.substitute(templateParams, replacingVars).toString();
        return new Flow(r,f,increment);
    }
}
