/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine.paramexplore;

import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.dataprocessing.*;
import java.util.ArrayList;
import java.util.Locale;
import eggloop.flow.simhya.simhya.simengine.*;
import eggloop.flow.simhya.simhya.simengine.utils.InactiveProgressMonitor;


/**
 *
 * @author Luca
 */
public class StochasticParamExplorator extends ParamExplorator {
    SimType simAlgorithm;
    StatisticsExploratorDataCollector collector;
    int runs = 1000;
    double finalTime = 100;
    double burnoutTime = 0.0;
    int points = 100;
    

    boolean saveTrajectories;
    boolean separateFiles;
    boolean saveFinalStateOnly;
    boolean storeTrajectoryDistributionData;
    boolean storeFinalStateDistributionData;
    String trajectoryFilename;
    ArrayList<String> varsToSave;

    private boolean firstConfiguration;



    public StochasticParamExplorator(FlatModel model, SimType algorithm) {
        super(model);
        if (!algorithm.isStochastic())
            throw new IllegalArgumentException("Stochastic explorator requires a stochastic simulation engine");
        else
            this.simAlgorithm = algorithm;
        varsToSave = null;
        saveFinalStateOnly = false;
        storeTrajectoryDistributionData = false;
        storeFinalStateDistributionData = true;
        saveTrajectories = false;
        separateFiles = false;
        trajectoryFilename = "output.txt";
    }

    public void saveTrajectoriesToSameFile(String filename) {
        this.saveTrajectories = true;
        this.separateFiles = false;
        trajectoryFilename = filename;
    }

    public void saveTrajectoriesToDifferentFiles(String filenameMatrix) {
        this.saveTrajectories = true;
        this.separateFiles = true;
        trajectoryFilename = filenameMatrix;
    }

   
  
    public void doNotSaveTrajectories() {
        this.saveTrajectories = false;
    }

    public void setRunsPerConfiguration(int runs) {
        this.runs = runs;
    }

    /**
     * Tells the explorer to save only the final state of each simulation
     * @param finalTime the final time of the simulation
     * @param saveFinalStateDistribution true if the distribution of final state is conserved
     */
    public void saveFinalStateOnly(double finalTime,  boolean saveFinalStateDistribution) {
        this.finalTime = finalTime;
        this.saveFinalStateOnly = true;
        this.storeFinalStateDistributionData = saveFinalStateDistribution;
        this.storeTrajectoryDistributionData = false;
    }

     /**
     * Tells the explorer to save trajectory data
     * @param finalTime the final time of each simulation
     * @param points the number of points per trajectory
     * @param storeDistributionData true if distribution data of trajectory point is to be kept.
     */
    public void saveTrajectoryData(double finalTime, int points, boolean storeDistributionData) {
        this.burnoutTime = 0.0;
        this.finalTime = finalTime;
        this.points = points;
        this.storeTrajectoryDistributionData = storeDistributionData;
        this.saveFinalStateOnly = false;
        this.storeFinalStateDistributionData = true;
    }

    /**
     * Tells the explorer to save trajectory data
     * @param burnoutTime the initial saving time
     * @param finalTime the final time of each simulation
     * @param points the number of points per trajectory
     * @param storeDistributionData true if distribution data of trajectory point is to be kept.
     */
    public void saveTrajectoryData(double burnoutTime, double finalTime, int points, boolean storeDistributionData) {
        this.burnoutTime = burnoutTime;
        this.finalTime = finalTime;
        this.points = points;
        this.storeTrajectoryDistributionData = storeDistributionData;
        this.saveFinalStateOnly = false;
        this.storeFinalStateDistributionData = true;
    }

    public void setVariablesToSave(ArrayList<String> vars) {
        this.varsToSave = vars;
    }
    

    private void setupDataCollector() {
        this.collector = new StatisticsExploratorDataCollector(this.paramSet);
        int n;
        if (this.varsToSave != null)
            n = varsToSave.size() + 1;
        else
            n = model.getStore().getNumberOfVariables() + model.getStore().getNumberOfExpressionVariables() + 1;
        collector.setupSaveOptions(n,saveFinalStateOnly, storeTrajectoryDistributionData, storeFinalStateDistributionData);
        
        ArrayList<String> vars = getVarNamesForCollector(varsToSave);
        collector.setVariableNames(vars);
        collector.setModelName(model.getName());
    }


    
    public StatisticsExploratorDataCollector run() {
        if (!this.checkMemoryRequirements(saveFinalStateOnly, storeTrajectoryDistributionData))
            throw new IllegalArgumentException("Too much memory required by the exploration.\n"
                    + "Try to save less variables, discard distribution data, or to save only final state data");
        setupDataCollector();
        firstConfiguration = true;
        this.explore();
        return collector;
    }



