/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.script;

import eggloop.flow.simhya.simhya.dataprocessing.*;
import eggloop.flow.simhya.simhya.dataprocessing.chart.PlotFileType;
import java.util.ArrayList;
import java.util.Locale;
import eggloop.flow.simhya.simhya.utils.ArrayUtils;
import javax.swing.JPanel;

/**
 *
 * @author luca
 */
public class PhasePlotter implements Plotter {
    public static final int TRAJECTORIES = 0;
    public static final int STATISTICS = 1;
    public static final int STATISTICS_EXPLORATION = 2;
    public static final int DETERMINISTIC_EXPLORATION = 3;
    private int dataType;
    private DataCollector trajectoryData = null;
    private TrajectoryStatistics statisticsData = null;
    private StatisticsExploratorDataCollector statisticsExplorationData = null;
    private TrajectoryExploratorDataCollector deterministicExplorationData  = null;
    private CommandManager cmd;

    
    public boolean plotStats = false;
    public String xvar = null;
    public String yvar = null;
    public String zvar = null;
    public String filename = "phaseplot";
    public PlotFileType fileType = PlotFileType.PNG;
    public ArrayList<Integer> trajectoryList = null;
    public boolean allTrajectories = false;
    public boolean plotToFile = false;
    public StatType stat = StatType.AVERAGE;
    public ArrayList<String> fixedParams = null;
    public ArrayList<ArrayList<Double>> fixedParamsValues = null;
    
    public String dataVariable;

    public PhasePlotter(String dataVariable, CommandManager cmd)  throws ScriptException  {
        this.cmd = cmd;
        this.dataVariable = dataVariable;
        if (!cmd.containsVariable(dataVariable))
            throw new ScriptException("Variable $" + dataVariable + " does not exist.");
        if (cmd.isTrajectoryDataVariable(dataVariable) && (!plotStats || 
                !cmd.trajectoryVariableContainsStatistics(dataVariable))) {
            this.trajectoryData = cmd.getCollector(dataVariable);
            this.dataType = PhasePlotter.TRAJECTORIES;
        }
        else if (cmd.isTrajectoryDataVariable(dataVariable) && plotStats 
                && cmd.trajectoryVariableContainsStatistics(dataVariable)) {
            this.statisticsData = cmd.getStatistics(dataVariable);
            this.dataType = PhasePlotter.STATISTICS;
        }
        else if (cmd.isStatisticsExploratorVariable(dataVariable)) {
            this.statisticsExplorationData = cmd.getStatisticExplorator(dataVariable);
            this.dataType = PhasePlotter.STATISTICS_EXPLORATION;
        }
        else if (cmd.isDeterministicExploratorVariable(dataVariable)) {
            this.deterministicExplorationData = cmd.getTrajectoryExplorator(dataVariable);
            this.dataType = PhasePlotter.DETERMINISTIC_EXPLORATION;
        }
        else
            throw new ScriptException("Variable $" + dataVariable + " does not contain simulation data.");
        //by default plots trajectory 0 only
        this.trajectoryList = new ArrayList<Integer>();
        this.trajectoryList.add(0);
    }


    
    

