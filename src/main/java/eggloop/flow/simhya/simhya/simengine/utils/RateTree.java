/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine.utils;
import java.util.ArrayList;
import cern.jet.random.engine.RandomEngine;

/**
 *
 * @author Luca
 */
public class RateTree  extends RateManager {
    private double[] rateTree;
    private int numberOfLeaves;
    private int indexOfFirstLeave;

    public RateTree(int numberOfTransitions, int [] stocID, double [] r) {
        super(numberOfTransitions, stocID);
        
    }

    public void updateRate(int index, double rate) {
        throw new RuntimeException("To be implemented yet");
    }

    public int sampleNextTransition(RandomEngine rand) {
       throw new RuntimeException("To be implemented yet");
    }

     public double getExitRate() {
         throw new RuntimeException("To be implemented yet");
     }

}
