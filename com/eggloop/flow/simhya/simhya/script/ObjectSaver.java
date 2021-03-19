/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.script;

import com.eggloop.flow.simhya.simhya.dataprocessing.*;
import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author luca
 */
public class ObjectSaver implements Saver {
    public static final int TRAJECTORIES = 0;
    public static final int STATISTICS = 1;
    public static final int STATISTICS_EXPLORATION = 2;
    public static final int DETERMINISTIC_EXPLORATION = 3;
    public static final int MODEL = 4;
    public static final int FLAT = -1;
    public static final int SBML = -2;
    public static final int MATLAB = -3;
    public static final int MATLAB_LN = -4;

    private int dataType;
    private DataCollector trajectoryData = null;
    private TrajectoryStatistics statisticsData = null;
    private StatisticsExploratorDataCollector statisticsExplorationData = null;
    private TrajectoryExploratorDataCollector deterministicExplorationData = null;
    private FlatModel modelData = null;
    private CommandManager cmd;

    public boolean saveStats = false;
    public boolean saveFinalStateOnly = false;
    public double saveTime = -1;
    public boolean allTrajectories = true;
    public int trajectoryId = 0;
    public StatType stat = StatType.AVERAGE;
    public String filename = "output.txt";
    public ArrayList<String> varsToSave = null;
    public int modelType = FLAT;
    public int SBMLlevel = 2;
    public int SBMLversion = 4;

    public String dataVariable;


    public ObjectSaver(String dataVariable, CommandManager cmd) throws ScriptException {
        this.cmd = cmd;
        this.dataVariable = dataVariable;
        if (!cmd.containsVariable(dataVariable))
            throw new ScriptException("Variable $" + dataVariable + " does not exist.");
        if (cmd.isTrajectoryDataVariable(dataVariable) && (!saveStats ||
                !cmd.trajectoryVariableContainsStatistics(dataVariable))) {
            this.trajectoryData = cmd.getCollector(dataVariable);
            this.dataType = ObjectSaver.TRAJECTORIES;
        } else if (cmd.isTrajectoryDataVariable(dataVariable) && saveStats
                && cmd.trajectoryVariableContainsStatistics(dataVariable)) {
            this.statisticsData = cmd.getStatistics(dataVariable);
            this.dataType = ObjectSaver.STATISTICS;
        } else if (cmd.isStatisticsExploratorVariable(dataVariable)) {
            this.statisticsExplorationData = cmd.getStatisticExplorator(dataVariable);
            this.dataType = ObjectSaver.STATISTICS_EXPLORATION;
        } else if (cmd.isDeterministicExploratorVariable(dataVariable)) {
            this.deterministicExplorationData = cmd.getTrajectoryExplorator(dataVariable);
            this.dataType = ObjectSaver.DETERMINISTIC_EXPLORATION;
        } else if (cmd.isModelVariable(dataVariable)) {
            this.modelData = cmd.getModel(dataVariable);
            this.dataType = ObjectSaver.MODEL;
        } else
            throw new ScriptException("Variable $" + dataVariable + " does not contain simulation or model data.");
    }


