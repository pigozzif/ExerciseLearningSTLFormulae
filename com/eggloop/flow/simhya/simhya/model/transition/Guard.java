/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.transition;


import com.eggloop.flow.simhya.simhya.model.store.Predicate;
import com.eggloop.flow.simhya.simhya.model.store.Store;

import java.util.ArrayList;


/**
 *
 * @author Luca
 */
public class Guard {
    private Predicate predicate;
 

    public Guard(Predicate p) {
        this.predicate = p;
    }

    /**
     * Evaluates the guard according to the current store. 
     * @return the status of the guard
     */
    public boolean evaluate() {
        return this.predicate.evaluate();
    }

    /**
     * get the list of variables that are involved in the guard
     * @return
     */
    public  ArrayList<Integer> getVariableList() {
         return predicate.getVariableList();
    }


    /**
     *
     * @return the guard predicate
     */
    public Predicate getPredicate() {
        return this.predicate;
    }

    protected Guard clone(Store newStore) {
        Guard g = new Guard(this.predicate.clone(newStore));
        return g;
    }

}
