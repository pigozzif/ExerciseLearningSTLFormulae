/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.script;

import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.modelchecking.SMCoutput;

/**
 *
 * @author luca
 */
public class SMCoutputVariable extends ScriptVariable {
    ArrayList<SMCoutput> outputList = null;

    public SMCoutputVariable(String name, SMCoutput out) {
        super(name);
        this.outputList = new ArrayList<SMCoutput> (); 
        this.outputList.add(out);
    }

     public SMCoutputVariable(String name, ArrayList<SMCoutput> list) {
        super(name);
        this.outputList = list;
    }

    @Override
    boolean isModelCheckingOutputVariable() {
        return true;    }
     
    
    boolean isList() {
        return outputList != null && outputList.size() > 0 ;
    }
     
    
    
}
