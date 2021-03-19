/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.dataprocessing;

import com.eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DTrajectory;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.Plot3DTrajectory;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DSurface;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.Plot3DSurface;
import com.eggloop.flow.simhya.simhya.simengine.paramexplore.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * This class extracts trajectories in the parameter space from data collected
 * by parameter exploration
 * @author Luca
 */
public class ExploratorDataProcessor {

    private ArrayList<ParamValueSet> paramSet;
    private int[][] fixedParamCodes;
    private boolean[] fixedParam;
    private ArrayList<Integer> variableParams;
    private int[] currentParams;
    private ParamSetKey key;
    private int numberOfVariableParams;
    private int numberOfTrajectories;
    private int generatedTrajectories;

    /**
     * Constructs a data processor from a parameter exploration set, a list of
     * explored parameters to be considered as variables, and a list of {@link ParamValueSet}
     * to determine the values of fixed parameters. If a parameter is not in this list,
     * then its first value is assumed by default. If its value set contains multiple values, than
     * the processor generates multiple trajectories.
     * @param paramSet the set of parameters explored.
     * @param variableParams the parameters acting as variables (currently supports up to three parameters,
     * but if this set contains more then one parameter, then there can be only one combination of fixed parameters)
     * @param fixedParams possible values of fixed parameters. 
     */
    public ExploratorDataProcessor(ArrayList<ParamValueSet> paramSet,
            ArrayList<String> variableParams,
            ArrayList<ParamValueSet> fixedParamsList) {
        this.paramSet = paramSet;
        this.currentParams = new int[paramSet.size()];
        java.util.Arrays.fill(currentParams, 0);
        this.fixedParam = new boolean[paramSet.size()];
        java.util.Arrays.fill(fixedParam, true);
        this.fixedParamCodes = new int[paramSet.size()][];
        java.util.Arrays.fill(fixedParamCodes, null);
        this.key = new ParamSetKey();
        this.variableParams = new ArrayList<Integer>();
        numberOfVariableParams = 0;
        numberOfTrajectories = 1;
        generatedTrajectories = 0;
        this.initialize(variableParams, fixedParamsList);

    }

    /**
     * Constructs a data processor from a parameter exploration set, a list of
     * explored parameters to be considered as variables, a list of
     * explored parameters to be considered fixed, and a list of list of the values they should take. 
     * If a parameter is not in this list,      * then its first value is assumed by default.
     * If its value set contains multiple values, than
     * the processor generates multiple trajectories.
     * @param paramSet the set of parameters explored.
     * @param variableParams the parameters acting as variables (currently supports up to three parameters,
     * but if this set contains more then one parameter, then there can be only one combination of fixed parameters)
     * @param fixedParams the parameters to fix
     * @param fixedParamsValues the values of the parameters to fix.
     */
    public ExploratorDataProcessor(ArrayList<ParamValueSet> paramSet,
            ArrayList<String> variableParams,
            ArrayList<String> fixedParams,
            ArrayList<ArrayList<Double>> fixedParamsValues) {
        this.paramSet = paramSet;
        this.currentParams = new int[paramSet.size()];
        java.util.Arrays.fill(currentParams, 0);
        this.fixedParam = new boolean[paramSet.size()];
        java.util.Arrays.fill(fixedParam, true);
        this.fixedParamCodes = new int[paramSet.size()][];
        java.util.Arrays.fill(fixedParamCodes, null);
        this.key = new ParamSetKey();
        this.variableParams = new ArrayList<Integer>();
        numberOfVariableParams = 0;
        numberOfTrajectories = 1;
        generatedTrajectories = 0;

        ArrayList<ParamValueSet> fixedParamsList = new ArrayList<ParamValueSet>();
        for (int i = 0; i < fixedParams.size(); i++) {
            ArrayList<Double> list = fixedParamsValues.get(i);
            double[] d = new double[list.size()];
            for (int j = 0; j < list.size(); j++) {
                d[j] = list.get(j);
            }
            ParamPoints p = new ParamPoints(fixedParams.get(i), true, d);
            fixedParamsList.add(p);
        }
        this.initialize(variableParams, fixedParamsList);
    }

