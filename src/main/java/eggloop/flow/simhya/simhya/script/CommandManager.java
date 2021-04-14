/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.script;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.PrintStream;
import java.util.Locale;
import eggloop.flow.simhya.simhya.model.flat.*;
import eggloop.flow.simhya.simhya.model.flat.parser.FlatParser;
import eggloop.flow.simhya.simhya.simengine.HybridSimulationManager;
import eggloop.flow.simhya.simhya.simengine.StochasticSimulationManager;
import eggloop.flow.simhya.simhya.simengine.OdeSimulationManager;
import eggloop.flow.simhya.simhya.simengine.SimType;
import eggloop.flow.simhya.simhya.simengine.paramexplore.*;
import eggloop.flow.simhya.simhya.dataprocessing.*;
import eggloop.flow.simhya.simhya.script.parser.ScriptParser;

import eggloop.flow.simhya.simhya.script.parser.ParseException;
import eggloop.flow.simhya.simhya.script.parser.TokenMgrError;

import eggloop.flow.simhya.hype.HypeModel;
import eggloop.flow.simhya.hype.parser.HypeParser;
//import eggloop.flow.simhya.biopepa.BiopepaModel;
//import simhya.GlobalOptions;
import eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;


import javax.swing.JPanel;
import eggloop.flow.simhya.simhya.GlobalOptions;
import eggloop.flow.simhya.simhya.modelchecking.SMCenvironment;
import eggloop.flow.simhya.simhya.modelchecking.SMCoutput;
import eggloop.flow.simhya.simhya.modelchecking.SMCtype;

/**
 * this is the main class of the script language, it essentially contains
 * all the methods to execute simulations, process output, and so on, and
 * it stores script model variables.
 * @author luca
 */
public class CommandManager implements Commander {
    private HashMap<String,ScriptVariable> variables;
    private PrintStream out;
    private PrintStream err;
    private ScriptParser parser;
    private HashMap<String,String> helpDescr;
    private HashMap<String,String> helpText;
    private String helpFile;

    private JPanel lastPanel;
    private String lastString;
    private int lastOutputType;
    public static final int NO_OUTPUT = 0;
    public static final int FRAME_OUTPUT = 1;
    public static final int STRING_OUTPUT = 2;

    private boolean embeddedInGui = true;
    private File currentDirectory;

    public CommandManager()  {
        this.variables = new HashMap<String,ScriptVariable>();
        this.out = System.out;
        this.err = System.out;
        this.parser = new ScriptParser(this);
        this.currentDirectory = new File("");
        this.helpFile = "help";
        this.helpDescr = new HashMap<String,String>();
        this.helpText = new HashMap<String,String>();
        this.loadHelp();
    }

    public void setEmbeddedInGuiStatus(boolean status) {
        this.embeddedInGui = status;
    }

    public boolean isEmbeddedInGUI() {
        return this.embeddedInGui;
    }

    public void printMessage(String message) {
        out.println(message);
    }

    private void printError(String message) {
        err.println("#ERROR: " + message);
    }

    private void printParseError(String message) {
        err.println("#ERROR: " + message);
    }

    public void setErrorStream(PrintStream err) {
        this.err = err;
    }

    public void setOutputStream(PrintStream out) {
        this.out = out;
    }





    public void executeCommand(String command) {
        try {
            this.parser.parseCommand(command);
        }
        catch(ParseException e) {
            this.printParseError(""+e + "  " + e.getMessage());
        }
        catch(TokenMgrError e) {
            this.printParseError(""+e + "  " + e.getMessage());
        }
        catch(Error e) {
            this.printError(e.getMessage());
        }
        catch(Exception e) {
            this.printError(e.getMessage());
        }
    }

    public void executeScriptFile(String filename) {
        try {
            java.io.BufferedReader reader =
                    new java.io.BufferedReader(new java.io.FileReader(getCorrectFilename(filename)));
            while (reader.ready()) {
                String s = reader.readLine();
                if (!s.isEmpty() && !isEmptyLine(s) && !isCommentLine(s)) {
                    out.println("\n > " + s + "\n");
                    this.executeCommand(s);
                }
            }
            reader.close();
        } catch(Exception e) {
            this.printError(e.getMessage());
        }
    }





    private boolean isCommentLine(String command) {
        int i = command.indexOf("//");
        if (i >= 0) {
            for (int j =0; j<i; j++)
                if ( command.charAt(j) != ' ' && command.charAt(j) != '\t' && command.charAt(j) != '\r' )
                    return false;
            return true;
        } else
            return false;
    }

    private boolean isEmptyLine(String command) {
        for (int j =0; j<command.length(); j++)
            if ( command.charAt(j) != ' ' && command.charAt(j) != '\t' && command.charAt(j) != '\r' )
                return false;
        return true;
    }


    public String getCurrentDirectory() {
        return this.currentDirectory.getAbsolutePath();
    }

    public void setCurrentDirectory(String dir) {
        this.currentDirectory = new File(dir);
    }


