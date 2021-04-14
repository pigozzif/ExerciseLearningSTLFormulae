/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matlab;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.ArrayList;

/**
 * @author luca
 */
public class IneffectiveBreathSimulator extends BreathSimulator {
    static public double step = 0.01;
    static public double phase_3_threshold = -8000;

    MersenneTwister rand = new MersenneTwister();
    double[][] covPoly1;
    double[][] covPoly2;
    double[][] covPoly3;
    double[] intercept;
    double[] intToSlo;
    double[] lenToInt;
    double[] meanLength;
    double[] meanNoisePoly;
    double[] meanPoly1;
    double[] meanPoly2;
    double[] meanPoly3;
    double[] noisesd;
    double[] sdLength;
    double[] sdNoisePoly;
    double[] slope;


    MultivariateNormalDistribution mnrnd1;
    MultivariateNormalDistribution mnrnd2;
    MultivariateNormalDistribution mnrnd3;

    public IneffectiveBreathSimulator(String folder) {
        covPoly1 = super.loadTable(folder + "covPoly1", 8, 8);
        covPoly2 = super.loadTable(folder + "covPoly2", 8, 8);
        covPoly3 = super.loadTable(folder + "covPoly3", 8, 8);
        intercept = super.loadVector(folder + "intercept", 5);
        intToSlo = super.loadVector(folder + "intToSlo", 2);
        lenToInt = super.loadVector(folder + "lenToInt", 2);
        meanLength = super.loadVector(folder + "meanLength", 4);
        meanNoisePoly = super.loadVector(folder + "meanNoisePoly", 3);
        meanPoly1 = super.loadVector(folder + "meanPoly1", 8);
        meanPoly2 = super.loadVector(folder + "meanPoly2", 8);
        meanPoly3 = super.loadVector(folder + "meanPoly3", 8);
        noisesd = super.loadVector(folder + "noisesd", 5);
        sdLength = super.loadVector(folder + "sdLength", 4);
        sdNoisePoly = super.loadVector(folder + "sdNoisePoly", 3);
        slope = super.loadVector(folder + "slope", 5);
        mnrnd1 = new MultivariateNormalDistribution(rand, meanPoly1, covPoly1);
        mnrnd2 = new MultivariateNormalDistribution(rand, meanPoly2, covPoly2);
        mnrnd3 = new MultivariateNormalDistribution(rand, meanPoly3, covPoly3);
    }


