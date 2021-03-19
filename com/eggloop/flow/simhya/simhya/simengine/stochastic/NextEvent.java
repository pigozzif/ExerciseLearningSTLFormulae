/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.stochastic;

/**
 *
 * @author Luca
 */
public class NextEvent {
    int nextTransition;
    double nextTime;
    boolean stop;
    boolean isDelayed;
    boolean deadlock;
    boolean isStochastic;

    public NextEvent() {
        this.isDelayed = false;
        this.stop = false;
        deadlock = false;
        isStochastic = false;
    }
}
