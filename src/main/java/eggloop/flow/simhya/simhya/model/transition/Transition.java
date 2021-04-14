/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.transition;

import eggloop.flow.simhya.simhya.model.store.Predicate;
import  eggloop.flow.simhya.simhya.model.store.faststore.ConstantPredicate;
import  eggloop.flow.simhya.simhya.model.store.faststore.ConstantFunction;
import java.util.ArrayList;
import  eggloop.flow.simhya.simhya.model.store.*;
import  eggloop.flow.simhya.simhya.model.ModelException;

/**
 *
 * @author Luca
 */
public class Transition {
    private String event;
    private TType type;
    private ContinuityStatus continuityStatus;
    private Guard guard;
    private TimedActivation firingTime;
    private Reset reset;
    private Rate rate;
    private Delay delay;
    private Reset delayedReset;
    private Guard delayedGuard;
    //notice, a delayed transition fires immediately the reset, and fires the delayed reset after a
    //given delay elapsed.

    private boolean isDelayed;
    private boolean isStopping;
    private boolean isStoppingAfterDelay;

    private boolean activationStatus;
    private boolean currentContinuityStatus;


    public Transition(String ev, TType type) {
        this.event = ev;
        this.type = type;
        this.rate = new Rate(new ConstantFunction(1));
        this.guard = new Guard(new ConstantPredicate(true));
        this.activationStatus = true;
        this.reset = new Reset();
        this.isDelayed = false;
        this.delay = null;
        this.delayedReset = null;
        this.delayedGuard = null;
        this.currentContinuityStatus = false;
        if (this.type == TType.HYBRID) {
            this.continuityStatus = new ContinuityStatus(new ConstantPredicate(false));
        }
        else {
            this.continuityStatus = null;
            if (this.type == TType.CONTINUOUS)
                this.currentContinuityStatus = true;
        }
        if (this.type == TType.TIMED)
            this.firingTime = new TimedActivation(new ConstantFunction(1));
        else this.firingTime = null;
        this.isStopping = false;
        this.isStoppingAfterDelay = false;
    }

    public void setType(TType type) {
        this.type = type;
    }


    

    /**
     * sets the guard
     * @param g
     */
    public void setGuard(Guard g) {
        this.guard = g;
        //this.activationStatus = g.evaluate();
    }

    /**
     * sets the rate of the transition
     * @param r
     */
    public void setRate(Rate r) {
        this.rate = r;
    }

    public void setReset(Reset r) {
        this.reset = r;
    }

    /**
     * sets the firing time of a timed transition
     * @param tg
     */
    public void setTimedActivation(TimedActivation tg) {
        this.firingTime = tg;
    }

    /**
     * Sets the continuity status predicate of a hybrid transition
     * @param s
     */
    public void setContinuityStatus(ContinuityStatus s) {
        this.continuityStatus = s;
        this.currentContinuityStatus = s.isTrue(false);
    }

    /**
     * Makes the transition delayed, with given delay.
     * Notice, currently not supported.
     * @param d
     * @param g the delayed guard, if false (at the scheduled delayed time), the delayed reset is not fired
     * @param r the delayed reset, can be the empty reset
     */
    public void setDelay(Delay d, Guard g, Reset r) {
        this.isDelayed = true;
        this.delay = d;
        this.delayedGuard = g;
        this.delayedReset = r;
    }


    /**
     * changes the type of the transition
     * @param t
     */
    public void changeType(TType t) {
        this.type = t;
    }

    /**
     *
     * @return the type of the transition
     */
    public TType getType() {
        return this.type;
    }

    /**
     * 
     * @return the event of the transition
     */
    public String getEvent() {
        return event;
    }


    /**
     *
     * @return true if the execution of the transition forces the simulation to stop.
     */
    public boolean isStopping() {
        return this.isStopping;
    }


    /**
     *
     * @return true if the execution of the transition forces the simulation to stop after a delay.
     */
    public boolean isStoppingAfterDelay() {
        return this.isStoppingAfterDelay && this.isDelayed;
    }
    /**
     * if set to true, the execution of the transition stops the simulation!
     * @param stopping
     */
    public void setStopping(boolean stopping) {
        this.isStopping = stopping;
    }

    /**
     * if set to true, the execution of the transition stops the simulation after a delay!
     * @param stopping
     */
    public void setStoppingAfterDelay(boolean stopping) {
        this.isStoppingAfterDelay = stopping;
    }

