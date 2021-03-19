package com.eggloop.flow.gpoptimisation.gpoptim;

import com.eggloop.flow.numeric.optimization.ObjectiveFunction;

public interface NoisyObjectiveFunction extends ObjectiveFunction {
	
	double getVarianceAt(double... point);

}
