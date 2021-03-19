/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import com.eggloop.flow.simhya.simhya.dataprocessing.chart.*;
//import umontreal.iro.lecuyer.charts.*;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;



/**
 *
 * @author Luca
 */
public class TrajectoryStatistics {
    double[][][] data;
    /**
     * Here we store the final state data for each trajectory
     */
    double[][] finalData;
    /**
     * average of trajectories for each sample point. Makes sense only for stochastic systems
     */
    double[][] average;
    /**
     * covariance of trajectories for each sample point. Makes sense only for stochastic systems
     */
    double[][][] covariance;
    /**
     * skew of trajectories for each sample point. Makes sense only for stochastic systems
     */
    double[][] skew;
    /**
     * kutosis of trajectories for each sample point. Makes sense only for stochastic systems
     */
    double[][] kurtosis;
    
    /**
     * average of trajectories for final state. Makes sense only for stochastic systems
     */
    double[] finalAverage;
    /**
     * covariance of trajectories for final state. Makes sense only for stochastic systems
     */
    double[][] finalCovariance;
    /**
     * skewness of trajectories for final state. Makes sense only for stochastic systems
     */
    double[] finalSkew;
    /**
     * kurtosis of trajectories for final state. Makes sense only for stochastic systems
     */
    double[] finalKurtosis;
    /**
     * Number of steps in each simulation run;
     */
    double[] totalSteps;
    /**
     * average number of steps;
     */
    double averageTotalSteps;
    /**
     * variance of Total steps;
     */
    double varianceTotalSteps;

    /**
     * A vector of length vars with name of variables stored
     */
    String[] names;
    /**
     * The name of the model
     */
    String modelName;

    /**
     * The number of variables stored
     */
    int vars;
    /**
     * The number of time points stored
     */
    int points;
    /**
     * The number of samples for each variable and time point
     */
    int runs;
    /**
     * true if the object has been initialized
     */
    boolean initialized;
    /**
     * true if statistics have been computed
     */
    boolean statisticsComputed;
    /**
     * true if only statistics have been kept, and distribution data thrown away
     */
    boolean rawDataDeleted;
    /**
     * true if final state distribution have been kept
     */
    public boolean keepFinalData;

    /**
     * True if the final time is different among trajectories
     */
    boolean differentFinalTimes;
    /**
     * True if the trajectory has been sampled looking at steps
     */
    boolean stepTrajectories;
    /**
     * True if stored trajectories have uneven length
     */
    boolean unevenLengthTrajectories;
    /**
     * The step distance between two data points
     */
    int stepSize;
    /**
     * the time lag between two data points
     */
    double timeStep;
    /**
     * the initial tie at which data started to be saved
     */
    double initialSavingTime;

    /**
     * a vector of integers with the ids of vars to plot
     */
    int [] varsToPlot;
    /**
     * The name of the variables to plot
     */
    ArrayList<String> varsToPlotNames;
    /**
     * true if tex images are to be saved as full latex documents
     */
    public boolean fullLatexDocument;

    /**
     * true if in computing the histogram of an empirical distribution, the size of bins is
     * computed automatically (check global options, the step is computed from data included between the
     * 0.25 and the 0.75 quantiles.
     */
    public boolean autoBin = true;
    /**
     * number of bins if autoBin is set to false
     */
    public int bins = 20;
    
    
    
    
    


//    TrajectoryStatistics(int vars, int points, int runs) {
//        varsToPlot = null;
//        this.vars = vars;
//        this.points = points;
//        this.runs = runs;
//        finalSteps = new DoubleArrayList();
//        data = new DoubleArrayList[vars][];
//        average = new double[vars][];
//        variance = new double[vars][];
//        finalData =  new DoubleArrayList[vars];
//        finalAverage = new double[vars];
//        finalVariance = new double[vars];
//        finalSkew = new double[vars];
//        finalKurtosis = new double[vars];
//        for (int i=0;i<vars;i++) {
//            data[i] = new DoubleArrayList[points];
//            average[i] = new double[points];
//            variance[i] = new double[points];
//            finalData[i] =  new DoubleArrayList();
//            for (int j=0;j<points;j++)
//                data[i][j] = new DoubleArrayList();
//        }
//        differentFinalTimes = false;
//        statisticsComputed = false;
//        this.rawDataDeleted = false;
//        keepFinalData = true;
//    }
    