    /**
     * completes the initialization of the object
     * @param variableParams
     * @param fixedParamsList
     */
    private void initialize(ArrayList<String> variableParams, ArrayList<ParamValueSet> fixedParamsList) {
        if (variableParams.size() > 3) {
            throw new UnsupportedOperationException("Maximum number of variable parameters is 3.");
        }
        for (String s : variableParams) {
            int id = this.getIndex(s);
            this.fixedParam[id] = false;
            this.variableParams.add(id);
            numberOfVariableParams++;
        }

        for (ParamValueSet par : fixedParamsList) {

//            System.out.println("Building admissible points for parameter " + par.getName());

            int id = this.getIndex(par.getName());
            if (!this.fixedParam[id]) {
                throw new IllegalArgumentException("Paramater " + par.getName() + " is both fixed and variable.");
            }
            if (this.fixedParamCodes[id] != null) {
                throw new IllegalArgumentException("Paramater " + par.getName() + " has already been fixed.");
            }
            int n = par.getPoints();
            this.fixedParamCodes[id] = new int[n];

//             System.out.println("It has " + n + " admissible points.");

            for (int i = 0; i < n; i++) {
                double x = par.getValue(i);
                int k = this.paramSet.get(id).getIndexOfClosestPoint(x);

//                 System.out.println("Point " + (i+1) + " is " + x + " and has index " + k);

                this.fixedParamCodes[id][i] = k;
            }
            //insert code to clean the array fixedParamCodes[id], removing dupliate values.
        }
        for (int i = 0; i < paramSet.size(); i++) {
            if (this.fixedParamCodes[i] == null) {
                this.fixedParamCodes[i] = new int[1];
                this.fixedParamCodes[i][0] = 0;
            }
        }
        for (int i = 0; i < paramSet.size(); i++) {
            if (this.fixedParam[i]) {
                this.numberOfTrajectories *= this.fixedParamCodes[i].length;
            }
        }
        if (this.numberOfVariableParams > 1 && this.numberOfTrajectories > 1) {
            throw new DataException("Only one configuration of fixed parameters is supported for more than one"
                    + "variable parameter");
        }
    }

    /**
     *
     * @param name
     * @return the index of parameter name in the parameter Set
     */
    private int getIndex(String name) {
        for (int i = 0; i < this.paramSet.size(); i++) {
            if (name.equals(this.paramSet.get(i).getName())) {
                return i;
            }
        }
        throw new DataException("Parameter " + name + " not found in the explored set");
    }

    /**
     * resets the mechanism to generate trajectories
     */
    public void reset() {
        generatedTrajectories = 0;
        java.util.Arrays.fill(currentParams, 0);
    }

    /**
     * fixes the next parameter combination of fixed parameters
     */
    private void nextFixedParamCombination() {
        int n = paramSet.size();
        int i;
        for (i = n - 1; i >= 0; i--) {
            if (this.fixedParam[i] && this.currentParams[i] < this.fixedParamCodes[i].length - 1) {
                this.currentParams[i]++;
                break;
            }
        }
        for (int j = i + 1; j < n; j++) {
            if (this.fixedParam[j]) {
                this.currentParams[j] = 0;
            }
        }
    }

      /**
     *
     * @return a string with the configuration of non trivial fixed parameters.
     */
    private String getFixedParamString() {
        String s = "";
        for (int i = 0; i < paramSet.size(); i++) {
            if (this.fixedParam[i] && this.fixedParamCodes[i].length > 1) {
                s += this.paramSet.get(i).getName() + "_"
                        + String.format(Locale.UK, "%.5f", this.paramSet.get(i).getCurrentValue());
            }
        }
        return s;
    }


