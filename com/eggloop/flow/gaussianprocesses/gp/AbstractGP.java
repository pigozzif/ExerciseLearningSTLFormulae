package com.eggloop.flow.gaussianprocesses.gp;

import com.eggloop.flow.numeric.algebra.linalg.JblasAlgebra;
import com.eggloop.flow.numeric.algebra.linalg.IAlgebra;
import com.eggloop.flow.numeric.algebra.linalg.NonPosDefMatrixException;
import com.eggloop.flow.gaussianprocesses.gp.kernels.KernelFunction;

public abstract class AbstractGP<PosteriorType extends GpPosterior> {

	final protected IAlgebra algebra;
	final private KernelFunction kernelFunction;
	protected GpDataset trainingSet = new GpDataset(1);

	public AbstractGP(KernelFunction kernelFunction) {
		this(new JblasAlgebra(), kernelFunction);
	}

	public AbstractGP(IAlgebra algebra, KernelFunction kernelFunction) {
		this.algebra = algebra;
		this.kernelFunction = kernelFunction;
	}

	public KernelFunction getKernel() {
		return kernelFunction;
	}

	public GpDataset getTrainingSet() {
		return trainingSet;
	}

	public void setTrainingSet(GpDataset trainingSet) {
		this.trainingSet = trainingSet;
	}

	abstract public PosteriorType getGpPosterior(GpDataset testSet)
			throws NonPosDefMatrixException;

	abstract public double getMarginalLikelihood()
			throws NonPosDefMatrixException;

	abstract public double[] getMarginalLikelihoodGradient()
			throws NonPosDefMatrixException;

}
