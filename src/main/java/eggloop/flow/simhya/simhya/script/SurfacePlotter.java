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
public class SurfacePlotter  implements Plotter {
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
    public String filename = "plot3d";
    public PlotFileType fileType = PlotFileType.PNG;
    
    public static final int COLORMAP = 0;
    public static final int SURFACE = 1;
    public int plotType = COLORMAP;

    public boolean plotToFile = false;
    public StatType stat = StatType.AVERAGE;
    public ArrayList<String> fixedParams = null;
    public ArrayList<ArrayList<Double>> fixedParamsValues = null;

    public String dataVariable;

    public SurfacePlotter(String dataVariable, CommandManager cmd)  throws ScriptException  {
        this.cmd = cmd;
        this.dataVariable = dataVariable;
        if (!cmd.containsVariable(dataVariable))
            throw new ScriptException("Variable $" + dataVariable + " does not exist.");
        if (cmd.isTrajectoryDataVariable(dataVariable) && (!plotStats || 
                !cmd.trajectoryVariableContainsStatistics(dataVariable))) {
            this.trajectoryData = cmd.getCollector(dataVariable);
            this.dataType = SurfacePlotter.TRAJECTORIES;
        }
        else if (cmd.isTrajectoryDataVariable(dataVariable) && plotStats 
                && cmd.trajectoryVariableContainsStatistics(dataVariable)) {
            this.statisticsData = cmd.getStatistics(dataVariable);
            this.dataType = SurfacePlotter.STATISTICS;
        }
        else if (cmd.isStatisticsExploratorVariable(dataVariable)) {
            this.statisticsExplorationData = cmd.getStatisticExplorator(dataVariable);
            this.dataType = SurfacePlotter.STATISTICS_EXPLORATION;
        }
        else if (cmd.isDeterministicExploratorVariable(dataVariable)) {
            this.deterministicExplorationData = cmd.getTrajectoryExplorator(dataVariable);
            this.dataType = SurfacePlotter.DETERMINISTIC_EXPLORATION;
        }
        else
            throw new ScriptException("Variable $" + dataVariable + " does not contain simulation data.");
    }

    public ArrayList<Double> extractSimpleList(ArrayList<ArrayList<Double>> doubleList) {
        if (doubleList == null)
            return null;
        ArrayList<Double> list = new ArrayList<Double>();
        for (ArrayList<Double> l : doubleList)
            list.add(l.get(0));
        return list;
    }


    
    public String plot() throws ScriptException {
        if (this.dataType == TRAJECTORIES && this.plotStats 
                && cmd.trajectoryVariableContainsStatistics(dataVariable)) {
            this.statisticsData = cmd.getStatistics(dataVariable);
            this.dataType = ObjectSaver.STATISTICS;
        }
        if (xvar == null || yvar == null || zvar == null)
            throw new ScriptException("Missing x, y, or z variable.");
        switch(this.dataType) {
            case TRAJECTORIES:
                throw new ScriptException("plot3d does not support trajectory data yet");
            case STATISTICS:
                throw new ScriptException("plot3d does not support trajectory statistics data yet");
//            case DETERMINISTIC_EXPLORATION:
//                if (this.plotType == COLORMAP) {
//                    if (plotToFile) {
//                            deterministicExplorationData.plot2ParamFinalStateAsColorMap(xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues),cmd.getCorrectFilename(filename),fileType);
//                            return "Plotted " +  zvar + " " + stat.toString() + " versus " + xvar + "-" + yvar +
//                                         " as a colormap  to file " + cmd.getCorrectFilename(filename) + " ";
//                    } else {
//                        if (cmd.isEmbeddedInGUI())  {
//                            JPanel panel = deterministicExplorationData.plot2ParamFinalStateAsColorMapInPanel(xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues));
//                            cmd.setPanelOutput(panel);
//                        }
//                        else
//                            deterministicExplorationData.plot2ParamFinalStateAsColorMap(xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues));
//                            return "Plotted " +  zvar + " versus " + xvar + "-" + yvar +
//                                         " as a colormap  to new window ";
//                    }
//
//                } else if (plotType == SURFACE) {
//                    if (plotToFile) {
//                            deterministicExplorationData.plot2ParamFinalStateAsSurface(xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues),cmd.getCorrectFilename(filename),fileType);
//                            return "Plotted " +  zvar + " " + stat.toString() + " versus " + xvar + "-" + yvar +
//                                         " as a colormap to file " + cmd.getCorrectFilename(filename) + " ";
//                    } else {
//                        if (cmd.isEmbeddedInGUI())  {
//                            JPanel panel = deterministicExplorationData.plot2ParamFinalStateAsSurfaceInPanel(xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues));
//                            cmd.setPanelOutput(panel);
//                        }
//                        else
//                             deterministicExplorationData.plot2ParamFinalStateAsSurface(xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues));
//                        return "Plotted " +  zvar  + " versus " + xvar + "-" + yvar +
//                                         " as a surface to new window ";
//                    }
//                } else
//                    throw new ScriptException("Unknown plot type");
//            case STATISTICS_EXPLORATION:
//                if (this.plotType == COLORMAP) {
//                    if (plotToFile) {
//                            statisticsExplorationData.plot2ParamFinalStateStatisticsAsColorMap(stat, xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues),cmd.getCorrectFilename(filename),fileType);
//                            return "Plotted " +  zvar + " " + stat.toString() + " versus " + xvar + "-" + yvar +
//                                         " as a colormap  to file " + cmd.getCorrectFilename(filename) + " ";
//                    } else {
//                        if (cmd.isEmbeddedInGUI())  {
////                            JPanel panel = statisticsExplorationData.plot2ParamFinalStateStatisticsAsColorMapInPanel(stat, xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues));
////                            cmd.setPanelOutput(panel);
//                        }
//                        else
//                            statisticsExplorationData.plot2ParamFinalStateStatisticsAsColorMap(stat, xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues));
//                        return "Plotted " +  zvar + " " + stat.toString() + " versus " + xvar + "-" + yvar +
//                                         " as a colormap  to new window ";
//                    }
//
//                } else if (plotType == SURFACE) {
//                    if (plotToFile) {
//                            statisticsExplorationData.plot2ParamFinalStateStatisticsAsSurface(stat, xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues),cmd.getCorrectFilename(filename),fileType);
//                            return "Plotted " +  zvar + " " + stat.toString() + " versus " + xvar + "-" + yvar +
//                                         " as a colormap to file " + cmd.getCorrectFilename(filename) + " ";
//                    } else {
//                        if (cmd.isEmbeddedInGUI())  {
//                            JPanel panel = statisticsExplorationData.plot2ParamFinalStateStatisticsAsSurfaceInPanel(stat, xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues));
//                            cmd.setPanelOutput(panel);
//                        }
//                        else
//                             statisticsExplorationData.plot2ParamFinalStateStatisticsAsSurface(stat, xvar, yvar, zvar, fixedParams, extractSimpleList(fixedParamsValues));
//                        return "Plotted " +  zvar + " " + stat.toString() + " versus " + xvar + "-" + yvar +
//                                         " as a surface to new window ";
//                    }
//                } else
//                    throw new ScriptException("Unknown plot type");
            default:
                throw new ScriptException("Unknown data type");
        }
    }

}
