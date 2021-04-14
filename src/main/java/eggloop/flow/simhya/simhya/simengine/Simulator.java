/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine;

import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;


/**
 * @author Luca
 */
public interface Simulator {


    /**
     * sets the initial time of the simulation
     *
     * @param time
     */
    public void setInitialTime(double time);

    /**
     * Sets the final time of the simulation
     *
     * @param time
     */
    public void setFinalTime(double time);

    //public void setFinalTime(TerminationCondition termination);
    public void setProgressMonitor(ProgressMonitor monitor);

    public ProgressMonitor getProgressMonitor();

    public void useChache(boolean useChache);

    public void initialize();

    public void reinitialize();

    public void resetModel(boolean resetToOriginalParameterValues);

    public void reset(DataCollector dc);

    public void run();

    /**
     * @return the last simulation time in milliseconds
     */
    public double getLastSimulationTimeInSecs();

    public double getLastSimulationTimeInMillisecs();

    public long getLastSimulationSteps();


}
