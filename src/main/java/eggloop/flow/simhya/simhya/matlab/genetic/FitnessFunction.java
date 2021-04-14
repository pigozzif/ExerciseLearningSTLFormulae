/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matlab.genetic;

/**
 *
 * @author luca
 */
public interface FitnessFunction {
    double compute(double p1, double p2, int size, double undef1, double undef2, int runs);
}
