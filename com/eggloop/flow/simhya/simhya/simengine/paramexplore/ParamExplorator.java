/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.paramexplore;

import java.util.ArrayList;
import java.util.Locale;
import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import com.eggloop.flow.simhya.simhya.simengine.ProgressMonitor;
import com.eggloop.flow.simhya.simhya.simengine.utils.*;
import java.io.PrintStream;

/**
 *
 * @author Luca
 */
public abstract class ParamExplorator {

    ArrayList<ParamValueSet> paramSet;
    FlatModel model;
    ProgressMonitor monitor;
    boolean useCache;
    PrintStream out;
    final int SILENT = 0;
    final int VERBOSE = 2;
    int verbosity = 1;
    
    

    
    ParamExplorator(FlatModel model) {
        this.model = model;
        model.resetToInitialState();
        paramSet = new ArrayList<ParamValueSet>();
        monitor = new PrintStreamProgressMonitor(System.out);
    }


    public void setProgressMonitor(ProgressMonitor monitor) {
        if (monitor != null)
            this.monitor = monitor;
    }

    public void setOutputStream(PrintStream out) {
        this.out = out;
        this.forceMonitor();
    }

    public void setVerbosityLevel(int level) {
        if (level < 0 )
            this.verbosity = SILENT;
        if (level > VERBOSE)
            this.verbosity = VERBOSE;
        else this.verbosity = level;
        this.forceMonitor();

//        System.out.println("Verbosity level set correctly to " + verbosity);

    }

