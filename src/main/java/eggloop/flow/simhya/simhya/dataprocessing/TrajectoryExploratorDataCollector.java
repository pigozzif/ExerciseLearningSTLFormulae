/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.dataprocessing;

import eggloop.flow.simhya.simhya.dataprocessing.chart.*;
import eggloop.flow.simhya.simhya.simengine.paramexplore.ParamValueSet;
import eggloop.flow.simhya.simhya.simengine.paramexplore.ParamSetKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.PrintStream;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Luca
 */
public class TrajectoryExploratorDataCollector {
    ArrayList<ParamValueSet> paramSet;
    HashMap<ParamSetKey,Trajectory> data;
    int numberOfSavedVars;
    boolean saveFinalStateOnly;
    ArrayList<String> variablesToShow;
    private double[] valuesToSave;

    private ArrayList<String> variableNames;
    String modelName;
    private ParamSetKey reusableKey;
    public boolean fullLatexDocument = false;

    public TrajectoryExploratorDataCollector(ArrayList<ParamValueSet> paramSet) {
        this.paramSet = paramSet;
        data = new HashMap<ParamSetKey,Trajectory>();
        variablesToShow = null;
        reusableKey = new ParamSetKey();
    }

     public void setupSaveOptions(int savedVars, boolean saveFinalStateOnly) {
        this.numberOfSavedVars = savedVars;
        this.saveFinalStateOnly = saveFinalStateOnly;
    }

    public void setVariableNames(ArrayList<String> vars) {
        this.variableNames = vars;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }


    /**
     * Adds a new data point to the data collection
     * @param t a TrajectoryStatistics
     */
    public void addDataPoint(Trajectory t) {
        ParamSetKey key = new ParamSetKey();
        key.defineFrom(paramSet);
        if (data.containsKey(key))
            throw new DataException("There is already a data point for the current configuration of parameters");
        data.put(key, t);
    }

    Trajectory getDataPoint() {
        reusableKey.defineFrom(paramSet);
        if (!data.containsKey(reusableKey))
            throw new DataException("There is no data point for the current configuration of parameters");
        return data.get(reusableKey);
    }



    /**
     * Sets a subset of variables to be shown, i.e. saved in csv files and plotted
     * in plot types where multiple variables can be dealt with.
     * @param vars
     */
    public void setVariablesToShow(ArrayList<String> vars) {
        this.variablesToShow = vars;
    }
    
    
    public ArrayList<String> getVariableNames() {
        return this.variableNames;
    }
    
    public ArrayList<ParamValueSet> getExploredParamSet() {
        return this.paramSet;
    }

    
    public boolean containsTrajectoryData() {
        return !this.saveFinalStateOnly;
    }
    
    public boolean containsFinalStateData() {
        return true;
    }

    /**
    * Saves the trajectories of each configuration to file.
    * @param filename the file name.
    */
    public void saveTrajectoryAsCSV(String filename) {
        int n;
        if (this.variablesToShow != null) n = variablesToShow.size() + 1; //we save also time
        else n = this.numberOfSavedVars;
        try {
            PrintStream stream = new PrintStream(filename);
            String s = "";
            for (int i=0;i<n;i++)
                s += (i>0 ? "\t\t" : "") + (variablesToShow != null ? ( i > 0 ? variablesToShow.get(i-1) : "time" ) : variableNames.get(i));
            for (int i=0;i<paramSet.size();i++)
                s += "\t\t" + paramSet.get(i).getName() + (!paramSet.get(i).isParameter() ? ".init" : "");
            stream.println(s);
            saveTrajectory(0,stream);
            stream.close();
        } catch (java.io.IOException e) { System.err.println("Error while saving to file"); }
    }

    /**
     * recursively explores all stored configurations and writes the trajectory statistics to stream
     * @param param the param set code
     * @param stream
     */
    private void saveTrajectory(int param,  PrintStream stream) {
        if (param == paramSet.size()) {
            reusableKey.defineFrom(paramSet);
            Trajectory traj = data.get(reusableKey);
            int [] codes = traj.getVarCodes(variablesToShow,false);
            String s = "";
            for (int i=0;i<traj.savedPoints;i++) {
                s += String.format(Locale.UK, "%.6f", traj.data[0][i]);
                for (int j=0;j<codes.length;j++)
                    s += "\t\t" + String.format(Locale.UK, "%.6f", traj.data[codes[j]][i]);
                for (int j=0;j<paramSet.size();j++)
                    s += "\t\t" + String.format(Locale.UK, "%.6f", paramSet.get(j).getCurrentValue());
                s += "\n";
            }
            stream.println(s);
        } else {
            ParamValueSet range = this.paramSet.get(param);
            for (int i=0;i<range.getPoints();i++) {
                range.setCurrentPoint(i);
                saveTrajectory(param+1,stream);
            }

        }
    }

