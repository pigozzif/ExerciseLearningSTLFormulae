/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.transition;

import  eggloop.flow.simhya.simhya.model.store.Function;
import java.util.ArrayList;
import  eggloop.flow.simhya.simhya.model.store.Store;

/**
 *
 * @author Luca
 */
public class Rate {
    private Function function;
  

    public Rate(Function function) {
        this.function = function;
    }

   /**
    * Computes the value of the rate according to the 
    * @return
    */
    public double compute() {
        return function.compute();
    }

    /**
     * 
     * @return the list of variables that are involved in the rate
     */
    public ArrayList<Integer> getVariableList() {
        return function.getVariableList();
    }

    /**
     *
     * @return the rate function
     */
    public Function getFunction() {
        return this.function;
    }

    
    protected Rate clone(Store newStore) {
        Rate r = new Rate(this.function.clone(newStore));
        return r;
    }
    
}