    public String getCorrectFilename(String file) {
        return currentDirectory.getAbsolutePath() + File.separatorChar + file;
    }

    public void loadSMCenvironment(String checkerVarName, String checkerFile, String modelVar) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            FlatModel m = var.model;
            SMCenvironment checker = new SMCenvironment(m);
            checker.loadEnvironment(getCorrectFilename(checkerFile));
            ModelCheckingVariable varC = new ModelCheckingVariable(checker,checkerVarName);
            this.variables.put(checkerVarName, varC);
            this.out.println("Model Checking Environment " + checkerFile + " loaded and stored in variable $" + checkerVarName);
        }
    }


    public void loadFlatModel(String modelVarName, String modelFile) {
        FlatParser flatParser = new FlatParser();
        FlatModel model;
        try {
            model = flatParser.parseFromFile(getCorrectFilename(modelFile));
        }
        catch(Exception e) { this.printError(e.getMessage()); return; }
        catch(Error e) { this.printError(e.getMessage()); return; }
        ModelVariable var = new ModelVariable(modelVarName,model);
        this.variables.put(modelVarName, var);
        this.out.println("Model " + modelFile + " loaded and stored in variable $" + modelVarName);
    }



    public void loadHypeModel(String modelVarName, String modelFile) {
        HypeParser hypeParser = new HypeParser();
        FlatParser flatParser = new FlatParser();
        HypeModel hypeModel;
        FlatModel flatModel;
        try {
            hypeModel = hypeParser.parseFromFile(getCorrectFilename(modelFile));
            String flat = hypeModel.generateFlatModelString();
            flatModel = flatParser.parseFromStringWithDots(flat);
        }
        catch(Exception e) {
            this.printError(e.getMessage()); return; }
        catch(Error e) {
            this.printError(e.getMessage()); return; }
        ModelVariable var = new ModelVariable(modelVarName,flatModel);
        this.variables.put(modelVarName, var);
        this.out.println("Model " + modelFile + " loaded and stored in variable $" + modelVarName);

    }

