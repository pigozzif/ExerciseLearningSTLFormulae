package com.eggloop.flow.learning;

import com.eggloop.flow.expr.Context;
import com.eggloop.flow.expr.Variable;
import com.eggloop.flow.gpoptimisation.gpoptim.GPOptimisation;
import com.eggloop.flow.gpoptimisation.gpoptim.GpoOptions;
import com.eggloop.flow.gpoptimisation.gpoptim.GpoResult;
import com.eggloop.flow.mitl.MiTL;
import com.eggloop.flow.mitl.MitlPropertiesList;
import com.eggloop.flow.model.Trajectory;
import com.eggloop.flow.numeric.optimization.ObjectiveFunction;
import com.eggloop.flow.parsers.MitlFactory;
import com.eggloop.flow.sampler.GridSampler;
import com.eggloop.flow.sampler.Parameter;
import com.eggloop.flow.simhya.simhya.matlab.genetic.Formula;
import com.eggloop.flow.simhya.simhya.matlab.genetic.FormulaPopulation;
import com.eggloop.flow.utils.string.StringUtils;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.IntStream;


public class ComputeAverage {
    public static double[] averageMultiTrajectory(int maxIterations, BiFunction<double[], double[], Double> fitness, String[] variablesUnique, double[] ds2Times, double[][][] normal_model, double[][][] ineffective_model, Formula formula, FormulaPopulation pop, double[] timeBoundsFormula, double atTime) {
        String[] variables = formula.getVariables();
        String[] boundsFormula = formula.getTimeBounds();
        double[] timeBoundsLb = Arrays.stream(boundsFormula).mapToDouble(x -> timeBoundsFormula[0]).toArray();
        double[] timeBoundsUb = Arrays.stream(boundsFormula).mapToDouble(x -> timeBoundsFormula[1]).toArray();
        double[] thrshldLb = Arrays.stream(variables).mapToDouble(pop::getLowerBound).toArray();
        double[] thrshldUb = Arrays.stream(variables).mapToDouble(pop::getUpperBound).toArray();
        double[] lb = new double[boundsFormula.length + variables.length];
        double[] ub = new double[boundsFormula.length + variables.length];
        System.arraycopy(timeBoundsLb, 0, lb, 0, timeBoundsLb.length);
        System.arraycopy(thrshldLb, 0, lb, boundsFormula.length, thrshldLb.length);
        System.arraycopy(timeBoundsUb, 0, ub, 0, timeBoundsUb.length);
        System.arraycopy(thrshldUb, 0, ub, boundsFormula.length, thrshldUb.length);
        ObjectiveFunction function = point -> {
            for (int i = 0; i < boundsFormula.length - 1; i += 2) {
                point[i + 1] = point[i] + point[i + 1] * (1 - point[i]);
            }
            final double[] p = point;
            point = IntStream.range(0, point.length).mapToDouble(i -> lb[i] + p[i] * (ub[i] - lb[i])).toArray();
            double[] value1 = computeAverageRobustnessMultiTrajectory(ds2Times, normal_model, variablesUnique, formula, point, atTime);
            double[] value2 = smcMultiTrajectories(ds2Times, normal_model, variablesUnique, formula, point, atTime);//computeAverageRobustnessMultiTrajectory(ds2Times, ineffective_model, variablesUnique, formula, point, atTime);
            //double abs = fitness.apply(value1, value2);
            double abs = - (value2[0] + value1[0]);
            if (Double.isNaN(abs)) {
                return 0;
            }
            return abs;
        };
        GridSampler custom = new GridSampler() {
            @Override
            public double[][] sample(int n, double[] lbounds, double[] ubounds) {
                double[][] res = new double[n][lbounds.length];
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < boundsFormula.length; j += 2) {
                        res[i][j] = lbounds[j] + Math.random() * (ubounds[j] - lbounds[j]);
                        res[i][j + 1] = res[i][j] + (Math.random()) * (ubounds[j] - res[i][j]);
                    }
                    for (int j = boundsFormula.length; j < res[i].length; j++) {
                        res[i][j] = lbounds[j] + Math.random() * (ubounds[j] - lbounds[j]);
                    }
                }
                return res;
            }
            @Override
            public double[][] sample(int n, Parameter[] params) {
                return new double[0][];
            }
        };
        GPOptimisation gpo = new GPOptimisation();
        GpoOptions options = new GpoOptions();
        options.setInitialSampler(custom);
        options.setMaxIterations(maxIterations);
        options.setHyperparamOptimisation(true);
        options.setUseNoiseTermRatio(true);
        options.setNoiseTerm(0);
        options.setGridSampler(custom);
        options.setGridSampleNumber(200);
        gpo.setOptions(options);
        GpoResult optimise;
        double[] lbU = IntStream.range(0, lb.length).mapToDouble(i -> 0).toArray();
        double[] ubU = IntStream.range(0, ub.length).mapToDouble(i -> 1).toArray();
        optimise = gpo.optimise(function, lbU, ubU);
        double[] v = optimise.getSolution();
        double[] vv = IntStream.range(0, v.length).mapToDouble(i -> lb[i] + v[i] * (ub[i] - lb[i])).toArray();
        double[] p1u1 = computeAverageRobustnessMultiTrajectory(ds2Times, normal_model, variablesUnique, formula, vv, atTime);
        double[] p2u2 = computeAverageRobustnessMultiTrajectory(ds2Times, ineffective_model, variablesUnique, formula, vv, atTime);
        double value;
        if (p1u1[0] > p2u2[0]) {
            value = ((p1u1[0] - p1u1[1]) + (p2u2[0] + p2u2[1])) / 2;
        } else {
            value = ((p2u2[0] - p2u2[1]) + (p1u1[0] + p1u1[1])) / 2;
        }
        char[] a = formula.toSign().toCharArray();
        for (int i = timeBoundsLb.length; i < vv.length; i++) {
            if (a[i - timeBoundsLb.length] == '1') {
                vv[i] = Math.max(vv[i] + value, 0);
            } else {
                vv[i] = Math.max(vv[i] - value, 0);
            }
        }
        return vv;
    }

    public static double[] computeAverageRobustnessMultiTrajectory(double[] times, double[][][] simulate, String[] variables, Formula formula, double[] parametersValues, double atTime) {
        double[] b = new double[simulate.length];
        Context ns = new Context();
        for (String s : variables) {
            new Variable(s, ns);
        }
        String[] parameterNames = formula.getParameters();
        MitlFactory factory = new MitlFactory(ns);
        String text = StringUtils.replace(formula.toString(), parameterNames, parametersValues) + "\n";
        MitlPropertiesList l = factory.constructProperties(text);
        MiTL prop = l.getProperties().get(0);
        for (int i = 0; i < simulate.length; i++) {
            Trajectory x = new Trajectory(times, ns, simulate[i]);
            b[i] = Math.abs(prop.evaluateValue(x, atTime));
            //b[i] = prop.evaluateValue(x, atTime);
        }
        double mean = Arrays.stream(b).sum() / b.length;
        double variance = Math.sqrt(Arrays.stream(b).map(x -> (x - mean) * (x - mean)).sum() / b.length);
        return new double[]{mean, variance};
    }

    public static double[] smcMultiTrajectories(double[] times, double[][][] trajectories, String[] variables, Formula formula, double[] formulaParameters, double atTime) {
        double[] b = new double[trajectories.length];
        Context ns = new Context();
        for (String s : variables) {
            new Variable(s, ns);
        }
        String[] parameterNames = formula.getParameters();
        MitlFactory factory = new MitlFactory(ns);
        String text = StringUtils.replace(formula.toString(), parameterNames, formulaParameters) + "\n";
        MitlPropertiesList l = factory.constructProperties(text);
        MiTL prop = l.getProperties().get(0);

        for (int i = 0; i < trajectories.length; i++) {
            Trajectory x = new Trajectory(times, ns, trajectories[i]);
            b[i] = prop.evaluate(x, atTime) ? 0 : 1;
            //b[i] = prop.evaluate(x, atTime) ? 1 : 0;
        }
        double mean = Arrays.stream(b).sum() / b.length;
        double variance = Arrays.stream(b).map(x -> (x - mean) * (x - mean)).sum() / b.length;
        return new double[]{mean, variance};
    }

    public static double[] smcMultiTrajectoriesFinal(double[] times, double[][][] trajectories, String[] variables, Formula formula, double[] formulaParameters, double atTime) {
        double[] b = new double[trajectories.length];
        Context ns = new Context();
        for (String s : variables) {
            new Variable(s, ns);
        }
        String[] parameterNames = formula.getParameters();
        MitlFactory factory = new MitlFactory(ns);
        String text = StringUtils.replace(formula.toString(), parameterNames, formulaParameters) + "\n";
        MitlPropertiesList l = factory.constructProperties(text);
        MiTL prop = l.getProperties().get(0);

        for (int i = 0; i < trajectories.length; i++) {
            Trajectory x = new Trajectory(times, ns, trajectories[i]);
            b[i] = prop.evaluate(x, atTime) ? 1 : 0;
        }
        double mean = Arrays.stream(b).sum() / b.length;
        double variance = Arrays.stream(b).map(x -> (x - mean) * (x - mean)).sum() / b.length;
        return new double[]{mean, variance};
    }

}