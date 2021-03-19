/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.utils;

import com.eggloop.flow.simhya.simhya.simengine.ProgressMonitor;

import java.io.PrintStream;
import java.util.Locale;

/**
 * @author Luca
 */
public class PrintStreamProgressMonitor implements ProgressMonitor {
    private PrintStream stream;
    private double finalTime;
    public double initialTime;
    private double granularity;
    private double currentPercentage;
    private long simTime;
    private double deltaTime;
    private long totalSteps;
    private long currentStep;
    private boolean silent;
    private boolean printEvents;

    public PrintStreamProgressMonitor(PrintStream stream) {
        this.stream = stream;
        initialTime = 0;
        finalTime = 0;
        totalSteps = 1;
        currentStep = 0;
        this.granularity = 0.05;
        deltaTime = finalTime - initialTime;
        silent = false;
        printEvents = false;
    }

    public double getLastSimulationTimeInMillisecs() {
        return (double) (simTime) / 1000000;
    }

    public double getLastSimulationTimeInSecs() {
        return (double) (simTime) / 1000000000;
    }

    public void setFinalTime(double finalTime) {
        this.finalTime = finalTime;
        deltaTime = finalTime - initialTime;
    }

    public void setInitialTime(double initialTime) {
        this.initialTime = initialTime;
        deltaTime = finalTime - initialTime;
    }

    public void setTotalSteps(long steps) {
        this.totalSteps = steps;
    }

    public void setPrintEvents(boolean print) {
        this.printEvents = print;
    }


    public void setProgress(double currentTime) {
        if (currentTime > deltaTime * currentPercentage) {
            double eta = (1 - currentPercentage) * (double) (System.nanoTime() - simTime) / 1000000000;
            if (!silent)
                stream.println(String.format(Locale.UK, "%.2f", currentPercentage * 100)
                        + "% completed. Remaining time: " + String.format(Locale.UK, "%.4f", eta) + " seconds ");
            currentPercentage += this.granularity;
        }
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setGranularity(double g) {
        this.granularity = g;
    }

    public void nextStep() {
        this.currentStep++;
        double time = (double) (System.nanoTime() - simTime) / 1000000000;
        double eta = (double) (totalSteps - currentStep) * time / (double) currentStep;
        if (this.currentStep < this.totalSteps)
            if (!silent)
                stream.println("Completed step " + currentStep + " of " + totalSteps + ". "
                        + "Remaining time: " + String.format(Locale.UK, "%.3f", eta) + " seconds ");
            else if (!silent)
                stream.println("Completed in time " + String.format(Locale.UK, "%.3f", time) + " seconds ");
    }


    public void start() {
        this.currentPercentage = this.granularity;
        simTime = System.nanoTime();
    }

    public void stop() {
        simTime = System.nanoTime() - simTime;
    }


    public void signalEvent(String name, double time) {
        if (this.printEvents) {
            stream.println("Event " + name + " occurred at time " + time);
        }
    }

    public void setPrintStream(PrintStream stream) {
        if (stream != null)
            this.stream = stream;
    }

}
