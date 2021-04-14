/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine.ode;

import java.util.ArrayList;

/**
 *
 * @author luca
 */
public enum IntegratorType {
    DP85, RK4, EULER;

    @Override
    public String toString() {
        switch(this) {
            case DP85:
                return "Dormant-Prince 8(5)";
                
            case RK4:
                return "Runge-Kutta 4 (fixed stepsize)";
            case EULER :
                return "Euler (first order)";
            default:
                throw new IllegalArgumentException("Unknown integrator type");
        }
    }
    
    public String toCode() {
        switch(this) {
            case DP85:
                return "DP85";
                
            case RK4:
                return "RK4";
            case EULER:
                return "EULER";
            default:
                throw new IllegalArgumentException("Unknown integrator type");
        }
    }


    public boolean isAdaptive() {
        switch(this) {
            case DP85:
                return true;

            case RK4:
                return false;
            default:
                throw new IllegalArgumentException("Unknown integrator type");
        }
    }
    
    
    public static ArrayList<String> getCodeList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(IntegratorType.DP85.toCode());
        list.add(IntegratorType.RK4.toCode());
        return list;
    }
    
    public static ArrayList<String> getDescriptionList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(IntegratorType.DP85.toString());
        list.add(IntegratorType.RK4.toString());
        return list;
    }
    
}
