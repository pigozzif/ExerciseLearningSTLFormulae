/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.hype;

/**
 *
 * @author luca
 */
public class FlowTransition {
    String name;
    String guard;
    String reset;
    String rate;

    public FlowTransition(String name, String guard, String reset, String rate) {
        this.name = name;
        this.guard = guard;
        this.reset = reset;
        this.rate = rate;
    }

    @Override
    public String toString() {
        return name + ":*[ " + guard + " :-> " + reset + " ]@{ " + rate + " };";
    }     
}