    /**
     *
     * @return the list of variables involved in guard.
     */
    public ArrayList<Integer> getGuardVariables() {
        if (guard != null)
            return this.guard.getVariableList();
         else
            return new ArrayList<Integer>();
    }

    /**
     *
     * @return the list of variables involved in rate function.
     */
    public ArrayList<Integer> getRateVariables() {
        if (rate != null && this.type != TType.TIMED)
            return this.rate.getVariableList();
        else
            return new ArrayList<Integer>();
    }

    
    /**
     *
     * @return the list of variables involved in continuity status.
     */
     public ArrayList<Integer> getContinuityStatusVariables() {
         if (this.type == TType.HYBRID && this.continuityStatus != null)
            return this.continuityStatus.getVariableList();
         else return new ArrayList<Integer>();
     }

    /**
     *
     * @return the list of variables involved in firing time of timed transitions.
     */
     public ArrayList<Integer> getNextFiringTimeVariables() {
        if (this.type == TType.TIMED && this.firingTime != null)
            return this.firingTime.getVariableList();
        else return new ArrayList<Integer>();
     }


     /**
      *
      * @return the list of variables updated by this transition
      */
     public ArrayList<Integer> getUpdatedVariables() {
          if (this.reset != null)
            return this.reset.getVariableList();
         else return new ArrayList<Integer>();
        
     }

      /**
      *
      * @return the list of variables updated by this transition
      */
     public ArrayList<Integer> getUpdatedVariablesAfterDelay() {
         if (this.delayedReset != null)
            return this.delayedReset.getVariableList();
         else return new ArrayList<Integer>();
     }


     /**
      * returns true if the transition can be treated as continuous.
      * @return true
      */
     public boolean isContinuouslyApproximable() {
         if (this.type == TType.INSTANTANEOUS || this.type == TType.TIMED)
            return false;
         boolean answer = true;
         for (int i=0;i<reset.getAtomicResets().length;i++)
            answer = answer && reset.getAtomicResets()[i].isConstantIncrement();
         return answer;
     }


    /**
     * This shold be used only by hybrdi transitions.
     * Continuous transition can be identified by querying their type.
     * @return the current continuity status of the transition
     *
     */
    public boolean isContinuous() {
        return this.currentContinuityStatus;
    }

    /**
     * 
     * @return the current value of the guard.
     */
    public boolean isActive() {
        return this.activationStatus;
    }

    /**
     *
     * @return true if the transition is delayed.
     */
    public boolean isDelayed() {
        return this.isDelayed;
    }


    /**
     * Executes the transition, modyfing the variables of the store.
     * @param store the store containing information about variables
     * @return if the transition is delayed and its type is among those supporting
     * delay (stochastic,instantaneous,timed), it returns the delay, computed from the state
     * of the store before the update. The delay after the update can be computed by calling
     * getCurrentDelay(). Return 0 if there is no delay.
     */
    public double execute(Store store) {
        double d = 0.0;
        if (isDelayed && isDelaySupported())
            d = this.delay.compute();
        this.reset.apply(store);
        return d;
    }

    /**
     * Executes the delayed reset.
     * @param store
     */
    public void executeDelayedReset(Store store) {
        this.delayedReset.apply(store);
    }



    /**
     * computes the current delay associated to the transition if any.
     * @return 0 if there is no delay
     */
    public double getCurrentDelay() {
        double d = 0.0;
        if (isDelayed && isDelaySupported())
            d = this.delay.compute();
        return d;
    }


    /**
     *
     * @return true if the transition type of the transition supports delay
     */
    private boolean isDelaySupported() {
        return false;
//        return this.type == TType.STOCHASTIC || this.type == TType.INSTANTANEOUS
//                || this.type == TType.TIMED;
    }


    /**
     *
     * @param currentTime the current simulation time
     * @return returns the next firing time of a timed transition.
     * If the firing time is less than current time, returns positive infinity
     */
    public double getNextFiringTime(double currentTime) {
        double ft = this.firingTime.compute();
        return (ft < currentTime ? Double.POSITIVE_INFINITY : ft);
    }


    /**
     * Evaluates the guard and stores the result in the activation status
     * @return true if the transition is active;
     */
    public boolean evaluateGuard() {
        this.activationStatus = this.guard.evaluate();
        return this.activationStatus ;
    }

    /**
     * Computes the rate of the transition
     * @return the current rate of the transition
     */
    public double computeRate() {
        return this.rate.compute();
    }


