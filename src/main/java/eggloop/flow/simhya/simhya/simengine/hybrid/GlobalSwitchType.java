/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.simengine.hybrid;

/**
 *
 * @author luca
 */
public enum GlobalSwitchType {
    NO_SWITCH,RULES,POPULATION,RATE,POPULATION_AND_RATE;

    @Override
    public String toString() {
        switch(this) {
            case NO_SWITCH:
                return "no switch";
            case RULES:
                return "rules only";
            case POPULATION:
                return "rules and population thresholds";
            case RATE:
                return "rules and rate thresholds";
            case POPULATION_AND_RATE: 
                return "rules and population and rate thresholds";
            default:
                return "unknown";
        }
    }
}