    private void forceMonitor() {
        if (this.verbosity == SILENT)
            this.monitor = new InactiveProgressMonitor();
        else 
            this.monitor = new PrintStreamProgressMonitor(out);
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * Adds a new parameter of initial value of variable to explore
     * @param name the name of the parameter/variable;
     * @param firstValue the first value in the range
     * @param lastValue the last value in the range
     * @param points the number of points to sample
     * @param logarithmic true if the interval range is sampled in logarithmic scale
     */
    public void addParameterToExplore(String name, double firstValue, double lastValue, int points, boolean logarithmic) {
        if ( model.containsExplorableVariable(name) ) {
            //we are changing the initial value of a variable
            ParamRange range = new ParamRange(false,name,firstValue,lastValue,points,logarithmic);
            this.paramSet.add(range);
        }
        else if( model.containsExplorableParameter(name) ) {
            //we are changing the value of a parameter
            ParamRange range = new ParamRange(true,name,firstValue,lastValue,points,logarithmic);
            this.paramSet.add(range);
        } else
            throw new IllegalArgumentException("There is no explorable variable or parameter with name " + name + "\n"
                    + "Either there is no parameter or variable with such name or\n"
                    + "they have an initial value defined in terms of an expression involving other variables and parameters.");
    }


    /**
     * Adds a parameter to explore, with a set of values explicitly specified
     * @param name the name of the parameter/variable
     * @param values an array of values
     */
    public void addParameterToExplore(String name, double[] values) {
        if ( model.containsExplorableVariable(name) ) {
            //we are changing the initial value of a variable
            ParamPoints range = new ParamPoints(name,false,values);
            this.paramSet.add(range);
        }
        else if( model.containsExplorableParameter(name) ) {
            //we are changing the value of a parameter
            ParamPoints range = new ParamPoints(name,true,values);
            this.paramSet.add(range);
        } else
            throw new IllegalArgumentException("There is no explorable variable or parameter with name " + name + "\n"
                    + "Either there is no parameter or variable with such name or\n"
                    + "they have an initial value defined in terms of an expression involving other variables and parameters.");
    }

    /**
     * Adds a parameter to explore, with a set of values explicitly specified
     * @param name the name of the parameter/variable
     * @param values a list of values
     */
    public void addParameterToExplore(String name, ArrayList<Double> values) {
        if ( model.containsExplorableVariable(name) ) {
            //we are changing the initial value of a variable
            ParamPoints range = new ParamPoints(name,false,values);
            this.paramSet.add(range);
        }
        else if( model.containsExplorableParameter(name) ) {
            //we are changing the value of a parameter
            ParamPoints range = new ParamPoints(name,true,values);
            this.paramSet.add(range);
        } else
            throw new IllegalArgumentException("There is no explorable variable or parameter with name " + name + "\n"
                    + "Either there is no parameter or variable with such name or\n"
                    + "they have an initial value defined in terms of an expression involving other variables and parameters.");
    }


    /**
     * Resets the model to initial state and sets its initial values of variables and
     * its values of parameters to the current combination.
     */
    void setCurrentParameterCombination() {
        model.resetToBasicInitialState();
        for (ParamValueSet range : paramSet) {
            double x = range.getCurrentValue();
            if (range.isParameter())
               model.setValueOfParameter(range.getName(), x);
            else 
               model.setValueOfVariable(range.getName(), x);
        }
        model.computeInitialValues();
    }
    
    /**
     * Constructs the names of variables ot be saved according to the order in which they are saved 
     * in the trajectories
     * @param varsToSave the list of variables that the used declared to save
     * @return 
     */
    ArrayList<String> getVarNamesForCollector(ArrayList<String> varsToSave) {
        ArrayList<String> vars = new ArrayList<String>();
        vars.add("time");
        if (varsToSave != null) {
            ArrayList<String> storeVars = model.getStore().getNameOfAllVariables();
            ArrayList<String> expVars = model.getStore().getNameOfAllExpressionVariables();
            for (String n : varsToSave)
                if (storeVars.contains(n))
                    vars.add(n);
            for (String n : varsToSave)
                if (expVars.contains(n))
                    vars.add(n);
        } else {
            vars.addAll(model.getStore().getNameOfAllVariables());
            vars.addAll(model.getStore().getNameOfAllExpressionVariables());
        }
        return vars;
    }

    /**
     * Checks the memory requirements.
     * @param onlyFinalTime true if only final state has to be saved.
     * @param keepAllData true if the explorator has to keep all trajectory data in memory.
     * If false, only statistics will be collected.
     * @return true if there is enough memory, false if the memory requirements are too high.
     */
    public abstract boolean checkMemoryRequirements(boolean onlyFinalTime, boolean keepAllData);


    /**
     *
     * @return the total number of different configurations that will be explored
     */
    long numberOfConfigurations() {
        long config = 1;
        for (ParamValueSet r : this.paramSet)
            config *= r.getPoints();
        return config;
    }

    /**
     *
     * @return a list of parameters that are explored. Returns only parameters, not variables.
     */
    ArrayList<String> getParametersToExplore() {
        ArrayList<String> list = new ArrayList<String>();
        for (ParamValueSet r : this.paramSet)
            if (r.isParameter())
                list.add(r.getName());
        if (list.isEmpty())
            return null;
        else
            return list;
    }


    String currentConfigurationToString() {
        String s = "";
        for (int i=0;i<this.paramSet.size();i++) {
            ParamValueSet r = this.paramSet.get(i);
            s += (i>0?"_":"") + r.getName() + "_" + String.format(Locale.UK, "%.6f", r.getCurrentValue());
        }
        return s;
    }
    

    /**
     * recursively explores the parameter space;
     * To be called by the proper function executing the exploration
     */
    void explore() {

        //System.out.println(paramSet.size());
        monitor.setTotalSteps(this.numberOfConfigurations());
        monitor.start();
        explore(0);
        monitor.stop();
    }

    /**
     * Recursively explores the parameter space
     * @param param
     * @param current
     */
    
    private void explore(int param) {
        
        if (param == paramSet.size()) {        
            exploreConfiguration();
            System.gc();
        } else {
            ParamValueSet range = this.paramSet.get(param);
            for (int i=0;i<range.getPoints();i++) {
                range.setCurrentPoint(i);
                //System.out.println("Parameter " + range.getName() + " set to " + range.getCurrentValue());
                explore(param+1);
            }
        }
    }

    /**
     * This function contains the code to perform an exploration of a point in the parameter space
     * Fill with the right code
     */
    abstract void exploreConfiguration();



   public double getExplorationTimeInSecs() {
       return monitor.getLastSimulationTimeInSecs();
   }



}