    /**
     * Returns a list of  1D trajectory where the final state statistics of the given var is plotted against
     * the different values of a parameter
     * @param vars a list of variable's names
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 1dTrajectory.
     */
    ArrayList<Plot2DTrajectory> getNextFinalState2DTrajectory(ArrayList<String> vars, StatType type,
            HashMap<ParamSetKey, TrajectoryStatistics> data) {
        if (this.numberOfVariableParams != 1) {
            throw new DataException("There are " + numberOfVariableParams + " variable parameters "
                    + "in the data set. 1 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int vpar = this.variableParams.get(0);
        ParamValueSet par = paramSet.get(vpar);
        int points = par.getPoints();
        int n = this.paramSet.size();
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }


        
        for (String var : vars) {
            Plot2DTrajectory traj = new Plot2DTrajectory(points);
            traj.name = var + (this.numberOfTrajectories > 1 ? "_" + getFixedParamString() : "");;
            int id = -1;
            for (int i = 0; i < points; i++) {
                par.setCurrentPoint(i);
                key.defineFrom(paramSet);
                TrajectoryStatistics stat = data.get(key);
                traj.x[i] = par.getCurrentValue();
                if (id < 0) {
                    id = stat.getVarIndex(var);
                }
                traj.y[i] = stat.getStat(type, id);
            }
            list.add(traj);
        }
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }


    /**
     * Returns a list of  1D trajectory where the final state statistics of the given var is plotted against
     * the different values of a parameter
     * @param vars a list of variable's names
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 1dTrajectory.
     */
    ArrayList<Plot2DTrajectory> getNextFinalState2DTrajectory(ArrayList<String> vars,
            HashMap<ParamSetKey, Trajectory> data) {
        if (this.numberOfVariableParams != 1) {
            throw new DataException("There are " + numberOfVariableParams + " variable parameters "
                    + "in the data set. 1 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int vpar = this.variableParams.get(0);
        ParamValueSet par = paramSet.get(vpar);
        int points = par.getPoints();
        int n = this.paramSet.size();
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }



        for (String var : vars) {
            Plot2DTrajectory traj = new Plot2DTrajectory(points);
            traj.name = var + (this.numberOfTrajectories > 1 ? "_" + getFixedParamString() : "");
            int id = -1;
            for (int i = 0; i < points; i++) {
                par.setCurrentPoint(i);
                key.defineFrom(paramSet);
                Trajectory tr = data.get(key);
                traj.x[i] = par.getCurrentValue();
                if (id < 0) {
                    id = tr.getVarId(var);
                }
                traj.y[i] = tr.finalState[id];
            }
            list.add(traj);
        }
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }

  
    /**
     * Returns a phase plane trajectory (var1 vs var2), for the given statistics and the
     * next configuration of parameters.
     * @param var1
     * @param var2
     * @param type
     * @param data
     * @return 
     */
    ArrayList<Plot2DTrajectory> getNextPhasePlaneTrajectory(String var1, String var2, StatType type,
            HashMap<ParamSetKey, TrajectoryStatistics> data) {
        if (this.numberOfVariableParams != 0) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set, 0 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int n = this.paramSet.size();
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }
        key.defineFrom(paramSet);
        TrajectoryStatistics stat = data.get(key);
        Plot2DTrajectory traj = new Plot2DTrajectory(stat.points);
        traj.name = getFixedParamString();
        for (int i = 0; i < stat.points; i++) {
            int id1 = stat.getVarIndex(var1);
            traj.x[i] = stat.getStat(type, id1, i);
            int id2 = stat.getVarIndex(var2);
            traj.y[i] = stat.getStat(type, id2, i);
        }
        list.add(traj);
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }


    /**
     * Returns a phase plane trajectory (var1 vs var2), for the given statistics and the
     * next configuration of parameters.
     * @param var1
     * @param var2
     * @param type
     * @param data
     * @return
     */
    ArrayList<Plot2DTrajectory> getNextPhasePlaneTrajectory(String var1, String var2,
            HashMap<ParamSetKey, Trajectory> data) {
        if (this.numberOfVariableParams != 0) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set, 0 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int n = this.paramSet.size();
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }
        key.defineFrom(paramSet);
        Trajectory tr = data.get(key);
        Plot2DTrajectory traj = new Plot2DTrajectory(tr.savedPoints);
        traj.name = getFixedParamString();
        int id1 = tr.getVarId(var1);
        int id2 = tr.getVarId(var2);
        for (int i = 0; i < tr.savedPoints; i++) {
            traj.x[i] = tr.data[id1][i];
            traj.y[i] = tr.data[id2][i];
        }
        list.add(traj);
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }

