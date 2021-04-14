/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.script;

import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.*;
import eggloop.flow.simhya.simhya.dataprocessing.chart.PlotFileType;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.JPanel;


/**
 *
 * @author luca
 */
public class TracePlotter implements Plotter {
    public static final int TRAJECTORIES = 0;
    public static final int STATISTICS_EXPLORATION = 2;
    public static final int DETERMINISTIC_EXPLORATION = 3;
    public static final int MULTIPLE = 4;
    public static final double FINAL_STATE = -1;
    private int dataType;
    private DataCollector[] trajectoryData = null;
    private StatisticsExploratorDataCollector statisticsExplorationData = null;
    private TrajectoryExploratorDataCollector deterministicExplorationData  = null;
    private CommandManager cmd;


    public boolean plotStats = false;
    public ArrayList<String> varsToPlot = null;
    public String filename = "plot";
    public PlotFileType fileType = PlotFileType.PNG;
    public ArrayList<Integer> trajectoryList = null;
    public int singleTrajectory = 0;
    public int firstTrajectory = -1;
    public int lastTrajectory = -1;
    public boolean allTrajectories = false;
    public boolean plotToFile = false;
    public StatType stat = StatType.AVERAGE;
    //xVar can be equal to time. In this case, we plot time traces and not parameter traces
    public String xVar = null;
    public double timePoint = FINAL_STATE;
    public ArrayList<String> fixedParams = null;
    public ArrayList<ArrayList<Double>> fixedParamsValues = null;

    public String dataVariable;
    
    public TracePlotter(String dataVariable, CommandManager cmd) throws ScriptException {
        this.cmd = cmd;
        this.dataVariable = dataVariable;
        if (!cmd.containsVariable(dataVariable))
            throw new ScriptException("Variable $" + dataVariable + " does not exist.");
        if (cmd.isTrajectoryDataVariable(dataVariable)) {
            this.trajectoryData = new DataCollector[1];
            this.trajectoryData[0] = cmd.getCollector(dataVariable);
            this.dataType = TracePlotter.TRAJECTORIES;
        }
        else if (cmd.isStatisticsExploratorVariable(dataVariable)) {
            this.statisticsExplorationData = cmd.getStatisticExplorator(dataVariable);
            this.dataType = TracePlotter.STATISTICS_EXPLORATION;
        }
        else if (cmd.isDeterministicExploratorVariable(dataVariable)) {
            this.deterministicExplorationData = cmd.getTrajectoryExplorator(dataVariable);
            this.dataType = TracePlotter.DETERMINISTIC_EXPLORATION;
        }
        else
            throw new ScriptException("Variable $" + dataVariable + " does not contain simulation data.");
    }

    public TracePlotter(ArrayList<String> dataVariables, CommandManager cmd) throws ScriptException {
        this.cmd = cmd;
        this.trajectoryData = new DataCollector[dataVariables.size()];
        this.dataType = TracePlotter.MULTIPLE;
        int i=0, j=0;
        for (String dataVariable : dataVariables) {
            if (!cmd.containsVariable(dataVariable))
                throw new ScriptException("Variable $" + dataVariable + " does not exist.");
            if (cmd.isTrajectoryDataVariable(dataVariable))
                this.trajectoryData[i++] = cmd.getCollector(dataVariable);
            else
            throw new ScriptException("Variable $" + dataVariable + " is in the list of variables but it "
                    + "is not a simulation output or a statistics variable.");
        }
        trajectoryData = java.util.Arrays.copyOf(trajectoryData, i);
    }


