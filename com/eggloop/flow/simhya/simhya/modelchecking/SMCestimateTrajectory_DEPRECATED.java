package com.eggloop.flow.simhya.simhya.modelchecking;///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.eggloop.flow.simhya.simhya.modelchecking;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import simhya.dataprocessing.chart.LineChart;
//import simhya.dataprocessing.chart.Plot2DTrajectory;
//import simhya.modelchecking.mtl.Truth;
//
//
///**
// *
// * @author Luca
// */
//public class SMCestimateTrajectory_DEPRECATED implements SMCcontroller, SMCoutput {
//    double[] estimate;
//    double[] upperConfidenceBound;
//    double[] lowerConfidenceBound;
//
//    long total;
//    long[] good;
//    long[] bad;
//
//    double error;
//    double confidence;
//    int points;
//
//    int samplesNeeded;
//    String formula;
//
//    boolean stop = false;
//
//
//    public Truth getTestResult() {
//        return Truth.UNDEFINED;
//    }
//
//    public boolean isEstimate() {
//        return true;
//    }
//
//    public void setCurrent(int c) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//
//    public boolean isExplorator() {
//        return true;
//    }
//
//
//
//
//    public boolean isTest() {
//        return false;
//    }
//
//    public SMCestimateTrajectory_DEPRECATED(int points) {
//        error = 1e-4;
//        confidence = 0.95;
//        this.points = points;
//        this.samplesNeeded = this.computeNeededSamples();
//        init();
//    }
//
//    public SMCestimateTrajectory_DEPRECATED(int points, double error, double confidence) {
//        this.error = error;
//        this.confidence = confidence;
//        this.points = points;
//        this.samplesNeeded = this.computeNeededSamples();
//        init();
//    }
//
//    public SMCestimateTrajectory_DEPRECATED(int points, int samples, double confidence) {
//        this.confidence = confidence;
//        this.points = points;
//        this.samplesNeeded = samples;
//        init();
//    }
//
//    private void init() {
//        estimate = new double[points];
//        this.upperConfidenceBound = new double[points];
//        this.lowerConfidenceBound = new double[points];
//        this.good = new long[points];
//        this.bad = new long[points];
//        Arrays.fill(good, 0);
//    }
//
//    //TO BE IMPLEMENTED
//    private int computeNeededSamples() {
//        return 1000;
//    }
//
//    public void setFormula(String formula) {
//        this.formula = formula;
//    }
//
//    public void addPoint(Truth[] truth) {
//        if (truth.length != points)
//            throw new RuntimeException("Number of points mismatch!");
//        total++;
//        for (int i=0;i<points;i++)
//            if (truth[i] == Truth.TRUE)
//                good[i]++;
//            else if (truth[i] == Truth.FALSE)
//                bad[i]++;
//        this.checkStopCondition();
//    }
//
//
//    private void checkStopCondition() {
//
//    }
//
//
//    public boolean stop() {
//        return stop;
//    }
//
//
//    public void finalization() {
//
//    }
//
//    public String extendedPrint() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public void setSignals(ArrayList<Plot2DTrajectory> signals) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public void bayesian() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public void bayesian(double pGood, double pBad) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public void regularise() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public void regularise(double good, double bad) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//
//
//
//    //TO DO: plot methods, save methods, ...
//
//
//    public String shortPrint() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public ArrayList<Plot2DTrajectory> getTrajectory() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public LineChart plotSignals(boolean all) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public LineChart plotTrajectory() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public String signalsToString(boolean all) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public boolean isTrajectory() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public boolean isFormula(String f) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public String getFormula() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public boolean hasSignals() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public Plot2DTrajectory getTopSignal() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//
//
//
//
//
//
//
//    public double getEstimateFalse() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public double getEstimateTrue() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public double getLowerConfidenceBoundFalse() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public double getLowerConfidenceBoundFalse(double newconfidence) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public double getLowerConfidenceBoundTrue() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public double getLowerConfidenceBoundTrue(double newconfidence) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public double getUpperConfidenceBoundFalse() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public double getUpperConfidenceBoundFalse(double newconfidence) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public double getUpperConfidenceBoundTrue() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    public double getUpperConfidenceBoundTrue(double newconfidence) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//
//}