    public String plot() throws ScriptException {
        if (xvar == null || yvar == null)
            throw new ScriptException("Missing x or y variable.");
        if (this.dataType == TRAJECTORIES && this.plotStats 
                && cmd.trajectoryVariableContainsStatistics(dataVariable)) {
            this.statisticsData = cmd.getStatistics(dataVariable);
            this.dataType = ObjectSaver.STATISTICS;
        }
        switch(this.dataType) {
            case TRAJECTORIES:
                DataCollector cdata = this.trajectoryData;
                if (this.allTrajectories) {
                    if (zvar != null) {
                        if (plotToFile) {
                            cdata.phaseSpacePlot(xvar, yvar, zvar, cmd.getCorrectFilename(filename), fileType);
                            return "Plotted all " + xvar + "-" + yvar + "-" + zvar
                                    + "-trajectories  to file " + cmd.getCorrectFilename(filename) + " ";
                        } else {
                            if (cmd.isEmbeddedInGUI())  {
                                JPanel panel = cdata.phaseSpacePlotToPanel(xvar, yvar, zvar);
                                cmd.setPanelOutput(panel);
                            }
                            else
                                cdata.phaseSpacePlotToScreen(xvar, yvar, zvar);
                            return "Plotted all " + xvar + "-" + yvar + "-" + zvar
                                    + "-trajectories  to new window ";
                        }
                    } else {
                        if (plotToFile) {
                            cdata.phasePlanePlot(xvar, yvar, cmd.getCorrectFilename(filename), fileType);
                            return "Plotted all " + xvar + "-" + yvar
                                    + "-trajectories  to file " + cmd.getCorrectFilename(filename) + " ";
                        } else {
                            if (cmd.isEmbeddedInGUI())  {
                                JPanel panel = cdata.phasePlanePlotToPanel(xvar, yvar);
                                cmd.setPanelOutput(panel);
                            }
                            else
                                cdata.phasePlanePlotToScreen(xvar, yvar);
                            return "Plotted all " + xvar + "-" + yvar 
                                    + "-trajectories  to new window ";
                        }
                    }
                } else {
                   if (zvar != null) {

                        if (plotToFile) {
                            cdata.phaseSpacePlot(xvar, yvar, zvar, cmd.getCorrectFilename(filename), fileType, ArrayUtils.toArray(trajectoryList));
                            return "Plotted selected " + xvar + "-" + yvar + "-" + zvar
                                    + "-trajectories  to file " + cmd.getCorrectFilename(filename) + " ";
                        } else {
                            if (cmd.isEmbeddedInGUI())  {
                                JPanel panel = cdata.phaseSpacePlotToPanel(xvar, yvar, zvar, ArrayUtils.toArray(trajectoryList));
                                cmd.setPanelOutput(panel);
                            }
                            else
                                cdata.phaseSpacePlotToScreen(xvar, yvar, zvar, ArrayUtils.toArray(trajectoryList));
                            return "Plotted selected " + xvar + "-" + yvar + "-" + zvar
                                    + "-trajectories  to new window ";
                        }
                    } else {
                        if (plotToFile) {
                            cdata.phasePlanePlot(xvar, yvar, cmd.getCorrectFilename(filename), fileType, ArrayUtils.toArray(trajectoryList));
                            return "Plotted selected " + xvar + "-" + yvar
                                    + "-trajectories  to file " + cmd.getCorrectFilename(filename) + " ";
                        } else {
                            if (cmd.isEmbeddedInGUI())  {
                                JPanel panel = cdata.phasePlanePlotToPanel(xvar, yvar, ArrayUtils.toArray(trajectoryList));
                                cmd.setPanelOutput(panel);
                            }
                            else
                                cdata.phasePlanePlotToScreen(xvar, yvar, ArrayUtils.toArray(trajectoryList));
                            return "Plotted selected " + xvar + "-" + yvar
                                    + "-trajectories  to new window ";
                        }
                    }
                }
            case STATISTICS:
                throw new ScriptException("Phaseplotter not yet supported for statistics data");
            case STATISTICS_EXPLORATION:
                StatisticsExploratorDataCollector sedata = this.statisticsExplorationData;
                if (zvar != null) {
                    if (plotToFile) {
                        sedata.plotParamPhaseSpaceStatistics(stat, xvar, yvar, zvar, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                            return "Plotted " + stat.toString() + " " + xvar + "-" + yvar + "-" + zvar
                                    + "-trajectories  to file " + cmd.getCorrectFilename(filename) + " ";
                    } else {
                        if (cmd.isEmbeddedInGUI())  {
                            JPanel panel = sedata.plotParamPhaseSpaceStatisticsToPanel(stat, xvar, yvar, zvar, fixedParams, fixedParamsValues);
                            cmd.setPanelOutput(panel);
                        }
                        else
                            sedata.plotParamPhaseSpaceStatisticsToScreen(stat, xvar, yvar, zvar, fixedParams, fixedParamsValues);
                        return "Plotted " + stat.toString() + " " + xvar + "-" + yvar + "-" + zvar
                                    + "-trajectories  to new window ";
                    }
                } else {
                    if (plotToFile) {
                        sedata.plotParamPhasePlaneStatistics(stat, xvar, yvar, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                            return "Plotted " + stat.toString() + " " + xvar + "-" + yvar
                                    + "-trajectories  to file " + cmd.getCorrectFilename(filename) + " ";
                    } else {
                        if (cmd.isEmbeddedInGUI())  {
                            JPanel panel = sedata.plotParamPhasePlaneStatisticsToPanel(stat, xvar, yvar, fixedParams, fixedParamsValues);
                            cmd.setPanelOutput(panel);
                        }
                        else
                            sedata.plotParamPhasePlaneStatisticsToScreen(stat, xvar, yvar, fixedParams, fixedParamsValues);
                        return "Plotted " + stat.toString() + " " + xvar + "-" + yvar
                                + "-trajectories  to new window ";
                    }
                }
                
            case DETERMINISTIC_EXPLORATION:
                TrajectoryExploratorDataCollector tedata = this.deterministicExplorationData;
                if (zvar != null) {
                    if (plotToFile) {
                        tedata.plotParamPhaseSpaceTrajectories(xvar, yvar, zvar, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                            return "Plotted " + xvar + "-" + yvar + "-" + zvar
                                    + "-trajectories  to file " + cmd.getCorrectFilename(filename) + " ";
                    } else {
                        if (cmd.isEmbeddedInGUI())  {
                            JPanel panel = tedata.plotParamPhaseSpaceTrajectoriesToPanel(xvar, yvar, zvar, fixedParams, fixedParamsValues);
                            cmd.setPanelOutput(panel);
                        }
                        else
                            tedata.plotParamPhaseSpaceTrajectoriesToScreen(xvar, yvar, zvar, fixedParams, fixedParamsValues);
                        return "Plotted " + xvar + "-" + yvar + "-" + zvar
                                    + "-trajectories  to new window ";
                    }
                } else {
                    if (plotToFile) {
                        tedata.plotParamPhasePlaneTrajectories(xvar, yvar, fixedParams, fixedParamsValues, cmd.getCorrectFilename(filename), fileType);
                        return "Plotted " + xvar + "-" + yvar
                                    + "-trajectories  to file " + cmd.getCorrectFilename(filename) + " ";
                    } else {
                        if (cmd.isEmbeddedInGUI())  {
                            JPanel panel = tedata.plotParamPhasePlaneTrajectoriesToPanel(xvar, yvar, fixedParams, fixedParamsValues);
                            cmd.setPanelOutput(panel);
                        }
                        else
                            tedata.plotParamPhasePlaneTrajectoriesToScreen(xvar, yvar, fixedParams, fixedParamsValues);
                        return "Plotted " + xvar + "-" + yvar
                                + "-trajectories  to new window ";
                    }
                }
            default:
                throw new ScriptException("Unknown data type found");
        }
    }
    
}