    public String save() throws ScriptException {
        if (this.dataType == TRAJECTORIES && this.saveStats
                && cmd.trajectoryVariableContainsStatistics(dataVariable)) {
            this.statisticsData = cmd.getStatistics(dataVariable);
            this.dataType = ObjectSaver.STATISTICS;
        }
        switch (this.dataType) {
            case TRAJECTORIES:
                DataCollector cdata = this.trajectoryData;
                if (!cdata.containsTrajectoryData() && !cdata.containsFinalStateData())
                    throw new ScriptException("Data Variable does not contain any trajectory information");
                cdata.setVarsToBePrinted(varsToSave);
                if (this.allTrajectories) {
                    cdata.saveAllTrajectoriesToCSV(cmd.getCorrectFilename(filename), true);
                    return "Saved all trajectories  to file " + cmd.getCorrectFilename(filename) + " ";
                } else {
                    cdata.saveSingleTrajectoryToCSV(trajectoryId, cmd.getCorrectFilename(filename));
                    return "Saved trajectory " + trajectoryId + " to file " + cmd.getCorrectFilename(filename) + " ";
                }
            case STATISTICS:
                TrajectoryStatistics sdata = this.statisticsData;
                sdata.setVarsToPlot(varsToSave);
                if (this.saveFinalStateOnly) {
                    sdata.saveFinalStateStatsAsCSV(cmd.getCorrectFilename(filename));
                    return "Saved final state statistics to file " + cmd.getCorrectFilename(filename) + " ";
                } else if (this.saveTime >= 0) {
                    sdata.saveStateStatsAsCSV(saveTime, cmd.getCorrectFilename(filename));
                    return "Saved state statistics at time " + String.format(Locale.UK, "%.4d", saveTime)
                            + " to file " + cmd.getCorrectFilename(filename) + " ";
                } else {
                    sdata.saveAsCSV(stat, cmd.getCorrectFilename(filename));
                    return "Saved " + stat.toString() + " trajectory to file " + cmd.getCorrectFilename(filename) + " ";
                }

            case STATISTICS_EXPLORATION:
                StatisticsExploratorDataCollector sedata = this.statisticsExplorationData;
                sedata.setVariablesToShow(varsToSave);
                if (this.saveFinalStateOnly) {
                    sedata.saveFinalStateStatisticAsCSV(stat, cmd.getCorrectFilename(filename));
                    return "Saved final state " + stat.toString() + " to file " + cmd.getCorrectFilename(filename) + " ";
                } else if (this.saveTime >= 0) {
                    sedata.saveStateStatisticsAsCSV(saveTime, stat, cmd.getCorrectFilename(filename));
                    return "Saved state " + stat.toString() + " at time " + String.format(Locale.UK, "%.4d", saveTime)
                            + " to file " + cmd.getCorrectFilename(filename) + " ";
                } else {
                    sedata.saveTrajectoryStatisticsAsCSV(stat, cmd.getCorrectFilename(filename));
                    return "Saved trajectory " + stat.toString() + " to file " + cmd.getCorrectFilename(filename) + " ";
                }
            case DETERMINISTIC_EXPLORATION:
                TrajectoryExploratorDataCollector dedata = this.deterministicExplorationData;
                dedata.setVariablesToShow(varsToSave);
                if (this.saveFinalStateOnly) {
                    dedata.saveFinalStateAsCSV(cmd.getCorrectFilename(filename));
                    return "Saved final state to file " + cmd.getCorrectFilename(filename) + " ";
                } else if (this.saveTime >= 0) {
                    dedata.saveStateAsCSV(saveTime, cmd.getCorrectFilename(filename));
                    return "Saved state at time " + String.format(Locale.UK, "%.4d", saveTime)
                            + " to file " + cmd.getCorrectFilename(filename) + " ";
                } else {
                    dedata.saveTrajectoryAsCSV(cmd.getCorrectFilename(filename));
                    return "Saved trajectory  to file " + cmd.getCorrectFilename(filename) + " ";
                }
            case MODEL:
                switch (this.modelType) {
                    case FLAT:
                        try {
                            PrintStream p = new PrintStream(cmd.getCorrectFilename(filename));
                            p.println(modelData.toModelLanguage());
                            p.close();
                            return "Saved model " + modelData.getName() + " to file " + cmd.getCorrectFilename(filename) + " ";
                        } catch (Exception e) {
                            throw new ScriptException("Cannot save model to file " + filename + ": " + e.getMessage());
                        }
                    case SBML:
                        try {
                            modelData.exportToSBML(this.SBMLlevel, this.SBMLversion, cmd.getCorrectFilename(filename));
                            return "Model " + modelData.getName() + " exported to SBML l" + this.SBMLlevel + "v" + this.SBMLversion + " file " + cmd.getCorrectFilename(filename) + " ";
                        } catch (Exception e) {
                            throw new ScriptException("Cannot save model to file " + filename + ": " + e.getMessage());
                        }
                    case MATLAB:
                        try {
                            String outfile = extractMatlabFunctionName(filename) + ".m";
                            PrintStream p = new PrintStream(cmd.getCorrectFilename(outfile));
                            p.println(modelData.exportToMatlab(extractMatlabFunctionName(filename)));
                            p.close();
                            return "Exported model " + modelData.getName() + " to the Matlab m file " + cmd.getCorrectFilename(outfile) + " ";
                        } catch (Exception e) {
                            throw new ScriptException("Cannot save model to file " + filename + ": " + e.getMessage());
                        }
                    case MATLAB_LN:
                        try {
                            String outfile = extractMatlabFunctionName(filename) + ".m";
                            PrintStream p = new PrintStream(cmd.getCorrectFilename(outfile));
                            p.println(modelData.exportToMatlabLinearNoise(extractMatlabFunctionName(filename)));
                            p.close();
                            return "Exported linear noise model " + modelData.getName() + " to the Matlab m file " + cmd.getCorrectFilename(outfile) + " ";
                        } catch (Exception e) {
                            throw new ScriptException("Cannot save model to file " + filename + ": " + e.getMessage());
                        }
                    default:
                        throw new ScriptException("Unknown model type found");
                }
            default:
                throw new ScriptException("Unknown data type found");
        }
    }


    private String extractMatlabFunctionName(String filename) {
        String s = (filename.split("[^a-zA-Z0-9_]"))[0];
        return s;
    }


//    public static void main(String[] args) throws Exception {
//
//    }


}
