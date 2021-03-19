/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.transition;

import  com.eggloop.flow.simhya.simhya.model.store.Function;
import java.util.ArrayList;
import  com.eggloop.flow.simhya.simhya.model.store.Store;

/**
 *
 * @author Luca
 */
public class TimedActivation {
    private Function function;


    public TimedActivation(Function function) {
        this.function = function;
    }

   /**
    * Computes the instant of time in which the transition should activate
    * @return
    */
    public double compute() {
        return function.compute();
    }

    /**
     *
     * @return the list of variables that are involved in the activation condition
     */
    public ArrayList<Integer> getVariableList() {
        return function.getVariableList();
    }


    /**
     *
     * @return the timed activation function
     */
    public Function getFunction() {
        return this.function;
    }
    
    
    protected TimedActivation clone(Store newStore) {
        TimedActivation r = new TimedActivation(this.function.clone(newStore));
        return r;
    }


}
