/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.util.Locale;
import com.eggloop.flow.simhya.simhya.model.store.Store;

import com.eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DTrajectory;



/**
 * This class store the traces of a single simulation of all variables of a model
 * or of a subset of them
 * @author Luca
 */
public class Trajectory {

    double[][] data;
    double[] finalState;
    long totalSteps;
    String[] names;
    double[] parameters;
    ArrayList<String> paramNames;

    private boolean saveAllVars;
    int[] varsToSave;
    int[] expVarsToSave;
    int numberOfVarsToSave;
    int numberOfExpVarsToSave;
    int dataDimension;
    int savedPoints = 0;

    private int[] varsToPrint;
    private int[] paramsToPrint;
    private boolean printHeader;
    private boolean printId;
    private boolean printFinalState;
    int id;

    private boolean finalized;
    private static final int ARRAY_SIZE = 1024;
    
    ArrayList<EventInfo> eventList;
    
    
    Trajectory(Store store) {
        id = 0;
        printHeader = true;
        printId = false;
        this.paramNames = store.getNameOfAllParameters();
        this.parameters = store.getCopyOfParametersValues();
        this.varsToSave = null;
        this.expVarsToSave = null;
        this.varsToPrint = null;
        this.paramsToPrint = null;
        this.saveAllVars = false;
        this.finalized = true;
    }

    public Trajectory(int id, int points, Store store) {
        this.id = id;
        finalized = false;
        printHeader = true;
        printId = false;
        ArrayList<String> n;
        this.saveAllVars = true;
        this.varsToSave = null;
        this.expVarsToSave = null;
        this.varsToPrint = null;
        this.paramsToPrint = null;
        this.numberOfVarsToSave = store.getNumberOfVariables();
        this.numberOfExpVarsToSave = store.getNumberOfExpressionVariables();
        this.paramNames = store.getNameOfAllParameters();
        this.parameters = store.getCopyOfParametersValues();
        this.dataDimension = 1 + numberOfVarsToSave + numberOfExpVarsToSave;
        data = new double[dataDimension][];
        finalState = new double[dataDimension];
        names = new String[dataDimension];
        data[0] = new double[points];
        names[0] = "time";
        int i = 1;
        n = store.getNameOfAllVariables();
        for (int j=0;j<numberOfVarsToSave;j++) {
            data[i] = new double[points];
            names[i] = n.get(j);
            i++; 
        }
        n = store.getNameOfAllExpressionVariables();
        for (int j=0;j<numberOfExpVarsToSave;j++) {
            data[i] = new double[points];
            names[i] = n.get(j);
            i++; 
        }
    }

    public Trajectory(int id, int points, Store store, ArrayList<String> varsToSave, ArrayList<String> expVarsToSave) {
        this.id = id;
        finalized = false;
        printHeader = true;
        printId = false;
        this.saveAllVars = false;
        this.varsToPrint = null;
        this.paramsToPrint = null;
        this.numberOfVarsToSave = varsToSave.size();
        this.varsToSave = new int[numberOfVarsToSave];
        for (int i=0;i<numberOfVarsToSave;i++)
            this.varsToSave[i] = store.getVariableID(varsToSave.get(i));
        this.numberOfExpVarsToSave = expVarsToSave.size();
        this.expVarsToSave = new int[numberOfExpVarsToSave];
        for (int i=0;i<numberOfExpVarsToSave;i++)
            this.expVarsToSave[i] = store.getExpressionVariableID(expVarsToSave.get(i));
        this.paramNames = store.getNameOfAllParameters();
        this.parameters = store.getCopyOfParametersValues();
        this.dataDimension = 1 + numberOfVarsToSave + numberOfExpVarsToSave;
        data = new double[dataDimension][];
        finalState = new double[dataDimension];
        names = new String[dataDimension];
        data[0] = new double[points];
        names[0] = "time";
        int i = 1;
        for (int j=0;j<numberOfVarsToSave;j++) {
            data[i] = new double[points];
            names[i] = varsToSave.get(j);
            i++;
        }
        for (int j=0;j<numberOfExpVarsToSave;j++) {
            data[i] = new double[points];
            names[i] = expVarsToSave.get(j);
            i++;
        }
    }

    public Trajectory(int id, Store store) {
        this(id,ARRAY_SIZE,store);
    }

    public Trajectory(int id, Store store, ArrayList<String> varsToSave, ArrayList<String> expVarsToSave) {
        this(id,ARRAY_SIZE,store,varsToSave,expVarsToSave);
    }
    
    
    
    public double getData(int i, int j) {
        return data[i][j];
    }
    
    public double[][] getAllData() {
        return this.data;
    }
    
    public double getFinalStateData(int i) {
        return this.finalState[i];
    }
    
    
    public long getTotalSteps() {
        return this.totalSteps;
    }

    public int getPoints() {
        return this.data[0].length;
        //return this.savedPoints;
    }
    
    public String[] getNames() {
        return this.names;
    }
    
    
    
    public boolean isFinalized() {
        return this.finalized;
    }

