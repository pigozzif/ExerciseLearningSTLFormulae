/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matlab.genetic;

/**
 *
 * @author luca
 */
public class RegularisedLogOddRatioFitness implements FitnessFunction {
    
    
    public double compute(double p1, double p2, int size, double undef1, double undef2, int runs) {
        double undef_penalty = (2*Math.log(runs+1)/Math.sqrt(GeneticOptions.undefined_reference_threshold))
                                    * Math.sqrt(Math.max(undef1, undef2));
        double size_penalty = GeneticOptions.size_penalty_coefficient*size;
//        System.out.println("log odd ratio " + Math.log(p1/p2));
//        System.out.println("size penalty " + size_penalty);
//        System.out.println("undef penalty " + undef_penalty);
        
        return Math.log(p1/p2) -size_penalty;//- undef_penalty;
    }
}
