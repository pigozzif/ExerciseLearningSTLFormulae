package com.eggloop.flow.utils.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TrajectoryMultiReconstructionCV implements DataSetSplit {
    private final double[] ds2Times;
    private final double[][][] ds2SpatialValues;
    private final double[] ds2Labels;
    private final Random random;
    private double[][][] poistiveTrainSet;
    private double[][][] poistiveValidationSet;
    private double[][][] negativeTrainSet;
    private double[][][] negativeValidationSet;

    public TrajectoryMultiReconstructionCV(double[] ds2Times, double[][][] ds2SpatialValues, double[] ds2Labels, Random random) {
        this.ds2Times = ds2Times;
        this.ds2SpatialValues = ds2SpatialValues;
        this.ds2Labels = ds2Labels;
        this.random = random;
    }

    public void split(int n, int fold) {
        List<Integer> positiveTrajectories = new ArrayList<>();
        List<Integer> negativeTrajectories = new ArrayList<>();
        for (int i = 0; i < ds2Labels.length; i++) {
            if (ds2Labels[i] == -1) {
                negativeTrajectories.add(i);
            } else if (ds2Labels[i] == 1) {
                positiveTrajectories.add(i);
            }
        }

        Collections.shuffle(positiveTrajectories, random);
        Collections.shuffle(negativeTrajectories, random);

        int positivelen = positiveTrajectories.size();
        int negativelen = negativeTrajectories.size();


        int trainPositiveSize = (int) ((double) positivelen * (fold - 1) / fold);
        int trainNegativeSize = (int) ((double) negativelen * (fold - 1) / fold);

        if (fold % 2 == 0) {
            trainPositiveSize++;
            trainNegativeSize++;
        }
        int validationPositiveSize = positivelen - trainPositiveSize;
        int validationNegativeSize = negativelen - trainNegativeSize;
        poistiveTrainSet = new double[trainPositiveSize][][];
        negativeTrainSet = new double[trainNegativeSize][][];
        poistiveValidationSet = new double[validationPositiveSize][][];
        negativeValidationSet = new double[validationNegativeSize][][];
        for (int i = 0; i < (positivelen / fold) * n; i++) {
            poistiveTrainSet[i] = ds2SpatialValues[positiveTrajectories.get(i)];
        }
        for (int i = (positivelen / fold) * n; i < (positivelen / fold) * (n + 1); i++) {
            poistiveValidationSet[i - (positivelen / fold) * n] = ds2SpatialValues[positiveTrajectories.get(i)];
        }
        for (int i = (positivelen / fold) * (n + 1); i < positivelen; i++) {
            poistiveTrainSet[i - (positivelen / fold)] = ds2SpatialValues[positiveTrajectories.get(i)];
        }

        for (int i = 0; i < (negativelen / fold) * n; i++) {
            negativeTrainSet[i] = ds2SpatialValues[negativeTrajectories.get(i)];
        }
        for (int i = (negativelen / fold) * n; i < (negativelen / fold) * (n + 1); i++) {
            negativeValidationSet[i - (negativelen / fold) * n] = ds2SpatialValues[negativeTrajectories.get(i)];
        }
        for (int i = (negativelen / fold) * (n + 1); i < negativelen; i++) {
            negativeTrainSet[i - (negativelen / fold)] = ds2SpatialValues[negativeTrajectories.get(i)];
        }

    }

    public double[][][] getPositiveTrainingSet() {
        return poistiveTrainSet;
    }

    public double[][][] getNegativeTrainingSet() {
        return negativeTrainSet;
    }

    public double[][][] getPoistiveTestSet() {
        return poistiveValidationSet;
    }

    public double[][][] getNegativeTestSet() {
        return negativeValidationSet;
    }

    public double[] getTimes() {
        return ds2Times;
    }

}