    /**
     * Saves the given state corresponding to the selected time of each configuration.
     * @param time a time point
     * @param filename the file name.
     */
    public void saveStateAsCSV(double time,  String filename) {
        int n;
        if (this.variablesToShow != null) n = variablesToShow.size(); else n = this.numberOfSavedVars;
        try {
            PrintStream stream = new PrintStream(filename);
            String s = "";
            for (int i=0;i<paramSet.size();i++)
                s += (i>0 ? "\t\t" : "") + paramSet.get(i).getName() + (!paramSet.get(i).isParameter() ? ".init" : "");
            for (int i=0;i<n;i++)
                s += "\t\t" + (variablesToShow != null ? variablesToShow.get(i) : variableNames.get(i));
            stream.println(s);
            saveState(0,time,stream);
            stream.close();
        } catch (java.io.IOException e) { System.err.println("Error while saving to file"); }
    }

    /**
     * recursively explores all stored configurations and writes the state to stream
     * @param param
     * @param time
     * @param stream
     */
     private void saveState(int param, double time, PrintStream stream) {
        if (param == paramSet.size()) {
            reusableKey.defineFrom(paramSet);
            Trajectory traj = data.get(reusableKey);
            int k = traj.getIndex(time);
            int [] codes = traj.getVarCodes(variablesToShow,true);
            String s = "";
            for (int i=0;i<paramSet.size();i++)
                s += (i>0?"\t\t":"") + String.format(Locale.UK, "%.6f", paramSet.get(i).getCurrentValue());
            for (int i=0;i<codes.length;i++)
                s += "\t\t" + String.format(Locale.UK, "%.6f", traj.data[codes[i]][k]);
            stream.println(s);
        } else {
            ParamValueSet range = this.paramSet.get(param);
            for (int i=0;i<range.getPoints();i++) {
                range.setCurrentPoint(i);
                saveState(param+1,time,stream);
            }

        }
    }



    /**
    *  Saves the final state of each configuration.
    * @param filename the file name.
    */
    public void saveFinalStateAsCSV(String filename) {
        int n;
        if (this.variablesToShow != null) n = variablesToShow.size(); else n = this.numberOfSavedVars;
        this.valuesToSave = new double[n];
        try {
            PrintStream stream = new PrintStream(filename);
            String s = "";
            for (int i=0;i<paramSet.size();i++)
                s += (i>0 ? "\t\t" : "") + paramSet.get(i).getName() + (!paramSet.get(i).isParameter() ? ".init" : "");
            for (int i=0;i<n;i++)
                s += "\t\t" + (variablesToShow != null ? variablesToShow.get(i) : variableNames.get(i));
            stream.println(s);
            saveFinalState(0,stream);
            stream.close();
        } catch (java.io.IOException e) { System.err.println("Error while saving to file"); }
    }



    /**
     * recursively explores all stored configurations and writes the final state to stream
     * @param param
     * @param type
     * @param stream
     */
    private void saveFinalState(int param,  PrintStream stream) {
        if (param == paramSet.size()) {
            reusableKey.defineFrom(paramSet);
            Trajectory traj = data.get(reusableKey);
            int [] codes = traj.getVarCodes(variablesToShow,true);
            String s = "";
            for (int i=0;i<paramSet.size();i++)
                s += (i>0?"\t\t":"") + String.format(Locale.UK, "%.6f", paramSet.get(i).getCurrentValue());
            for (int i=0;i<codes.length;i++)
                s += "\t\t" + String.format(Locale.UK, "%.6f", traj.finalState[codes[i]]);
            stream.println(s);
        } else {
            ParamValueSet range = this.paramSet.get(param);
            for (int i=0;i<range.getPoints();i++) {
                range.setCurrentPoint(i);
                saveFinalState(param+1,stream);
            }

        }
    }


