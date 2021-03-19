package com.eggloop.flow.utils.data;

public interface DataSetSplit {
    double[][][] getPositiveTrainingSet();

    double[][][] getNegativeTrainingSet();

    double[][][] getPoistiveTestSet();

    double[][][] getNegativeTestSet();

    double[] getTimes();
}