    public String plot() throws ScriptException {
        if (dataType == TracePlotter.MULTIPLE) {
             throw new ScriptException("Plot from multiple datasets not supported yet!");
        } else if (dataType == TracePlotter.TRAJECTORIES && !this.plotStats) {
            DataCollector coll = this.trajectoryData[0];
            if (!coll.containsTrajectoryData())
                throw new ScriptException("Data variable does not contain trajectory data.");
            if (this.varsToPlot != null)
                coll.setVarsToBePrinted(varsToPlot);
            else
                coll.setAllVarsToBePrinted();
            if (this.allTrajectories) {
                if (plotToFile) {
                    coll.plotAllTrajectories(cmd.getCorrectFilename(filename), fileType);
                    return "Plotted all trajectories to " + cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = coll.plotAllTrajectoriesToPanel();
                        cmd.setPanelOutput(panel);
                    }
                    else
                        coll.plotAllTrajectoriesToScreen();
                    return "Plotted all trajectories to new window ";
                }

            } else if (this.trajectoryList != null) {
                if (plotToFile) {
                    coll.plotTrajectories(trajectoryList, cmd.getCorrectFilename(filename), fileType);
                    return "Plotted trajectory list to " + cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = coll.plotTrajectoriesToPanel(trajectoryList);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        coll.plotTrajectoriesToScreen(trajectoryList);
                    return "Plotted trajectory list to new window ";
                }

            } else if (this.firstTrajectory > -1) {
                if (plotToFile) {
                    coll.plotTrajectories(firstTrajectory,lastTrajectory, cmd.getCorrectFilename(filename), fileType);
                    return "Plotted trajectories from " + firstTrajectory + " to " + lastTrajectory
                            + " to "+ cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI()) {
                        JPanel panel = coll.plotTrajectoriesToPanel(firstTrajectory,lastTrajectory);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        coll.plotTrajectoriesToScreen(firstTrajectory,lastTrajectory);
                    return "Plotted trajectories from " + firstTrajectory + " to " + lastTrajectory
                            + " to new window ";
                }
            } else {
                if (plotToFile) {
                    coll.plotSingleTrajectory(singleTrajectory, cmd.getCorrectFilename(filename), fileType);
                    return "Plotted trajectory " + singleTrajectory + " to "+ cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = coll.plotSingleTrajectoryToPanel(singleTrajectory);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        coll.plotSingleTrajectoryToScreen(singleTrajectory);
                    return "Plotted trajectory " + singleTrajectory + " to new window ";
                }
            }
        } else if (dataType == TracePlotter.TRAJECTORIES && this.plotStats) {
            if (!trajectoryData[0].containsStatisticsData())
                throw new ScriptException("Data variable does not contain statistics!");
            if ((stat == StatType.SKEW || stat == StatType.KURTOSIS) && !trajectoryData[0].containsStatisticsTrajectoryDataOnHigherOrderMoments())
                throw new ScriptException("Data variable does not contain skew or kurtosis statistics!");
            TrajectoryStatistics stats = this.trajectoryData[0].getTrajectoryStatistics();
            if (this.varsToPlot != null)
                stats.setVarsToPlot(varsToPlot);
            else
                stats.setVarsToPlot(null);
           if (plotToFile) {
                stats.plotTrajectoryStatistics(stat, cmd.getCorrectFilename(filename), fileType);
                return "Plotted " + stat.toString() +" trace to "+ cmd.getCorrectFilename(filename) + " ";
            }
            else {
                if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = stats.plotTrajectoryStatisticsToPanel(stat);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        stats.plotTrajectoryStatisticsToScreen(stat);
                return "Plotted " + stat.toString() +" trace to new window ";
            }
        } else if (dataType == TracePlotter.STATISTICS_EXPLORATION) {
            StatisticsExploratorDataCollector data = this.statisticsExplorationData;
            if (xVar == null)
                throw new ScriptException("Parameter for x-axis not specified, please specify it by x=par");
            if (xVar.equals("time")) {
                if (plotToFile) {
                    data.plotTrajectoryStatistics(stat, varsToPlot, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                    return "Plotted  time series to "+ cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = data.plotTrajectoryStatisticsToPanel(stat, varsToPlot, fixedParams, fixedParamsValues);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        data.plotTrajectoryStatisticsToScreen(stat, varsToPlot, fixedParams, fixedParamsValues);
                    return "Plotted  time series to new window ";
                }
            } else if (this.timePoint == FINAL_STATE) {
                if (plotToFile) {
                    data.plot1ParamFinalStateStatistics(stat, xVar, varsToPlot, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                    return "Plotted final state versus " + xVar +" to "+ cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = data.plot1ParamFinalStateStatisticsToPanel(stat, xVar, varsToPlot, fixedParams, fixedParamsValues);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        data.plot1ParamFinalStateStatisticsToScreen(stat, xVar, varsToPlot, fixedParams, fixedParamsValues);
                    return "Plotted final state versus " + xVar +" to new window ";
                }
            } else  {
                if (plotToFile) {
                    data.plot1ParamStateStatistics(stat, xVar, timePoint, varsToPlot, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                    return "Plotted  state at time " + String.format(Locale.UK,"%.4f" , timePoint)
                            + " versus " + xVar +" to "+ cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = data.plot1ParamStateStatisticsToPanel(stat, xVar, timePoint, varsToPlot, fixedParams, fixedParamsValues);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        data.plot1ParamStateStatisticsToScreen(stat, xVar, timePoint, varsToPlot, fixedParams, fixedParamsValues);
                    return "Plotted  state at time " + String.format(Locale.UK,"%.4f" , timePoint)
                            + " versus " + xVar +" to new window ";
                }
            }
        } else if (dataType == TracePlotter.DETERMINISTIC_EXPLORATION) {
            TrajectoryExploratorDataCollector data = this.deterministicExplorationData;
            if (xVar == null)
                throw new ScriptException("Parameter for x-axis not specified, please specify it by x=par");
            if (xVar.equals("time")) {
                if (plotToFile) {
                    data.plotTrajectories(varsToPlot, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                    return "Plotted  time series to "+ cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = data.plotTrajectoriesToPanel(varsToPlot, fixedParams, fixedParamsValues);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        data.plotTrajectoriesToScreen(varsToPlot, fixedParams, fixedParamsValues);
                    return "Plotted  time series to new window ";
                }
            } else if (this.timePoint == FINAL_STATE) {
                if (plotToFile) {
                    data.plot1ParamFinalState(xVar, varsToPlot, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                    return "Plotted final state versus " + xVar +" to "+ cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = data.plot1ParamFinalStateToPanel(xVar, varsToPlot, fixedParams, fixedParamsValues);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        data.plot1ParamFinalStateToScreen(xVar, varsToPlot, fixedParams, fixedParamsValues);
                    return "Plotted final state versus " + xVar +" to new window ";
                }
            } else  {
                if (plotToFile) {
                    data.plot1ParamState(xVar, varsToPlot, timePoint, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                    return "Plotted  state at time " + String.format(Locale.UK,"%.4f" , timePoint)
                            + " versus " + xVar +" to "+ cmd.getCorrectFilename(filename) + " ";
                }
                else {
                    if (cmd.isEmbeddedInGUI())  {
                        JPanel panel = data.plot1ParamStateToPanel(xVar, timePoint, varsToPlot, fixedParams, fixedParamsValues);
                        cmd.setPanelOutput(panel);
                    }
                    else
                        data.plot1ParamStateToScreen(xVar, timePoint, varsToPlot, fixedParams, fixedParamsValues);
                    return "Plotted  state at time " + String.format(Locale.UK,"%.4f" , timePoint)
                            + " versus " + xVar +" to new window ";
                }
            }
        } else {
            throw new ScriptException("There is no data to plot...");
        }    
    }


    private ArrayList<Double> extractSimpleList(ArrayList<ArrayList<Double>> doubleList) {
        if (doubleList == null)
            return null;
        ArrayList<Double> list = new ArrayList<Double>();
        for (ArrayList<Double> l : doubleList)
            list.add(l.get(0));
        return list;
    }



}
