/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.hype;



/**
 *
 * @author luca
 */
public class EventTransition {
    String name;
    String guard;
    String reset;
    boolean stochastic;
    String rate;

    public EventTransition(String name, String guard, String reset) {
        this.name = name;
        this.guard = guard;
        this.reset = reset;
        this.stochastic = false;
        this.rate = String.valueOf(1);
    }

    public EventTransition(String name, String guard, String reset, String rate) {
        this.name = name;
        this.guard = guard;
        this.reset = reset;
        this.rate = rate;
        this.stochastic = true;
    }

    
    public String toString() {
        return name + ":[ " + guard + " :-> " + reset + " ]" + (stochastic ? "@{ " : "@inf{ ") + rate + " };";
    }
    
}
