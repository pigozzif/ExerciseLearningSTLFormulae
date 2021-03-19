/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.utils;

import java.util.Arrays;
import com.eggloop.flow.simhya.simhya.simengine.SimulationException;
import com.eggloop.flow.simhya.simhya.GlobalOptions;
import cern.jet.random.engine.RandomEngine;

/**
 *
 * @author Luca
 */
public abstract class RateManager { 
    int[] stochToModelIDs;
    int[] modelToStocIDs;


  
    public RateManager(int numberOfTransitions, int [] stocID) {
        this.stochToModelIDs = Arrays.copyOf(stocID, stocID.length);
        this.modelToStocIDs = new int[numberOfTransitions];
        Arrays.fill(modelToStocIDs, -1);
        for (int i=0;i<stocID.length;i++)
            this.modelToStocIDs[stocID[i]] = i;
    }


    
      
     /**
     * updates the rate with given index. It is care of the calling method
     * to pass the right values of index
     * @param index
     * @param rate
     */
    public abstract void updateRate(int id, double rate);

    /**
     *
     * @return the exit rate of the model
     */
    public abstract double getExitRate();


     /**
     * samples the  index of the transition to be executed next
     * @param rand a random engine
     * @return
     */
    public abstract int sampleNextTransition(RandomEngine rand);



}
