package com.eggloop.flow.simhya.simhya.matlab;

import com.eggloop.flow.simhya.simhya.matlab.genetic.Formula;
import com.eggloop.flow.simhya.simhya.matlab.genetic.FormulaPopulation;
import com.eggloop.flow.simhya.simhya.matlab.genetic.GeneticOptions;
import com.eggloop.flow.simhya.simhya.matlab.genetic.RegularisedLogOddRatioFitness;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        int N = 2;
        double Tf=100;
//        GeneticOptions geneticOptions = new GeneticOptions();
//        geneticOptions.setFitness_type();
        FormulaPopulation pop = new FormulaPopulation(N);

        GeneticOptions.setMin_time_bound(0);
        GeneticOptions.setMax_time_bound(1);
        GeneticOptions.setUndefined_reference_threshold(0.1);
        GeneticOptions.setSize_penalty_coefficient(1);

        GeneticOptions.setFitness_type("regularised_logodds");
        //TODO: SAGGIUSTARE LA SCALE DA MATLAB

//        fitnessOptions.type = 1; %0=normal, 1=modified
//        fitnessOptions.urf = GeneticOptions.undefined_reference_threshold;
//        fitnessOptions.spc = GeneticOptions.size_penalty_coefficient;
//        fitnessOptions.scale = 10;

        int nvar = 2;
        String[] variables = new String[]{"flow", "flow1"};
        double[] lower = new double[]{-10000, -500};
        double[] upper = new double[]{0, 500};
        for (int i = 0; i < nvar; i++) {
            pop.addVariable(variables[i], lower[i], upper[i]);
        }

        String[] pars = new String[]{"Tl_1", "Tu_1", "Theta_2"};
            double[] vv = new double[]{0,1,-300};

        String FF = "P=?[ F[Tl_1,Tu_1] {flow1 <= Theta_2} ]";
        Formula formula = pop.loadFormula(FF, pars, vv);
        System.out.println(formula.toString());
        System.out.println(pop.getFormula(0).toString());


        NormalBreathSimulator normal_model = new NormalBreathSimulator(NormalBreathSimulator.class.getResource("data/normal_model/").getPath());
        normal_model.setFinalExpirationPhaseOnly(true);
        normal_model.setSavePhase(false);
        IneffectiveBreathSimulator ineffective_model = new IneffectiveBreathSimulator(IneffectiveBreathSimulator.class.getResource("data/ineffective_model/").getPath());
        ineffective_model.setFinalExpirationPhaseOnly(true);
        ineffective_model.setSavePhase(false);
        int runs = 1000;
        double[] p1u1 = smc(pop, formula, ineffective_model, Tf, runs);
        double[] p2u2 = smc(pop, formula, ineffective_model, Tf, runs);
        RegularisedLogOddRatioFitness fitnessFucntion = new RegularisedLogOddRatioFitness();
        double fitness = fitnessFucntion.compute(p1u1[0], p2u2[0], formula.getFormulaSize(), p1u1[1], p2u2[1], runs);
        System.out.println(fitness);






    }

    static double[] smc(FormulaPopulation popgen, Formula formula, BreathSimulator simulator, double Tf, int samples){
        int[] data = popgen.modelCheck(simulator, formula, samples, Tf);
        double p = (double)(Arrays.stream(data).filter(x -> x == 1).count() + 1) / (samples + 2);
        double u = (double)(Arrays.stream(data).filter(x -> x == -1).count()) / (samples);
//        p = (sum(data==1)+1)/(samples+2);
//        u = (sum(data==-1))/(samples)
        return new double[]{p,u};

    }

    static double[] averageRobustness(FormulaPopulation popgen, Formula formula, BreathSimulator simulator, double Tf, int samples){
        int[] data = popgen.modelCheck(simulator, formula, samples, Tf);
        double p = (double)(Arrays.stream(data).filter(x -> x == 1).count() + 1) / (samples + 2);
        double u = (double)(Arrays.stream(data).filter(x -> x == -1).count()) / (samples);
//        p = (sum(data==1)+1)/(samples+2);
//        u = (sum(data==-1))/(samples)
        return new double[]{p,u};

    }
}
