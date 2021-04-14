/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matlab;
import eggloop.flow.simhya.simhya.utils.RandomGenerator;

/**
 *
 * @author luca
 */
public class HmmHeartSimulator {
    double[] initial_distribution;
    double[][] transition_matrix;
    double[][] observation_emission_matrix;
    double[] gaussian_emission_mean;
    double[] gaussian_emission_std;
    int states;
    int observations;
    final int INIT_SAMPLE_SIZE = 10000;

    public HmmHeartSimulator(double[] initial_distribution, double[][] transition_matrix, double[][] observation_emission_matrix, double[] gaussian_emission_mean, double[] gaussian_emission_std) {
        this.initial_distribution = initial_distribution;
        this.transition_matrix = transition_matrix;
        this.observation_emission_matrix = observation_emission_matrix;
        this.gaussian_emission_mean = gaussian_emission_mean;
        this.gaussian_emission_std = gaussian_emission_std;
        this.states = this.initial_distribution.length;
        this.observations = this.observation_emission_matrix[0].length;
    }
    
    
    public double[][] sample(double finalTime) {
        int steps = 1;
        double[][] x = new double[observations+1][INIT_SAMPLE_SIZE];
        
        int state = RandomGenerator.sample(initial_distribution);
        int obs = RandomGenerator.sample(this.observation_emission_matrix[state]);
        x[0][0] = 0;
        for (int i=1;i<=observations;i++)
            x[i][0] = -1;
        x[obs+1][0]=1;
        double deltaT = -1;
        while (deltaT<=0)
            deltaT = RandomGenerator.nextNormal(this.gaussian_emission_mean[state], this.gaussian_emission_std[state]);
        double  t = deltaT; 
        while (t < finalTime) {
            if (steps >= x[0].length) {
                for (int i=0;i<=observations;i++)
                    x[i] = java.util.Arrays.copyOf(x[i], 2*steps);
            }
            //new state
            state = RandomGenerator.sample(this.transition_matrix[state]);
            //new discrete obs
            int newobs = RandomGenerator.sample(this.observation_emission_matrix[state]);
            //update x
            x[0][steps] = t;
            for (int i=1;i<=observations;i++)
                x[i][steps] = x[i][steps-1];
            x[obs+1][steps] = -1;
            x[newobs+1][steps] = 1;
            obs=newobs;
            //new time
            deltaT = -1;
            while (deltaT<=0)
                deltaT = RandomGenerator.nextNormal(this.gaussian_emission_mean[state], this.gaussian_emission_std[state]);
            t += deltaT;
            steps++;
            
        }
        //add final state
        if (steps >= x[0].length) {
            for (int i=0;i<=observations;i++)
                x[i] = java.util.Arrays.copyOf(x[i], steps+1);
        }
        x[0][steps] = finalTime;
        for (int i=1;i<=observations;i++)
            x[i][steps] = x[i][steps-1];
        steps++;
        //trim to size
        if (steps < x[0].length) {
            for (int i=0;i<=observations;i++)
                x[i] = java.util.Arrays.copyOf(x[i], steps);
        }
        return x;
    }
    
    
    
    
    
}
