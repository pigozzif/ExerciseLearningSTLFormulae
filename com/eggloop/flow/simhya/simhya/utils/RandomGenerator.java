/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.utils;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import cern.jet.random.Normal;
import cern.jet.random.Distributions;
import java.util.ArrayList;

/**
 *
 * @author Luca
 */
public class RandomGenerator {
    private static final java.util.Random randSeeder;
    private static MersenneTwister64 rand;
    private static final Normal norm;
    private static boolean forceSeed;
    private static int seed; 

    static {
        randSeeder = new java.util.Random(System.currentTimeMillis());
        seed = randSeeder.nextInt();
        rand = new MersenneTwister64(seed);
        norm = new Normal(0.0,1.0,rand);
        forceSeed = false;
    }
    
    public static void forceSeed(boolean status) {
        forceSeed = status;
    }
    
    public static void resetSeed() {
        seed = randSeeder.nextInt();
        rand = new MersenneTwister64(seed);
    }
    
    
    public static void setSeed(int s) {
        seed = s;
        rand = new MersenneTwister64(seed);
    }

    public static double nextDouble() {
        return rand.nextDouble();
        
    }
    
    public static double nextDouble(double x1, double x2) {
        return x1 + (x2-x1)*rand.nextDouble();
    }

    public static boolean flipCoin(double p) {
        return rand.nextDouble() < p;
    }
    
    public static int nextInt() {
        return Math.abs(rand.nextInt());
    }

    public static int nextInt(int n) {
        int i = Math.abs(rand.nextInt()) % n;
        //System.out.println("Generating a random integer <= " + n + ": " + i);
        return i;
    }

    public static long nextLong() {
        return rand.nextLong();
    }

    public static double expDist(double lambda, RandomEngine r) {
        return -1*(1/lambda)*Math.log(r.nextDouble());
    }

    public static double nextExpDist(double lambda) {
        return -1*(1/lambda)*Math.log(rand.nextDouble());
    }
    
    public static double nextNormal(double mean, double std) {
        return norm.nextDouble(mean, std);
    }
    
    public static int nextGeometric(double prob) {
        return Distributions.nextGeometric(prob, rand);
    }

    public static RandomEngine getNewRandomGenerator() {
        if (forceSeed)
            return new MersenneTwister64(seed);
        else
            return new MersenneTwister64(randSeeder.nextInt());
    }

     /**
     * samples from a given distribution described by a vector of non-negative weights.
     * No control is done on non-negativity of the array.
     * @param probDist a vector of non-negative weights
     * @param r a random engine
     * @return the index of the element chosen.
     */
    public static int sample(double[] probDist, RandomEngine r) {
        double sum = 0;
        for (int i=0;i<probDist.length;i++)
            sum += probDist[i];
        return sample(probDist,sum,r);
    }

     /**
     * samples from a given distribution described by a vector of non-negative weights.
     * No control is done on non-negativity of the array.
     * @param probDist a vector of non-negative weights
     * @return the index of the element chosen.
     */
    public static int sample(double[] probDist) {
        double sum = 0;
        for (int i=0;i<probDist.length;i++)
            sum += probDist[i];
        return sample(probDist,sum,rand);
    }

    /**
     * samples from a given distribution described by a vector of non-negative weights.
     * No control is done on non-negativity of the array.
     * @param probDist a vector of non-negative weights
     * @param sum the sum of all weights
     * @param r a random engine
     * @return the index of the element chosen.
     */
    public static int sample(double[] probDist, double sum, RandomEngine r) {
        double p = r.nextDouble() * sum;
        double s = 0;
        for (int i=0;i<probDist.length;i++)  {
            s += probDist[i];
            if (p <= s)
                return i;
        }
        throw new RuntimeException("Failed to sample from probDist: " + java.util.Arrays.toString(probDist) + ""
                + " with sum " + sum);
    }

     /**
     * samples from a given distribution described by a vector of non-negative weights.
     * No control is done on non-negativity of the array.
     * @param probDist a vector of non-negative weights
     * @param sum the sum of all weights
     * @return the index of the element chosen.
     */
    public static int sample(double[] probDist, double sum) {
        return sample(probDist, sum, rand);
    }
    

}
