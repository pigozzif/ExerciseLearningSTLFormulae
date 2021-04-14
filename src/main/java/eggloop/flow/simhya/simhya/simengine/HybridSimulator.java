/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine;

import eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;

/**
 *
 * @author luca
 */
public interface HybridSimulator extends DeterministicSimulator {

    public long getLastSimulationNumberOfStochasticEvents();
    public void addGlobalSwitchingRule(String rule);
    public void addGlobalSwitchingRuleFromFile(String filename);
    
    
}
