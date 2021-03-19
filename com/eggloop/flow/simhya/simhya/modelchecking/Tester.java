/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking;

import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.matlab.SimHyAModel;

/**
 *
 * @author luca
 */
public class Tester {
     public static void main(String[] args) throws Exception {
        SimHyAModel m = new SimHyAModel();
        m.loadModel("SIR_M.txt");
        SMCenvironment checker = new SMCenvironment(m.getModel());
        checker.loadEnvironment("sir_F.txt");
        
        System.out.println("Loaded model and formulae!");
        
        checker.options.estimationType = EstimationMethod.FREQUENTIST_ADAPTIVE;
        checker.options.confidence = 0.95;
        checker.options.error = 0.05;
        checker.options.type = SMCtype.POINTWISE;
          
        
        ArrayList<String> formulae = new  ArrayList<String>();
        formulae.add("extinction");
        formulae.add("stableR");
        formulae.add("peak_height");
        checker.adaptFinalTime(formulae);
        
        ArrayList<SMCoutput> outlist = checker.modelCheck(formulae);
        
        System.out.println("Model checking completed");
        
        for (SMCoutput out : outlist)
            System.out.print(out.shortPrint());
        
        
        
        
    }
}
