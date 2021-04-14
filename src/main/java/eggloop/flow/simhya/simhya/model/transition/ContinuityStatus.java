/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.transition;

import eggloop.flow.simhya.simhya.model.store.Predicate;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.model.store.Store;

/**
 *
 * @author Luca
 */
public class ContinuityStatus {
    private Predicate predicateWhileTrue;
    private Predicate predicateWhileFalse;
    private boolean singlePredicate;

    public ContinuityStatus(Predicate p) {
        this.predicateWhileTrue = p;
        this.predicateWhileFalse = p;
        this.singlePredicate = true;
    }
    
     public ContinuityStatus(Predicate pFalse,Predicate pTrue) {
        this.predicateWhileTrue = pTrue;
        this.predicateWhileFalse = pFalse;
        this.singlePredicate = false;
    }

    /**
     *
     * @return the list of variables invoved in the definition of the predicate
     */
    public ArrayList<Integer> getVariableList() {
        ArrayList<Integer> list = predicateWhileFalse.getVariableList();
        for (Integer i : predicateWhileTrue.getVariableList())
            if (!list.contains(i))
                list.add(i);
        return list;
    }

 
    /**
     *
     * @return true if the transition has to be treated as continuous and deterministic,
     * false if it has to be treated as discrete and stochastic
     * 
     * @param status the current continuity status of the transition
     */
    public boolean isTrue(boolean status) {
        if (status)
            return this.predicateWhileTrue.evaluate();
        else return this.predicateWhileFalse.evaluate();
    }


    public boolean hasSinglePredicate() {
        return this.singlePredicate;
    }
    
    /**
     * Returns one predicate defining continuity status
     * @param status if true, the predicate telling when to switch from continuous to
     * discrete is returned, if false, the predicate telling when to switch from discrete to
     * continuous is returned
     * @return 
     */
    public Predicate getPredicate(boolean status) {
        if (status)
            return this.predicateWhileTrue;
        else return this.predicateWhileFalse;
    }
    
    protected ContinuityStatus clone(Store newStore) {
        ContinuityStatus c;
        if (this.singlePredicate) {
            c = new ContinuityStatus(this.predicateWhileFalse.clone(newStore));
        } else {
            c = new ContinuityStatus(this.predicateWhileFalse.clone(newStore),
                    this.predicateWhileTrue.clone(newStore));
        }
        return c;
    }

}
