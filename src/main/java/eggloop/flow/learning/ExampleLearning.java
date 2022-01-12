package eggloop.flow.learning;

import eggloop.flow.simhya.simhya.matlab.genetic.Formula;
import eggloop.flow.simhya.simhya.matlab.genetic.FormulaPopulation;
import eggloop.flow.simhya.simhya.matlab.genetic.GeneticOptions;
import eggloop.flow.utils.data.DataSetSplit;
import eggloop.flow.utils.data.TrajectoryMultiReconstruction;
import eggloop.flow.utils.data.TrajectoryMultiReconstructionCV;
import eggloop.flow.utils.files.Utils;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eggloop.flow.learning.ComputeAverage.*;


public class ExampleLearning {
    private static Learning learning;
    private static final Logger LOGGER = LoggerFactory.get();
    // root directory where your data will be found
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/main/java/eggloop/flow/data/";
    // discrimination function to be used as objective function
    private static final BiFunction<double[], double[], Double> DISCRIMINATION_FUNCTION = (x, y) -> (x[0] - y[0]) / (Math.abs(x[1] + y[1]));
    // file with labels, +1 for positive, -1 for negative
    private static double[] ds2Labels = Utils.readVectorFromFile(FILE_PATH + "trainLabels.csv");
    // file with times for the trajectories' samples (for the ith sample, the time is the same across all trajectories)
    private static double[] ds2Times = Utils.readVectorFromFile(FILE_PATH + "trainTimes.csv");
    // file with data, one row for each trajectory. If there are (x_1, x_2) variables in the dataset, each row will look like this:
    // x_1^1, x_2^1, x_1^2, x_2^2, x_1^3, x_2^3, ..., x_1^T, x_2^T
    // for a total of T timesteps, and using the superscript to denote the current timestep
    private static double[][][] ds2SpatialValues = Utils.readMatrixMultiFromFile(ds2Times.length, FILE_PATH + "trainData.csv");
    private static long TIMEOUT_SECONDS = 15 * 60; // 15 minutes

    public static void main(String[] args) {
        int seed = 0; // current seed
        int count = 0; // number of successful runs
        while (count < 10) {
            LOGGER.setLevel(Level.FINE);
            learning = new Learning(LOGGER, DISCRIMINATION_FUNCTION);
            //learn(new Random(seed), LOGGER);
            Random random = new Random(seed);
            try {
                CompletableFuture.supplyAsync(() -> learnCV(random, LOGGER)).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                count += 1;
            }
            catch (Exception ignored) {
                seed += 1;
                LOGGER.log(Level.INFO, "RUN TIMEOUT");
            }
        }
    }

    private static void learn(Random random, Logger logger) {
        // load data, and split them into train/test sets according to the following percentage:
        double trainPercent = 0.8;
        TrajectoryMultiReconstruction data = new TrajectoryMultiReconstruction(ds2Times, ds2SpatialValues, ds2Labels, trainPercent, random);
        data.split();
        // run main algorithm and log performance
        double[][] performances = core(random, data, logger);
        System.out.println("Classified as Negative Trajectories [percentage, variance] = " + Arrays.toString(performances[0]));
        System.out.println("Classified as Positive Trajectories [percentage, variance] = " + Arrays.toString(performances[1]));
    }

    private static int learnCV(Random random, Logger logger) {
        // load data, and split them into train/test sets folds using k-fold approach
        TrajectoryMultiReconstructionCV data = new TrajectoryMultiReconstructionCV(ds2Times, ds2SpatialValues, ds2Labels, random);
        data.split(3, 5);
        // run main algorithm and log performance
        double[][] performances = core(random, data, logger);
        System.out.println("Classified as Negative Trajectories [percentage, variance] = " + Arrays.toString(performances[0]));
        System.out.println("Classified as Positive Trajectories [percentage, variance] = " + Arrays.toString(performances[1]));
        return 0;
    }