//    public void loadBiopepaModel(String modelVarName, String modelFile) {
//        FlatParser flatParser = new FlatParser();
//        BiopepaModel biopepaModel;
//        FlatModel flatModel;
//        try {
//            biopepaModel = new BiopepaModel();
//            String flat = biopepaModel.loadBiopepa(modelFile);
//
////            System.out.println(flat);
//
//            flatModel = flatParser.parseFromStringWithDots(flat);
//        }
//        catch(Exception e) { this.printError(e.getMessage()); return; }
//        catch(Error e) { this.printError(e.getMessage()); return; }
//        ModelVariable var = new ModelVariable(modelVarName,flatModel);
//        this.variables.put(modelVarName, var);
//        this.out.println("Model " + modelFile + " loaded and stored in variable $" + modelVarName);
//    }

    /**
     * Generates the linear noise approximation of a model
     * @param LNmodelVar
     * @param modelVar
     */
    public void generateLinearNoiseFlatModel(String LNmodelVar, String modelVar) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            FlatModel m =  var.model.generateLinearNoiseApproximation();
            ModelVariable var1 = new ModelVariable(LNmodelVar,m);
            this.variables.put(LNmodelVar, var1);
            this.out.println("Linear Noise approximation of model " + var.model.getName() + " generated and stored in variable $" + LNmodelVar);
        }
    }


    public StochasticSimulationManager newStochasticSimulation(String modelVar, SimType type, double finalTime, int runs) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return null;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return null;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            StochasticSimulationManager manager = new StochasticSimulationManager(var.model, type, finalTime, runs);
            out.println("Stochastic " + type.toString() + " simulator for model $" + modelVar + " correctly initialized.");
            return manager;
        }
    }

    public void runStochasticSimulation(StochasticSimulationManager manager, String collectorVar) {
        manager.setOutStream(out);
        manager.run();
        DataCollector collector = manager.getDataCollector();
        CollectorVariable var = new CollectorVariable(collectorVar,collector);
        this.variables.put(collectorVar, var);
        out.println("Simulation data stored in variable $" + collectorVar);
    }

    public OdeSimulationManager newOdeSimulation(String modelVar, double finalTime) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return null;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return null;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            OdeSimulationManager manager = new OdeSimulationManager(var.model, finalTime);
            out.println("ODE simulator for model $" + modelVar + " correctly initialized.");
            return manager;
        }
    }

    public OdeSimulationManager newLnOdeSimulation(String modelVar, LinearNoiseFlatModel model, double finalTime) {
        OdeSimulationManager manager = new OdeSimulationManager(model, finalTime);
        out.println("Linear Noise ODE simulator for model $" + modelVar + " correctly initialized.");
        return manager;
    }

    public void runOdeSimulation(OdeSimulationManager manager, String collectorVar) {
        manager.setOutStream(out);
        manager.run();
        DataCollector collector = manager.getDataCollector();
        CollectorVariable var = new CollectorVariable(collectorVar,collector);
        this.variables.put(collectorVar, var);
        out.println("Simulation data stored in variable $" + collectorVar);
    }


    /**
     * Return a model checking variable
     * @param varS
     * @return
     */
    public ArrayList<SMCoutput> getSMCoutput(String varS) {
        if (!this.variables.containsKey(varS)) {
            this.printError("Variable " + varS + " not defined.");
            return null;
        }
        else if (!this.variables.get(varS).isModelCheckingOutputVariable()) {
            this.printError("Variable " + varS + " is not a model checking output variable.");
            return null;
        }
        else {
            SMCoutputVariable var = (SMCoutputVariable)variables.get(varS);
            return var.outputList;
        }
    }

    /**
     * Return a model checking variable
     * @param varS
     * @return
     */
    public SMCenvironment getSMCenvinronment(String varS) {
        if (!this.variables.containsKey(varS)) {
            this.printError("Variable " + varS + " not defined.");
            return null;
        }
        else if (!this.variables.get(varS).isModelCheckingVariable()) {
            this.printError("Variable " + varS + " is not a model checking variable.");
            return null;
        }
        else {
            ModelCheckingVariable var = (ModelCheckingVariable)variables.get(varS);
            return var.checker;
        }
    }

    /**
     * Returns the model, if it is a modelVar!
     * @param modelVar
     * @return
     */
    public FlatModel getModel(String modelVar) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return null;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return null;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            return var.model;
        }
    }

    public HybridSimulationManager newHybridSimulation(String modelVar, double finalTime, int runs) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return null;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return null;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            HybridSimulationManager manager = new HybridSimulationManager(var.model, finalTime, runs);
            out.println("Hybrid simulator for model $" + modelVar + " correctly initialized.");
            return manager;
        }
    }

    public void runHybridSimulation(HybridSimulationManager manager, String collectorVar) {
        manager.setOutStream(out);
        manager.run();
        DataCollector collector = manager.getDataCollector();
        CollectorVariable var = new CollectorVariable(collectorVar,collector);
        this.variables.put(collectorVar, var);
        out.println("Simulation data stored in variable $" + collectorVar);
    }



    public StochasticParamExplorator newStochasticExplorator(String modelVar, SimType type) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return null;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return null;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            StochasticParamExplorator explorer = new StochasticParamExplorator(var.model, type);
            out.println("Stochastic " + type.toString() + " parameter explorator for model $" + modelVar + " correctly initialized.");
            explorer.setOutputStream(out);
            return explorer;
        }
    }

    public void runStochasticExploration(StochasticParamExplorator explorer, String explorerVar) {
        StatisticsExploratorDataCollector collector = explorer.run();
        out.println("Stochastic parameter exploration completed in " +
                String.format(Locale.UK, "%.4f", explorer.getExplorationTimeInSecs()) + " seconds.");
        StatisticsExplorationVariable var = new StatisticsExplorationVariable(explorerVar,collector);
        this.variables.put(explorerVar, var);
        out.println("Exploration data stored in variable $" + explorerVar);
    }


    public DeterministicParamExplorator newDeterministicExplorator(String modelVar) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return null;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return null;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            DeterministicParamExplorator explorer = new DeterministicParamExplorator(var.model);
            out.println("Deterministic parameter explorator for model $" + modelVar + " correctly initialized.");
            explorer.setOutputStream(out);
            return explorer;
        }
    }

    public void runDeterministicExploration(DeterministicParamExplorator explorer, String explorerVar) {
        TrajectoryExploratorDataCollector collector = explorer.run();
        out.println("Deterministic parameter exploration completed in " +
                String.format(Locale.UK, "%.4f", explorer.getExplorationTimeInSecs()) + " seconds.");
        TrajectoryExplorationVariable var = new TrajectoryExplorationVariable(explorerVar,collector);
        this.variables.put(explorerVar, var);
        out.println("Exploration data stored in variable $" + explorerVar);
    }

    public LinearNoiseParamExplorator newLinearNoiseExplorator(String modelVar) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return null;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return null;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            LinearNoiseParamExplorator explorer = new LinearNoiseParamExplorator(var.model);
            out.println("Linear noise parameter explorator for model $" + modelVar + " correctly initialized.");
            explorer.setOutputStream(out);
            return explorer;
        }
    }

    public void runLinearNoiseExploration(LinearNoiseParamExplorator explorer, String explorerVar) {
        TrajectoryExploratorDataCollector collector = explorer.run();
        out.println("Linear noise parameter exploration completed in " +
                String.format(Locale.UK, "%.4f", explorer.getExplorationTimeInSecs()) + " seconds.");
        TrajectoryExplorationVariable var = new TrajectoryExplorationVariable(explorerVar,collector);
        this.variables.put(explorerVar, var);
        out.println("Exploration data stored in variable $" + explorerVar);
    }


    public HybridParamExplorator newHybridExplorator(String modelVar) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return null;
        }
        else if (!this.variables.get(modelVar).isModelVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable.");
            return null;
        }
        else {
            ModelVariable var = (ModelVariable)variables.get(modelVar);
            HybridParamExplorator explorer = new HybridParamExplorator(var.model);
            out.println("Hybrid parameter explorator for model $" + modelVar + " correctly initialized.");
            explorer.setOutputStream(out);
            return explorer;
        }
    }

    public void runHybridExploration(HybridParamExplorator explorer, String explorerVar) {
        StatisticsExploratorDataCollector collector = explorer.run();
        out.println("Hybrid parameter exploration completed in " +
                String.format(Locale.UK, "%.4f", explorer.getExplorationTimeInSecs()) + " seconds.");
        StatisticsExplorationVariable var = new StatisticsExplorationVariable(explorerVar,collector);
        this.variables.put(explorerVar, var);
        out.println("Exploration data stored in variable $" + explorerVar);
    }



    public DataCollector getCollector(String collectorVar) {
        if (!this.variables.containsKey(collectorVar)) {
            this.printError("Variable $" + collectorVar + " not defined");
            return null;
        } else if (!this.variables.get(collectorVar).isCollectorVariable()) {
            this.printError("Variable $" + collectorVar + " is not a collector variable");
            return null;
        } else {
            CollectorVariable var = (CollectorVariable)this.variables.get(collectorVar);
            return var.collector;
        }
     }

    public StatisticsExploratorDataCollector getStatisticExplorator(String statExpVar) {
        if (!this.variables.containsKey(statExpVar)) {
            this.printError("Variable $" + statExpVar + " not defined");
            return null;
        } else if (!this.variables.get(statExpVar).isStatisticsExplorationVariable()) {
            this.printError("Variable " + statExpVar + " is not a statistics exploration variable");
            return null;
        } else {
            StatisticsExplorationVariable var = (StatisticsExplorationVariable)this.variables.get(statExpVar);
            return var.statisticsExplorationData;
        }
     }

    public TrajectoryExploratorDataCollector getTrajectoryExplorator(String trajExpVar) {
        if (!this.variables.containsKey(trajExpVar)) {
            this.printError("Variable $" + trajExpVar + " not defined");
            return null;
        } else if (!this.variables.get(trajExpVar).isTrajectoryExplorationVariable()) {
            this.printError("Variable $" + trajExpVar + " is not a deterministic explorator variable");
            return null;
        } else {
            TrajectoryExplorationVariable var = (TrajectoryExplorationVariable)this.variables.get(trajExpVar);
            return var.trajectoryExplorationData;
        }
     }

    public boolean isDataVariable(String variable) {
        return this.isTrajectoryDataVariable(variable) ||
                this.isDeterministicExploratorVariable(variable) || this.isStatisticsExploratorVariable(variable);
    }


    public boolean isTrajectoryDataVariable(String variable) {
        return this.variables.containsKey(variable) &&
                this.variables.get(variable).isCollectorVariable();
    }