    /**
     * Plots the selected final state  for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param param the parameter of interest
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @return
     */
    public JFrame plot1ParamFinalStateToScreen(String param,
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        ArrayList<String> varparams = new ArrayList<String>();
        varparams.add(param);
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        if (vars == null) vars = this.variableNames;
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextFinalState2DTrajectory(vars, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextFinalState2DTrajectory(vars, data);
        }
        LineChart chart = new LineChart(trajList,param,"value");
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " exploration of " + param);
        return frame;
    }

    public JPanel plot1ParamFinalStateToPanel(String param,
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        ArrayList<String> varparams = new ArrayList<String>();
        varparams.add(param);
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        if (vars == null) vars = this.variableNames;
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextFinalState2DTrajectory(vars, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextFinalState2DTrajectory(vars, data);
        }
        LineChart chart = new LineChart(trajList,param,"value");
        JPanel frame = chart.getPanel();
        return frame;
    }

    /**
     * Plots to a file the selected final state for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param param the parameter of interest
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @param filename the name of the file
     * @param filetype the type of the file
     */
     public void plot1ParamFinalState(String param,
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues,
                                   String filename, PlotFileType filetype) {
        ArrayList<String> varparams = new ArrayList<String>();
        varparams.add(param);
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        if (vars == null) vars = this.variableNames;
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextFinalState2DTrajectory(vars,data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextFinalState2DTrajectory(vars,  data);
        }
        LineChart chart = new LineChart(trajList,param,"value");
        chart.fullLatexDocument = this.fullLatexDocument;
        chart.saveToFile(filename,filetype);
    }


     /**
      * Plots a family of phase plane curves for the two given variables.
      * The family is determined by the fixed parameters.
      * @param var1
      * @param var2
      * @param fixedParams
      * @param fixedParamsValues
      * @return
      */
     public JFrame plotParamPhasePlaneTrajectoriesToScreen(String var1, String var2,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        ArrayList<String> varparams = new ArrayList<String>();
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextPhasePlaneTrajectory(var1, var2, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhasePlaneTrajectory(var1, var2,  data);
        }
        Line2DChart chart = new Line2DChart(trajList,var1,var2);
        chart.removeLegend();
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + ";  phase plane trajectories of " + var1 + "  " + var2);
        return frame;
    }
     
     public JPanel plotParamPhasePlaneTrajectoriesToPanel(String var1, String var2,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        ArrayList<String> varparams = new ArrayList<String>();
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextPhasePlaneTrajectory(var1, var2, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhasePlaneTrajectory(var1, var2,  data);
        }
        Line2DChart chart = new Line2DChart(trajList,var1,var2);
        chart.removeLegend();
        JPanel frame = chart.getPanel();
        return frame;
    }


     /**
      * Plots a family of phase space curves for the three given variables.
      * The family is determined by the fixed parameters.
      * @param var1
      * @param var2
      * @param fixedParams
      * @param fixedParamsValues
      * @return
      */
     public JFrame plotParamPhaseSpaceTrajectoriesToScreen(String var1, String var2, String var3,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        ArrayList<String> varparams = new ArrayList<String>();
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot3DTrajectory> trajList = new ArrayList<Plot3DTrajectory> ();
        ArrayList<Plot3DTrajectory> t = processor.getNextPhaseSpaceTrajectory(var1, var2, var3, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhaseSpaceTrajectory(var1, var2, var3, data);
        }
        Line3DChart chart = new Line3DChart(trajList,var1,var2,var3);
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + ";  phase space trajectories of " + var1 + "  " + var2 + "  " + var3);
        return frame;
    }
     
    public JPanel plotParamPhaseSpaceTrajectoriesToPanel(String var1, String var2, String var3,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        ArrayList<String> varparams = new ArrayList<String>();
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot3DTrajectory> trajList = new ArrayList<Plot3DTrajectory> ();
        ArrayList<Plot3DTrajectory> t = processor.getNextPhaseSpaceTrajectory(var1, var2, var3, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhaseSpaceTrajectory(var1, var2, var3, data);
        }
        Line3DChart chart = new Line3DChart(trajList,var1,var2,var3);
        JPanel frame = chart.getPanel();
        return frame;
    }


     /**
     * Plots the selected  state  for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param param the parameter of interest
     * @param vars the vars to plot
      * @param time the time of the state to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @return
     */
    public JFrame plot1ParamStateToScreen(String param, double time,
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        if (this.saveFinalStateOnly)
            throw new DataException("Trajectories have not been saved.");
        ArrayList<String> varparams = new ArrayList<String>();
        varparams.add(param);
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        if (vars == null) vars = this.variableNames;
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextState2DTrajectory(vars, time, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextState2DTrajectory(vars, time, data);
        }
        LineChart chart = new LineChart(trajList,param,"value");
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " exploration of " + param);
        return frame;
    }

    public JPanel plot1ParamStateToPanel(String param, double time,
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        if (this.saveFinalStateOnly)
            throw new DataException("Trajectories have not been saved.");
        ArrayList<String> varparams = new ArrayList<String>();
        varparams.add(param);
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        if (vars == null) vars = this.variableNames;
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextState2DTrajectory(vars, time, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextState2DTrajectory(vars, time, data);
        }
        LineChart chart = new LineChart(trajList,param,"value");
        JPanel frame = chart.getPanel();
        return frame;
    }

    

    /**
     * Plots to a file the selected state for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param param the parameter of interest
     * @param vars the vars to plot
     * @param time the time of the state to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @param filename the name of the file
     * @param filetype the type of the file
     */
     public void plot1ParamState(String param,
                                   ArrayList<String> vars, double time,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues,
                                   String filename, PlotFileType filetype) {
         if (this.saveFinalStateOnly)
            throw new DataException("Trajectories have not been saved.");
        ArrayList<String> varparams = new ArrayList<String>();
        varparams.add(param);
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        if (vars == null) vars = this.variableNames;
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextState2DTrajectory(vars, time, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextState2DTrajectory(vars, time, data);
        }
        LineChart chart = new LineChart(trajList,param,"value");
        chart.fullLatexDocument = this.fullLatexDocument;
        chart.saveToFile(filename,filetype);
    }

     /**
     * Plots trajectories  for the given vars,
     * holding explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @return
     */
    public JFrame plotTrajectoriesToScreen(ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        if (this.saveFinalStateOnly)
            throw new DataException("Trajectories have not been saved.");
        ArrayList<String> varparams = new ArrayList<String>();
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        if (vars == null) vars = this.variableNames;
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextTimed2DTrajectory(vars, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextTimed2DTrajectory(vars, data);
        }
        LineChart chart = new LineChart(trajList,"time","value");
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " time traces.");
        return frame;
    }

    public JPanel plotTrajectoriesToPanel(ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        if (this.saveFinalStateOnly)
            throw new DataException("Trajectories have not been saved.");
        ArrayList<String> varparams = new ArrayList<String>();
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        if (vars == null) vars = this.variableNames;
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextTimed2DTrajectory(vars, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextTimed2DTrajectory(vars, data);
        }
        LineChart chart = new LineChart(trajList,"time","value");
        JPanel frame = chart.getPanel();
        return frame;
    }
    

    /**
     * Plots to a file trajectories  for the given vars,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @param filename the name of the file
     * @param filetype the type of the file
     */
     public void plotTrajectories(ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues,
                                   String filename, PlotFileType filetype) {
         if (this.saveFinalStateOnly)
            throw new DataException("Trajectories have not been saved.");
        ArrayList<String> varparams = new ArrayList<String>();
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        if (vars == null) vars = this.variableNames;
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextTimed2DTrajectory(vars, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextTimed2DTrajectory(vars, data);
        }
        LineChart chart = new LineChart(trajList,"time","value");
        chart.fullLatexDocument = this.fullLatexDocument;
        chart.saveToFile(filename,filetype);
    }


