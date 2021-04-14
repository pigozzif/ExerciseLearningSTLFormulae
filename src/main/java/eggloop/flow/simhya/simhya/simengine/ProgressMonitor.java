/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine;
import java.io.PrintStream;

/**
 *
 * @author Luca
 */
public interface ProgressMonitor {

    public void setFinalTime(double finalTime);
    public void setInitialTime(double initialTime);
    public void setTotalSteps(long steps);
    public void start();
    public void setProgress(double currentTime);
    public void nextStep();
    public void stop();
    public double getLastSimulationTimeInMillisecs();
    public double getLastSimulationTimeInSecs();
    public void setSilent(boolean silent);
    public void setPrintEvents(boolean print);
    public void signalEvent(String name, double time);
    public void setPrintStream(PrintStream stream);
    
}