    TrajectoryStatistics() {
        varsToPlot = null;
        this.vars = 0;
        this.points = 0;
        this.runs = 0;
//        finalSteps = null;
        data = null;
        average = null;
        skew = null;
        kurtosis = null;
        covariance = null;
        finalData =  null;
        finalAverage = null;
        finalCovariance = null;
        finalSkew = null;
        finalKurtosis = null;
        differentFinalTimes = false;
        statisticsComputed = false;
        rawDataDeleted = false;
        keepFinalData = true;
    }

    
    boolean containsData() {
        return data != null;
    }
    
    boolean containsFinalStateData() {
        return finalData != null;
    }
    
    boolean containsHigherOrderStat() {
        return skew != null;
    }
    
    boolean containsHigherOrderFinalStateStat() {
        return finalSkew != null;
    }
    
    public double[][] getAverage(){
        int n = average.length-1;
        for (int i=0;i<vars;i++)
            average[i][n] = this.finalAverage[i];
        return this.average;
    }
    
    
    public double[][] getFinalData() {
        return this.finalData;
    }
    
    
//    /**
//     * finalizes the object by computing statistics.
//     * Method to be called by the datacollector required to generate the statistics.
//     */
//    void computeStatistics() {
//        if (!initialized)
//            throw new DataException("Data of statistics collector not initialized");
//        if (stepTrajectories) {
//            for (int j=0;j<points;j++) {
//                average[0][j] = Descriptive.mean(data[0][j]);
//                variance[0][j] = Descriptive.sampleVariance(data[0][j],average[0][j]);
//            }
//            finalAverage[0] = Descriptive.mean(finalData[0]);
//            finalVariance[0] = Descriptive.sampleVariance(finalData[0],finalAverage[0]);
//        }
//        else {
//            for (int j=0;j<points;j++) {
//                average[0][j] = data[0][j].get(0);
//                variance[0][j] = 0;
//            }
//            if (differentFinalTimes) {
//                finalAverage[0] = Descriptive.mean(finalData[0]);
//                finalVariance[0] = Descriptive.sampleVariance(finalData[0],finalAverage[0]);
//                finalSkew[0] = Descriptive.sampleSkew(finalData[0], finalAverage[0], finalVariance[0]);
//                finalKurtosis[0] = Descriptive.sampleKurtosis(finalData[0], finalAverage[0], finalVariance[0]);
//            } else {
//                finalAverage[0] = finalData[0].get(0);
//                finalVariance[0] = 0;
//                finalSkew[0] = 0;
//                finalKurtosis[0] = 0;
//            }
//        }
//        for (int i=1;i<vars;i++) {
//            for (int j=0;j<points;j++) {
//                average[i][j] = Descriptive.mean(data[i][j]);
//                variance[i][j] = Descriptive.sampleVariance(data[i][j],average[i][j]);
//            }
//            finalAverage[i] = Descriptive.mean(finalData[i]);
//            finalVariance[i] = Descriptive.sampleVariance(finalData[i],finalAverage[i]);
//            finalSkew[i] = Descriptive.sampleSkew(finalData[i], finalAverage[i], finalVariance[i]);
//            finalKurtosis[i] = Descriptive.sampleKurtosis(finalData[i], finalAverage[i], finalVariance[i]);
//        }
//        statisticsComputed = true;
//    }
//
//    /**
//     * If called, only statistics will be preserved and distribution data
//     * will be erased. Be careful, this method is irreversible. Use it to free memory
//     */
//    public void deleteRawData() {
//        if (!statisticsComputed)
//            throw new DataException("Statistics not yet computed");
//        data = null;
//        if (!this.keepFinalData)
//            this.finalData = null;
//        //calls the garbage collector;
//        System.gc();
//        this.rawDataDeleted = true;
//    }


    public ArrayList<String> getVariableNames() {
        ArrayList<String> n = new ArrayList<String>();
        for (int i=1;i<names.length;i++)
            n.add(names[i]);
        return n;
    }
    

//    public void printFinalStateData() {
//        System.out.println("There are " + this.finalData.length + " variables ");
//        for (int i=0;i<finalData.length;i++)
//            System.out.println("Variable " + this.names[i] + ": " + finalData[i].toString());
//        System.out.println("We have to plot variables " + java.util.Arrays.toString(this.varsToPlot));
//    }

