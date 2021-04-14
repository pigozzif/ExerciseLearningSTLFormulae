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
public class StatisticsExploratorDataCollector {
    ArrayList<ParamValueSet> paramSet;
    HashMap<ParamSetKey,TrajectoryStatistics> data;
    int numberOfSavedVars;

    //information contained in the trajectory statistics
    boolean saveFinalStateOnly;
    boolean storeTrajectoryDistributionData;
    boolean storeFinalStateDistributionData;

    ArrayList<String> variablesToShow;

    private double[] valuesToSave;
    private ArrayList<String> variableNames;
    String modelName;

    private ParamSetKey reusableKey;

    public boolean fullLatexDocument = false;

    
    public StatisticsExploratorDataCollector(ArrayList<ParamValueSet> paramSet) {
        this.paramSet = paramSet;
        data = new HashMap<ParamSetKey,TrajectoryStatistics>();
        variablesToShow = null;
        reusableKey = new ParamSetKey();
    }

    public void setupSaveOptions(int savedVars, boolean saveFinalStateOnly,
            boolean storeTrajectoryDistributionData,
            boolean storeFinalStateDistributionData) {
        this.numberOfSavedVars = savedVars;
        this.saveFinalStateOnly = saveFinalStateOnly;
        this.storeFinalStateDistributionData = storeFinalStateDistributionData;
        this.storeTrajectoryDistributionData = storeTrajectoryDistributionData;
    }

    public void setVariableNames(ArrayList<String> vars) {
        this.variableNames = vars;
    }
    
    public ArrayList<String> getVariableNames() {
        return this.variableNames;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public ArrayList<ParamValueSet> getExploredParamSet() {
        return this.paramSet;
    }


    /**
     * Adds a new data point to the data collection
     * @param t a TrajectoryStatistics
     */
    public void addDataPoint(TrajectoryStatistics t) {
        ParamSetKey key = new ParamSetKey();
        key.defineFrom(paramSet);
        if (data.containsKey(key))
            throw new DataException("There is already a data point for the current configuration of parameters");
        data.put(key, t);
    }

    TrajectoryStatistics getDataPoint() {
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
    
    
    public boolean containsTrajectoryData() {
        return this.storeTrajectoryDistributionData && !this.saveFinalStateOnly;
    }
    
    public boolean containsFinalStateData() {
        return this.storeFinalStateDistributionData;
    }
    
    public boolean containsTrajectoryStatistics() {
        return !this.saveFinalStateOnly;
    }
    
    


    /**
    *  Saves the given statistics for the whole trajectory of each configuration.
    * @param type the statistics to be saved
    * @param filename the file name.
    */
    public void saveTrajectoryStatisticsAsCSV(StatType type, String filename) {
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
            saveTrajectory(0,type,stream);
            stream.close();
        } catch (java.io.IOException e) { System.err.println("Error while saving to file"); }
    }

    /**
     * recursively explores all stored configurations and writes the trajectory statistics to stream
     * @param param
     * @param type
     * @param stream
     */
    private void saveTrajectory(int param,  StatType type, PrintStream stream) {
        if (param == paramSet.size()) {
            reusableKey.defineFrom(paramSet);
            TrajectoryStatistics stat = data.get(reusableKey);
            int [] codes = stat.getVarCodes(variablesToShow,false);
            String s = "";
            for (int i=0;i<stat.points;i++) {
                s += String.format(Locale.UK, "%.6f", stat.average[0][i]);
                for (int j=0;j<codes.length;j++)
                    s += "\t\t" + String.format(Locale.UK, "%.6f", stat.getStat(type, codes[j], i));
                for (int j=0;j<paramSet.size();j++)
                    s += "\t\t" + String.format(Locale.UK, "%.6f", paramSet.get(j).getCurrentValue());
                s += "\n";
            }
            stream.println(s);
        } else {
            ParamValueSet range = this.paramSet.get(param);
            for (int i=0;i<range.getPoints();i++) {
                range.setCurrentPoint(i);
                saveTrajectory(param+1,type,stream);
            }

        }
    }

    /**
     * Saves the givem statistics for the state corresponding to the selected time of each configuration.
     * @param time a time point
     * @param type the statistics to be saved
     * @param filename the file name.
     */
    public void saveStateStatisticsAsCSV(double time, StatType type, String filename) {
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
            saveState(0,type,time,stream);
            stream.close();
        } catch (java.io.IOException e) { System.err.println("Error while saving to file"); }
    }

