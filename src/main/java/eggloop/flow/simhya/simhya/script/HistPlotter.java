/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.script;

import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.StatisticsExploratorDataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.TrajectoryExploratorDataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.TrajectoryStatistics;
import eggloop.flow.simhya.simhya.dataprocessing.chart.PlotFileType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author luca
 */
public class HistPlotter implements Plotter {
    public static final int TRAJECTORIES = 0;
    public static final int STATISTICS = 1;
    public static final int STATISTICS_EXPLORATION = 2;
    public static final int DETERMINISTIC_EXPLORATION = 3;
    private int dataType = -1;
    private DataCollector trajectoryData = null;
    private TrajectoryStatistics statisticsData = null;
    private StatisticsExploratorDataCollector statisticsExplorationData = null;
    private TrajectoryExploratorDataCollector deterministicExplorationData = null;
    private CommandManager cmd;

    public String filename = "hist";
    public PlotFileType fileType = PlotFileType.PNG;
    public boolean plotToFile = false;
    public ArrayList<String> varsToPlot = null;
    public int bins = 25;
    public boolean autoBin = true;
    public static final double FINAL_STATE = -1;
    public double timePoint = FINAL_STATE;

    public String dataVariable;

    public HistPlotter(String dataVariable, CommandManager cmd) throws ScriptException {
        this.cmd = cmd;
        this.dataVariable = dataVariable;
        if (!cmd.containsVariable(dataVariable))
            throw new ScriptException("Variable $" + dataVariable + " does not exist.");
        if (cmd.isTrajectoryDataVariable(dataVariable) &&
                !cmd.trajectoryVariableContainsStatistics(dataVariable)) {
            this.trajectoryData = cmd.getCollector(dataVariable);
            this.dataType = HistPlotter.TRAJECTORIES;
        } else if (cmd.isTrajectoryDataVariable(dataVariable) &&
                cmd.trajectoryVariableContainsStatistics(dataVariable)) {
            this.statisticsData = cmd.getStatistics(dataVariable);
            this.dataType = HistPlotter.STATISTICS;
        } else if (cmd.isStatisticsExploratorVariable(dataVariable)) {
            this.statisticsExplorationData = cmd.getStatisticExplorator(dataVariable);
            this.dataType = HistPlotter.STATISTICS_EXPLORATION;
        } else if (cmd.isDeterministicExploratorVariable(dataVariable)) {
            this.deterministicExplorationData = cmd.getTrajectoryExplorator(dataVariable);
            this.dataType = HistPlotter.DETERMINISTIC_EXPLORATION;
        } else
            throw new ScriptException("Variable $" + dataVariable + " does not contain simulation data.");
    }


    public String plot() throws ScriptException {
        if (this.dataType == TRAJECTORIES
                && cmd.trajectoryVariableContainsStatistics(dataVariable)) {
            this.statisticsData = cmd.getStatistics(dataVariable);
            this.dataType = ObjectSaver.STATISTICS;
        }
        switch (this.dataType) {
            case TRAJECTORIES:
                throw new ScriptException("hist does not support trajectory data yet");
            case STATISTICS:
                statisticsData.autoBin = this.autoBin;
                statisticsData.bins = this.bins;
                if (varsToPlot != null)
                    statisticsData.setVarsToPlot(varsToPlot);
                if (this.timePoint == FINAL_STATE) {
                    if (!statisticsData.supportfinalStateHistogram())
                        throw new ScriptException("Cannot plot distribution at final time: trajectory data not saved");
                    if (plotToFile) {
                        statisticsData.plotFinalDistribution(cmd.getCorrectFilename(filename), fileType);
                        return "Plotted final time distribution to file " + cmd.getCorrectFilename(filename) + " ";
                    } else {
                        if (cmd.isEmbeddedInGUI()) {
                            JPanel panel = statisticsData.plotFinalDistributionToPanel();
                            cmd.setPanelOutput(panel);
                        } else
                            statisticsData.plotFinalDistributionToScreen();
                        return "Plotted final time distribution to a new window ";
                    }
                } else if (timePoint >= 0.0) {
                    if (!statisticsData.supportHistogram())
                        throw new ScriptException("Cannot plot distribution at time " + timePoint + ": trajectory data not saved");
                    if (plotToFile) {
                        statisticsData.plotDistribution(timePoint, cmd.getCorrectFilename(filename), fileType);
                        return "Plotted distribution at time " +
                                String.format(Locale.UK, "%.4f", timePoint) + " to file " + cmd.getCorrectFilename(filename) + " ";
                    } else {
                        if (cmd.isEmbeddedInGUI()) {
                            JPanel panel = statisticsData.plotDistributionToPanel(timePoint);
                            cmd.setPanelOutput(panel);
                        } else
                            statisticsData.plotDistributionToScreen(timePoint);
                        return "Plotted distribution at time " +
                                String.format(Locale.UK, "%.4f", timePoint) + " to a new window ";
                    }

                } else
                    throw new ScriptException("Wrong time point specification, must be either final or a positive number");
            case DETERMINISTIC_EXPLORATION:
                throw new ScriptException("hist does not support deterministic exploration data yet");
            case STATISTICS_EXPLORATION:
                throw new ScriptException("hist does not support statistical exploration data yet");
            default:
                throw new ScriptException("Unknown data type");
        }


    }


}