    /**
     * saves as a cvs file the trajectory data for the given stat type. 
     * @param type
     * @param filename
     */
    public void saveAsCSV(StatType type, String filename) {
        if (!initialized)
            throw new DataException("Data of statistics collector not initialized");
        try {
            PrintStream p = new PrintStream(new FileOutputStream(filename,false));
            //p.print(toString(type));
            if (this.containsStatistics(type))
                toStringTime(type,p);
            p.close();
        }
        catch (IOException e) {
            System.err.println("Error: cannot save to file " + filename + ";\n" + e);
        }

    }

    /**
     * Saves as CSV all final state statistics for all variables
     * @param filename
     */
    public void saveFinalStateStatsAsCSV(String filename) {
       if (!initialized)
            throw new DataException("Data of statistics collector not initialized");
        try {
            PrintStream p = new PrintStream(new FileOutputStream(filename,false));
            p.print(this.finalStateStatistics());
            p.close();
        }
        catch (IOException e) {
            System.err.println("Error: cannot save to file " + filename + ";\n" + e);
        }
    }

    /**
     * Save the statistics of all variables at a given time instant
     * @param time
     * @param filename
     */
    public void saveStateStatsAsCSV(double time, String filename) {
       if (!initialized)
            throw new DataException("Data of statistics collector not initialized");
       int k = this.getIndex(time);
        try {
            PrintStream p = new PrintStream(new FileOutputStream(filename,false));
            p.print(this.StateStatistics(k));
            p.close();
        }
        catch (IOException e) {
            System.err.println("Error: cannot save to file " + filename + ";\n" + e);
        }
    }

//    /**
//     * Save the statistics of all variables at a given simulation step.
//     * @param step
//     * @param filename
//     */
//    public void saveStateStatsAsCSV(int step, String filename) {
//       if (!initialized)
//            throw new DataException("Data of statistics collector not initialized");
//       int k = this.getIndex(step);
//        try {
//            PrintStream p = new PrintStream(new FileOutputStream(filename,false));
//            p.print(this.StateStatistics(k));
//            p.close();
//        }
//        catch (IOException e) {
//            System.err.println("Error: cannot save to file " + filename + ";\n" + e);
//        }
//    }
//

    

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
                codes[i] = getVarIndex(vars.get(i));
            else if (!includeTime)
                codes[i] = i+1;
            else
                codes[i] = i;
        }
        return codes;
    }

    /**
     * Stores in the values array the given statistics at final time.
     * @param type the statistics type
     * @param vars an array of var ids.
     * @param values the array with returned values.
     */
    void getFinalTimeStatistics(StatType type, ArrayList<String> vars, double[] values) {
        if (vars != null) {
            for (int i=0;i<vars.size();i++)
                values[i] = this.getStat(type, getVarIndex(vars.get(i)));
        } else {
            for (int i=0;i<this.vars;i++)
                values[i] = this.getStat(type, i);
        }   
    }
    
    
    private boolean containsStatistics(StatType type) {
        if (type == StatType.SKEW || type == StatType.KURTOSIS)
            if (!this.containsHigherOrderStat())
                return false;
        return true;
    }

    private String toString(StatType type) {
        
//        if (stepTrajectories)
//            s = this.toStringStep(type);
//        else 
//        if (this.unevenLengthTrajectories)
//            s = this.toStringTimeUneven(type);
//        else 
        
        String s = "";
        if (this.containsStatistics(type))
            s = this.toStringTime(type);
        return s;
    }
    
    
    
    private String toStringTime(StatType type) {
        String s = "";
        s += "## Statistics collected from " + runs + " trajectories.\n";
        if (this.differentFinalTimes)
             s += "##WARNING## Trajectories terminated at different times.\n";
        for (int j=0;j<names.length;j++)
           s += String.format(Locale.US, "%30s", names[j]);
        s += "\n";
        for (int i=0;i<points;i++) {
            s += String.format(Locale.US, "%30.6f", average[0][i]);
            for (int j=1;j<vars;j++)
                s += String.format(Locale.US, "%30.6f", getStat(type,j,i));
            s += "\n";
        }
        return s;
    }
    
    
     private void toStringTime(StatType type, PrintStream p) {
        p.print("## Statistics collected from " + runs + " trajectories.\n");
        if (this.differentFinalTimes)
             p.print("##WARNING## Trajectories terminated at different times.\n");
        for (int j=0;j<names.length;j++) 
           p.print(String.format(Locale.US, "%30s", names[j]));
        p.print("\n");
        for (int i=0;i<points;i++) {
            p.print(String.format(Locale.US, "%30.6f", average[0][i]));
            for (int j=1;j<vars;j++)
                p.print(String.format(Locale.US, "%30.6f", getStat(type,j,i)));
            p.print("\n");
        }
    }

