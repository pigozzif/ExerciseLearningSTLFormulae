/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.hype;


/**
 *
 * @author Luca
 */
/*
 * We want to avoid cycles in the synch trees. But we can do this by imposing that
 * 1. all (seq) controllers in the rhs of  the definition of a controller have been 
 * defined previously
 * 2. the name of the controller does not appear in the rhs (can be accomplished by 1, 
 * adding the controller to the controller list after parsing its rhs)
 */

public class Controller {
    String name;
    SynchronizationNode synchRoot;

    public Controller(String name, SynchronizationNode synchRoot) {
        this.name = name;
        this.synchRoot = synchRoot;
    }
    
    
}
