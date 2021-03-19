package com.eggloop.flow.learning;

import com.eggloop.flow.simhya.simhya.matlab.genetic.Formula;
import com.eggloop.flow.simhya.simhya.matlab.genetic.FormulaPopulation;
import com.eggloop.flow.simhya.simhya.matlab.genetic.GeneticOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Learning {
    private final Logger logger;
    private final BiFunction<double[], double[], Double> function;

    public Learning(Logger logger, BiFunction<double[], double[], Double> function) {
        this.logger = logger;
        this.function = function;
    }

    GeneticPopulation optimizeGenerationParameters(FormulaPopulation pop, String[] variables, double[] ds2Times, double[][][] normal_model, double[][][] ineffective_model, double[][][] normal_model_test, double[][][] ineffective_model_test, double atTime) {
        List<Formula> rankFormulae = new ArrayList<>();
        List<double[]> rankParameters = new ArrayList<>();
        List<Double> rankScore = new ArrayList<>();
        for (int i = 0; i < pop.getPopulationSize(); i++) {
            Formula formula = pop.getFormula(i);
            //gpucb
            logger.log(Level.FINE, "OPTIMIZE PARAMETER OF FORMULA " + formula.toString());
            double[] bestParamterFormula = ComputeAverage.averageMultiTrajectory(50, function, variables, ds2Times, normal_model, ineffective_model, formula, pop, new double[]{GeneticOptions.min_time_bound, GeneticOptions.max_time_bound}, atTime);
            logger.log(Level.FINE, "BEST PARAMS: " + Arrays.toString(bestParamterFormula));
            logger.log(Level.FINE, " ");
            try {
                double score = getScore(formula, pop, variables, ds2Times, normal_model, ineffective_model, normal_model_test, ineffective_model_test,atTime);
                if (Double.isNaN(score)) {
                    score=Double.NEGATIVE_INFINITY;
                }
                rankScore.add(score);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                rankScore.add(Double.NEGATIVE_INFINITY);
            } finally {
                rankFormulae.add(formula);
                rankParameters.add(bestParamterFormula);
            }
        }
        return new GeneticPopulation(rankFormulae, rankParameters, rankScore);
    }

    private double getScore(Formula formula, FormulaPopulation pop, String[] variables, double[] ds2Times, double[][][] normal_model, double[][][] ineffective_model, double[][][] normal_model_test, double[][][] ineffective_model_test, double atTime) {
        double[] parameters = ComputeAverage.averageMultiTrajectory(30, function, variables, ds2Times, normal_model, ineffective_model, formula, pop, new double[]{GeneticOptions.min_time_bound, GeneticOptions.max_time_bound}, atTime);
        //computation of G
        double[] p1 = ComputeAverage.computeAverageRobustnessMultiTrajectory(ds2Times, normal_model_test, variables, formula, parameters, atTime);
        double[] p2 = /*ComputeAverage.smcMultiTrajectories(ds2Times, normal_model_test, variables, formula, parameters, atTime);*/ComputeAverage.computeAverageRobustnessMultiTrajectory(ds2Times, ineffective_model_test, variables, formula, parameters, atTime);
        //return - (p1[0] + p2[0]);
        return function.apply(p1, p2);
    }

}