//    public boolean isStatisticsVariable(String variable) {
//        return this.variables.containsKey(variable) &&
//                this.variables.get(variable).isStatisticsVariable();
//    }

    public boolean isStatisticsExploratorVariable(String variable) {
        return this.variables.containsKey(variable) &&
                this.variables.get(variable).isStatisticsExplorationVariable();
    }

    public boolean isDeterministicExploratorVariable(String variable) {
        return this.variables.containsKey(variable) &&
                this.variables.get(variable).isTrajectoryExplorationVariable();
    }

    public boolean isModelVariable(String variable) {
        return this.variables.containsKey(variable) &&
                this.variables.get(variable).isModelVariable();
    }



    public boolean isSMCoutputVariable(String variable) {
        return this.variables.containsKey(variable) &&
                this.variables.get(variable).isModelCheckingOutputVariable();
    }


    public boolean isModelCheckingVariable(String variable) {
        return this.variables.containsKey(variable) &&
                this.variables.get(variable).isModelCheckingVariable();
    }

    public void runModelChecking(SMCenvironment smc, ArrayList<String> formulae, String outputVar) {
        ArrayList<SMCoutput> output = smc.modelCheck(formulae);
        out.println("Model checking completed in " +
                String.format(Locale.UK, "%.4f", smc.getLastExecutionTime()) + " seconds.");
        SMCoutputVariable var = new SMCoutputVariable(outputVar,output);
        this.variables.put(outputVar, var);
        out.println("Model checking data stored in variable $" + outputVar );
        out.println();
        //need some control here!
        for (SMCoutput o : output)
            out.println(o.shortPrint());

    }


    public void runModelChecking(SMCenvironment smc, ArrayList<String> formulae, String param, int points, double[] vals, String outputVar) {
        ArrayList<SMCoutput> output = smc.modelCheck(formulae,param,points,vals);
        out.println("Model checking completed in " +
                String.format(Locale.UK, "%.4f", smc.getLastExecutionTime()) + " seconds.");
        SMCoutputVariable var = new SMCoutputVariable(outputVar,output);
        this.variables.put(outputVar, var);
        out.println("Model checking data stored in variable $" + outputVar );
        out.println();
        //need some control here!
        for (SMCoutput o : output)
            out.println(o.shortPrint());

    }