//    private String toStringTimeUneven(StatType type) {
//        String s = "";
//        s += "## Statistics collected from " + runs + " trajectories.\n";
//        if (this.differentFinalTimes)
//             s += "##WARNING## Trajectories of uneven length and duration.\n";
//        s += String.format(Locale.US, "%30s", "runs");
//        for (int j=0;j<names.length;j++)
//           s += String.format(Locale.US, "%30s", names[j]);
//        s += "\n";
//        for (int i=0;i<points;i++) {
//            s += String.format(Locale.US, "%30d", data[0][i].length);
//            s += String.format(Locale.US, "%30.6f", average[0][i]);
//            for (int j=1;j<vars;j++)
//                s += String.format(Locale.US, "%30.6f", getStat(type,j,i));
//            s += "\n";
//        }
//        s += "\n" + "## Final state statistics:" +"\n";
//        s += finalStateStatistics();
//        return s;
//    }

    private String finalStateStatistics() {
        String s = "";
        s += String.format(Locale.US, "%30s", "Variable");
        s += String.format(Locale.US, "%30s", "Mean");
        s += String.format(Locale.US, "%30s", "Conf. Int. 95% Half Length");
        s += String.format(Locale.US, "%30s", "Std. Dev.");
        s += String.format(Locale.US, "%30s", "Variance");
        s += String.format(Locale.US, "%30s", "Coeff. Var.");
        s += String.format(Locale.US, "%30s", "Fano Factor");
        if (this.containsHigherOrderFinalStateStat()) {
            s += String.format(Locale.US, "%30s", "Skew");
            s += String.format(Locale.US, "%30s", "Kurtosis");
        } else s +=  String.format(Locale.US, "%60s","");
        s += "\n";


        s += String.format(Locale.US, "%30s", "total steps");
        s += String.format(Locale.US, "%30.6f", averageTotalSteps);
        s += String.format(Locale.US, "%30.6f", 1.96*Descriptive.standardError(runs, varianceTotalSteps));
        s += String.format(Locale.US, "%30.6f", Descriptive.standardDeviation(varianceTotalSteps));
        s += String.format(Locale.US, "%30.6f", varianceTotalSteps);
        s += String.format(Locale.US, "%30.6f", Descriptive.standardDeviation(varianceTotalSteps)/averageTotalSteps);
        s += String.format(Locale.US, "%30.6f", varianceTotalSteps/averageTotalSteps);
        if (this.containsFinalStateData()) {
            DoubleArrayList x = new DoubleArrayList(totalSteps);
            s += String.format(Locale.US, "%30.6f", Descriptive.sampleSkew(x, averageTotalSteps, varianceTotalSteps));
            s += String.format(Locale.US, "%30.6f", Descriptive.sampleKurtosis(x, averageTotalSteps, varianceTotalSteps));
        } else s +=  String.format(Locale.US, "%60s","");
        s += "\n";

        
        
        for (int i=0;i<vars;i++) {
            s += String.format(Locale.US, "%30s", names[i]);
            s += String.format(Locale.US, "%30.6f", getStat(StatType.AVERAGE,i));
            s += String.format(Locale.US, "%30.6f", 1.96*getStat(StatType.STDERR,i));
            s += String.format(Locale.US, "%30.6f", getStat(StatType.STDDEV,i));
            s += String.format(Locale.US, "%30.6f", getStat(StatType.VARIANCE,i));
            s += String.format(Locale.US, "%30.6f", getStat(StatType.CV,i));
            s += String.format(Locale.US, "%30.6f", getStat(StatType.FANO,i));
            if (this.containsHigherOrderFinalStateStat()) {
                s += String.format(Locale.US, "%30.6f", getStat(StatType.SKEW,i));
            s += String.format(Locale.US, "%30.6f", getStat(StatType.KURTOSIS,i));
            } else s +=  String.format(Locale.US, "%60s","");
            s += "\n";
        }
        return s;
    }




    /**
     * Returns a string with statistics at a given state k
     * @param k the index of the state
     * @return
     */
    private String StateStatistics(int k) {
        String s = "";
        s += String.format(Locale.US, "%30s", "Variable");
        s += String.format(Locale.US, "%30s", "Mean");
        s += String.format(Locale.US, "%30s", "Conf. Int. 95% Half Length");
        s += String.format(Locale.US, "%30s", "Std. Dev.");
        s += String.format(Locale.US, "%30s", "Variance");
        s += String.format(Locale.US, "%30s", "Coeff. Var.");
        s += String.format(Locale.US, "%30s", "Fano Factor");
        if (this.containsHigherOrderStat()) {
            s += String.format(Locale.US, "%30s", "Skew");
            s += String.format(Locale.US, "%30s", "Kurtosis");
        } 
        s += "\n";

        
        for (int i=0;i<vars;i++) {
            s += String.format(Locale.US, "%30s", names[i]);
            s += String.format(Locale.US, "%30.6f", getStat(StatType.AVERAGE,k,i));
            s += String.format(Locale.US, "%30.6f", 1.96*getStat(StatType.STDERR,k,i));
            s += String.format(Locale.US, "%30.6f", getStat(StatType.STDDEV,k,i));
            s += String.format(Locale.US, "%30.6f", getStat(StatType.VARIANCE,k,i));
            s += String.format(Locale.US, "%30.6f", getStat(StatType.CV,k,i));
            s += String.format(Locale.US, "%30.6f", getStat(StatType.FANO,k,i));
            if (this.containsHigherOrderStat()) {
                s += String.format(Locale.US, "%30.6f", getStat(StatType.SKEW,k,i));
                s += String.format(Locale.US, "%30.6f", getStat(StatType.KURTOSIS,k,i));
            }
            s += "\n";
        }

        return s;
    }

    
