/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval.function;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.Exponential;
import cern.jet.random.Normal;
import cern.jet.random.Gamma;
import cern.jet.random.Beta;
import cern.jet.random.Binomial;
import cern.jet.random.HyperGeometric;
import cern.jet.random.Poisson;
import umontreal.iro.lecuyer.probdist.GeometricDist;
import umontreal.iro.lecuyer.probdist.WeibullDist;
import umontreal.iro.lecuyer.probdist.NormalDistQuick;

/**
 *
 * @author Luca
 */
public class RandomGenerator {
    private static final java.util.Random randSeeder;
    private static final MersenneTwister64 rand;
    private static final Exponential exp;
    private static final Normal norm;
    private static final Gamma gamma;
    private static final Beta beta;
    private static final Binomial bin;
    private static final HyperGeometric hyper;
    private static final Poisson poisson;

    static {
        randSeeder = new java.util.Random(System.currentTimeMillis());
        rand = new MersenneTwister64(randSeeder.nextInt());
        exp = new Exponential(1.0, rand);
        norm = new Normal(0.0,1.0,rand);
        gamma = new cern.jet.random.Gamma(1.0, 1.0, rand);
        beta = new cern.jet.random.Beta(1.0, 1.0, rand);
        bin = new cern.jet.random.Binomial(1, 0.5, rand);
        hyper = new cern.jet.random.HyperGeometric(1, 1, 1, rand);
        poisson = new cern.jet.random.Poisson(1.0, rand);
    }

    public static double nextUniform() {
        return rand.nextDouble();
    }
    
    public static double nextUniform(double x) {
        return x * rand.nextDouble();
    }
    
    public static double nextUniform(double x1, double x2) {
        return x1 +  rand.nextDouble()  * (x2-x1);
    }

    public static double nextExponential(double lambda) {
        return exp.nextDouble(lambda);
    }
    
    public static double nextNormal() {
        return norm.nextDouble(0.0, 1.0);
    }
    
    public static double nextNormal(double mean) {
        return norm.nextDouble(mean, 1.0);
    }

    public static double nextNormal(double mean, double stdDev) {
        return norm.nextDouble(mean, stdDev);
    }
    
    public static double nextWeibull(double shape, double scale, double location) {
        return WeibullDist.inverseF(shape, scale, location, rand.nextDouble());
    }
    
    public static double nextWeibull(double shape, double scale) {
        return WeibullDist.inverseF(shape, scale, 0, rand.nextDouble());
    }
    
    public static int nextGeometric(double p) {
        return GeometricDist.inverseF(p, rand.nextDouble());
    }
    
    public static double nextGamma(double shape, double rate) {
        return gamma.nextDouble(shape, rate);
    }
    
    public static double nextBeta(double alphaPar, double betaPar) {
        return beta.nextDouble(alphaPar, betaPar);
    }

    public static int nextBinomial(double N, double p) {
        return bin.nextInt((int)N, p);
    }
    
    /**
     * Returns an hypergeometric distributed random number
     * @param N the size of the population
     * @param k number of successes
     * @param n number of draws
     * @return 
     */
    public static int nextHyperGeometric(double N, double k, double n) {
        return hyper.nextInt((int)N, (int)k, (int)n);
    }
    
    public static int nextPoisson(double lambda) {
        return poisson.nextInt(lambda);
    }
    
    public static double normalDist(double x) {
        return NormalDistQuick.cdf01(x);
    }
    
    public static double normalDist(double x,double mu,double sigma) {
        return NormalDistQuick.cdf(x, mu, sigma);
    }


    
}
