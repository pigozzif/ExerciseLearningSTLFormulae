package com.eggloop.flow.gaussianprocesses.gp.kernels;

import com.eggloop.flow.gaussianprocesses.gp.GpDataset;

public interface KernelFunction {

	double calculate(final double[] x1, final double[] x2);

	double calculateDerivative(final double[] x1,
							   final double[] x2, final int derivativeI);

	double calculateSecondDerivative(final double[] x1,
									 final double[] x2, final int derivativeI, final int derivativeJ);

	double calculateHyperparamDerivative(final double[] x1,
										 final double[] x2, final int hyperparamIndex);

	double[] getHypeparameters();

	double[] getDefaultHyperarameters(GpDataset data);

	void setHyperarameters(double[] hyp);

}
