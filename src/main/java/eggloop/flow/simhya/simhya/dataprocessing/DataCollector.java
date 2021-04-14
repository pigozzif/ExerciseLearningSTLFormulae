/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.dataprocessing;
import eggloop.flow.simhya.simhya.dataprocessing.chart.*;
//import simhya.model.store.Store;
//import simhya.model.flat.FlatModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
//import simhya.GlobalOptions;


/**
 *
 * @author Luca
 */
public interface DataCollector {
    
    
    public void setVarsToBeSaved(ArrayList<String> varsToSave);
    public void saveAllVariables();
    public List<String> getNameOfSavedVariables();
    
    public void setPrintConditionByTime(int points, double finalTime);
    public void setPrintConditionByTime(double burnout, int points, double finalTime);
    //public void setPrintConditionByTime(double burnout, double step);
    public void setPrintConditionByTime(double burnout, double step, double finalTime);
   // public void setPrintConditionByStep(int step);
    public void saveLastFullTrajectory();
    public void saveOnlyFinalState();
    
    public void storeWholeTrajectoryData(int runs);
    public void storeFinalStateDataOnly(int runs);
    public void storeStatisticsOnly(int runs);
    public void storeStrategySMC();
    public void automaticStoreStrategy(int runs);
    
    
    public boolean dataNeeded(double nextTime);
    public boolean dataNeeded(double nextTime, long stepNumber);
    public void putData(double nextTime);
    public void putFinalState(double time, long steps);

    public Trajectory getTrajectory(int id);
    public void newTrajectory();
    public Trajectory getLastFullTrajectory();
    public void clearAll();
    public void addTimeTrace(String var, double[] values);
    public void addFinalStateTrace(String var, double value);
    
    public boolean containsStatisticsData();
    public boolean containsTrajectoryData();
    public boolean containsFinalStateData();
    public boolean containsStatisticsTrajectoryDataOnHigherOrderMoments();
    public boolean containsStatisticsFinalStateDataOnHigherOrderMoments();
    public TrajectoryStatistics getTrajectoryStatistics();
    public int getNumberOfTrajectories();
    
    ///from here on, save functions and plt functions. Have to be separated and delegated to different objects
    
    
    public void setVarsToBePrinted(ArrayList<String> vars);
    public void setAllVarsToBePrinted();
    public void setParamsToBePrinted(ArrayList<String> pars);
    public void setNoParamToBePrinted();
        
    public void saveSingleTrajectoryToCSV(int trajectory, String filename);
    public void saveAllTrajectoriesToCSV(String filename, boolean printRunNumber);
    public void saveAllTrajectoriesToCSV(String filename, boolean printRunNumber, boolean append);


    public void plotSingleTrajectory(int id, String filename, PlotFileType target);
    public void plotAllTrajectories(String filename, PlotFileType target);
    public void plotTrajectories(int first, int last, String filename, PlotFileType target);
    public void plotTrajectories(ArrayList<Integer> trajIds, String filename, PlotFileType target);
    public JFrame plotSingleTrajectoryToScreen(int id);
    public JFrame plotAllTrajectoriesToScreen();
    public JFrame plotTrajectoriesToScreen(int first, int last);
    public JFrame plotTrajectoriesToScreen(ArrayList<Integer> trajIds);
    public JPanel plotSingleTrajectoryToPanel(int id);
    public JPanel plotAllTrajectoriesToPanel();
    public JPanel plotTrajectoriesToPanel(int first, int last);
    public JPanel plotTrajectoriesToPanel(ArrayList<Integer> trajIds);
    public JFrame phasePlanePlotToScreen(String var1, String var2, int... ids);
    public JFrame phasePlanePlotToScreen(String var1, String var2);
    public JPanel phasePlanePlotToPanel(String var1, String var2, int... ids);
    public JPanel phasePlanePlotToPanel(String var1, String var2);
    public void phasePlanePlot(String var1, String var2, String filename, PlotFileType target, int... ids);
    public void phasePlanePlot(String var1, String var2, String filename, PlotFileType target);
    public JFrame phaseSpacePlotToScreen(String var1, String var2, String var3, int... ids);
    public JFrame phaseSpacePlotToScreen(String var1, String var2, String var3);
    public JPanel phaseSpacePlotToPanel(String var1, String var2, String var3, int... ids);
    public JPanel phaseSpacePlotToPanel(String var1, String var2, String var3);
    public void phaseSpacePlot(String var1, String var2, String var3, String filename, PlotFileType target, int... ids);
    public void phaseSpacePlot(String var1, String var2, String var3, String filename, PlotFileType target);
  
    

}
