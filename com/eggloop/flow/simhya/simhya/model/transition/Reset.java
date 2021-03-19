/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.transition;

import  com.eggloop.flow.simhya.simhya.model.ModelException;
import  com.eggloop.flow.simhya.simhya.model.store.Function;
import  com.eggloop.flow.simhya.simhya.model.store.Store;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Luca
 */
public class Reset {
    private AtomicReset[] atomicResets;
    private int numberOfResets;
    

    public Reset() {
         atomicResets = new AtomicReset[5];
         numberOfResets = 0;
    }

    /**
     *
     * @return the list of variables that are updated by the reset.
     */
    public ArrayList<Integer> getVariableList() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i=0;i<numberOfResets;i++)
            list.addAll(atomicResets[i].getUpdatedVariables());
        return list;
    }

    /**
     * applies the updates, modifying the store!
     *
     */
    public void apply(Store store) {
        //computes the new values using old store values
        for (int i=0;i<numberOfResets;i++)
            atomicResets[i].computeNewValues();
        //modifies the store
        for (int i=0;i<numberOfResets;i++)
            atomicResets[i].updateStoreVariables(store.getVariablesReference());
    }


    /**
     * returns true if there is already an atomic reset for the specified variable
     * @param variable
     * @return
     */
    private boolean containsBasicAtomicReset(int variable) {
        if (variable == -1)
            return false;
        for (int i=0;i<numberOfResets;i++)
            if (atomicResets[i].isSimpleResetForVariable(variable))
                return true;
        return false;
    }

    /**
     * Adds a new basic atomic reset. Throws an exception if one tries to add
     * more than one basic atomic reset for the same variable
     * @param variable the variable to be reset
     * @param name the name of the variable
     * @param resetFunction the reset function
     */
    public void addAtomicReset(int variable, String name, Function resetFunction) {
        if (containsBasicAtomicReset(variable))
            throw new ModelException("Reset contains already a rule for variable " + variable);
        AtomicReset r = new BasicAtomicReset(variable, name, resetFunction);
        if (numberOfResets == atomicResets.length)
            atomicResets = Arrays.copyOf(atomicResets, 2*atomicResets.length);
        atomicResets[numberOfResets++] = r;
    }


    /**
     * Adds a basic atomic reset.
     * @param r
     */
    public void addAtomicReset(AtomicReset r) {
        if (r instanceof BasicAtomicReset)
            if (containsBasicAtomicReset(((BasicAtomicReset)r).variable))
                throw new ModelException("Reset contains already a basic rule for variable " +
                        ((BasicAtomicReset)r).variable);
         if (numberOfResets == atomicResets.length)
            atomicResets= Arrays.copyOf(atomicResets, 2*atomicResets.length);
        atomicResets[numberOfResets++] = r;
    }

    /**
     *
     * @return the array of atomic resets
     */
    public AtomicReset[] getAtomicResets() {
        return Arrays.copyOf(atomicResets, numberOfResets);
    }


    protected Reset clone(Store newStore) {
        Reset r =  new Reset();
        for (int i=0;i<numberOfResets;i++)
            r.addAtomicReset(this.atomicResets[i].clone(newStore));    
        return r;
    }


}