    /**
     * Adds a  new point to the trajectory, at given time
     * @param time
     * @param vars the value of standard variables
     * @param expVars the value of expression variables
     */
    void add(double time,double[] vars, double[] expVars) {
        if (savedPoints == data[0].length) {
            for (int i=0;i<this.dataDimension;i++)
                data[i] = Arrays.copyOf(data[i], 2*savedPoints);
        }
        if (this.saveAllVars) {
            int i = 0;
            data[i++][savedPoints] = time;
            for (int j=0;j<numberOfVarsToSave;j++)
               data[i++][savedPoints] = vars[j];
            for (int j=0;j<numberOfExpVarsToSave;j++)
               data[i++][savedPoints] = expVars[j];
        }
        else {
            int i = 0;
            data[i++][savedPoints] = time;
            for (int j=0;j<numberOfVarsToSave;j++)
               data[i++][savedPoints] = vars[varsToSave[j]];
            for (int j=0;j<numberOfExpVarsToSave;j++)
                data[i++][savedPoints] = expVars[expVarsToSave[j]];
        }
        savedPoints++;
    }

    public void addFinalState(double time, double[] vars, double[] expVars) {
        if (this.saveAllVars) {
            int i = 0;
            finalState[i++] = time;
            for (int j=0;j<numberOfVarsToSave;j++)
               finalState[i++] = vars[j];
            for (int j=0;j<numberOfExpVarsToSave;j++)
               finalState[i++] = expVars[j];
        }
        else {
            int i = 0;
            finalState[i++] = time;
            for (int j=0;j<numberOfVarsToSave;j++)
               finalState[i++] = vars[varsToSave[j]];
            for (int j=0;j<numberOfExpVarsToSave;j++)
                finalState[i++] = expVars[expVarsToSave[j]];
        }
        finalized = true;
    }


    /**
     *
     * @param vars a list of variables, can be null, in which case the returned array will contain
     * numberd from 1 to k, where k  is the number of saved vars minus one. Time is never included, and must be
     * dealt separately
     * @param includeTime if true and vars is null, it includes into var codes the code of time
     * @return an array with the variable codes of the given variables. Raises an
     * exception if a variable is not stores
     */
    int[] getVarCodes( ArrayList<String> vars, boolean includeTime) {
        int n;
        if (vars == null) {
            if (!includeTime) n = names.length - 1;
            else n = names.length;
        }
        else n = vars.size();
        int [] codes = new int[n];
        for (int i=0;i<n;i++) {
            if (vars!= null)
                codes[i] = getVarId(vars.get(i));
            else if (!includeTime)
                codes[i] = i+1;
            else
                codes[i] = i;
        }
        return codes;
    }

    public void saveAsCSV(String filename, boolean append, boolean printId,boolean printFinalState) {
        if (!finalized)
            throw new DataException("Trajectory is still collecting data. No final state is present");
        if (append)
                this.printHeader = false;
        this.printId = printId;
        this.printFinalState = printFinalState;
        try {          
            PrintStream p = new PrintStream(new FileOutputStream(filename,append));
            p.print(this.toString());
            p.close();
        }
        catch (IOException e) {
            System.err.println("Error: cannot save to file " + filename + ";\n" + e);
        }
        finally {
             this.printHeader = true;
             this.printId = false;
             this.printFinalState = false;
        } 
    }
   
    public void setParametersToPrint(ArrayList<String> p) {
        this.paramsToPrint = new int[p.size()];
        int j=0;
        for (String n : p) {
            int i = this.paramNames.indexOf(n);
            if (i >= 0)
                paramsToPrint[j++] = i;
            else {
                paramsToPrint = null;
                throw new DataException("Parameter " + n + " does not exist.");
            }
        }
    }

    public void setVariablesToPrint(int[] v) {
        this.varsToPrint = v;
//        this.varsToPrint = new int[v.size() + 1];
//        varsToPrint[0] = 0;
//        int j=1;
//        for (String n : v) {
//            int i = getNameIndex(n);
//            if (i >= 0)
//                varsToPrint[j++] = i;
//            else {
//                varsToPrint = null;
//                throw new DataException("Variable " + n + " does not exist or has not been saved.");
//            }
//        }
    }
    
    public void printAllVariables() {
        this.varsToPrint = null;
    }
    
    private int getNameIndex(String n) {
        for (int i=1;i<this.dataDimension;i++)
            if (names[i].equals(n))
                return i;
        return -1;
    }
 
    public void debugToScreen() {
         System.out.println("Data points: " + this.savedPoints);
         for (int j=0;j<dataDimension;j++) {
            System.out.print(names[j] + " :: ");
            System.out.println(Arrays.toString(data[j]));
         }
         System.out.println("Final state: ");
         System.out.println(Arrays.toString(names));
         System.out.println(Arrays.toString(finalState));
    }




