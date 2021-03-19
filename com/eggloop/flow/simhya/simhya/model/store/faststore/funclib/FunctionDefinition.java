/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.model.store.faststore.funclib;

import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.model.store.*;
import com.eggloop.flow.simhya.simhya.matheval.SymbolArray;

/**
 *
 * @author Luca
 */
class FunctionDefinition {
    String name;
    int args;
    ArrayList<ArgType> argTypes;

    FunctionDefinition(String name) {
        this.name = name;
        args  = 0;
        argTypes = new ArrayList();
    }

    void addArgument(ArgType type) {
        args++;
        argTypes.add(type);
    }

    boolean checkArgument(int i,String arg,Store store) {
        ArgType type = argTypes.get(i);
        switch(type) {
            case PARAMETER:
                return store.getParametersReference().containsSymbol(arg);
            case VARIABLE:
                return store.getVariablesReference().containsSymbol(arg);
            case NUMBER:
                try { Double.parseDouble(arg); return true; }
                catch (NumberFormatException e) { return false; }
        }
        return false;
    }

    boolean checkArgument(int i,String arg,Store store,SymbolArray localVars) {
        ArgType type = argTypes.get(i);
        switch(type) {
            case PARAMETER:
                return store.getParametersReference().containsSymbol(arg);
            case VARIABLE:
                return (localVars.containsSymbol(arg) || store.getVariablesReference().containsSymbol(arg));
            case NUMBER:
                try { Double.parseDouble(arg); return true; }
                catch (NumberFormatException e) { return false; }
        }
        return false;
    }

    ArgType getType(int i) {
        return argTypes.get(i);
    }

}
