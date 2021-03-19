/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.matlab;

/**
 *
 * @author luca
 */
public class Tester {
    
    
    public static void main(String[] args) throws Exception {
        SimHyAModel m = new SimHyAModel();
        m.loadModel("SIR_M.txt");
        m.loadSMCformulae("sir_F.txt");
        m.smc("extinction", 150,100);
        
        
    }
    
}
