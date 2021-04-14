/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.transition;

import eggloop.flow.simhya.simhya.model.store.Function;
import eggloop.flow.simhya.simhya.model.store.Store;

/**
 *
 * @author Luca
 */
public class Delay {
    private Function function;
    

    public Delay(Function function) {
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
     * @return the delay function
     */
    public Function getFunction() {
        return this.function;
    }
    
    protected Delay clone(Store newStore) {
        Delay d = new Delay(this.function.clone(newStore));
        return d;
    }

}