//    public void computeStatistics(String collectorVar, String statVar) {
//        DataCollector collector = this.getCollector(collectorVar);
//        if (collector != null) {
//            TrajectoryStatistics stat;
//            if (collector instanceof StochasticDataCollector) {
//                try {
//                    stat = ((StochasticDataCollector)collector).getTrajectoryStatistics();
//                } catch (Exception e) {
//                    this.printError(e.getMessage());
//                    return;
//               }
//
//            } else if (collector instanceof HybridDataCollector) {
//                try {
//                    stat = ((HybridDataCollector)collector).getTrajectoryStatistics();
//                } catch (Exception e) {
//                    this.printError(e.getMessage());
//                    return;
//               }
//            } else {
//                this.printError("Data contains a determinsitic traces, cannot collect statistics!");
//                return;
//            }
//            StatisticsVariable var = new StatisticsVariable(statVar,stat);
//            this.variables.put(statVar, var);
//            out.println("Statistics computed for $" + collectorVar + " and stored in variable $" + statVar);
//        }
//    }

    public TrajectoryStatistics getStatistics(String dataVar) {
        if (!this.variables.containsKey(dataVar)) {
            this.printError("Variable " + dataVar + " not defined");
            return null;
        } else if (!this.variables.get(dataVar).isCollectorVariable()) {
            this.printError("Variable " + dataVar + " is not a trajectory data variable");
            return null;
        } else if (this.variables.get(dataVar).isCollectorVariable() && !this.getCollector(dataVar).containsStatisticsData()) {
            this.printError("Variable " + dataVar + " does not contains statistics");
            return null;
        }
        else {
            return this.getCollector(dataVar).getTrajectoryStatistics();
        }
     }

    public void plot(Plotter plotter, ArrayList<String> data) {
        try {
            String s = plotter.plot() + "for datasets ";
            int i=0;
            for (String s1 : data)
                s += (i++ > 0 ? ", $" : "$") + s1;
            out.println(s);
        } catch (Exception e) {
            this.printError(e.getMessage());
        }
    }

    public void plot(Plotter plotter, String data) {
        try {
            String s = plotter.plot();
            s += "for dataset $" + data;
            out.println(s);
        } catch (Exception e) {
            this.printError(e.getMessage());
        }
    }

    public void save(Saver saver, String data) {
        try {
            String s = saver.save();
            s += "for dataset $" + data;
            out.println(s);
        } catch (Exception e) {
            this.printError(e.getMessage());
        }
    }


    public void changeSymbolValue(String modelVar, String symbol, double value) {
        if (!this.variables.containsKey(modelVar)) {
            this.printError("Variable " + modelVar + " not defined.");
            return;
        }
        else if (!this.variables.get(modelVar).isModelVariable() && !this.variables.get(modelVar).isModelCheckingVariable()) {
            this.printError("Variable " + modelVar + " is not a model variable or a model checking variable.");
            return;
        } else {
            if (this.variables.get(modelVar).isModelVariable()) {
                ModelVariable var = (ModelVariable)variables.get(modelVar);
                if (var.model.getStore().getParametersReference().containsSymbol(symbol)) {
                    var.model.changeInitialValueOfParameter(symbol, value);
                    out.println("Value of " + symbol + " changed to " +
                            String.format(Locale.UK, "%.4f", value) + " in $" + modelVar);
                }
                else if (var.model.getStore().getVariablesReference().containsSymbol(symbol)) {
                    var.model.changeInitialValueOfVariable(symbol, value);
                    out.println("Value of " + symbol + " changed to " +
                            String.format(Locale.UK, "%.4f", value) + " in $" + modelVar);
                }
                else
                    this.printError(symbol + " is not a variable or a parameter of $" + modelVar);
            }
            else if (this.variables.get(modelVar).isModelCheckingVariable()) {
                ModelCheckingVariable var = (ModelCheckingVariable)variables.get(modelVar);
                var.checker.setParameter(symbol, value);
            }
        }



    }


    public void help() {
        out.println("SimHyA " + GlobalOptions.version +" help system");
        out.println("");
        out.println("Command List");
        String [] commands = this.helpDescr.keySet().toArray(new String[0]);
        java.util.Arrays.sort(commands);
        for (String c : commands)
            out.println("  " + c + " -- " + this.helpDescr.get(c));
        out.println("\nRun \"help commandname\" to obtain help on a specific command");
    }

    public void help(String command) {
        out.println("SimHyA " + GlobalOptions.version +" help system");
        out.println("");
        String h = this.helpText.get(command);
        if (h==null)
            out.println("  Unknown command: " + command);
        else
            out.println(h);
    }

    public void free(String variable) {
        if (!this.containsVariable(variable))
            this.printError("Variable $" + variable + " does not exist.");
        else {
            this.variables.remove(variable);
            System.gc();
            this.out.println("Variable $" + variable + " correctly freed.");
        }
    }


    private void loadHelp() {
        try {
            java.io.BufferedReader r;
            r = new java.io.BufferedReader(new java.io.FileReader(helpFile));
            String c = null, d=null, h = "",l;
            while (r.ready()) {
                l = r.readLine();
                if (l.startsWith("#")) {
                    //found a new command
                    c = l.substring(1).trim();
                    if (this.helpDescr.containsKey(c)) {
                        this.printError("Failed to load help system: command " + c + " already defined");
                        return;
                    }
                    d = null;
                    h = "";
                } else if (l.startsWith("%")) {
                    if (d != null) {
                        this.printError("Failed to load help system: description of command " + c + " already defined");
                        return;
                    }
                    d = l.substring(1).trim();
                    this.helpDescr.put(c, d);
                } else {
                    h += l + "\n";
                    this.helpText.put(c, h);
                }
            }
            r.close();
        } catch (java.io.IOException e) {
            this.printError("Failed to load the help file. Check that the file \"help\""
                    + "is in the main program directory and that it is not corrupted."
                    + "No help will be available!");
            return;
        }
    }

    public boolean containsVariable(String name) {
        return this.variables.containsKey(name);
    }


    public void setPanelOutput(JPanel panel) {
        this.lastPanel = panel;
        this.lastOutputType = CommandManager.FRAME_OUTPUT;
    }

    public void setStringOutput(String text) {
        this.lastString = text;
        this.lastOutputType = CommandManager.STRING_OUTPUT;
    }


    ////////////////////////////////////////////////////////////////
    //////////// IMPLEMENTATION OF COMMANDER INTERFACE ////////////
    ///////////////////////////////////////////////////////////////


    public Object execute(String command) {
        this.lastOutputType = CommandManager.NO_OUTPUT;
        this.executeCommand(command);
        switch (this.lastOutputType) {
            case CommandManager.FRAME_OUTPUT:
                return this.lastPanel;
            case CommandManager.STRING_OUTPUT:
                return this.lastString;
            case CommandManager.NO_OUTPUT:
                return null;
            default:
                return null;
        }
    }

    public ArrayList<Symbol> getExplorableParameters(String variable) {
        ArrayList<Symbol> list = new ArrayList<Symbol>();
        if (this.isModelVariable(variable)) {
            FlatModel model = this.getModel(variable);
            ArrayList<String> n = model.getStore().getNameOfAllParameters();
            double [] v = model.getStore().getParametersValues();
            for (int i=0;i<n.size();i++)
                if (model.containsExplorableParameter(n.get(i)))
                    list.add(new Symbol(n.get(i),v[i]));
            n = model.getStore().getNameOfAllVariables();
            v = model.getStore().getVariablesValues();
            for (int i=0;i<n.size();i++)
                if (model.containsExplorableVariable(n.get(i)))
                    list.add(new Symbol(n.get(i),v[i]));
        }  else {
            throw new RuntimeException("Wrong variable name, only model variables support query on parameters");
        }
        return list;
    }

    public ArrayList<ExploredSymbol> getExploredParameters(String variable) {
        ArrayList<ExploredSymbol> list = new ArrayList<ExploredSymbol>();
        if (this.isDeterministicExploratorVariable(variable)) {
            TrajectoryExploratorDataCollector data = this.getTrajectoryExplorator(variable);
            for (ParamValueSet p : data.getExploredParamSet()) {
                if (p instanceof ParamPoints) {
                    list.add(new ExploredSymbol(p.getName(),((ParamPoints)p).getPointValues()));
                } else if (p instanceof ParamRange) {
                    list.add(new ExploredSymbol(p.getName(),((ParamRange)p).getFirst(),((ParamRange)p).getLast(),
                            ((ParamRange)p).getPoints(),((ParamRange)p).isLog()));
                }
            }
        } else if (this.isStatisticsExploratorVariable(variable)) {
            StatisticsExploratorDataCollector data = this.getStatisticExplorator(variable);
            for (ParamValueSet p : data.getExploredParamSet()) {
                if (p instanceof ParamPoints) {
                    list.add(new ExploredSymbol(p.getName(),((ParamPoints)p).getPointValues()));
                } else if (p instanceof ParamRange) {
                    list.add(new ExploredSymbol(p.getName(),((ParamRange)p).getFirst(),((ParamRange)p).getLast(),
                            ((ParamRange)p).getPoints(),((ParamRange)p).isLog()));
                }
            }
        } else {
            throw new RuntimeException("Wrong variable name, explorator variable required");
        }
        return list;
    }

    public ArrayList<Symbol> getParameters(String variable) {
        ArrayList<Symbol> list = new ArrayList<Symbol>();
        if (this.isModelVariable(variable)) {
            FlatModel model = this.getModel(variable);
            ArrayList<String> n = model.getStore().getNameOfAllParameters();
            double [] v = model.getStore().getParametersValues();
            for (int i=0;i<n.size();i++)
                list.add(new Symbol(n.get(i),v[i]));
        }  else {
            throw new RuntimeException("Wrong variable name, only moel variables support query on parameters");
        }
        return list;
    }

    public ArrayList<Symbol> getVariables(String variable) {
        ArrayList<Symbol> list = new ArrayList<Symbol>();
        if (this.isModelVariable(variable)) {
            FlatModel model = this.getModel(variable);
            ArrayList<String> n = model.getStore().getNameOfAllVariables();
            double [] v = model.getStore().getVariablesValues();
            for (int i=0;i<n.size();i++)
                list.add(new Symbol(n.get(i),v[i]));
            n = model.getStore().getNameOfAllExpressionVariables();
            v = model.getStore().getExpressionVariablesValues();
            for (int i=0;i<n.size();i++)
                list.add(new Symbol(n.get(i),v[i]));
        } else if (this.isTrajectoryDataVariable(variable)) {
            DataCollector data = this.getCollector(variable);
            List<String> n = data.getNameOfSavedVariables();
            for (int i=0;i<n.size();i++)
                list.add(new Symbol(n.get(i),0));
//        } else if (this.isStatisticsVariable(variable)) {
//            TrajectoryStatistics data = this.getStatistics(variable);
//            ArrayList<String> n = data.getVariableNames();
//            for (int i=0;i<n.size();i++)
//                list.add(new Symbol(n.get(i),0));
        } else if (this.isDeterministicExploratorVariable(variable)) {
            TrajectoryExploratorDataCollector data = this.getTrajectoryExplorator(variable);
            ArrayList<String> n = data.getVariableNames();
            for (int i=0;i<n.size();i++)
                list.add(new Symbol(n.get(i),0));
        } else if (this.isStatisticsExploratorVariable(variable)) {
            StatisticsExploratorDataCollector data = this.getStatisticExplorator(variable);
            ArrayList<String> n = data.getVariableNames();
            for (int i=0;i<n.size();i++)
                list.add(new Symbol(n.get(i),0));
        } else {
            throw new RuntimeException("Wrong variable name, this is not a model or data variable");
        }
        return list;
    }

    public ArrayList<String> getListOfDataVariables() {
        ArrayList<String> list = new ArrayList<String>();
        for (String s : this.variables.keySet())
            if (this.isDataVariable(s))
                list.add(s);
        return list;
    }

    public ArrayList<String> getListOfDeterministicExploratorDataVariables() {
        ArrayList<String> list = new ArrayList<String>();
        for (String s : this.variables.keySet())
            if (this.isDeterministicExploratorVariable(s))
                list.add(s);
        return list;
    }

    public ArrayList<String> getListOfModelVariables() {
        ArrayList<String> list = new ArrayList<String>();
        for (String s : this.variables.keySet())
            if (this.isModelVariable(s))
                list.add(s);
        return list;
    }

