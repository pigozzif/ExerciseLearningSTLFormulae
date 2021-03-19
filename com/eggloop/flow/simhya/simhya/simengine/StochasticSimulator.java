/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine;

/**
 *
 * @author luca
 */
public interface StochasticSimulator extends Simulator {

    public long getLastSimulationDelayedSteps();
    public long getLastSimulationInstantaneousSteps();
    public long getLastSimulationStochasticSteps();
    public long getLastSimulationTimedSteps();

}
