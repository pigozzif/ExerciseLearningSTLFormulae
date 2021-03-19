/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.ode;

import com.eggloop.flow.simhya.simhya.dataprocessing.OdeDataCollector;
import com.eggloop.flow.simhya.simhya.model.flat.*;
import com.eggloop.flow.simhya.simhya.simengine.SimulationException;

/**
 *
 * @author luca
 */
public class OdeSimulator extends AbstractOdeSimulator {

    public OdeSimulator(FlatModel model, OdeDataCollector collector) {
        super(model, collector);
        //ignore delays and continue
//        if (model.containsDelayedTransitions())
//            throw new SimulationException("Ode simulators do not support delays");
        if (model.containsInstantaneousTransitions() || model.containsTimedTransitions())
            throw new SimulationException("Wrong integrator");
        if (model.containsNonContinuouslyApproximableStochasticTransitions())
            throw new SimulationException("There are stochastic transitions not continously approximable, use hybrid simulator");
        if (model.isLinearNoiseApproximation()) {
            LinearNoiseFlatModel lnModel = (LinearNoiseFlatModel)model;
            this.function = new LinearNoiseOdeFunction(lnModel);
        }
        else if (model.containsGuardedTransitions())
            this.function = new GuardedOdeFunction(model);
        else
            this.function = new PureOdeFunction(model);
    }

    public void initialize() {
        integrator = super.newIntegrator();
        integrator.addStepHandler(this);
        initialized = true;
        collector.collectEventData(false);
    }

    public void reinitialize() {
        integrator = super.newIntegrator();
        integrator.addStepHandler(this);
        initialized = true;
        collector.collectEventData(false);
    }





    


}
