/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.utils;
import cern.jet.random.engine.RandomEngine;

/**
 *
 * @author Luca
 */
public interface RateSampler {

    /**
     * updates the rate stored in the position identified by the given index
     * @param index
     * @param rate
     */
    public void updateRate(int index, double rate);

    /**
     * returns the index of the rate sampled according to the current value of rates
     * @param rand
     * @return
     */
    public int sampleNextTransition(RandomEngine rand);


    /**
     * returns the sum of all rates contained
     * @return
     */
    public double getTotalRate();


}