    /**
     * Returns a phase space trajectory (var1,var2, var3), for the
     * next configuration of parameters.
     * @param var1
     * @param var2
      * @param var3
     * @param type
     * @param data
     * @return
     */
    ArrayList<Plot3DTrajectory> getNextPhaseSpaceTrajectory(String var1, String var2, StatType type,
            String var3, HashMap<ParamSetKey, TrajectoryStatistics> data) {
        if (this.numberOfVariableParams != 0) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set, 0 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int n = this.paramSet.size();
        ArrayList<Plot3DTrajectory> list = new ArrayList<Plot3DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }
        key.defineFrom(paramSet);
        TrajectoryStatistics stat = data.get(key);
        Plot3DTrajectory traj = new Plot3DTrajectory(stat.points);
        traj.name = getFixedParamString();
        for (int i = 0; i < stat.points; i++) {
            int id1 = stat.getVarIndex(var1);
            traj.x[i] = stat.getStat(type, id1, i);
            int id2 = stat.getVarIndex(var2);
            traj.y[i] = stat.getStat(type, id2, i);
            int id3 = stat.getVarIndex(var3);
            traj.z[i] = stat.getStat(type, id3, i);
        }
        list.add(traj);
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }

     /**
     * Returns a phase space trajectory (var1,var2, var3), for the 
     * next configuration of parameters.
     * @param var1
     * @param var2
      * @param var3
     * @param type
     * @param data
     * @return
     */
    ArrayList<Plot3DTrajectory> getNextPhaseSpaceTrajectory(String var1, String var2,
            String var3, HashMap<ParamSetKey, Trajectory> data) {
        if (this.numberOfVariableParams != 0) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set, 0 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int n = this.paramSet.size();
        ArrayList<Plot3DTrajectory> list = new ArrayList<Plot3DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }
        key.defineFrom(paramSet);
        Trajectory tr = data.get(key);
        Plot3DTrajectory traj = new Plot3DTrajectory(tr.savedPoints);
        traj.name = getFixedParamString();
        for (int i = 0; i < tr.savedPoints; i++) {
            int id1 = tr.getVarId(var1);
            traj.x[i] = tr.data[id1][i];
            int id2 = tr.getVarId(var2);
            traj.y[i] = tr.data[id2][i];
            int id3 = tr.getVarId(var3);
            traj.z[i] = tr.data[id3][i];
        }
        list.add(traj);
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }

    /**
     * Returns a list of 1D trajectory where the state statistics at the given time  of the given var is plotted against
     * the different values of a parameter
     * @param vars a list of variable's names
     * @param time the time point at which collecting the statistics
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 1dTrajectory.
     */
    ArrayList<Plot2DTrajectory> getNextState2DTrajectory(ArrayList<String> vars, double time, StatType type,
            HashMap<ParamSetKey, TrajectoryStatistics> data) {
        if (this.numberOfVariableParams != 1) {
            throw new DataException("There are " + numberOfVariableParams + " variable parameters "
                    + "in the data set. 1 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int vpar = this.variableParams.get(0);
        ParamValueSet par = paramSet.get(vpar);
        int points = par.getPoints();
        int n = this.paramSet.size();
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        for (String var : vars) {
            Plot2DTrajectory traj = new Plot2DTrajectory(points);
            traj.name = var + (this.numberOfTrajectories > 1 ? "_" + getFixedParamString() : "");
            int id  = -1;
            for (int i = 0; i < points; i++) {
                par.setCurrentPoint(i);
                key.defineFrom(paramSet);
                TrajectoryStatistics stat = data.get(key);
                int index = stat.getIndex(time);
                traj.x[i] = par.getCurrentValue();
                if (id < 0)
                    id = stat.getVarIndex(var);
                traj.y[i] = stat.getStat(type, id, index);
            }
            list.add(traj);
        }
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }


    /**
     * Returns a list of 1D trajectory where the state statistics at the given time  of the given var is plotted against
     * the different values of a parameter
     * @param vars a list of variable's names
     * @param time the time point at which collecting the statistics
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 1dTrajectory.
     */
    ArrayList<Plot2DTrajectory> getNextState2DTrajectory(ArrayList<String> vars, double time, 
            HashMap<ParamSetKey, Trajectory> data) {
        if (this.numberOfVariableParams != 1) {
            throw new DataException("There are " + numberOfVariableParams + " variable parameters "
                    + "in the data set. 1 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int vpar = this.variableParams.get(0);
        ParamValueSet par = paramSet.get(vpar);
        int points = par.getPoints();
        int n = this.paramSet.size();
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        for (String var : vars) {
            Plot2DTrajectory traj = new Plot2DTrajectory(points);
            traj.name = var + (this.numberOfTrajectories > 1 ? "_" + getFixedParamString() : "");
            int id  = -1;
            for (int i = 0; i < points; i++) {
                par.setCurrentPoint(i);
                key.defineFrom(paramSet);
                Trajectory tr = data.get(key);
                int index = tr.getIndex(time);
                traj.x[i] = par.getCurrentValue();
                if (id < 0)
                    id = tr.getVarId(var);
                traj.y[i] = tr.data[id][index];
            }
            list.add(traj);
        }
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }

    /**
     * Returns a list of  1D trajectory plotting the statistics versus time for a given configuration of fixed parameters
     * requires no variable parameter.
     * @param vars a list of variable's names
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 1dTrajectory.
     */
    ArrayList<Plot2DTrajectory> getNextTimed2DTrajectory(ArrayList<String> vars, StatType type,
            HashMap<ParamSetKey, TrajectoryStatistics> data) {
        if (this.numberOfVariableParams != 0) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set, 0 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int n = this.paramSet.size();
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }
        key.defineFrom(paramSet);
        TrajectoryStatistics stat = data.get(key);


        for (String var : vars) {
            Plot2DTrajectory traj = new Plot2DTrajectory(stat.points);
            traj.name = var + "_" + getFixedParamString();
            for (int i = 0; i < stat.points; i++) {
                traj.x[i] = stat.average[0][i];
                int id = stat.getVarIndex(var);
                traj.y[i] = stat.getStat(type, id, i);
            }
            list.add(traj);
        }
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }

      /**
     * Returns a list of  1D trajectory plotting the statistics versus time for a given configuration of fixed parameters
     * requires no variable parameter.
     * @param vars a list of variable's names
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 1dTrajectory.
     */
    ArrayList<Plot2DTrajectory> getNextTimed2DTrajectory(ArrayList<String> vars,
            HashMap<ParamSetKey, Trajectory> data) {
        if (this.numberOfVariableParams != 0) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set, 0 required");
        }
        if (this.generatedTrajectories == this.numberOfTrajectories) {
            return null;
        }
        int n = this.paramSet.size();
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
//                System.out.println("Setting parameter " + paramSet.get(i).getName() + " to admissible point "  + this.currentParams[i] +
//                        " with value " + this.fixedParamCodes[i][this.currentParams[i]]);
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }
        key.defineFrom(paramSet);
        Trajectory tr = data.get(key);


        for (String var : vars) {
            Plot2DTrajectory traj = new Plot2DTrajectory(tr.savedPoints);
            traj.name = var + "_" + getFixedParamString();
            for (int i = 0; i < tr.savedPoints; i++) {
                traj.x[i] = tr.data[0][i];
                int id = tr.getVarId(var);
                traj.y[i] = tr.data[id][i];
            }
            list.add(traj);
        }
        this.generatedTrajectories++;
        this.nextFixedParamCombination();
        return list;
    }


    /**
     * Returns the 2d trajectory where of the final state statistics of the given var against
     * the different values of two variable parameter
     * @param var a variable's name
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 2dTrajectory.
     */
    Plot2DSurface getFinalState2DSurface(String var, StatType type,
            HashMap<ParamSetKey, TrajectoryStatistics> data) {
        if (this.numberOfVariableParams != 2) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set; 2 required");
        }
        if (this.numberOfTrajectories != 1) {
            throw new DataException("There are " + numberOfTrajectories + " combinations of fixed parameters. 1 required");
        }
        int vparX = this.variableParams.get(0);
        int vparY = this.variableParams.get(1);
        ParamValueSet parX = paramSet.get(vparX);
        ParamValueSet parY = paramSet.get(vparY);
        int pointsX = parX.getPoints();
        int pointsY = parY.getPoints();
        int n = this.paramSet.size();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        Plot2DSurface traj = new Plot2DSurface(pointsX, pointsY);
        traj.name = var;
        for (int i = 0; i < pointsX; i++) {
            parX.setCurrentPoint(i);
            traj.x[i] = parX.getCurrentValue();
            for (int j = 0; j < pointsY; j++) {
                parY.setCurrentPoint(j);
                key.defineFrom(paramSet);
                TrajectoryStatistics stat = data.get(key);
                if (i == 0) {
                    traj.y[j] = parY.getCurrentValue();
                }
                int id = stat.getVarIndex(var);
                traj.z[i][j] = stat.getStat(type, id);
            }
        }
        return traj;
    }

     /**
     * Returns the 2d trajectory where of the final state statistics of the given var against
     * the different values of two variable parameter
     * @param var a variable's name
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 2dTrajectory.
     */
    Plot2DSurface getFinalState2DSurface(String var,
            HashMap<ParamSetKey, Trajectory> data) {
        if (this.numberOfVariableParams != 2) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set; 2 required");
        }
        if (this.numberOfTrajectories != 1) {
            throw new DataException("There are " + numberOfTrajectories + " combinations of fixed parameters. 1 required");
        }
        int vparX = this.variableParams.get(0);
        int vparY = this.variableParams.get(1);
        ParamValueSet parX = paramSet.get(vparX);
        ParamValueSet parY = paramSet.get(vparY);
        int pointsX = parX.getPoints();
        int pointsY = parY.getPoints();
        int n = this.paramSet.size();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        Plot2DSurface traj = new Plot2DSurface(pointsX, pointsY);
        traj.name = var;
        for (int i = 0; i < pointsX; i++) {
            parX.setCurrentPoint(i);
            traj.x[i] = parX.getCurrentValue();
            for (int j = 0; j < pointsY; j++) {
                parY.setCurrentPoint(j);
                key.defineFrom(paramSet);
                Trajectory tr = data.get(key);
                if (i == 0) {
                    traj.y[j] = parY.getCurrentValue();
                }
                int id = tr.getVarId(var);
                traj.z[i][j] = tr.finalState[id];
            }
        }
        return traj;
    }

        /**
     * Returns the 2d trajectory where of a state statistics of the given var against
     * the different values of two variable parameter
     * @param var a variable's name
     * @param time the time of the state
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 2dTrajectory.
     */
    Plot2DSurface getState2DSurface(String var, double time, StatType type,
            HashMap<ParamSetKey, TrajectoryStatistics> data) {
        if (this.numberOfVariableParams != 2) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set; 2 required");
        }
        if (this.numberOfTrajectories != 1) {
            throw new DataException("There are " + numberOfTrajectories + " combinations of fixed parameters. 1 required");
        }
        int vparX = this.variableParams.get(0);
        int vparY = this.variableParams.get(1);
        ParamValueSet parX = paramSet.get(vparX);
        ParamValueSet parY = paramSet.get(vparY);
        int pointsX = parX.getPoints();
        int pointsY = parY.getPoints();
        int n = this.paramSet.size();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        Plot2DSurface traj = new Plot2DSurface(pointsX, pointsY);
        traj.name = var;
        for (int i = 0; i < pointsX; i++) {
            parX.setCurrentPoint(i);
            traj.x[i] = parX.getCurrentValue();
            for (int j = 0; j < pointsY; j++) {
                parY.setCurrentPoint(j);
                key.defineFrom(paramSet);
                TrajectoryStatistics stat = data.get(key);
                if (i == 0) {
                    traj.y[j] = parY.getCurrentValue();
                }
                int id = stat.getVarIndex(var);
                int index = stat.getIndex(time);
                traj.z[i][j] = stat.getStat(type, id, index);
            }
        }
        return traj;
    }

     /**
     * Returns the 2d trajectory where of a state statistics of the given var against
     * the different values of two variable parameter
     * @param var a variable's name
     * @param time the time of the state
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 2dTrajectory.
     */
     Plot2DSurface getState2DSurface(String var, double time, 
            HashMap<ParamSetKey, Trajectory> data) {
        if (this.numberOfVariableParams != 2) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set; 2 required");
        }
        if (this.numberOfTrajectories != 1) {
            throw new DataException("There are " + numberOfTrajectories + " combinations of fixed parameters. 1 required");
        }
        int vparX = this.variableParams.get(0);
        int vparY = this.variableParams.get(1);
        ParamValueSet parX = paramSet.get(vparX);
        ParamValueSet parY = paramSet.get(vparY);
        int pointsX = parX.getPoints();
        int pointsY = parY.getPoints();
        int n = this.paramSet.size();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        Plot2DSurface traj = new Plot2DSurface(pointsX, pointsY);
        traj.name = var;
        for (int i = 0; i < pointsX; i++) {
            parX.setCurrentPoint(i);
            traj.x[i] = parX.getCurrentValue();
            for (int j = 0; j < pointsY; j++) {
                parY.setCurrentPoint(j);
                key.defineFrom(paramSet);
                Trajectory tr = data.get(key);
                if (i == 0) {
                    traj.y[j] = parY.getCurrentValue();
                }
                int id = tr.getVarId(var);
                int index = tr.getIndex(time);
                traj.z[i][j] = tr.data[id][index];
            }
        }
        return traj;
    }

    /**
     * Returns the 3d trajectory where of the final state statistics of the given var against
     * the different values of three variable parameter
     * @param var a variable's name
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 3dTrajectory.
     */
    Plot3DSurface getFinalState3DSurface(String var, StatType type,
            HashMap<ParamSetKey, TrajectoryStatistics> data) {
        if (this.numberOfVariableParams != 3) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set; 3 required");
        }
        if (this.numberOfTrajectories != 1) {
            throw new DataException("There are " + numberOfTrajectories + " combinations "
                    + "of fixed parameters. 1 required");
        }
        int vparX = this.variableParams.get(0);
        int vparY = this.variableParams.get(1);
        int vparZ = this.variableParams.get(2);
        ParamValueSet parX = paramSet.get(vparX);
        ParamValueSet parY = paramSet.get(vparY);
        ParamValueSet parZ = paramSet.get(vparZ);
        int pointsX = parX.getPoints();
        int pointsY = parY.getPoints();
        int pointsZ = parZ.getPoints();
        int n = this.paramSet.size();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        Plot3DSurface traj = new Plot3DSurface(pointsX, pointsY, pointsZ);
        traj.name = var;
        for (int i = 0; i < pointsX; i++) {
            parX.setCurrentPoint(i);
            traj.x[i] = parX.getCurrentValue();
            for (int j = 0; j < pointsY; j++) {
                parY.setCurrentPoint(j);
                if (i == 0) {
                    traj.y[j] = parY.getCurrentValue();
                }
                for (int k=0;k<pointsZ;k++) {
                    parZ.setCurrentPoint(k);
                    if (i == 0 && j == 0) {
                        traj.z[k] = parZ.getCurrentValue();
                    }
                    key.defineFrom(paramSet);
                    TrajectoryStatistics stat = data.get(key);
                    int id = stat.getVarIndex(var);
                    traj.v[i][j][k] = stat.getStat(type, id);
                }               
            }
        }
        return traj;

    }


     /**
     * Returns the 3d trajectory where of the final state statistics of the given var against
     * the different values of three variable parameter
     * @param var a variable's name
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 3dTrajectory.
     */
    Plot3DSurface getFinalState3DSurface(String var,
            HashMap<ParamSetKey, Trajectory> data) {
        if (this.numberOfVariableParams != 3) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set; 3 required");
        }
        if (this.numberOfTrajectories != 1) {
            throw new DataException("There are " + numberOfTrajectories + " combinations "
                    + "of fixed parameters. 1 required");
        }
        int vparX = this.variableParams.get(0);
        int vparY = this.variableParams.get(1);
        int vparZ = this.variableParams.get(2);
        ParamValueSet parX = paramSet.get(vparX);
        ParamValueSet parY = paramSet.get(vparY);
        ParamValueSet parZ = paramSet.get(vparZ);
        int pointsX = parX.getPoints();
        int pointsY = parY.getPoints();
        int pointsZ = parZ.getPoints();
        int n = this.paramSet.size();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        Plot3DSurface traj = new Plot3DSurface(pointsX, pointsY, pointsZ);
        traj.name = var;
        for (int i = 0; i < pointsX; i++) {
            parX.setCurrentPoint(i);
            traj.x[i] = parX.getCurrentValue();
            for (int j = 0; j < pointsY; j++) {
                parY.setCurrentPoint(j);
                if (i == 0) {
                    traj.y[j] = parY.getCurrentValue();
                }
                for (int k=0;k<pointsZ;k++) {
                    parZ.setCurrentPoint(k);
                    if (i == 0 && j == 0) {
                        traj.z[k] = parZ.getCurrentValue();
                    }
                    key.defineFrom(paramSet);
                    Trajectory tr = data.get(key);
                    int id = tr.getVarId(var);
                    traj.v[i][j][k] = tr.finalState[id];
                }
            }
        }
        return traj;

    }

    /**
     * Returns the 3d trajectory where of a state statistics of the given var against
     * the different values of three variable parameter
     * @param var a variable's name
     * @param time the time of the state
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 3dTrajectory.
     */
    Plot3DSurface getState3DSurface(String var, double time, StatType type,
            HashMap<ParamSetKey, TrajectoryStatistics> data) {
        if (this.numberOfVariableParams != 3) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set; 3 required");
        }
        if (this.numberOfTrajectories != 1) {
            throw new DataException("There are " + numberOfTrajectories + " combinations "
                    + "of fixed parameters. 1 required");
        }
        int vparX = this.variableParams.get(0);
        int vparY = this.variableParams.get(1);
        int vparZ = this.variableParams.get(2);
        ParamValueSet parX = paramSet.get(vparX);
        ParamValueSet parY = paramSet.get(vparY);
        ParamValueSet parZ = paramSet.get(vparZ);
        int pointsX = parX.getPoints();
        int pointsY = parY.getPoints();
        int pointsZ = parZ.getPoints();
        int n = this.paramSet.size();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        Plot3DSurface traj = new Plot3DSurface(pointsX, pointsY, pointsZ);
        traj.name = var;
        for (int i = 0; i < pointsX; i++) {
            parX.setCurrentPoint(i);
            traj.x[i] = parX.getCurrentValue();
            for (int j = 0; j < pointsY; j++) {
                parY.setCurrentPoint(j);
                if (i == 0) {
                    traj.y[j] = parY.getCurrentValue();
                }
                for (int k=0;k<pointsZ;k++) {
                    parZ.setCurrentPoint(k);
                    if (i == 0 && j == 0) {
                        traj.z[k] = parZ.getCurrentValue();
                    }
                    key.defineFrom(paramSet);
                    TrajectoryStatistics stat = data.get(key);
                    int id = stat.getVarIndex(var);
                    int index = stat.getIndex(time);
                    traj.v[i][j][k] = stat.getStat(type, id, index);
                }
            }
        }
        return traj;

    }

     /**
     * Returns the 3d trajectory where of a state statistics of the given var against
     * the different values of three variable parameter
     * @param var a variable's name
     * @param time the time of the state
     * @param type the statistics to plot
     * @param data the data collected by the statistical explorator.
     * @return a 3dTrajectory.
     */
    Plot3DSurface getState3DSurface(String var, double time, 
            HashMap<ParamSetKey, Trajectory> data) {
        if (this.numberOfVariableParams != 3) {
            throw new DataException("There are " + numberOfVariableParams + " "
                    + "variable parameters in the data set; 3 required");
        }
        if (this.numberOfTrajectories != 1) {
            throw new DataException("There are " + numberOfTrajectories + " combinations "
                    + "of fixed parameters. 1 required");
        }
        int vparX = this.variableParams.get(0);
        int vparY = this.variableParams.get(1);
        int vparZ = this.variableParams.get(2);
        ParamValueSet parX = paramSet.get(vparX);
        ParamValueSet parY = paramSet.get(vparY);
        ParamValueSet parZ = paramSet.get(vparZ);
        int pointsX = parX.getPoints();
        int pointsY = parY.getPoints();
        int pointsZ = parZ.getPoints();
        int n = this.paramSet.size();
        //fix the value of fixed parameters
        for (int i = 0; i < n; i++) {
            if (this.fixedParam[i]) {
                this.paramSet.get(i).setCurrentPoint(this.fixedParamCodes[i][this.currentParams[i]]);
            }
        }

        Plot3DSurface traj = new Plot3DSurface(pointsX, pointsY, pointsZ);
        traj.name = var;
        for (int i = 0; i < pointsX; i++) {
            parX.setCurrentPoint(i);
            traj.x[i] = parX.getCurrentValue();
            for (int j = 0; j < pointsY; j++) {
                parY.setCurrentPoint(j);
                if (i == 0) {
                    traj.y[j] = parY.getCurrentValue();
                }
                for (int k=0;k<pointsZ;k++) {
                    parZ.setCurrentPoint(k);
                    if (i == 0 && j == 0) {
                        traj.z[k] = parZ.getCurrentValue();
                    }
                    key.defineFrom(paramSet);
                    Trajectory tr = data.get(key);
                    int id = tr.getVarId(var);
                    int index = tr.getIndex(time);
                    traj.v[i][j][k] = tr.data[id][index]; 
                }
            }
        }
        return traj;

    }
}
