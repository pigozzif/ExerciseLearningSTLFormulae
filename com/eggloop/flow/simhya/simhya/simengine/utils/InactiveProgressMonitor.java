/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.utils;
import java.io.PrintStream;
import com.eggloop.flow.simhya.simhya.simengine.ProgressMonitor;

/**
 *
 * @author Luca
 */
public class InactiveProgressMonitor implements ProgressMonitor {
    private long simTime;

    public double getLastSimulationTimeInMillisecs() {
        return (double)(simTime)/1000000;
    }

    public double getLastSimulationTimeInSecs() {
        return (double)(simTime)/1000000000;
    }

    public void nextStep() {
        
    }

    public void setFinalTime(double finalTime) {
        
    }

    public void setInitialTime(double initialTime) {
        
    }

    public void setProgress(double currentTime) {
        
    }

    public void setTotalSteps(long steps) {
        
    }

    public void start() {
        simTime = System.nanoTime();
    }

    public void stop() {
        simTime = System.nanoTime() - simTime;
    }

    public void setSilent(boolean silent) {
        
    }

    public void setPrintEvents(boolean print) {
        
    }

    public void signalEvent(String name, double time) {
        
    }

    public void setPrintStream(PrintStream stream) {

    }

    


}