    /**
     * Evaluates the continuity status of the transition, storing the result
     * If used for a non hybrid transition, raises an exception.
     * @return true if the trnaisiton is to be treated continuous
     */
    public boolean evaluateContinuityStatus() {
        if (this.continuityStatus == null)
            throw new ModelException("Transition " +this.getEvent() + " is not HYBRID");
        this.currentContinuityStatus = this.continuityStatus.isTrue(currentContinuityStatus);
        return this.currentContinuityStatus;
    }

    public Predicate getContinuityStatusPredicate(boolean status) {
        if (this.continuityStatus != null)
            return this.continuityStatus.getPredicate(status);
        else return null;
    }
    

    public Predicate getGuardPredicate() {
        if (guard != null)
            return guard.getPredicate();
        else return null;
    }

    public Function getRateFunction() {
        if (rate != null)
            return rate.getFunction();
        else return null;
    }

    public Function getTimedActivationFunction() {
        if (firingTime != null)
            return this.firingTime.getFunction();
        else return null;
    }

    public Function getDelayFunction() {
        if (delay != null)
            return this.delay.getFunction();
        else return null;
    }

    public AtomicReset[] getResetList() {
        if (reset != null)
            return reset.getAtomicResets();
        else return null;
    }

    public AtomicReset[] getDelayedResetList() {
        if (delayedReset != null)
            return this.delayedReset.getAtomicResets();
        else return null;
    }

    public Predicate getDelayedGuardPredicate() {
        if (delayedGuard != null)
            return this.delayedGuard.getPredicate();
        else return null;
    }


    public String toModelLanguage(Store store) {
        String s = "";
        s += this.event + ":[ ";
        s += this.guard.getPredicate().toModelLanguage();
        s += " :-> ";
        s += resetToModelLanguage(reset,store,isStopping) + " ]";
        switch(this.type) {
            case STOCHASTIC:
            case CONTINUOUS:
            case HYBRID:
                s += "@{ " + rate.getFunction().toModelLanguage() + " }";
                break;
            case INSTANTANEOUS:
                s += "@inf{ " + rate.getFunction().toModelLanguage() + " }";
                break;
            case TIMED:
                s += "@time{ " + this.firingTime.getFunction().toModelLanguage() + " }";
                break;
            default:
                break;
        }
        if (delay != null) {
            s += "delay{ " + delay.getFunction().toModelLanguage() + " }:[ ";
            s += this.delayedGuard.getPredicate().toModelLanguage();
            s += " :-> ";
            s += resetToModelLanguage(this.delayedReset,store,isStoppingAfterDelay) + " ]";
        }
        if (this.continuityStatus != null) {
            s += "cont{" + this.continuityStatus.getPredicate(false).toModelLanguage() + 
                   (continuityStatus.hasSinglePredicate() ? "" : 
                    " : " + continuityStatus.getPredicate(true).toModelLanguage()) + "}";
        }
        s += ";\n";
        return s;
    }

    private String atomicResetToModelLanguage(Store store,AtomicReset[] ar, int i) {
        return ar[i].toModelLanguage(store);
    }

    private String resetToModelLanguage(Reset r, Store store, boolean stopping) {
        String s = "";
        AtomicReset [] a = r.getAtomicResets();
        for (int i=0;i<a.length;i++)
           s += (i>0?"; ":"") + atomicResetToModelLanguage(store,a,i);
        if (stopping)
            s +=  (s.equals("")?"": "; ") + "stop";
        return s;
    }

    
    public Transition clone(Store newStore) {
        Transition t = new Transition(this.event,this.type);
        t.activationStatus = this.activationStatus;
        t.continuityStatus = (this.continuityStatus != null ? this.continuityStatus.clone(newStore) : null);
        t.delay = (this.delay!=null?this.delay.clone(newStore):null);
        t.delayedGuard = (this.delayedGuard!=null?this.delayedGuard.clone(newStore):null);
        t.delayedReset = (this.delayedReset!=null?this.delayedReset.clone(newStore):null);
        t.firingTime = (this.firingTime!=null?this.firingTime.clone(newStore):null);
        t.guard = (this.guard!=null?this.guard.clone(newStore):null);
        t.isDelayed=this.isDelayed;
        t.isStopping=this.isStopping;
        t.isStoppingAfterDelay=this.isStoppingAfterDelay;
        t.rate=(this.rate!=null?this.rate.clone(newStore):null);
        t.reset=(this.reset!=null?this.reset.clone(newStore):null);
        return t;
    }

}