//    private String toStringStep(StatType type) {
//        String s = "";
//        s += "## Statistics collected from " + runs + " trajectories, saved by step.\n";
//        s += String.format(Locale.US, "%30s", "step");
//        s += String.format(Locale.US, "%30s", "runs");
//        for (int j=0;j<names.length;j++)
//           s += String.format(Locale.US, "%30s", names[j]);
//        s += "\n";
//        for (int i=0;i<points;i++) {
//            s += String.format(Locale.US, "%30d", i*stepSize);
//            s += String.format(Locale.US, "%30d", data[0][i].size());
//            for (int j=0;j<vars;j++)
//                s += String.format(Locale.US, "%30.6f", getStat(type,j,i));
//            s += "\n";
//        }
//       s += "\n" + "## Final state statistics:" +"\n";
//       s += finalStateStatistics();
//       return s;
//    }

    /**
     * returns the specified statistics for variable i at saved step j
     * @param type the statistics type
     * @param i the index of the variable
     * @param j the index of the saved point
     * @return
     */
    double getStat(StatType type, int i, int j) {
        switch(type) {
            case AVERAGE:
                return average[i][j];
            case VARIANCE:
                return covariance[i][i][j];
            case STDDEV:
                return Descriptive.standardDeviation(covariance[i][i][j]);
            case CV:
                return (covariance[i][i][j] > 0 ? Descriptive.standardDeviation(covariance[i][i][j])/average[i][j] : 0.0);
            case FANO:
                return (covariance[i][i][j] > 0 ? covariance[i][i][j]/average[i][j] : 0.0);
            case SKEW:
                    return (skew != null ? skew[i][j] : 0);
            case KURTOSIS:
                    return (kurtosis != null ? kurtosis[i][j] : 0);
            case STDERR:
                return Descriptive.standardError(runs, covariance[i][i][j]);
            default:
                return 0.0;
        }
    }

    /**
     * returns the specified final state statistics of variable with index i
     * @param type the statistics type
     * @param i the index of the variable
     * @return
     */
    double getStat(StatType type, int i) {
        switch(type) {
            case AVERAGE:
                return finalAverage[i];
            case VARIANCE:
                return finalCovariance[i][i];
            case STDDEV:
                return  Descriptive.standardDeviation(finalCovariance[i][i]);
            case CV:
                return (finalCovariance[i][i]>0 ? Descriptive.standardDeviation(finalCovariance[i][i])/finalAverage[i] : 0.0);
            case FANO:
                return (finalCovariance[i][i]>0 ? finalCovariance[i][i]/finalAverage[i] : 0.0);
            case SKEW:
                return (finalSkew != null ? finalSkew[i] : 0);
            case KURTOSIS:
                return (finalKurtosis != null ? finalKurtosis[i] : 0);
            case STDERR:
                return Descriptive.standardError(runs, finalCovariance[i][i]);
            default:
                return 0.0;
        }
    }

    /**
     * returns the index of data point corresponding to the time instant
     * closest to the specified time
     * @param time
     * @return
     */
    int getIndex(double time) {
        if (stepTrajectories) 
            throw new DataException("Trajectories are step based, cannot identify an index for a time point");
        int k = (int)Math.round((time-this.initialSavingTime)/this.timeStep);
        if (k >= points)
            throw new DataException("Trajectories do not reach time " + time);
        return k;
    }

    /**
     * returns the index of data point corresponding to the given step.
     * @param step
     * @return
     */
    int getIndex(int step) {
        if (!stepTrajectories)
            throw new DataException("Trajectories are not step based, cannot identify an index for a given step");
        int k = (step + stepSize/2)/stepSize;
        if (k >= points)
            throw new DataException("Trajectories do not reach step " + step);
        return k;
    }

    /**
     * Instructs to plot the specified subset fo variables
     * @param vars
     */
    public void setVarsToPlot(ArrayList<String> vars) {
        if (vars != null) {
            this.varsToPlotNames = vars;
            this.varsToPlot = new int[vars.size()];
            try {
                for (int i=0;i<vars.size();i++) {
                    int k = getVarIndex(vars.get(i));
                    varsToPlot[i] = k;
                }
            }
            catch (IllegalArgumentException e) {
                this.varsToPlot = null;
                throw new DataException(e.getMessage());
            }
        } else {
            this.varsToPlot = null;
        }
    }

    /**
     * returns the index of the variable with name n, throws an exception if the variable does
     * not exist
     * @param n
     * @return
     */
    int getVarIndex(String n) {
        for (int i=0;i<names.length;i++)
            if (names[i].equals(n))
                return i;
        throw new IllegalArgumentException("Variable " + n + " does not exist.");
    }

    /**
     * Instructs to plot or save all variables.s
     */
    void plotAllVars() {
        this.varsToPlot = null;
    }




    /**
     * generates an histogram chart from the final state distribution of
     * given subset of variables.
     * @return an {@link Histogram2DChart} object
     */
    private Histogram2DChart getFinalStateDistributionChart() {
        ArrayList<PlotDistributionState> list = new ArrayList<PlotDistributionState>();
        Histogram2DChart chart;
        if (varsToPlot == null)
            for (int j=1;j<vars;j++) {
                double[] x  = new double[runs];
                for (int i=0;i<runs;i++)
                    x[i] = finalData[i][j];
                PlotDistributionState t = new PlotDistributionState(names[j],x);
                list.add(t);
            }
        else
            for (int j=0;j<varsToPlot.length;j++) {
                double[] x  = new double[runs];
                for (int i=0;i<runs;i++)
                    x[i] = finalData[i][varsToPlot[j]];
                PlotDistributionState t = new PlotDistributionState(names[varsToPlot[j]],x);
                list.add(t);
            }
        if (this.autoBin)
            chart = new Histogram2DChart(list);
        else
            chart = new Histogram2DChart(list,bins);
        chart.fullLatexDocument = this.fullLatexDocument;
        return chart;
    }


