/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.modelchecking.mtl.signal;

import com.eggloop.flow.simhya.simhya.matlab.SimHyAModel;

/**
 *
 * @author luca
 */
public class Tester {
    
    public static void main(String[] args) throws Exception {
        System.out.println("Estiquaatzi!");
        
        BooleanSignal s1 = new BooleanSignal(0,10);
        BooleanSignal s2 = new BooleanSignal(0,12);
        s1.addNextPositive(0,5,true,false);
        s1.addNextPositive(7,9,true,true);
        s1.finalise();
        s2.addNextPositive(2,4,false,false);
        s2.addNextPositive(8,10,false,true);
        s2.finalise();
        
        System.out.println("s1: " + s1.toString());
        System.out.println("s2: " + s2.toString());
        
        BooleanSignal s0;
        
        s0 = BooleanSignalTransducer.and(s1, s2);
        System.out.println("s1 AND s2: " + s0.toString());
        
        s0 = BooleanSignalTransducer.or(s1, s2);
        System.out.println("s1 OR s2: " + s0.toString());
        
        s0 = BooleanSignalTransducer.imply(s1, s2);
        System.out.println("s1 --> s2: " + s0.toString());
        
        s0 = BooleanSignalTransducer.not(s1);
        System.out.println("NOT s1: " + s0.toString());
        
        s0 = BooleanSignalTransducer.eventually(s1,0,5);
        System.out.println("EV[0,5] s1: " + s0.toString());
        
        s0 = BooleanSignalTransducer.eventually(s1,1,2);
        System.out.println("EV[1,2] s1: " + s0.toString());
        
        s0 = BooleanSignalTransducer.always(s1,0,1);
        System.out.println("ALW[0,1] s1: " + s0.toString());
        
        s0 = BooleanSignalTransducer.always(s1,1,2);
        System.out.println("ALW[1,2] s1: " + s0.toString());
        
        s0 = BooleanSignalTransducer.until(s1,s2,0,1);
        System.out.println("s1 U[0,1] s2: " + s0.toString());
        
        
      
        
        
        
    }
    
    
}
