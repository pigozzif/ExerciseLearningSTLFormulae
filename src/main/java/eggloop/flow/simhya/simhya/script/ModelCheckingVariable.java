/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.script;

import eggloop.flow.simhya.simhya.modelchecking.SMCenvironment;

/**
 *
 * @author luca
 */
public class ModelCheckingVariable extends ScriptVariable {
    SMCenvironment checker;

    public ModelCheckingVariable(SMCenvironment checker, String name) {
        super(name);
        this.checker = checker;
    }

    @Override
    boolean isModelCheckingVariable() {
        return true;
    }
    
    
    
}