//   /**
//     * generates an histogram chart from the state distribution of
//     * given subset of variables at a given step
//     * @param step the step at which sampling the distribution
//     * @return an {@link Histogram2DChart} object
//     */
//    private Histogram2DChart getStateDistributionChart(int step) {
//        ArrayList<PlotDistributionState> list = new ArrayList<PlotDistributionState>();
//        Histogram2DChart chart;
//        int index = this.getIndex(step);
//        if (varsToPlot == null)
//            for (int j=1;j<vars;j++) {
//                PlotDistributionState t = new PlotDistributionState(names[j],this.data[j][index]);
//                list.add(t);
//            }
//        else
//            for (int j=0;j<varsToPlot.length;j++) {
//                PlotDistributionState t = new PlotDistributionState(names[varsToPlot[j]],this.data[varsToPlot[j]][index]);
//                list.add(t);
//            }
//        if (this.autoBin)
//            chart = new Histogram2DChart(list);
//        else
//            chart = new Histogram2DChart(list,bins);
//        chart.fullLatexDocument = this.fullLatexDocument;
//        return chart;
//    }

    /**
     * generates an histogram chart from the state distribution of
     * given subset of variables at a given time
     * @param time the time at which sampling the distribution
     * @return an {@link Histogram2DChart} object
     */
    private Histogram2DChart getStateDistributionChart(double time) {
        ArrayList<PlotDistributionState> list = new ArrayList<PlotDistributionState>();
        Histogram2DChart chart;
        int index = this.getIndex(time);
        if (varsToPlot == null)
            for (int j=1;j<vars;j++) {
                double[] x  = new double[runs];
                for (int i=0;i<runs;i++)
                    x[i] = this.data[i][j][index];
                PlotDistributionState t = new PlotDistributionState(names[j],x);
                list.add(t);
            }
        else
            for (int j=0;j<varsToPlot.length;j++) {
                double[] x  = new double[runs];
                for (int i=0;i<runs;i++)
                    x[i] = this.data[i][varsToPlot[j]][index];
                PlotDistributionState t = new PlotDistributionState(names[varsToPlot[j]],x);
                list.add(t);
            }
        if (this.autoBin)
            chart = new Histogram2DChart(list);
        else
            chart = new Histogram2DChart(list,bins);
        chart.fullLatexDocument = this.fullLatexDocument;
        return chart;
    }

    /**
     * generates a 3d histogram for the distribution along a trajectory
     * of the given variable
     * @param var
     * @return
     */
    private Histogram3DChart getTrajectoryDistributionChart(String var) {
        /*
         * TODO: implement this!
         */
        return null;
    }


    /**
     * Generates a Line chart for the trajectories of  a given statistics.
     * @param stat
     * @return
     */
    private LineChart getTrajectoriesChart(StatType stat) {
        ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
        if (varsToPlot == null)
            for (int j=1;j<vars;j++) {
                Plot2DTrajectory t = new Plot2DTrajectory(this.points);
                t.name = names[j];
                t.x = average[0];
                t.y = getStatVector(stat,j);
                list.add(t);
//                System.out.println(t.name);
//                System.out.println(java.util.Arrays.toString(t.x));
//                System.out.println(java.util.Arrays.toString(t.y));
                
            }
        else
            for (int j=0;j<varsToPlot.length;j++) {
                Plot2DTrajectory t = new Plot2DTrajectory(this.points);
                t.name = names[varsToPlot[j]];
                t.x = average[0];
                t.y = getStatVector(stat,varsToPlot[j]);
                list.add(t);
//                System.out.println(t.name);
//                System.out.println(java.util.Arrays.toString(t.x));
//                System.out.println(java.util.Arrays.toString(t.y));
            }
        LineChart chart = new LineChart(list,"time",stat.toString());
        chart.fullLatexDocument = this.fullLatexDocument;
        return chart;
    }

    /**
     * Plots to a new window the timed trajectory for the given statistics
     * @param stat the statistics type
     * @return a JFrame handle
     */
    public JFrame plotTrajectoryStatisticsToScreen(StatType stat) {
        if (this.containsStatistics(stat)) {
            LineChart chart = this.getTrajectoriesChart(stat);
            JFrame frame = chart.show();
            frame.setTitle("SimHyA: " + this.modelName + " trajectory " + stat.toString());
            return frame;
        } else return null;
    }
    
    public JPanel plotTrajectoryStatisticsToPanel(StatType stat) {
        if (this.containsStatistics(stat)) {
            LineChart chart = this.getTrajectoriesChart(stat);
            JPanel frame = chart.getPanel();
            return frame;
        } else return null;
    }

    /**
     * Plots to a file of given type the timed trajectory for the given statistics
     * @param stat the statistics type
     * @param filename the name of the file
     * @param target the file type.
     */
    public void plotTrajectoryStatistics(StatType stat, String filename, PlotFileType target) {
       if (this.containsStatistics(stat)) {
            LineChart chart = this.getTrajectoriesChart(stat);
            chart.saveToFile(filename,target);
       }
    }

    /**
     * returns the trace of statistics for variable k
     * @param stat the statistics tyoe
     * @param k the index of the variable
     * @return a double vector
     */
    private double[] getStatVector(StatType stat,int k) {
        double[] v = new double[points];
        for (int i=0;i<points;i++)
            v[i] = this.getStat(stat, k, i);
        return v;
    }



    /**
     * Plots to screen the distribution of the final state for the selected variables 
     * (set them using the method {@link setVarsToPlot}).
     * @return the JFrame of the plot
     */
    public JFrame plotFinalDistributionToScreen() {
        if (this.containsFinalStateData()) {
            Histogram2DChart chart = this.getFinalStateDistributionChart();
            JFrame frame = chart.show();
            frame.setTitle("SimHyA: " + this.modelName + " distribution of final state");
            return frame;
        } else return null;
    }
    
    public JPanel plotFinalDistributionToPanel() {
        if (this.containsFinalStateData()) {
            Histogram2DChart chart = this.getFinalStateDistributionChart();
            JPanel frame = chart.getPanel();
            return frame; 
        } else return null;
    }    
    
    /**
     * Plots to file the distribution of the final state for the selected variables (set them using the method
     * {@link setVarsToPlot}.
     * @param filename the name of the file
     * @param target the file type.
     */
    public void plotFinalDistribution(String filename, PlotFileType target) {
        if (this.containsFinalStateData()) {
            Histogram2DChart chart = this.getFinalStateDistributionChart();
            chart.saveToFile(filename, target);
        }
    }

    /**
     * Plots to screen the distribution of  the selected variables at specified time (set them using the method
     * {@link setVarsToPlot}.
     * @param time the time at which plotting the distribution
     * @return the JFrame of the plot
     */
    public JFrame plotDistributionToScreen(double time) {
        if (this.containsData()) {
            Histogram2DChart chart = this.getStateDistributionChart(time);
            JFrame frame = chart.show();
            frame.setTitle("SimHyA: " + this.modelName + " distribution at time " + String.format(Locale.UK, "%.3f", time));
            return frame;
        } else return null;
        
    }
    
    public JPanel plotDistributionToPanel(double time) {
        if (this.containsData()) {
            Histogram2DChart chart = this.getStateDistributionChart(time);
            JPanel frame = chart.getPanel();
            return frame;
        } else return null;
    }

    /**
     * Plots to file the distribution of  the selected variables at specified time (set them using the method
     * {@link setVarsToPlot}.
     * @param time the time at which plotting the distribution
     * @param filename the name of the file
     * @param target the file type.
     */
    public void plotDistribution(double time, String filename, PlotFileType target) {
        if (this.containsData()) {
            Histogram2DChart chart = this.getStateDistributionChart(time);
            chart.saveToFile(filename, target);
        }
    }

