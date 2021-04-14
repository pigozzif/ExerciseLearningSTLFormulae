package eggloop.flow.gpoptimisation.gpoptim;

import eggloop.flow.numeric.optimization.ObjectiveFunction;

public interface NoisyObjectiveFunction extends ObjectiveFunction {
	
	double getVarianceAt(double... point);

}
