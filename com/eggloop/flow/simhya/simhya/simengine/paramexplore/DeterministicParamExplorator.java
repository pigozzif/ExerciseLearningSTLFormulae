/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.paramexplore;

import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import com.eggloop.flow.simhya.simhya.dataprocessing.*;
import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.simengine.*;
import com.eggloop.flow.simhya.simhya.simengine.utils.InactiveProgressMonitor;
import com.eggloop.flow.simhya.simhya.simengine.ode.*;
import com.eggloop.flow.simhya.simhya.GlobalOptions;

/**
 *
 * @author Luca
 */
public class DeterministicParamExplorator extends ParamExplorator {

   
    TrajectoryExploratorDataCollector collector;
    double finalTime = 100;
    double burnoutTime = 0.0;
    int points = GlobalOptions.samplePoints;


    boolean saveTrajectories;
    boolean separateFiles;
    boolean saveFinalStateOnly;
    boolean storeTrajectoryData;
    String trajectoryFilename;
    ArrayList<String> varsToSave;

    private boolean firstConfiguration;

    //ode integrator parameters
    double atol = -1;
    double rtol = -1;
    double maxStep = -1;
    double minStep = -1;
    double maxEventStep = -1;
    int maxEventIter = -1;
    double maxEventError = -1;
    IntegratorType odetype = null;


    public DeterministicParamExplorator(FlatModel model) {
        super(model);
        varsToSave = null;
        saveFinalStateOnly = true;
        storeTrajectoryData = false;
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

   
    /**
     * Tells the explorer to save only the final state of each simulation
     * @param finalTime the final time of the simulation
     * @param saveFinalStateDistribution true if the distribution of final state is conserved
     */
    public void saveFinalStateOnly(double finalTime) {
        this.finalTime = finalTime;
        this.saveFinalStateOnly = true;
        this.storeTrajectoryData = false;
    }

     /**
     * Tells the explorer to save trajectory data
     * @param finalTime the final time of each simulation
     * @param points the number of points per trajectory
     */
    public void saveTrajectoryData(double finalTime, int points) {
        this.burnoutTime = 0.0;
        this.finalTime = finalTime;
        this.points = points;
        this.storeTrajectoryData = true;
        this.saveFinalStateOnly = false;
    }

    /**
     * Tells the explorer to save trajectory data
     * @param burnoutTime the initial saving time
     * @param finalTime the final time of each simulation
     * @param points the number of points per trajectory
     */
    public void saveTrajectoryData(double burnoutTime, double finalTime, int points) {
        this.burnoutTime = burnoutTime;
        this.finalTime = finalTime;
        this.points = points;
        this.storeTrajectoryData = true;
        this.saveFinalStateOnly = false;
    }

    public void setVariablesToSave(ArrayList<String> vars) {
        this.varsToSave = vars;
    }

    public void setAbsoluteTolerance(double atol) {
        this.atol = atol;
    }

    public void setMaxErrorForEvents(double maxEventError) {
        this.maxEventError = maxEventError;
    }

    public void setMaxIterationforEvents(int maxEventIter) {
        this.maxEventIter = maxEventIter;
    }

    public void setMaxStepIncrementForEvents(double maxEventStep) {
        this.maxEventStep = maxEventStep;
    }

    public void setMaximumStepSize(double maxStep) {
        this.maxStep = maxStep;
    }

    public void setMinimumStepSize(double minStep) {
        this.minStep = minStep;
    }

    public void setIntegrator(IntegratorType odetype) {
        this.odetype = odetype;
    }

    public void setRelativeTolerance(double rtol) {
        this.rtol = rtol;
    }




    void setupDataCollector() {
        this.collector = new TrajectoryExploratorDataCollector(this.paramSet);
        int n;
        if (this.varsToSave != null)
            n = varsToSave.size() + 1;
        else
            n = model.getStore().getNumberOfVariables() + model.getStore().getNumberOfExpressionVariables() + 1;
        collector.setupSaveOptions(n,saveFinalStateOnly);
        ArrayList<String> vars = getVarNamesForCollector(varsToSave);
        collector.setVariableNames(vars);
        collector.setModelName(model.getName());
    }



    public TrajectoryExploratorDataCollector run() {
        if (!this.checkMemoryRequirements(saveFinalStateOnly,true))
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
        data = 4;
        //data per variable per trajectory
        data += (!saveFinalStateOnly ? 2*points : 0);
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
        DeterministicSimulator sim;
        OdeDataCollector localCollector = new OdeDataCollector(model);

        //set data collector parameters
        localCollector.storeWholeTrajectoryData(1);
        if (this.saveFinalStateOnly)
            localCollector.saveOnlyFinalState();
        else if (burnoutTime > 0.0)
            localCollector.setPrintConditionByTime(burnoutTime, points, finalTime);
        else
            localCollector.setPrintConditionByTime(points, finalTime);
        if (this.varsToSave != null)
            localCollector.setVarsToBeSaved(varsToSave);
        localCollector.setParamsToBePrinted(getParametersToExplore());

        sim = SimulatorFactory.newODEsimulator(model, localCollector);
        //set simulation parameters
        sim.setProgressMonitor(new InactiveProgressMonitor());
        sim.setFinalTime(finalTime);
        if (odetype != null)
            sim.setIntegrator(odetype);
        if (atol > 0)
            sim.setAbsoluteTolerance(atol);
        if (rtol > 0)
            sim.setRelativeTolerance(rtol);
        if (this.maxStep > 0)
            sim.setMaximumStepSize(maxStep);
        if (this.minStep > 0)
            sim.setMinimumStepSize(this.minStep);
        if (this.maxEventError > 0)
            sim.setMaxErrorForEvents(this.maxEventError);
        if (this.maxEventStep > 0)
            sim.setMaxStepIncrementForEvents(this.maxEventStep);
        if (this.maxEventIter > 0)
            sim.setMaxIterationforEvents(maxEventIter);

        
        sim.initialize();
        setCurrentParameterCombination();
        sim.resetModel(false);
        sim.run();
        
        postProcessSimulation(localCollector);

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

        //store trajectory data to trajectory data collector
        collector.addDataPoint(localCollector.getTrajectory(0));
        monitor.nextStep();
    }

    
    void postProcessSimulation(OdeDataCollector localCollector) {
        
    }


}