//    public ArrayList<String> getListOfStatisticsDataVariables() {
//        ArrayList<String> list = new ArrayList<String>();
//        for (String s : this.variables.keySet())
//            if (this.isStatisticsVariable(s))
//                list.add(s);
//        return list;
//    }

    public ArrayList<String> getListOfStatisticsExploratorDataVariables() {
        ArrayList<String> list = new ArrayList<String>();
        for (String s : this.variables.keySet())
            if (this.isStatisticsExploratorVariable(s))
                list.add(s);
        return list;
    }

    public ArrayList<String> getListOfTrajectoryDataVariables() {
        ArrayList<String> list = new ArrayList<String>();
        for (String s : this.variables.keySet())
            if (this.isTrajectoryDataVariable(s))
                list.add(s);
        return list;
    }

    public boolean exploratorVariableContainsFinalStateData(String variable) {
        if (this.isStatisticsExploratorVariable(variable)) {
            StatisticsExploratorDataCollector data = this.getStatisticExplorator(variable);
            return data.containsFinalStateData();
        } else if (this.isDeterministicExploratorVariable(variable)) {
            return true;
        }  else return false;
    }

    public boolean exploratorVariableContainsFinalStateStatistics(String variable) {
        if (this.isStatisticsExploratorVariable(variable)) {
            return true;
        } else if (this.isDeterministicExploratorVariable(variable)) {
            return false;
        }  else return false;
    }

    public boolean exploratorVariableContainsTrajectoryData(String variable) {
        if (this.isStatisticsExploratorVariable(variable)) {
            StatisticsExploratorDataCollector data = this.getStatisticExplorator(variable);
            return data.containsTrajectoryData();
        } else if (this.isDeterministicExploratorVariable(variable)) {
            TrajectoryExploratorDataCollector data = this.getTrajectoryExplorator(variable);
            return data.containsTrajectoryData();
        }  else return false;
    }

    public boolean exploratorVariableContainsTrajectoryStatistics(String variable) {
        if (this.isStatisticsExploratorVariable(variable)) {
            StatisticsExploratorDataCollector data = this.getStatisticExplorator(variable);
            return data.containsTrajectoryStatistics();
        } else if (this.isDeterministicExploratorVariable(variable)) {
            return false;
        }  else return false;
    }

    public ArrayList<String> getIntegratorCodeList() {
        return IntegratorType.getCodeList();
    }

    public ArrayList<String> getIntegratorDescriptionList() {
        return IntegratorType.getDescriptionList();
    }

    public double getDefaultAtol() {
        return GlobalOptions.defaultAbsoluteTolerance;
    }

    public double getDefaultMaxEventError() {
        return GlobalOptions.defaultMaxErrorForEvents;
    }

    public int getDefaultMaxEventIter() {
        return GlobalOptions.defaultMaxIterationforEvents;
    }

    public double getDefaultMaxEventStep() {
        return GlobalOptions.defaultMaxStepIncrementForEvents;
    }

    public double getDefaultMaxStepSize() {
        return GlobalOptions.defaultMaxStepSize;
    }

    public double getDefaultMinStepSize() {
        return GlobalOptions.defaultMinStepSize;
    }

    public double getDefaultRtol() {
        return GlobalOptions.defaultRelativeTolerance;
    }

    public int getDefaultSamplePoints() {
        return GlobalOptions.samplePoints;
    }

    public String getModelName(String variable) {
        if (this.isModelVariable(variable)) {
            FlatModel m = this.getModel(variable);
            return m.getName();
        }
        else return null;
    }

    public File getWorkingDirectory() {
        return currentDirectory;
    }

    public void setWorkingDirectory(File directory) {
        this.currentDirectory = directory;
    }

    public boolean trajectoryVariableContainsFinalStateData(String variable) {
        if (this.isTrajectoryDataVariable(variable)) {
            DataCollector data = this.getCollector(variable);
            return data.containsFinalStateData();
        } else return false;
    }

    public boolean trajectoryVariableContainsFinalStateHigherOrderMoments(String variable) {
        if (this.isTrajectoryDataVariable(variable)) {
            DataCollector data = this.getCollector(variable);
            return data.containsStatisticsFinalStateDataOnHigherOrderMoments();
        } else return false;
    }

    public boolean trajectoryVariableContainsStatistics(String variable) {
        if (this.isTrajectoryDataVariable(variable)) {
            DataCollector data = this.getCollector(variable);
            return data.containsStatisticsData();
        } else return false;
    }

    public boolean trajectoryVariableContainsTrajectoryData(String variable) {
        if (this.isTrajectoryDataVariable(variable)) {
            DataCollector data = this.getCollector(variable);
            return data.containsTrajectoryData();
        } else return false;
    }

    public boolean trajectoryVariableContainsTrajectoryHigherOrderMoments(String variable) {
        if (this.isTrajectoryDataVariable(variable)) {
            DataCollector data = this.getCollector(variable);
            return data.containsStatisticsTrajectoryDataOnHigherOrderMoments();
        } else return false;
    }

    public int getNumberOfRunsInTrajectoryVariable(String variable) {
        if (this.isTrajectoryDataVariable(variable)) {
            DataCollector data = this.getCollector(variable);
            return data.getNumberOfTrajectories();
        } else return -1;
    }



    public static void main(String[] args) throws Exception {
        CommandManager cmd = new CommandManager();
        cmd.setEmbeddedInGuiStatus(false);
        cmd.setOutputStream(System.out);
        cmd.setErrorStream(System.err);

        String model = "m", checker = "c", data="d";
        String model_file = "SIR_M.txt";
        String checker_file = "sir_F.txt";

        cmd.executeCommand("$m = load(SIR_M.txt,flat)");
        cmd.executeCommand("$c = load(sir_F.txt,check=$m)");
        //cmd.executeCommand("$d1 = mc($c,error=0.05,confidence=0.95,signal,adaptive=50,bayesian,prior=(1,1),significance=0.005)");
        cmd.executeCommand("$d = mc($c,error=0.05,confidence=0.95,formula=[extinction],bayesian,signal,adaptive=50,prior=(1,1),typeI=0.01,typeII=0.02,explore=[T_ext:50:200],points=100)");
        //cmd.executeCommand("mc.save($d,signal,file=outputMC.txt)");
        cmd.executeCommand("mc.plot($d,formula=[extinction])");
        cmd.executeCommand("$d1 = mc($c,ode,runs=1)");


//        cmd.loadFlatModel(model, model_file);
//        cmd.loadSMCenvironment(checker, checker_file, model);

//        ModelCheckerManager man = new ModelCheckerManager(checker,data,cmd);
//        man.error = 0.05;
//        man.type = SMCtype.SIGNAL;
//        man.check();
//
//        ModelCheckingOutputManager outman = new ModelCheckingOutputManager(data,cmd);
//        outman.getAllFormulas();
//        outman.saveSignals();
//        outman.filename = "outputMC.txt";
//        cmd.save(outman, data);
//
//        outman = new ModelCheckingOutputManager(data,cmd);
//        outman.allSignals  =false;
//        outman.plotSignals = true;
//        ArrayList<String> list = new ArrayList<String>();
//        list.add("extinction");
//        outman.formulas = list;
//        cmd.plot(outman, data);


    }






}
