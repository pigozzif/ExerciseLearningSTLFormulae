/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine;

/**
 *
 * @author Luca
 */
public enum SimType {
    SSA, GB, ODE, HYBRID, LN;

    @Override
    public String toString() {
        switch(this) {
            case SSA:
                return "SSA (Gillespie)";
            case GB:
                return "Gibson-Bruck";
            case ODE:
                return "ODE integrator";
            case HYBRID:
                return "Hybrid Simulation";
            case LN:
                return "Linear Noise";
            default:
                throw new IllegalArgumentException("Unknown simulation type");
        }
    }

    public boolean isStochastic() {
        switch(this) {
            case SSA:
                return true;
            case GB:
                return true;
            case ODE:
                return false;
            case HYBRID:
                return false;
            case LN:
                return false;
            default:
                throw new IllegalArgumentException("Unknown simulation type");
        }
    }


    public boolean isHybrid() {
        switch(this) {
            case SSA:
                return false;
            case GB:
                return false;
            case ODE:
                return false;
            case HYBRID:
                return true;
            case LN:
                return false;
            default:
                throw new IllegalArgumentException("Unknown simulation type");
        }
    }


    public boolean isDeterministic() {
        switch(this) {
            case SSA:
                return false;
            case GB:
                return false;
            case ODE:
                return true;
            case HYBRID:
                return false;
            case LN:
                return true;
            default:
                throw new IllegalArgumentException("Unknown simulation type");
        }
    }
}
