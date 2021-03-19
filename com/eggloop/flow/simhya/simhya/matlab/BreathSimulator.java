/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.matlab;

import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;

import java.util.ArrayList;
import java.util.Locale;

/**
 *
 * @author luca
 */
public abstract class BreathSimulator implements BasicSimulator {
    boolean continuousSimulation = false;
    boolean finalExpirationPhaseOnly = false;
    boolean savePhase = false;
    
    
    double normRnd(double mean, double stdev) {
        return RandomGenerator.nextNormal(mean, stdev);
    }

    public void setSavePhase(boolean savePhase) {
        this.savePhase = savePhase;
    }
    
    

    public void setContinuousSimulation(boolean continuousSimulation) {
        this.continuousSimulation = continuousSimulation;
    }

    public void setFinalExpirationPhaseOnly(boolean finalExpirationPhaseOnly) {
        this.finalExpirationPhaseOnly = finalExpirationPhaseOnly;
    }
    
    
    double[][] convertToArray(ArrayList<Double> x1, ArrayList<Double> x2, 
                    ArrayList<Double> x3) {
        if (x1.size() != x2.size() || x1.size() != x3.size())
            throw new RuntimeException("Lists of different length. Aborting");
        int n = x1.size();
        double[][] x = new double[3][n];
        for (int i=0;i<n;i++) {
            x[0][i] = x1.get(i);
            x[1][i] = x2.get(i);
            x[2][i] = x3.get(i);
        }
        return x;
    }
    
    double[][] convertToArray(ArrayList<Double> x1, ArrayList<Double> x2, 
                    ArrayList<Double> x3, ArrayList<Double> x4) {
        if (x1.size() != x2.size() || x1.size() != x3.size() || x1.size() != x4.size())
            throw new RuntimeException("Lists of different length. Aborting");
        int n = x1.size();
        double[][] x = new double[4][n];
        for (int i=0;i<n;i++) {
            x[0][i] = x1.get(i);
            x[1][i] = x2.get(i);
            x[2][i] = x3.get(i);
            x[3][i] = x4.get(i);
        }
        return x;
    }
    
    
    double[][] generateDiagonalMatrix(double[] diag) {
        int n = diag.length;
        double[][] x = new double[n][n];
        for (int i=0;i<n;i++)
            for (int j=0;j<n;j++)
                x[i][j] = 0;
        for (int i=0;i<n;i++)
            x[i][i] = diag[i];
        return x;
    }
    
    
    
    /**
     * loads a vector of doubles from a comma separated file, where coeffs are in a single row
     * @param file
     * @param dim
     * @return 
     */
    double[] loadVector(String file, int dim) {
        //System.out.println("Loading vector from " + file + ". Its expected dimension is " + dim);
        double [] x = new double[dim];
        try {
            //System.out.println("Trying to open the file");
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
            //System.out.println("Success");
            String line = reader.readLine();
            //System.out.println("Read first line of the file: " + line);
            java.util.Scanner scanner = new java.util.Scanner(line);
            scanner.useDelimiter(",");
            scanner.useLocale(Locale.US);
            //System.out.println("String scanner initialised");
            int i=0;
            while (scanner.hasNext() && i < dim) {
                //System.out.println("Trying to read element " + i + " of the vector");
                x[i++] = scanner.nextDouble();
                //System.out.println("Succeded. Element is " + x[i-1]);
            }
            return x;
        } catch (Exception e) {
            //System.out.println("Something bad happened! An exception occurred with message " + e.getMessage());
            //e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        
        
    }
    
    /**
     * loads a Table file, with he given number of rows and columns.
     * @param file
     * @param rows
     * @param cols
     * @return 
     */
    double[][] loadTable(String file, int rows, int cols) {
        double [][] x = new double[rows][cols];
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
            for (int i=0;i<rows;i++) {
                String line = reader.readLine();
                java.util.Scanner scanner = new java.util.Scanner(line);
                scanner.useDelimiter(",");
                scanner.useLocale(Locale.US);
                int j=0;
                while (scanner.hasNext() && j<cols) {
                    x[i][j++] = scanner.nextDouble();
                }              
            }
            return x;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
}
