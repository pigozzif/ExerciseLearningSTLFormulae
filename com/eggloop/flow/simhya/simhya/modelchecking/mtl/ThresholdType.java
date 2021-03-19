/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking.mtl;

/**
 *
 * @author Luca
 */
public enum ThresholdType {
    LESS, LESS_EQUAL, GREATER, GREATER_EQUAL;

    @Override
    public String toString() {
        switch(this) {
            case LESS:
                return "<";
            case LESS_EQUAL:
                return "<=";
            case GREATER:
                return ">";
            case GREATER_EQUAL:
                return ">=";
            default:
                return "";
        }
    }
    
    
}