    /**
     * recursively explores all stored configurations and writes the state statistics to stream
     * @param param
     * @param type
     * @param time
     * @param stream
     */
     private void saveState(int param,  StatType type, double time, PrintStream stream) {
        if (param == paramSet.size()) {
            reusableKey.defineFrom(paramSet);
            TrajectoryStatistics stat = data.get(reusableKey);
            int k = stat.getIndex(time);
            int [] codes = stat.getVarCodes(variablesToShow,true);
            String s = "";
            for (int i=0;i<paramSet.size();i++)
                s += (i>0?"\t\t":"") + String.format(Locale.UK, "%.6f", paramSet.get(i).getCurrentValue());
            for (int i=0;i<codes.length;i++)
                s += "\t\t" + String.format(Locale.UK, "%.6f", stat.getStat(type, codes[i], k));
            stream.println(s);
        } else {
            ParamValueSet range = this.paramSet.get(param);
            for (int i=0;i<range.getPoints();i++) {
                range.setCurrentPoint(i);
                saveState(param+1,type,time,stream);
            }

        }
    }

    

    /**
    *  Saves the givem statistics for the final state of each configuration.
    * @param type the statistics to be saved
    * @param filename the file name.
    */
    public void saveFinalStateStatisticAsCSV(StatType type, String filename) {
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
            saveFinalState(0,type,stream);
            stream.close();
        } catch (java.io.IOException e) { System.err.println("Error while saving to file"); }
    }


   
    /**
     * recursively explores all stored configurations and writes the final state statistics to stream
     * @param param
     * @param type
     * @param stream
     */
    private void saveFinalState(int param,  StatType type, PrintStream stream) {
        if (param == paramSet.size()) {
            reusableKey.defineFrom(paramSet);
            TrajectoryStatistics stat = data.get(reusableKey);
            stat.getFinalTimeStatistics(type, variablesToShow, valuesToSave);
            String s = "";
            for (int i=0;i<paramSet.size();i++)
                s += (i>0?"\t\t":"") + String.format(Locale.UK, "%.6f", paramSet.get(i).getCurrentValue());
            for (int i=0;i<valuesToSave.length;i++)
                s += "\t\t" + String.format(Locale.UK, "%.6f", valuesToSave[i]);
            stream.println(s);
        } else {
            ParamValueSet range = this.paramSet.get(param);
            for (int i=0;i<range.getPoints();i++) {
                range.setCurrentPoint(i);
                saveFinalState(param+1,type,stream);
            }
                
        }
    }


    /**
     * Plots the selected final state statistics for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param param the parameter of interest
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value. 
     * @param fixedParamsValues the values of fixed parameters 
     * @return
     */
    public JFrame plot1ParamFinalStateStatisticsToScreen(StatType type, String param,
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
        ArrayList<Plot2DTrajectory> t = processor.getNextFinalState2DTrajectory(vars, type, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextFinalState2DTrajectory(vars, type, data);
        }
        LineChart chart = new LineChart(trajList,param,type.toString());
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " exploration of " + param + ", " + type.toString());
        return frame;
    }

    
    public JPanel plot1ParamFinalStateStatisticsToPanel(StatType type, String param,
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
        ArrayList<Plot2DTrajectory> t = processor.getNextFinalState2DTrajectory(vars, type, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextFinalState2DTrajectory(vars, type, data);
        }
        LineChart chart = new LineChart(trajList,param,type.toString());
        JPanel panel = chart.getPanel();
        return panel;
    }
    

    /**
     * Plots to a file the selected final state statistics for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param param the parameter of interest
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters 
     * @param filename the name of the file
     * @param filetype the type of the file
     */
     public void plot1ParamFinalStateStatistics(StatType type, String param,
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
        ArrayList<Plot2DTrajectory> t = processor.getNextFinalState2DTrajectory(vars, type, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextFinalState2DTrajectory(vars, type, data);
        }
        LineChart chart = new LineChart(trajList,param,type.toString());
        chart.fullLatexDocument = this.fullLatexDocument;
        chart.saveToFile(filename,filetype);
    }
     
     
     /**
      * Plots a family of phase plane curves for the two given variables. 
      * The family is determined by the fixed parameters.
      * @param type
      * @param var1
      * @param var2
      * @param fixedParams
      * @param fixedParamsValues
      * @return 
      */
     public JFrame plotParamPhasePlaneStatisticsToScreen(StatType type, String var1, String var2,
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
        ArrayList<Plot2DTrajectory> t = processor.getNextPhasePlaneTrajectory(var1, var2, type, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhasePlaneTrajectory(var1, var2, type, data);
        }
        Line2DChart chart = new Line2DChart(trajList,var1,var2);
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + "; " + type.toString() + " phase plane of " + var1 + "  " + var2);
        return frame;
    }

     public JPanel plotParamPhasePlaneStatisticsToPanel(StatType type, String var1, String var2,
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
        ArrayList<Plot2DTrajectory> t = processor.getNextPhasePlaneTrajectory(var1, var2, type, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhasePlaneTrajectory(var1, var2, type, data);
        }
        Line2DChart chart = new Line2DChart(trajList,var1,var2);
        JPanel panel = chart.getPanel();
        return panel;
    }
     
     
     
     /**
      * Plots a family of phase space curves for the three given variables.
      * The family is determined by the fixed parameters.
      * @param type
      * @param var1
      * @param var2
      * @param var3
      * @param fixedParams
      * @param fixedParamsValues
      * @return
      */
     public JFrame plotParamPhaseSpaceStatisticsToScreen(StatType type, String var1, String var2,
                                   String var3, ArrayList<String> fixedParams,
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
        ArrayList<Plot3DTrajectory> t = processor.getNextPhaseSpaceTrajectory(var1, var2, type, var3, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextPhaseSpaceTrajectory(var1, var2, type, var3, data);
        }
        Line3DChart chart = new Line3DChart(trajList,var1,var2,var3);
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + "; " + type.toString() + " phase space of " + var1 + "  " + var2 + "  " + var3);
        return frame;
    }

     
     public JPanel plotParamPhaseSpaceStatisticsToPanel(StatType type, String var1, String var2,
                                   String var3, ArrayList<String> fixedParams,
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
        ArrayList<Plot3DTrajectory> t = processor.getNextPhaseSpaceTrajectory(var1, var2, type, var3, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextPhaseSpaceTrajectory(var1, var2, type, var3, data);
        }
        Line3DChart chart = new Line3DChart(trajList,var1,var2,var3);
        JPanel panel = chart.getPanel();
        return panel;
    }
     
     /**
     * Plots the selected final state statistics for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param param the parameter of interest
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @return
     */
    public JFrame plot1ParamStateStatisticsToScreen(StatType type, String param, double time,
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        if (this.saveFinalStateOnly)
            throw new DataException("Trajectory statistics not saved.");
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
        ArrayList<Plot2DTrajectory> t = processor.getNextState2DTrajectory(vars, time, type, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextState2DTrajectory(vars, time, type, data);
        }
        LineChart chart = new LineChart(trajList,param,type.toString());
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " exploration of " + param + ", " + type.toString());
        return frame;
    }

    public JPanel plot1ParamStateStatisticsToPanel(StatType type, String param, double time,
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        if (this.saveFinalStateOnly)
            throw new DataException("Trajectory statistics not saved.");
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
        ArrayList<Plot2DTrajectory> t = processor.getNextState2DTrajectory(vars, time, type, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextState2DTrajectory(vars, time, type, data);
        }
        LineChart chart = new LineChart(trajList,param,type.toString());
        JPanel panel = chart.getPanel();
        return panel;
    }
    
     /**
     * Plots to a file the selected final state statistics for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param param the parameter of interest
     * @param time the time of the state to plot
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @param filename the name of the file
     * @param filetype the type of the file
     */
     public void plot1ParamStateStatistics(StatType type, String param, double time,
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues,
                                   String filename, PlotFileType filetype) {
         if (this.saveFinalStateOnly)
            throw new DataException("Trajectory statistics not saved.");
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
        ArrayList<Plot2DTrajectory> t = processor.getNextState2DTrajectory(vars, time, type, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextState2DTrajectory(vars, time, type, data);
        }
        LineChart chart = new LineChart(trajList,param,type.toString());
        chart.fullLatexDocument = this.fullLatexDocument;
        chart.saveToFile(filename,filetype);
    }


     /**
     * Plots the time trajectories for the selected statistics for the given vars,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @return
     */
    public JFrame plotTrajectoryStatisticsToScreen(StatType type, 
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        if (this.saveFinalStateOnly)
            throw new DataException("Trajectory statistics not saved.");
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
        ArrayList<Plot2DTrajectory> t = processor.getNextTimed2DTrajectory(vars, type, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextTimed2DTrajectory(vars, type, data);
        }
        LineChart chart = new LineChart(trajList,"time",type.toString());
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName  + type.toString() + " time traces");
        return frame;
    }

    
    public JPanel plotTrajectoryStatisticsToPanel(StatType type, 
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues ) {
        if (this.saveFinalStateOnly)
            throw new DataException("Trajectory statistics not saved.");
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
        ArrayList<Plot2DTrajectory> t = processor.getNextTimed2DTrajectory(vars, type, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextTimed2DTrajectory(vars, type, data);
        }
        LineChart chart = new LineChart(trajList,"time",type.toString());
        JPanel panel = chart.getPanel();
        return panel;
    }

    
     /**
     * Plots to a file tthe trajectories for the selected statistics for the given vars,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param vars the parameter of interest
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @param filename the name of the file
     * @param filetype the type of the file
     */
     public void plotTrajectoryStatistics(StatType type, 
                                   ArrayList<String> vars,
                                   ArrayList<String> fixedParams,
                                   ArrayList<ArrayList<Double>> fixedParamsValues,
                                   String filename, PlotFileType filetype) {
         if (this.saveFinalStateOnly)
            throw new DataException("Trajectory statistics not saved.");
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
        ArrayList<Plot2DTrajectory> t = processor.getNextTimed2DTrajectory(vars, type, data);
        while (t != null) {
            trajList.addAll(t);
            t = processor.getNextTimed2DTrajectory(vars, type, data);
        }
        LineChart chart = new LineChart(trajList,"time",type.toString());
        chart.fullLatexDocument = this.fullLatexDocument;
        chart.saveToFile(filename,filetype);
    }

    


     /**
     * Plots the selected final state statistics for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @return
     */
    public JFrame plot2ParamFinalStateStatisticsAsSurface(StatType type, String param1, String param2,
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
        Plot2DSurface traj = processor.getFinalState2DSurface(var, type, data);
        SurfaceChart chart = new SurfaceChart(traj,param1,param2);
        JFrame frame = chart.show();
        frame.setTitle("SimHyA: " + this.modelName + " exploration of " + var
                + " " + type.toString() + " versus " + param1 + "--" + param2);
        return frame;
    }

    public JPanel plot2ParamFinalStateStatisticsAsSurfaceInPanel(StatType type, String param1, String param2,
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
        Plot2DSurface traj = processor.getFinalState2DSurface(var, type, data);
        SurfaceChart chart = new SurfaceChart(traj,param1,param2);
        JPanel frame = chart.getPanel();
        return frame;
    }
    
    
     /**
     * Plots the selected final state statistics for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
     * @return
     */
