/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine;

import com.eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;

/**
 *
 * @author luca
 */
public interface DeterministicSimulator extends Simulator {
    
    
    public long getLastSimulationNumberOfInstantaneousEvents();
    public long getLastSimulationNumberOfTimedEvents();
    
    public void printEvents(boolean print);

    public void setRelativeTolerance(double tol);
    public void setAbsoluteTolerance(double tol);
    public void setMinimumStepSize(double stepSize);
    public void setMaximumStepSize(double stepSize);
    public void setIntegrator(IntegratorType integ);
    public void setMaxErrorForEvents(double maxErrorForEvents);
    public void setMaxIterationforEvents(int maxIterationforEvents);
    public void setMaxStepIncrementForEvents(double maxStepIncrementForEvents);


}
