package com.eggloop.flow.utils.data;

import java.util.Random;

public class Bootstrap {

public static double[][] bootstrap(int n, double[][] matrix, Random ran){
    double[][] res = new double[n][];
    for (int i = 0; i < n; i++) {
        int index = ran.nextInt(matrix.length);
        res[i]=matrix[index];
    }
    return res;

}
}
