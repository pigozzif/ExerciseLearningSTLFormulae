/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine;
import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
import com.eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import com.eggloop.flow.simhya.simhya.dataprocessing.OdeDataCollector;
import com.eggloop.flow.simhya.simhya.dataprocessing.HybridDataCollector;
//import com.eggloop.flow.simhya.simhya.simengine.hybrid.HybridSimulatorWithDynamicSwitching;
import com.eggloop.flow.simhya.simhya.simengine.ode.OdeSimulator;
//import com.eggloop.flow.simhya.simhya.simengine.ode.OdeSimulatorWithEvents;
import com.eggloop.flow.simhya.simhya.simengine.stochastic.*;
import com.eggloop.flow.simhya.simhya.simengine.stochastic.PureGuardedSSASimulator;
import com.eggloop.flow.simhya.simhya.GlobalOptions;

/**
 *
 * @author Luca
 */
public class SimulatorFactory {


    public static Simulator newSimulator(SimType type, FlatModel model, DataCollector collector) {
        switch (type) {
            case SSA:
                return SimulatorFactory.newSSAsimulator(model, collector);
            case GB:
                return SimulatorFactory.newGBsimulator(model, collector);
            case ODE:
                return SimulatorFactory.newODEsimulator(model, collector);
            case HYBRID:
                return SimulatorFactory.newHybridSimulator(model, collector);
            default:
                throw new UnsupportedOperationException(type.toString() + " to be implemented");
        }
    }

    public static StochasticSimulator newSSAsimulator(FlatModel model, DataCollector collector) {
        if (model.containsDelayedTransitions())
            return new SSASimulatorWithDelays(model, collector);
        else if (model.containsInstantaneousTransitions() || model.containsTimedTransitions())
            return new SSASimulator(model, collector);
        else if (model.containsGuardedTransitions()) {
            PureGuardedSSASimulator sim = new PureGuardedSSASimulator(model, collector);
            if (model.getNumberOfTransitions() - model.getAverageNumberOfTransitionsToUpdate()
                    <= GlobalOptions.thresholdForUsingDependencyGraph)
                sim.setUseDependencyGraph(false);
            return sim;
        }
        else {
            PureSSASimulator sim  = new PureSSASimulator(model, collector);
            if (model.getNumberOfTransitions() - model.getAverageNumberOfTransitionsToUpdate()
                    <= GlobalOptions.thresholdForUsingDependencyGraph)
                sim.setUseDependencyGraph(false);
            return sim;
        }
        /*
         * TODO: heuristic to deactivate dependency graph in pure SSA simulator/PureGuardedSSA
         */
    }

    public static StochasticSimulator newGBsimulator(FlatModel model, DataCollector collector) {
        if (model.containsDelayedTransitions())
            return new GBSimulatorWithDelays(model, collector);
        else if (model.containsInstantaneousTransitions() || model.containsTimedTransitions())
            return new GBSimulator(model, collector);
        else if (model.containsGuardedTransitions())
            return new PureGuardedGBSimulator(model, collector);
        else
            return new PureGBSimulator(model, collector);
    }


    public static DeterministicSimulator newODEsimulator(FlatModel model, DataCollector collector) {
        if (!(collector instanceof OdeDataCollector))
            throw new SimulationException("Data Collector is of the wrong type");
        if (model.containsNonContinuouslyApproximableStochasticTransitions())
            throw new SimulationException("There are stochastic transitions not continously approximable in the model, use hybrid simulator");
//        if (model.containsInstantaneousTransitions() || model.containsTimedTransitions())
//            return new OdeSimulatorWithEvents(model, (OdeDataCollector)collector);
        else return new OdeSimulator(model, (OdeDataCollector)collector);
    }


    /**
     * Generates a hybrid simulator with local switching and rule based global switching
     * @param model
     * @param collector
     * @return
     */
    public static HybridSimulator newHybridSimulator(FlatModel model, DataCollector collector) {
//        if (!(collector instanceof HybridDataCollector))
//            throw new SimulationException("Data Collector is of the wrong type");
//        return new HybridSimulatorWithDynamicSwitching(model, (HybridDataCollector)collector, true);
   return null;
    }


    /**
     * Generates a hybrid simulator with local switching and rule based global switching or
     * no switching at all
     * @param model
     * @param collector
     * @param switching
     * @return
     */
    public static HybridSimulator newHybridSimulator(FlatModel model, DataCollector collector, boolean switching) {
        if (!(collector instanceof HybridDataCollector))
            throw new SimulationException("Data Collector is of the wrong type");
//        return new HybridSimulatorWithDynamicSwitching(model, (HybridDataCollector)collector, switching);
 return null;
    }

    /**
     * Generates a hybrid simulator with local switching, rule based global switching,
     * and population based global switching rules.
     * There is a condition on multiplicity of a variable. A variable above the
     * a transition are continuous, the transition itself is continuous
     * It is required that continuous2discreteThreshold < discrete2continuousThreshold.
     *
     * @param model
     * @param collector
     * @param discrete2continuousThreshold threshold to switch from a discrete to a continuous variable
     * @param continuous2discreteThreshold threshold to switch from a a continuous to a discrete variable
     * @return
     */
    public static HybridSimulator newHybridSimulator(FlatModel model, DataCollector collector,
            double discrete2continuousThreshold, double continuous2discreteThreshold) {
        if (!(collector instanceof HybridDataCollector))
            throw new SimulationException("Data Collector is of the wrong type");
//        return new HybridSimulatorWithDynamicSwitching(model, (HybridDataCollector)collector,
//                discrete2continuousThreshold, continuous2discreteThreshold);
        return null;
    }

}





