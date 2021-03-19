/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.matlab;

import java.io.File;
import java.util.ArrayList;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.distribution.NormalDistribution;
import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;


/**
 *
 * @author luca
 */
public class NormalBreathSimulator extends BreathSimulator {
    static public double step = 0.01;
    static public double phase_3_threshold = -8000;
    
    private static MersenneTwister rand = new MersenneTwister();
    //MultivariateNormalDistribution xxxxxxx;
    //vectors containing statistics for the normal breath
    double[] noisesd;
    double[] meanLength;
    double[] slope;
    double[] lenToInt;
    double[] intercept;
    double[] sdLength;
    double[] intToSlo;

    public NormalBreathSimulator(String folder) {
        //loading normal parameters
        noisesd = loadVector(folder + "noisesd", 5);
        meanLength = loadVector(folder + "meanLength", 5);
        slope = loadVector(folder + "slope", 5);
        lenToInt = loadVector(folder + "lenToInt", 4);
        intercept = loadVector(folder + "intercept", 5);
        sdLength = loadVector(folder + "sdLength", 5);
        intToSlo = loadVector(folder + "intToSlo", 2);  
        
    }
    
    
    public double[][] simulate(double tf) {
        ArrayList<Double> time = new ArrayList<Double>();
        ArrayList<Double> phase = new ArrayList<Double>();
        ArrayList<Double> flow = new ArrayList<Double>();
        ArrayList<Double> flow1 = new ArrayList<Double>();
        
        //generating length and fixing coefficients
        int[] duration = new int[5];
        for (int p=0;p<5;p++) {
            duration[p] = 0;
            while (duration[p] <= 0 && meanLength[p] > 0)
                duration[p] = (int)Math.round(normRnd(meanLength[p], sdLength[p]));
        }
        intercept[4] = Math.pow(duration[4],3)*lenToInt[0] + 
                Math.pow(duration[4],2)*lenToInt[1] + duration[4]*lenToInt[2] + lenToInt[3];
        slope[4] =  intToSlo[0]*intercept[4]+intToSlo[1];
        
        //initial point
        double t = 0;
        double f = 0;
        if (!super.finalExpirationPhaseOnly) {
            time.add(t);
            flow.add(f);
            phase.add(1.0);
        }
    
        
        //phases 1 and 2
        int p,j;
        for (p=0;p<=1;p++) {
            for (j=0;j<duration[p];j++) {   
                t += step;
                f = slope[p]*f + intercept[p] + normRnd(0, noisesd[p]);
                if (!super.finalExpirationPhaseOnly) {
                    phase.add(new Double(p+1));
                    time.add(t);
                    flow.add(f);
                }
            }
        }
        
        //phase 3
        p=2;
        while (f > phase_3_threshold) {
            t += step;
            f = slope[p]*f + intercept[p] + normRnd(0,noisesd[p]);   
            if (!super.finalExpirationPhaseOnly) {
                phase.add(new Double(p+1));
                time.add(t);
                flow.add(f);
            }
        }
        
        //phase 4
        p=3;
        for (j=0;j<duration[p];j++) {   
            t += step;
            f = slope[p]*f + intercept[p] + normRnd(0, noisesd[p]);
            if (!super.finalExpirationPhaseOnly) {
                phase.add(new Double(p+1));
                time.add(t);
                flow.add(f);
            }
        }
        
        
        //phase 5
        p=4;
        if (super.finalExpirationPhaseOnly) {
            t = 0;
            phase.add(new Double(p+1));
            time.add(t);
            flow.add(f);
        }
        for (j=0;j<duration[p];j++) {   
            t += step;
            f = slope[p]*f + intercept[p] + normRnd(0, noisesd[p]);
            phase.add(new Double(p+1));
            time.add(t);
            flow.add(f);
            
        }
        
       
        //compute flow1
        int m = flow.size();
        for (j=0;j<flow.size()-1;j++)
            flow1.add(flow.get(j+1) - flow.get(j));
        //remove last point
        time.remove(m-1);
        flow.remove(m-1);
        phase.remove(m-1);
        
        double[][] x;
        if (super.savePhase)
            x = convertToArray(time, phase, flow, flow1);
        else
            x = convertToArray(time, flow, flow1);
        return x;
    }
    
    
    
    
    
    
}
