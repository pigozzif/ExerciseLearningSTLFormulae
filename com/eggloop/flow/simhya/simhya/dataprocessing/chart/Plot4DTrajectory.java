/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing.chart;

/**
 *
 * @author Luca
 */
public class Plot4DTrajectory {
    public String name;
    public double [] x;
    public double [] y;
    public double [] z;
    public double [][][] v;

    public Plot4DTrajectory(int Xpoints, int Ypoints, int Zpoints) {
        x = new double[Xpoints];
        y = new double[Ypoints];
        z = new double[Zpoints];
        for (int i=0;i<Xpoints;i++) {
            v[i] = new double[Ypoints][];
            for (int j=0;j<Ypoints;j++)
                v[i][j] = new double[Zpoints];
        }
    }

}