    @Override
    public boolean checkMemoryRequirements(boolean onlyFinalTime, boolean keepAllData) {
        int varN; 
        long data, conf;
        if (this.varsToSave == null)
            varN = model.getStore().getNumberOfVariables() + model.getStore().getNumberOfExpressionVariables();
        else
            varN = varsToSave.size();
        varN++; //need to store time
        conf = numberOfConfigurations();
        //data per variable per final state
        data = 4 + (storeTrajectoryDistributionData || storeFinalStateDistributionData ? runs : 0);
        //data per variable per trajectory
        data += (!saveFinalStateOnly ? points*(2 + (storeTrajectoryDistributionData ? runs : 0 )) : 0);
        //bytes per double times number of configurations times number of saved variables
        data *= 8*conf*varN; 
        System.gc();
        long freeMem = Runtime.getRuntime().freeMemory();
        if (data < 0 || data >= freeMem)
            return false;
        return true;
    }

    @Override
    void exploreConfiguration() {
        Simulator sim;
        StochasticDataCollector localCollector = new StochasticDataCollector(model);
        //set data collector parameters
        if (this.saveFinalStateOnly)
            localCollector.saveOnlyFinalState();
        else if (burnoutTime > 0.0)
            localCollector.setPrintConditionByTime(burnoutTime, points, finalTime);
        else
            localCollector.setPrintConditionByTime(points, finalTime);
        if (this.varsToSave != null)
            localCollector.setVarsToBeSaved(varsToSave);
        localCollector.setParamsToBePrinted(getParametersToExplore());
        //add here storage mechanism
        if (this.storeTrajectoryDistributionData)
            localCollector.storeWholeTrajectoryData(runs);
        else if (this.storeFinalStateDistributionData)
            localCollector.storeFinalStateDataOnly(runs);
        else
            localCollector.storeStatisticsOnly(runs);
        sim = SimulatorFactory.newSimulator(simAlgorithm, model, localCollector);
        //set simulation parameters
        sim.setProgressMonitor(new InactiveProgressMonitor());
        sim.setFinalTime(finalTime);
        sim.initialize();
        //localCollector.clearAllTrajectories();
        
        long time = System.nanoTime();
        for (int i=0;i<runs;i++) {
            setCurrentParameterCombination();
            localCollector.newTrajectory();
            sim.resetModel(false);
            sim.reinitialize();
            sim.run();
            if (this.verbosity == 2) {
                out.println("Run " + (i+1) + " of " + runs + " completed in "
                        + String.format(Locale.US, "%.4f", sim.getLastSimulationTimeInSecs()) + " seconds. "
                        + "Time remaining for current batch of runs: " + String.format(Locale.US, "%.4f", ((double)(System.nanoTime() - time)/1000000000)/(i+1)*(runs-i-1) ) + " seconds.");
            }
        }

        if (this.saveTrajectories) {
            if (this.separateFiles) {
                String file = this.trajectoryFilename + "_" + this.currentConfigurationToString() + ".txt";
                localCollector.saveAllTrajectoriesToCSV(file, false);
            } else {
                if (firstConfiguration) {
                    firstConfiguration = false;
                    localCollector.saveAllTrajectoriesToCSV(trajectoryFilename, false, false);
                } else
                    localCollector.saveAllTrajectoriesToCSV(trajectoryFilename, false, true);
            }
        }

        TrajectoryStatistics stats = localCollector.getTrajectoryStatistics();
        //allow to delete data only for traj but not for final point
//        if (!this.storeTrajectoryDistributionData) {
//            stats.keepFinalData = this.storeFinalStateDistributionData;
//            stats.deleteRawData();
//        }
         //store stat data to stat data collector
        collector.addDataPoint(stats);
        monitor.nextStep();
    }





}