    @Override
    public String toString() {
        String s = "";
        if (this.printHeader) {
            if (varsToPrint == null)
                for (int j=0;j<dataDimension;j++)
                    s += (j>0?"\t\t":"") + String.format(Locale.US, "%s", names[j]);
                else
                    for (int j=0;j<varsToPrint.length;j++)
                        s += (j>0?"\t\t":"") + String.format(Locale.US, "%s", names[varsToPrint[j]]);
            if (paramsToPrint != null)
                for (int j=0;j<paramsToPrint.length;j++)
                    s += "\t\t" + String.format(Locale.US, "%s", paramNames.get(paramsToPrint[j]));
            if (this.printId)
                s += "\t\t" + String.format(Locale.US, "%s", "simulation_number");
            s += "\n";
        }
        for (int i=0;i<savedPoints;i++) {
            if (varsToPrint == null)
                for (int j=0;j<dataDimension;j++)
                    s += (j>0?"\t\t":"") + String.format(Locale.US, "%.6f", data[j][i]);
                else
                    for (int j=0;j<varsToPrint.length;j++)
                        s += (j>0?"\t\t":"") + String.format(Locale.US, "%.6f", data[varsToPrint[j]][i]);
            if (paramsToPrint != null)
                for (int j=0;j<paramsToPrint.length;j++)
                    s += "\t\t" + String.format(Locale.US, "%.6f", parameters[paramsToPrint[j]]);
            if (this.printId)
                s += "\t\t" + String.format(Locale.US, "%d", id);
            s += "\n";
        }
        if (this.printFinalState) {
            if (varsToPrint == null)
                for (int j=0;j<dataDimension;j++)
                    s += (j>0?"\t\t":"") + String.format(Locale.US, "%.6f", finalState[j]);
                else
                    for (int j=0;j<varsToPrint.length;j++)
                        s += (j>0?"\t\t":"") + String.format(Locale.US, "%.6f", finalState[varsToPrint[j]]);
            if (paramsToPrint != null)
                for (int j=0;j<paramsToPrint.length;j++)
                    s += "\t\t" + String.format(Locale.US, "%.6f", parameters[paramsToPrint[j]]);
            if (this.printId)
                s += "\t\t" + String.format(Locale.US, "%d", id);
            s += "\n";
        }
        return s;
    }

   
    ArrayList<Plot2DTrajectory> getTrajectoriesToPlot() {
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        if (varsToPrint == null)
            for (int j=1;j<dataDimension;j++) {
                Plot2DTrajectory t = new Plot2DTrajectory(this.savedPoints);
                t.name = names[j];
                t.x = data[0];
                t.y = data[j];
                list.add(t);
            }
        else
            for (int j=1;j<varsToPrint.length;j++) {
                Plot2DTrajectory t = new Plot2DTrajectory(this.savedPoints);
                t.name = names[varsToPrint[j]];
                t.x = data[0];
                t.y = data[varsToPrint[j]];
                
//                for (int k=0;k<this.dataDimension;k++) {
//                    System.out.println("data[" + k+"].length = " + this.data[k].length);
//                }
                
                
                list.add(t);
            }
        return list;
    }
    
    
    int getVarId(String name) {
        for (int i=0;i<names.length;i++)
            if (names[i].equals(name))
                return i;
        throw new DataException("Variable " + name + " does not exist in trajectory");
    }


    /**
     * returns the index of data point corresponding to the time instant
     * closest to the specified time
     * @param time
     * @return
     */
    int getIndex(double time) {
        int k = (int)Math.round((time-this.data[0][0])/(this.data[0][1] - this.data[0][0] ));
        if (k >= this.savedPoints)
            throw new DataException("Trajectories do not reach time " + time);
        return k;
    }


    /**
     * Adds a new time trace to the current trajectory, for variable var
     * If the variable is already defined or if the length of the time trace is not 
     * as that of others, throws an exception
     * @param var
     * @param values 
     */
    public void addTimeTrace(String var, double[] values) {
        if (this.getNameIndex(var) != -1)
            throw new DataException("There is already a variable with name " + var + " in trajectory " + id);
        if (values.length != this.data[0].length)
            throw new DataException("The tiem trajectory for variable " + var + " has wrong length; "
                    + "it is " + values.length  + " but should be " + this.data[0].length);
        
        double [][] newData = new double[this.dataDimension+1][];
        System.arraycopy(this.data, 0, newData, 0, this.dataDimension);
        newData[dataDimension] = Arrays.copyOf(values, data[0].length);
        dataDimension++;
        data = newData;
        this.finalState = Arrays.copyOf(finalState, finalState.length+1);
        this.finalState[finalState.length-1] = values[values.length-1];
        this.names = Arrays.copyOf(names, names.length+1);
        names[names.length-1] = var;
    }
    
    
    /**
     * Adds a new point to the final state of the current trajectory, for variable var
     * If the variable is already defined, throws an exception
     * @param var
     * @param values 
     */
    public void addFinalStateTrace(String var, double value) {
        if (this.getNameIndex(var) != -1)
            throw new DataException("There is already a variable with name " + var + " in trajectory " + id);
        dataDimension++;
        this.finalState = Arrays.copyOf(finalState, finalState.length+1);
        this.finalState[finalState.length-1] = value;
        this.names = Arrays.copyOf(names, names.length+1);
        names[names.length-1] = var;
    }
    
    
    public String eventListToString() {
        String s = "";
        for (EventInfo e : this.eventList)
            s += e.toString(id) + "\n";
        return s;
    }
    
}
