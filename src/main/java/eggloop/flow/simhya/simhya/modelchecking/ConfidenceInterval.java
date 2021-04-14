/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

/**
 *
 * @author luca
 */
public class ConfidenceInterval {
    double lower;
    double upper;

    public ConfidenceInterval(double lower, double upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public double length() {
        return upper-lower;
    }
}

