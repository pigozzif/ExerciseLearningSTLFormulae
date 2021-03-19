package com.eggloop.flow.learning;

import com.eggloop.flow.simhya.simhya.matlab.genetic.Formula;
import com.eggloop.flow.simhya.simhya.matlab.genetic.FormulaPopulation;
import com.eggloop.flow.simhya.simhya.matlab.genetic.GeneticOptions;
import com.eggloop.flow.utils.data.DataSetSplit;
import com.eggloop.flow.utils.data.TrajectoryMultiReconstruction;
import com.eggloop.flow.utils.data.TrajectoryMultiReconstructionCV;
import com.eggloop.flow.utils.files.Utils;

import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.eggloop.flow.learning.ComputeAverage.smcMultiTrajectories;


public class ExampleLearningMultiVariables {
    private static final Logger LOGGER = com.eggloop.flow.learning.LoggerFactory.get();
    private static final UnaryOperator<String> FILE_PATH = Utils.getFilePath(com.eggloop.flow.learning.Learning.class);
    private static final BiFunction<double[], double[], Double> DISCRIMINATION_FUNCTION = (x, y) -> (x[0] - y[0]) / (Math.abs(x[1] + y[1]));
    private static double[] ds2Labels = Utils.readVectorFromFile(FILE_PATH.apply("multivariablesdata/labels"));
    private static double[] ds2Times = Utils.readVectorFromFile(FILE_PATH.apply("multivariablesdata/times"));
    private static double[][][] ds2SpatialValues = Utils.readMatrixMultiFromFile(ds2Times.length, FILE_PATH.apply("multivariablesdata/values"));

//    private static double[] ds2Times = Utils.readVectorFromFile(FILE_PATH.apply("temporal/synthTime_12.txt"));
//    private static double[] ds2Labels = Utils.readVectorFromFile(FILE_PATH.apply("temporal/synthLabels_12.txt"));
//    private static double[][][] ds2SpatialValues = Utils.readMatrixMultiFromFile(ds2Times.length, FILE_PATH.apply("temporal/synthData_12.txt"));
    private static com.eggloop.flow.learning.Learning learning;

    public static void main(String[] args) {
        LOGGER.setLevel(Level.FINE);
        learning = new com.eggloop.flow.learning.Learning(LOGGER, DISCRIMINATION_FUNCTION);
//        learn(new Random(), LOGGER);
        learnCV(new Random(), LOGGER);
    }

    private static void learn(Random random, Logger logger) {
        //import database
        TrajectoryMultiReconstruction data = new TrajectoryMultiReconstruction(ds2Times, ds2SpatialValues, ds2Labels, 0.8, random);
        data.split();
        double[][] performances = core(random, data, logger);
        System.out.println("Classified as Negative Trajectories [percentage, variance] = " + Arrays.toString(performances[0]));
        System.out.println("Classified as Positive Trajectories [percentage, variance] = " + Arrays.toString(performances[1]));
    }

    private static void learnCV(Random random, Logger logger) {
        //import database
        TrajectoryMultiReconstructionCV data = new TrajectoryMultiReconstructionCV(ds2Times, ds2SpatialValues, ds2Labels, random);
        data.split(3, 5);
        double[][] performances = core(random, data, logger);
        System.out.println("Classified as Negative Trajectories [percentage, variance] = " + Arrays.toString(performances[0]));
        System.out.println("Classified as Positive Trajectories [percentage, variance] = " + Arrays.toString(performances[1]));
    }