    public double[][] simulate(double tf) {
        ArrayList<Double> time = new ArrayList<Double>();
        ArrayList<Double> phase = new ArrayList<Double>();
        ArrayList<Double> flow = new ArrayList<Double>();
        ArrayList<Double> flow1 = new ArrayList<Double>();

        int[] duration = new int[5];
        for (int p = 0; p < 4; p++) {
            duration[p] = 0;
            while (duration[p] <= 0 && meanLength[p] > 0)
                duration[p] = (int) Math.round(normRnd(meanLength[p], sdLength[p]));
        }

        double[] poly1 = mnrnd1.sample();
        while (poly1[0] < 0)
            poly1 = mnrnd1.sample();
        double[] poly2 = mnrnd2.sample();
        while (poly2[0] < 0)
            poly2 = mnrnd2.sample();
        double[] poly3 = mnrnd3.sample();
        while (poly3[0] < 0)
            poly3 = mnrnd3.sample();

        int length1 = (int) Math.round(poly1[0]);
        int length2 = (int) Math.round(poly2[0]);
        int length3 = (int) Math.round(poly3[0]);

        duration[4] = length1 + length2 + length3;
        intercept[4] = lenToInt[0] / (duration[4] - lenToInt[1]);
        slope[4] = intToSlo[0] * intercept[4] + intToSlo[1];

        //initial point
        double t = 0;
        double f = 0;
        if (!super.finalExpirationPhaseOnly) {
            time.add(t);
            flow.add(f);
            phase.add(1.0);
        }

        //phases 1 and 2
        int p, j;
        for (p = 0; p <= 1; p++) {
            for (j = 0; j < duration[p]; j++) {
                t += step;
                f = slope[p] * f + intercept[p] + normRnd(0, noisesd[p]);
                if (!super.finalExpirationPhaseOnly) {
                    phase.add(new Double(p + 1));
                    time.add(t);
                    flow.add(f);
                }
            }
        }

        //phase 3
        p = 2;
        while (f > phase_3_threshold) {
            t += step;
            f = slope[p] * f + intercept[p] + normRnd(0, noisesd[p]);
            if (!super.finalExpirationPhaseOnly) {
                phase.add(new Double(p + 1));
                time.add(t);
                flow.add(f);
            }
        }

        //phase 4
        p = 3;
        for (j = 0; j < duration[p]; j++) {
            t += step;
            f = slope[p] * f + intercept[p] + normRnd(0, noisesd[p]);
            if (!super.finalExpirationPhaseOnly) {
                phase.add(new Double(p + 1));
                time.add(t);
                flow.add(f);
            }
        }

        //phase 5: here we have a hierarchical model
        //first generate perturbation curve, then the normal flow cirve

        double[] simDif1 = linspace(length1);
        for (int i = 0; i < simDif1.length; i++)
            simDif1[i] = poly1[1] * simDif1[i] + poly1[2] * Math.pow(simDif1[i], 2)
                    + poly1[3] * Math.pow(simDif1[i], 3) + poly1[4] * Math.pow(simDif1[i], 4)
                    + poly1[5] * Math.pow(simDif1[i], 5) + poly1[6] * Math.pow(simDif1[i], 6)
                    + poly1[7] * Math.pow(simDif1[i], 7);// + normRnd(meanNoisePoly[0],sdNoisePoly[0]);
        double[] simDif2 = linspace(length2);
        for (int i = 0; i < simDif2.length; i++)
            simDif2[i] = poly2[1] * simDif2[i] + poly2[2] * Math.pow(simDif2[i], 2)
                    + poly2[3] * Math.pow(simDif2[i], 3) + poly2[4] * Math.pow(simDif2[i], 4)
                    + poly2[5] * Math.pow(simDif2[i], 5) + poly2[6] * Math.pow(simDif2[i], 6)
                    + poly2[7] * Math.pow(simDif2[i], 7); //+ normRnd(meanNoisePoly[1],sdNoisePoly[1]);
        double[] simDif3 = linspace(length3);
        for (int i = 0; i < simDif3.length; i++)
            simDif3[i] = poly3[1] * simDif3[i] + poly3[2] * Math.pow(simDif3[i], 2)
                    + poly3[3] * Math.pow(simDif3[i], 3) + poly3[4] * Math.pow(simDif3[i], 4)
                    + poly3[5] * Math.pow(simDif3[i], 5) + poly3[6] * Math.pow(simDif3[i], 6)
                    + poly3[7] * Math.pow(simDif3[i], 7); // + normRnd(meanNoisePoly[2],sdNoisePoly[2]);
        double[] simDif = joinArrays(simDif1, simDif2, simDif3);

        //phase 5
        p = 4;
        if (super.finalExpirationPhaseOnly) {
            t = 0;
            phase.add(new Double(p + 1));
            time.add(t);
            flow.add(f);
        }
        for (j = 0; j < duration[p]; j++) {
            t += step;
            f = slope[p] * f + intercept[p] + normRnd(0, noisesd[p]);
            phase.add(new Double(p + 1));
            time.add(t);
            flow.add(f + simDif[j]);
        }

        int m = flow.size();
        for (j = 0; j < flow.size() - 1; j++)
            flow1.add(flow.get(j + 1) - flow.get(j));
        //remove last point
        time.remove(m - 1);
        flow.remove(m - 1);
        phase.remove(m - 1);

        double[][] x;
        if (super.savePhase)
            x = convertToArray(time, phase, flow, flow1);
        else
            x = convertToArray(time, flow, flow1);
        return x;
    }


    double[] linspace(int l) {
        double[] x = new double[l];
        for (int i = 0; i < l; i++) {
            double j = i + 1;
            x[i] = j / l;
        }
        return x;
    }


    double[] joinArrays(double[] x1, double[] x2, double[] x3) {
        int n1 = x1.length;
        int n2 = x2.length;
        int n3 = x3.length;
        double[] x = new double[n1 + n2 + n3];
        int n = 0;
        for (int i = 0; i < n1; i++)
            x[n++] = x1[i];
        for (int i = 0; i < n2; i++)
            x[n++] = x2[i];
        for (int i = 0; i < n3; i++)
            x[n++] = x3[i];
        return x;
    }

}
