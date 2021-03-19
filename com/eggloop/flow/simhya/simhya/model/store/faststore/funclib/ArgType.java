/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.store.faststore.funclib;

/**
 *
 * @author Luca
 */
public enum ArgType {
        PARAMETER, VARIABLE, NUMBER;

    @Override
    public String toString() {
        switch(this) {
            case PARAMETER:
                return "parameter";
            case VARIABLE:
                return "variable";
            case NUMBER:
                return "number";
        }
        return null;
    }
}
