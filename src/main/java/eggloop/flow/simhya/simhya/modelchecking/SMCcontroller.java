/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DTrajectory;
import eggloop.flow.simhya.simhya.modelchecking.mtl.Truth;

/**
 *
 * @author Luca
 */
public interface SMCcontroller extends SMCoutput {
    
    public void addPoint(Truth[] truth);
    public boolean stop();
    public void finalization();
    public boolean isExplorator();
 
    public void setFormula(String formula);
    public void setSignals(ArrayList<Plot2DTrajectory> signals);
    
    public void setCurrent(int c);
    
    
}