//     /**
//     * Plots the selected final state for the given vars versus a parameter,
//     * holding other explored parameter fixed to the specified values. If more than one value is
//     * passed for a fixed parameter, a trajectory for each such value is plotted.
//     * @param param the parameter of interest
//     * @param vars the vars to plot
//     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
//     * from param) are set to their first value.
//     * @param fixedParamsValues the values of fixed parameters
//     * @return
//     */
//    public JFrame plot2ParamFinalStateAsSurface(String param1, String param2,
//                                   String var,
//                                   ArrayList<String> fixedParams,
//                                   ArrayList<Double> fixedParamsValues ) {
//        ArrayList<String> varParams = new ArrayList<String>();
//        varParams.add(param1);
//        varParams.add(param2);
//        ArrayList<ArrayList<Double>> fixedParamsValuesList = new ArrayList<ArrayList<Double>>();
//        if (fixedParams != null) {
//            for (Double d : fixedParamsValues) {
//                ArrayList<Double> list = new ArrayList<Double>();
//                list.add(d);
//                fixedParamsValuesList.add(list);
//            }
//        } else fixedParams = new ArrayList<String>();
//        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
//                varParams,fixedParams,fixedParamsValuesList);
//        processor.reset();
//        Plot2DSurface traj = processor.getFinalState2DSurface(var,  data);
//        SurfaceChart chart = new SurfaceChart(traj,param1,param2);
//        JFrame frame = chart.show();
//        frame.setTitle("SimHyA: " + this.modelName + " exploration of " + var
//                 + " versus " + param1 + "--" + param2);
//        return frame;
//    }

    
    public JPanel plot2ParamFinalStateAsSurfaceInPanel(String param1, String param2,
                                   String var,
                                   ArrayList<String> fixedParams,
                                   ArrayList<Double> fixedParamsValues ) {
        ArrayList<String> varParams = new ArrayList<String>();
        varParams.add(param1);
        varParams.add(param2);
        ArrayList<ArrayList<Double>> fixedParamsValuesList = new ArrayList<ArrayList<Double>>();
        if (fixedParams != null) {
            for (Double d : fixedParamsValues) {
                ArrayList<Double> list = new ArrayList<Double>();
                list.add(d);
                fixedParamsValuesList.add(list);
            }
        } else fixedParams = new ArrayList<String>();
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varParams,fixedParams,fixedParamsValuesList);
        processor.reset();
        Plot2DSurface traj = processor.getFinalState2DSurface(var,  data);
        SurfaceChart chart = new SurfaceChart(traj,param1,param2);
        JPanel frame = chart.getPanel();
        return frame;
    }

    
    
     /**
     * Plots the selected final state  for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param param the parameter of interest
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @return
     */
