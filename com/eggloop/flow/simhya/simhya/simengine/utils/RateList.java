/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.utils;

import java.util.Arrays;
import cern.jet.random.engine.RandomEngine;
import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;
/**
 *
 * @author Luca
 */
public class RateList extends RateManager {
    private double[] rates;
    private double exitRate;
    private int n;

    public RateList(int numberOfTransitions, int [] stocID, double [] r) {
        super(numberOfTransitions, stocID);
        rates = Arrays.copyOf(r, r.length);
        n = rates.length;
        this.exitRate = 0;
        for (int i=0;i<n;i++)
            exitRate += rates[i];
    }
    
   
    //Subtracting positive quantities increases the numerical error
    //trying to sum rate any time the exit rate is needed.
   
    public void updateRate(int id, double rate) {
        int index = modelToStocIDs[id];
        //exitRate += (rate - rates[index]);
        rates[index] = rate;
    }

   
    public int sampleNextTransition(RandomEngine rand) {
        int index = RandomGenerator.sample(rates, exitRate, rand);
        return stochToModelIDs[index];
    }


    public double getExitRate() {
        //added this code, to recompute rate precisely.
        //at any step! This function is called once per step!
        exitRate = 0;
        for (int i=0;i<n;i++)
            exitRate += rates[i];
         return this.exitRate;
     }

}