//    public JFrame plot2ParamFinalStateStatisticsAsColorMap(StatType type, String param1, String param2,
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
//        Plot2DSurface traj = processor.getFinalState2DSurface(var, type, data);
//        ColorMapChart chart = new ColorMapChart(traj,param1,param2);
//        JFrame frame = chart.show();
//        frame.setTitle("SimHyA: " + this.modelName + " exploration of " + var
//                + " " + type.toString() + " versus " + param1 + "--" + param2);
//        return frame;
//    }

//    public JPanel plot2ParamFinalStateStatisticsAsColorMapInPanel(StatType type, String param1, String param2,
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
//        Plot2DSurface traj = processor.getFinalState2DSurface(var, type, data);
//        ColorMapChart chart = new ColorMapChart(traj,param1,param2);
//        JPanel frame = chart.getPanel();
//        return frame;
//    }
    
    /**
      * Plots a family of phase plane curves for the two given variables.
      * The family is determined by the fixed parameters.
      * @param type
      * @param var1
      * @param var2
      * @param fixedParams
      * @param fixedParamsValues
      * @param filename
      * @param filetype
      * @return
      */
     public void  plotParamPhasePlaneStatistics(StatType type, String var1, String var2,
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
        ArrayList<Plot2DTrajectory> t = processor.getNextPhasePlaneTrajectory(var1, var2, type, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhasePlaneTrajectory(var1, var2, type, data);
        }
        Line2DChart chart = new Line2DChart(trajList,var1,var2);
        chart.saveToFile(filename, filetype);
    }


     /**
      * Plots a family of phase space curves for the three given variables.
      * The family is determined by the fixed parameters.
      * @param type
      * @param var1
      * @param var2
      * @param var3
      * @param fixedParams
      * @param fixedParamsValues
      * @param filename
      * @param filetype
      * @return
      */
     public void  plotParamPhaseSpaceStatistics(StatType type, String var1, String var2, String var3,
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
        ArrayList<Plot3DTrajectory> t = processor.getNextPhaseSpaceTrajectory(var1, var2, type, var3, data);
        while (t != null) {
            trajList.addAll(t);
            t =  processor.getNextPhaseSpaceTrajectory(var1, var2, type, var3, data);
        }
        Line3DChart chart = new Line3DChart(trajList,var1,var2,var3);
        chart.saveToFile(filename, filetype);
    }

     /**
     * Plots the selected final state statistics for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
      * @param filename the name of the file
      * @param filetype the file type
     * @return
     */
    public void plot2ParamFinalStateStatisticsAsSurface(StatType type, String param1, String param2,
                                   String var,
                                   ArrayList<String> fixedParams,
                                   ArrayList<Double> fixedParamsValues,
                                   String filename, PlotFileType filetype) {
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
        Plot2DSurface traj = processor.getFinalState2DSurface(var, type, data);
        SurfaceChart chart = new SurfaceChart(traj,param1,param2);
        chart.saveToFile(filename, filetype);
    }


     /**
     * Plots the selected final state statistics for the given vars versus a parameter,
     * holding other explored parameter fixed to the specified values. If more than one value is
     * passed for a fixed parameter, a trajectory for each such value is plotted.
     * @param type the statistics to plot
     * @param param the parameter of interest
     * @param vars the vars to plot
     * @param fixedParams the parameters to hold fixed. Explored parameters not inthis list (and different
     * from param) are set to their first value.
     * @param fixedParamsValues the values of fixed parameters
      * * @param filename the name of the file
      * @param filetype the file type
     * @return
     */
//    public void plot2ParamFinalStateStatisticsAsColorMap(StatType type, String param1, String param2,
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
//        Plot2DSurface traj = processor.getFinalState2DSurface(var, type, data);
//        ColorMapChart chart = new ColorMapChart(traj,param1,param2);
//        JFrame frame = chart.show();
//        chart.saveToFile(filename, filetype);
//    }

    /*
     * TO DO: implementare metodi di plot e save!!!
     *
     *
     * PLOT
     *
     * - collection of trajectories for one or more vars and different parameter values (2D)
     * - parameter vs state statistics
     * - phasespace for different combinations of parameters.
     *
     * TO IMPLEMENT GRAPH 
     * - 3 parameters vs statistics of one variable (3D - scatterplot)

     * TO IMPLEMENT ALSO DATA EXTRACTION AND CHECK GRAPH...
     * - parameter vs distribution of a variable at final state (3D - histogram)

     */

}