    private static double[][] core(Random random, DataSetSplit data, Logger logger) {
        // partition dataset into positive and negative training/test sets
        double[][][] positiveTrainingSet = data.getPositiveTrainingSet();
        double[][][] negativeTrainingSet = data.getNegativeTrainingSet();
        double[][][] positiveTestSet = data.getPoistiveTestSet();
        double[][][] negativeTestSet = data.getNegativeTestSet();
        // load times
        double[] ds2Times = data.getTimes();
        int NE = 50;   // number of formulae in the initial population
        FormulaPopulation pop = new FormulaPopulation(NE);
        // TODO:
        // the variables in the dataset. You should list them as a String[]
        String[] variables = new String[]{"x"};

        // TODO:
        // when to compute robustness and lower/upper bounds of variables' range of variation
        double atTime = 0;  // when to compute robustness
        double[] lower = new double[]{3.2398};//{9.6054};//{0.07991119774311442};
        double[] upper = new double[]{80.081};//{48.758};//{0.9103496717930831};
        //double[] lower = new double[]{0, 0};
        //double[] upper = new double[]{80, 45};

        logger.log(Level.INFO, "Formula Variables:" + Arrays.toString(variables));
        logger.log(Level.INFO, "Formula Variables Lower Bounds:" + Arrays.toString(lower));
        logger.log(Level.INFO, "Formula Variables Upper Bounds:" + Arrays.toString(upper));

        for (int i = 0; i < variables.length; i++) {
            pop.addVariable(variables[i], lower[i], upper[i]);
        }

        // set genetic options
        // TODO: set maximum and minimum time bound
        GeneticOptions.setInit__prob_of_true_atom(0);
        GeneticOptions.setMin_time_bound(0);
        GeneticOptions.setMax_time_bound(99);

        // adding initial Formulae (atomic + G + F + U)
        pop.addGeneticInitFormula(pop.getVariableNumber());

        // adding random formulae
        double maxNumRand = 10;
        for (int i = 0; i < maxNumRand; i++) {
            pop.addRandomInitFormula();
        }
        GeneticPopulation generation = null;
        int NG = 3;  // number of generations to run evolutionary algorithm
        logger.log(Level.INFO, "NUMBER OF GENERATIONS:" + NG);
        logger.log(Level.INFO, "GENETIC ALGORITHM - START");
        for (int k = 0; k < NG; k++) {
            logger.log(Level.INFO, "GENERATION #:" + k);
            logger.log(Level.INFO, "> OPTIMIZING POPULATION PARAMETER");
            generation = learning.optimizeGenerationParameters(pop, variables, ds2Times, positiveTrainingSet, negativeTrainingSet, positiveTestSet, negativeTestSet, atTime);
            generation.sort();
            logger.log(Level.INFO, "-----------------------------");
            logger.log(Level.INFO, "FORMULA GENERATION");
            //logger.log(Level.INFO, generation.toString());
            logger.log(Level.INFO, "-----------------------------");
            logger.log(Level.INFO, "> GETTING THE HALF BEST INDIVIDUALS ");
            GeneticPopulation bestHalf = generation.getBestHalf();
            logger.log(Level.INFO, "> APPLYING GENETIC OPERATIONS: OFFSPRING FORMULA GENERATION");
            bestHalf.geneticOperations(random, pop);
            logger.log(Level.INFO, "> OPTIMIZING OFFSPRING FORMULA PARAMETER");
            GeneticPopulation betsHalfGeneration = learning.optimizeGenerationParameters(pop, variables, ds2Times, positiveTrainingSet, negativeTrainingSet, positiveTestSet, negativeTestSet, atTime);
            betsHalfGeneration.sort();
            betsHalfGeneration.getBestHalf();
            logger.log(Level.INFO, "> UNION OFFSPRING - PARENTS");
            generation.addall(betsHalfGeneration);
        }
        generation.sort();
        logger.log(Level.INFO, "GENETIC ALGORITHM - END");
        logger.log(Level.INFO, "-----------------------------");
        logger.log(Level.INFO, "LAST FORMULA GENERATION");
        //logger.log(Level.INFO, generation.toString());
        logger.log(Level.INFO, "-----------------------------");

        Formula bestFormula = generation.getBestFromula();
        logger.log(Level.INFO, "REFINING BEST PARAMETER FORMULA");
        double[] bestFormulaParameters = ComputeAverage.averageMultiTrajectory(100, DISCRIMINATION_FUNCTION, variables, ds2Times, positiveTrainingSet, negativeTrainingSet, bestFormula, pop, new double[]{GeneticOptions.min_time_bound, GeneticOptions.max_time_bound}, atTime);

        System.out.println("__________________________________________________");
        System.out.println("__________________BEST FORMULA____________________");
        System.out.println("__________________________________________________");
        System.out.println(bestFormula.toString());
        System.out.println("Best Formula Parameters:" + Arrays.toString(bestFormula.getParameters()) + ":::::" + Arrays.toString(bestFormulaParameters));
        double[] negativeSetPerformance = smcMultiTrajectoriesFinal(ds2Times, negativeTestSet, variables, bestFormula, bestFormulaParameters, atTime);
        double[] positiveSetPerformance = smcMultiTrajectoriesFinal(ds2Times, positiveTestSet, variables, bestFormula, bestFormulaParameters, atTime);
        return new double[][]{negativeSetPerformance, positiveSetPerformance};
    }

}