//    public JFrame plot2ParamFinalStateAsColorMap(String param1, String param2,
//                                   String var,
//                                   ArrayList<String> fixedParams,
//                                   ArrayList<Double> fixedParamsValues ) {
//        ArrayList<String> varParams = new ArrayList<String>();
//        varParams.add(param1);
//        varParams.add(param2);
//        ArrayList<ArrayList<Double>> fixedParamsValuesList = new ArrayList<ArrayList<Double>>();
//        if (fixedParams != null) {
//            for (Double d : fixedParamsValues) {
//                ArrayList<Double> list = new ArrayList<Double>();
//                list.add(d);
//                fixedParamsValuesList.add(list);
//            }
//        } else fixedParams = new ArrayList<String>();
//        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
//                varParams,fixedParams,fixedParamsValuesList);
//        processor.reset();
//        Plot2DSurface traj = processor.getFinalState2DSurface(var, data);
//        ColorMapChart chart = new ColorMapChart(traj,param1,param2);
//        JFrame frame = chart.show();
//        frame.setTitle("SimHyA: " + this.modelName + " exploration of " + var
//                 + " versus " + param1 + "--" + param2);
//        return frame;
//    }

//    public JPanel plot2ParamFinalStateAsColorMapInPanel(String param1, String param2,
//                                   String var,
//                                   ArrayList<String> fixedParams,
//                                   ArrayList<Double> fixedParamsValues ) {
//        ArrayList<String> varParams = new ArrayList<String>();
//        varParams.add(param1);
//        varParams.add(param2);
//        ArrayList<ArrayList<Double>> fixedParamsValuesList = new ArrayList<ArrayList<Double>>();
//        if (fixedParams != null) {
//            for (Double d : fixedParamsValues) {
//                ArrayList<Double> list = new ArrayList<Double>();
//                list.add(d);
//                fixedParamsValuesList.add(list);
//            }
//        } else fixedParams = new ArrayList<String>();
//        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
//                varParams,fixedParams,fixedParamsValuesList);
//        processor.reset();
//        Plot2DSurface traj = processor.getFinalState2DSurface(var, data);
//        ColorMapChart chart = new ColorMapChart(traj,param1,param2);
//        JPanel frame = chart.getPanel();
//        return frame;
//    }

    
    /**
      * Plots a family of phase plane curves for the two given variables.
      * The family is determined by the fixed parameters.
      * @param var1
      * @param var2
      * @param fixedParams
      * @param fixedParamsValues
      * @param filename the name of the file
      * @param filetype the type of the file
      * @return
      */
     public void plotParamPhasePlaneTrajectories(String var1, String var2,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues,
                                   String filename, PlotFileType filetype) {
        ArrayList<String> varparams = new ArrayList<String>();
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot2DTrajectory> trajList = new ArrayList<Plot2DTrajectory> ();
        ArrayList<Plot2DTrajectory> t = processor.getNextPhasePlaneTrajectory(var1, var2, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhasePlaneTrajectory(var1, var2,  data);
        }
        LineChart chart = new LineChart(trajList,var1,var2);
        chart.removeLegend();
        chart.saveToFile(filename, filetype);
    }

     /**
      * Plots a family of phase space curves for the three given variables.
      * The family is determined by the fixed parameters.
      * @param var1
      * @param var2
      * @param var3
      * @param fixedParams
      * @param fixedParamsValues
      * @param filename the name of the file
      * @param filetype the type of the file
      * @return
      */
     public void plotParamPhaseSpaceTrajectories(String var1, String var2, String var3,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues,
                                   String filename, PlotFileType filetype) {
        ArrayList<String> varparams = new ArrayList<String>();
        if (fixedParams == null) {
            fixedParams = new ArrayList<String>();
            fixedParamsValues = new ArrayList<ArrayList<Double>>();
        }
        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
                varparams,fixedParams,fixedParamsValues);
        processor.reset();
        ArrayList<Plot3DTrajectory> trajList = new ArrayList<Plot3DTrajectory> ();
        ArrayList<Plot3DTrajectory> t = processor.getNextPhaseSpaceTrajectory(var1, var2, var3, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhaseSpaceTrajectory(var1, var2, var3, data);
        }
        Line3DChart chart = new Line3DChart(trajList,var1,var2,var3);
        chart.saveToFile(filename, filetype);
    }


     /**
     * Plots the selected final state for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param param the parameter of interest
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
      * @param filename the name of the file
      * @param filetype the type of the file
     * @return
     */