    private static double[][] core(Random random, DataSetSplit data, Logger logger) {
        double[][][] positiveTrainingSet = data.getPositiveTrainingSet();
        double[][][] negativeTrainingSet = data.getNegativeTrainingSet();
        double[][][] positiveTestSet = data.getPoistiveTestSet();
        double[][][] negativeTestSet = data.getNegativeTestSet();
        double[] ds2Times = data.getTimes();
        int NE = 50;   // number of formulae in the initial population
        FormulaPopulation pop = new FormulaPopulation(NE);
        double atTime = 0;

        String[] variables = new String[]{"x", "y", "v", "z"};

        // addVariableInformation to population, bounds of the interval parameters
        double[] lower = new double[]{0, 0, 0, 0}; // lowerbound vector for x,y,v,x
        double[] upper = new double[]{80, 45, 80, 45}; //upperbound vector for x,y,v,x

        logger.log(Level.INFO, "Formula Variables:" + Arrays.toString(variables));
        logger.log(Level.INFO, "Formula Variables Lower Bounds:" + Arrays.toString(lower));
        logger.log(Level.INFO, "Formula Variables Upper Bounds:" + Arrays.toString(upper));

        for (int i = 0; i < variables.length; i++) {
            pop.addVariable(variables[i], lower[i], upper[i]);
        }

        // set genetic options
        GeneticOptions.setInit__prob_of_true_atom(0);
        GeneticOptions.setMin_time_bound(0);
        GeneticOptions.setMax_time_bound(11);

        // adding initial Formulae (atomic + G + F + U)
        pop.addGeneticInitFormula(pop.getVariableNumber());

        // adding random formulae
        double maxNumRand = 10;
        for (int i = 0; i < maxNumRand; i++) {
            pop.addRandomInitFormula();
        }
        com.eggloop.flow.learning.GeneticPopulation generation = null;
        int NG = 3;
        logger.log(Level.INFO, "NUMBER OF GENERATIONS:" + NG);
        logger.log(Level.INFO, "GENETIC ALGORITHM - START");
        for (int k = 0; k < NG; k++) {
            logger.log(Level.INFO, "GENERATION #:" + k);
            logger.log(Level.INFO, "> OPTIMIZING POPULATION PARAMETER");
            generation = learning.optimizeGenerationParameters(pop, variables, ds2Times, positiveTrainingSet, negativeTrainingSet, positiveTestSet, negativeTestSet,atTime);
            generation.sort();
            logger.log(Level.INFO, "-----------------------------");
            logger.log(Level.INFO, "FORMULA GENERATION");
            logger.log(Level.INFO, generation.toString());
            logger.log(Level.INFO, "-----------------------------");
            logger.log(Level.INFO, "> GETTING THE HALF BEST INDIVIDUALS ");
            GeneticPopulation bestHalf = generation.getBestHalf();
            logger.log(Level.INFO, "> APPLYING GENETIC OPERATIONS: OFFSPRING FORMULA GENERATION");
            bestHalf.geneticOperations(random, pop);
            logger.log(Level.INFO, "> OPTIMIZING OFFSPRING FORMULA PARAMETER");
            GeneticPopulation betsHalfGeneration = learning.optimizeGenerationParameters(pop, variables, ds2Times, positiveTrainingSet, negativeTrainingSet, positiveTestSet, negativeTestSet,atTime);
            betsHalfGeneration.sort();
            betsHalfGeneration.getBestHalf();
            logger.log(Level.INFO, "> UNION OFFSPRING - PARENTS");
            generation.addall(betsHalfGeneration);
        }
        generation.sort();
        logger.log(Level.INFO, "GENETIC ALGORITHM - END");
        logger.log(Level.INFO, "-----------------------------");
        logger.log(Level.INFO, "LAST FORMULA GENERATION");
        logger.log(Level.INFO, generation.toString());
        logger.log(Level.INFO, "-----------------------------");

        Formula bestFormula = generation.getBestFromula();
        logger.log(Level.INFO, "REFINING BEST PARAMETER FORMULA");
        double[] bestFormulaParameters = com.eggloop.flow.learning.ComputeAverage.averageMultiTrajectory(200, DISCRIMINATION_FUNCTION, variables, ds2Times, positiveTrainingSet, negativeTrainingSet, bestFormula, pop, new double[]{GeneticOptions.min_time_bound, GeneticOptions.max_time_bound}, atTime);

        System.out.println("__________________________________________________");
        System.out.println("__________________BEST FORMULA____________________");
        System.out.println("__________________________________________________");
        System.out.println(bestFormula.toString());
        System.out.println("Best Formula Parameters:" + Arrays.toString(bestFormula.getParameters()) + ":::::" + Arrays.toString(bestFormulaParameters));
        double[] negativeSetPerformance = smcMultiTrajectories(ds2Times, negativeTestSet, variables, bestFormula, bestFormulaParameters, atTime);
        double[] positiveSetPerformance = smcMultiTrajectories(ds2Times, positiveTestSet, variables, bestFormula, bestFormulaParameters, atTime);
        return new double[][]{negativeSetPerformance, positiveSetPerformance};
    }

}