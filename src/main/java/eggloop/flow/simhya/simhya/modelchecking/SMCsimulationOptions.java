/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

import eggloop.flow.simhya.simhya.simengine.SimType;
import eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;

/**
 *
 * @author luca
 */
public class SMCsimulationOptions {
    public double finalTime;
    public SimType simType = SimType.SSA; 
    public IntegratorType integrator = IntegratorType.DP85;
    public double stepSize = 0.1;
    
    public boolean continuousOutput() {
        return simType == SimType.HYBRID || simType == SimType.ODE || simType == SimType.LN;
    }
}