//    public void plot2ParamFinalStateAsSurface(String param1, String param2,
//                                   String var,
//                                   ArrayList<String> fixedParams,
//                                   ArrayList<Double> fixedParamsValues,
//                                   String filename, PlotFileType filetype) {
//        ArrayList<String> varParams = new ArrayList<String>();
//        varParams.add(param1);
//        varParams.add(param2);
//        ArrayList<ArrayList<Double>> fixedParamsValuesList = new ArrayList<ArrayList<Double>>();
//        if (fixedParams != null) {
//            for (Double d : fixedParamsValues) {
//                ArrayList<Double> list = new ArrayList<Double>();
//                list.add(d);
//                fixedParamsValuesList.add(list);
//            }
//        } else fixedParams = new ArrayList<String>();
//        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
//                varParams,fixedParams,fixedParamsValuesList);
//        processor.reset();
//        Plot2DSurface traj = processor.getFinalState2DSurface(var,  data);
//        SurfaceChart chart = new SurfaceChart(traj,param1,param2);
//        chart.saveToFile(filename, filetype);
//    }
//
//
//     /**
//     * Plots the selected final state  for the given vars versus a parameter,
//     * holding other explored parameter fixed to the specified values. If more than one value is
//     * passed for a fixed parameter, a trajectory for each such value is plotted.
//     * @param param the parameter of interest
//     * @param vars the vars to plot
//     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
//     * from param) are set to their first value.
//     * @param fixedParamsValues the values of fixed parameters
//      * @param filename the name of the file
//      * @param filetype the type of the file
//     * @return
//     */
//    public void plot2ParamFinalStateAsColorMap(String param1, String param2,
//                                   String var,
//                                   ArrayList<String> fixedParams,
//                                   ArrayList<Double> fixedParamsValues,
//                                   String filename, PlotFileType filetype) {
//        ArrayList<String> varParams = new ArrayList<String>();
//        varParams.add(param1);
//        varParams.add(param2);
//        ArrayList<ArrayList<Double>> fixedParamsValuesList = new ArrayList<ArrayList<Double>>();
//        if (fixedParams != null) {
//            for (Double d : fixedParamsValues) {
//                ArrayList<Double> list = new ArrayList<Double>();
//                list.add(d);
//                fixedParamsValuesList.add(list);
//            }
//        } else fixedParams = new ArrayList<String>();
//        ExploratorDataProcessor processor = new  ExploratorDataProcessor(this.paramSet,
//                varParams,fixedParams,fixedParamsValuesList);
//        processor.reset();
//        Plot2DSurface traj = processor.getFinalState2DSurface(var, data);
//        ColorMapChart chart = new ColorMapChart(traj,param1,param2);
//        chart.saveToFile(filename, filetype);
//    }

}
