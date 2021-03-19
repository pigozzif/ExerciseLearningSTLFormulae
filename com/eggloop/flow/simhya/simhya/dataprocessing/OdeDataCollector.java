/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.dataprocessing;


//import java.util.Locale;

import com.eggloop.flow.simhya.simhya.model.flat.FlatModel;
//import org.apache.commons.math.ode.sampling.StepInterpolator;
//import org.apache.commons.math.ode.DerivativeException;
//import java.util.ArrayList;
//import java.io.PrintStream;

/**
 * @author luca
 */
public class OdeDataCollector extends AbstractContinuousDataCollector {

    public OdeDataCollector(FlatModel model) {
        super(model);
    }

    public TrajectoryStatistics getTrajectoryStatistics() {
        throw new UnsupportedOperationException("Statistics computation not supported by ODE data");
    }


}
