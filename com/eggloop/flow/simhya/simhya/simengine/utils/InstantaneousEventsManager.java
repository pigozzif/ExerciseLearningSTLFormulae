/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.simengine.utils;

import cern.jet.random.engine.RandomEngine;
import java.util.Locale;
import com.eggloop.flow.simhya.simhya.simengine.SimulationException;
import com.eggloop.flow.simhya.simhya.utils.RandomGenerator;
import com.eggloop.flow.simhya.simhya.GlobalOptions;

/**
 *
 * @author Luca
 */
public class InstantaneousEventsManager {
    //stores id and priorities
    private int[] enabledEvents;
    private double[] priorities;
    private int eventsInQueue;
    private double sumOfPriorities;
    private double lastFiringTime;
    private int firingSequenceLength;
    


    public InstantaneousEventsManager(int numberOfInstantaneousTransitions) {
        this.enabledEvents = new int[numberOfInstantaneousTransitions];
        this.priorities = new double[numberOfInstantaneousTransitions];
        this.eventsInQueue = 0;
        this.sumOfPriorities = 0;
        lastFiringTime = 0;
        this.firingSequenceLength = 0;
    }

    /**
     * resets the event manager. Now it is ready to run a new simulation
     */
    public void reset(int numberOfInstantaneousTransitions) {
        this.enabledEvents = new int[numberOfInstantaneousTransitions];
        this.priorities = new double[numberOfInstantaneousTransitions];
        this.eventsInQueue = 0;
        this.sumOfPriorities = 0;
        lastFiringTime = 0;
        this.firingSequenceLength = 0;
    }

    /**
     * sets the initial time of the simulation. Needed for the internal control of
     * number of instantaneous transitions fired in a row. Default time is zero.
     * @param time
     */
    public void setInitialSimulationTime(double time) {
        this.lastFiringTime = time;
        this.firingSequenceLength = 0;
    }

    /**
     *
     * @return true if there are instantaneous events  enabled in the manager
     */
    public boolean areInstantaneousEventsEnabled() {
        return (this.eventsInQueue > 0);
    }

    private boolean contains(int id) {
        for (int i=0;i<this.eventsInQueue;i++)
            if (this.enabledEvents[i] == id)
                return true;
        return false;
    }

    /**
     * Adds an event to the manager, with given id and priority
     * @param id
     * @param priority
     */
    public void addEnabledEvent(int id, double priority) {
        if (!contains(id)) {
            this.enabledEvents[this.eventsInQueue] = id;
            this.priorities[this.eventsInQueue] = priority;
            this.eventsInQueue++;
            this.sumOfPriorities += priority;
        }
    }


    private int find(int id) {
        for (int i=0;i<this.eventsInQueue;i++)
            if (this.enabledEvents[i] == id)
                return i;
        return -1;
    }

    private void swap(int i, int j) {
        int k = this.enabledEvents[i];
        double h = this.priorities[i];
        this.enabledEvents[i] = this.enabledEvents[j];
        this.priorities[i] = this.priorities[j];
        this.enabledEvents[j] = k;
        this.priorities[j] = h;
    }

    /**
     * removes an active instantaneous event from the manager
     * @param id
     */
    public void removeEvent(int id) {
        int index = find(id);
        if (index != -1) {
            this.sumOfPriorities -= this.priorities[index];
            swap(index,this.eventsInQueue-1);
            this.eventsInQueue--;
        }
    }

    /**
     * returns the next instantaneous event to be executed, ties are solved according to priorities
     * @param rand a random engine
     * @return
     */
    public int getNextEvent(RandomEngine rand, double currentTime) {
        if (this.lastFiringTime == currentTime) {
            this.firingSequenceLength ++;
        } else {
            this.lastFiringTime = currentTime;
            this.firingSequenceLength = 1;
        }
        if (this.firingSequenceLength > GlobalOptions.maximumSequenceOfInstantaneousTransitions)
            throw new SimulationException("Fired more than " + GlobalOptions.maximumSequenceOfInstantaneousTransitions + " instantaneous transitions in a row.\n There can be an infinite loop. If this is intended, raise the threshold number.");
        if (this.eventsInQueue == 1)
            return this.enabledEvents[0];
        int index = RandomGenerator.sample(priorities, sumOfPriorities, rand);
        return this.enabledEvents[index];
    }


    /**
     * updates the priority of an event
     * @param id the ide of the event 
     * @param priority the new priority
     */
    public void updatePriority(int id, double priority) {
        int index = find(id);
        if (index == -1)
            throw new SimulationException("Trying to update an instantaneous event not in the manager");
        this.sumOfPriorities += (priority - this.priorities[index]);
        this.priorities[index] = priority;
    }

    @Override
    public String toString() {
        String s = "[";
        for (int i=0;i<this.eventsInQueue;i++) {
            s += (i > 0 ? "," : "") + "(";
            s += this.enabledEvents[i] + ",";
            s += String.format(Locale.US, "%.3f", this.priorities[i]) + ")";
        }
        s += "]";
        return s;
    }







}
