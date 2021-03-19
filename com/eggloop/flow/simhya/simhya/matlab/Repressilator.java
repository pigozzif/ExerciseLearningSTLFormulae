/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.matlab;

import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;

/**
 *
 * @author luca
 */
public class Repressilator implements BasicSimulator {
    double dt;
    double gamma;
    double light_hours;
    boolean saveOneSpecies=false;
    int points;
    
    //parameters
    double[] yinit = {0.9710, 0.4682, 0.5500};
    double[] pinit = {0.05, 0.9, 0.3};

    double km = 0.005;
    double kp = 0.000002;
    double ke = 10;

    double[] A = {0, -0.0056, -0.0072, -0.0050};
    double[] B = {0, 0.0066, 0.0102, 0.0050};
    double[] L = {0, 0.0067, 0.0097, 0.0060};
    double S = 1e-5;
    
    
    double[] light_plus = {5.1193e-04, 0.1621};
    double[] light_minus = {0.0260, 0.0060};
    
 

    public Repressilator(double dt, double gamma, double light_hours, int points) {
        this.dt = dt;
        this.gamma = gamma;
        this.light_hours = light_hours;
        this.points = points;
    }

    public Repressilator(double dt, double gamma, double light_hours, boolean saveOneSpecies, int points) {
        this.dt = dt;
        this.gamma = gamma;
        this.light_hours = light_hours;
        this.saveOneSpecies = saveOneSpecies;
        this.points = points;
    }
    
    
    public void setSaveOneSpecies(boolean saveOneSpecies) {
        this.saveOneSpecies = saveOneSpecies;
    }

    public void setS(double S) {
        this.S = S;
    }
    
    
    
    
    
    public boolean[] smc(SimHyAModel m, String phi, double tf, int runs) {
        return m.smc_sim(phi, tf, this, runs);
    }
    
    
    
    
    public double[][] simulate(double tf) {
        //identify the numebr of steps.
        int printstep = (int)Math.ceil(Math.ceil(tf/dt)/(points));
        int steps = printstep * (points);
        int current = 0;
        
        
        double[][] X = new double[7][points+1];
        //initial variables
        double y1 = yinit[0];
        int q1 = (RandomGenerator.flipCoin(pinit[0]) ? 1 : 0);
        double p1 = 0;
        double y2 = yinit[1];
        int q2 = (RandomGenerator.flipCoin(pinit[1]) ? 1 : 0);
        double p2 = 0;
        double y3 = yinit[2];
        int q3 = (RandomGenerator.flipCoin(pinit[2]) ? 1 : 0);
        double p3 = 0;
        
        double time = 0;
        double[] b = {525.0/12.0*light_hours,525.0/12.0*(24-light_hours)};
        double tb = 0;
        
        double sigma = Math.sqrt(S)*Math.sqrt(dt);

        int phase = 0;


        double u1 = RandomGenerator.nextExpDist(1);
        double u2 = RandomGenerator.nextExpDist(1);
        double u3 = RandomGenerator.nextExpDist(1);
        
        
     
        X[0][current] = time;
        X[1][current] = y1;
        X[2][current] = y2;
        X[3][current] = y3;
        X[4][current] = q1;
        X[5][current] = q2;
        X[6][current] = q3;

        
        for (int i=1;i<=steps;i++) {
            
            
            double fm1 = km;
            double fp1 = kp*Math.exp(ke*y3);
            double fm2 = km;
            double fp2 = kp*Math.exp(ke*y1);
            double fm3 = (1-gamma)*km + gamma*light_plus[phase];
            double fp3 = (1-gamma)*kp*Math.exp(ke*y2) + gamma*light_minus[phase];

            p1 = p1 + dt*((1-q1)*fp1 + q1*fm1);
            p2 = p2 + dt*((1-q2)*fp2 + q2*fm2);
            p3 = p3 + dt*((1-q3)*fp3 + q3*fm3);

            y1 = y1 + dt*(A[1]*q1+B[1]-L[1]*y1) + sigma*RandomGenerator.nextNormal(0, 1);
            y2 = y2 + dt*(A[2]*q2+B[2]-L[2]*y2) + sigma*RandomGenerator.nextNormal(0, 1);
            y3 = y3 + dt*(A[3]*q3+B[3]-L[3]*y3) + sigma*RandomGenerator.nextNormal(0, 1);

            time = time + dt;
            tb = tb + dt;

            if (p1>u1) {
                p1 = 0;
                u1 = RandomGenerator.nextExpDist(1);
                q1 = 1-q1;
            }
            
            if (p2>u2) {
                p2=0;
                u2 = RandomGenerator.nextExpDist(1);
                q2 = 1 - q2;
            }

            if (p3>u3) {
                p3=0;
                u3 = RandomGenerator.nextExpDist(1);
                q3 = 1-q3;
            }

            if (tb>=b[phase]) {
                tb = 0;
                phase = 1-phase;
            }
            
            if (i % printstep == 0) {
                current++;
                X[0][current] = time;
                X[1][current] = y1;
                X[2][current] = y2;
                X[3][current] = y3;
                X[4][current] = q1;
                X[5][current] = q2;
                X[6][current] = q3;
            }  
        }
        
        if (this.saveOneSpecies) {
            double [][] Y = new double[2][];
            Y[0] = X[0];
            Y[1] = X[3];
            return Y;
        } else
            return X;
    }
    
    
    
    
    
    
    
}
