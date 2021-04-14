/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.hype;

/**
 *
 * @author luca
 */
public class GuardResetPair {
    String guard;
    String reset;

    public GuardResetPair(String guard, String reset) {
        this.guard = guard;
        this.reset = reset;
    }
    
    public String toString(){
    	return "guard: "+guard+" reset: " + reset;
    }
    
}