//    /**
//     * Plots to screen the distribution of  the selected variables at specified step (set them using the method
//     * {@link setVarsToPlot}.
//     * @param step the step at which plotting the distribution
//     * @return the JFrame of the plot
//     */
//    public JFrame plotDistributionToScreen(int step) {
//        Histogram2DChart chart = this.getStateDistributionChart(step);
//        JFrame frame = chart.show();
//        frame.setTitle("SimHyA: " + this.modelName + " distribution at step " + step);
//        return frame;
//    }
//    
//    public JPanel plotDistributionToPanel(int step) {
//        Histogram2DChart chart = this.getStateDistributionChart(step);
//        JPanel frame = chart.getPanel();
//        return frame;
//    }
//
//    /**
//     * Plots to file the distribution of  the selected variables at specified step (set them using the method
//     * {@link setVarsToPlot}.
//     * @param step the step at which plotting the distribution
//     * @param filename the name of the file
//     * @param target the file type.
//     */
//    public void plotDistribution(int step, String filename, PlotFileType target) {
//        Histogram2DChart chart = this.getStateDistributionChart(step);
//        chart.saveToFile(filename, target);
//    }

    /**
     * 
     * @param type
     * @return 
     */
    public boolean supportsStatistics(StatType type) {
        switch(type) {
            case AVERAGE:
                return finalAverage != null && average != null;
            case VARIANCE:
                return finalCovariance != null && covariance != null;
            case STDDEV:
                return  this.supportsStatistics(StatType.AVERAGE) && this.supportsStatistics(StatType.VARIANCE);
            case CV:
                return this.supportsStatistics(StatType.AVERAGE) && this.supportsStatistics(StatType.VARIANCE);
            case FANO:
                return this.supportsStatistics(StatType.AVERAGE) && this.supportsStatistics(StatType.VARIANCE);
            case SKEW:
                return this.finalSkew != null && skew != null;
            case KURTOSIS:
                return this.finalKurtosis != null && kurtosis != null;
            case STDERR:
                return this.supportsStatistics(StatType.AVERAGE) && this.supportsStatistics(StatType.VARIANCE);
            default:
                return false;
        }
        
        
    }
    
    
    public boolean supportHistogram() {
        return this.data != null;
    }

    public boolean supportfinalStateHistogram() {
        return this.finalData != null;
    }
    
}
