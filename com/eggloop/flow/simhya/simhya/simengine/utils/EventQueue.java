/*
 *
 * In house simple implementation of an Heap with ArrayList!
 */

package com.eggloop.flow.simhya.simhya.simengine.utils;


import com.eggloop.flow.simhya.simhya.GlobalOptions;
import java.util.Arrays;


/**
 *
 * @author Luca
 */
public class EventQueue {
    private EventQueueNode[] transitionToQueueNode;
    private EventQueueNode[] queue;
    private int queueSize;
    private EventQueueNode lastEvent;


    /**
     * Inits a queue for a fixed number of transitions.
     * @param numberOfTransitions
     */
    public EventQueue(int numberOfTransitions) {
        lastEvent = null;
        queueSize = 0;
        queue = new EventQueueNode[GlobalOptions.minimumSizeEventQueue];
        Arrays.fill(queue, null);
        transitionToQueueNode = new EventQueueNode[numberOfTransitions];
        Arrays.fill(transitionToQueueNode, null);
    }

  

    

    /**
     *
     * @return the firing time of the next event
     */
    public double getNextFiringTime() {
        if (queueSize == 0)
            return Double.POSITIVE_INFINITY;
        else
            return queue[0].time;
    }

    /**
     *
     * @return the id of the firing event, removing it from the queue
     */
    public int extractFiringEvent() {
        if (queueSize == 0)
            throw new RuntimeException("Event Queue is empty");
        lastEvent = queue[0];
        queueSize--;
        swap(0,queueSize);
        //queue[queueSize] = null;
        heapify(0);
        if (!lastEvent.isDelayedEvent)
            transitionToQueueNode[lastEvent.id] = null;
        if (queue.length > GlobalOptions.minimumSizeEventQueue && queueSize < queue.length/4)
                queue = Arrays.copyOf(queue, queue.length/2);
        return lastEvent.id;
    }


    /**
     * Returns the id of the firing event, leaving it in the queue and updating its firing time
     * @param newFiringTime
     * @return
     */
    public int extractFiringEvent(double newFiringTime) {
       if (queueSize == 0)
            throw new RuntimeException("Event Queue is empty");
        lastEvent = queue[0];
        queue[0].time = newFiringTime;
        heapify(0);
        return lastEvent.id;
    }

    /**
     *
     * @return true if the last event that fired is a delayed event;
     */
    public boolean isLastEventDelayed() {
        if (lastEvent == null)
            throw new RuntimeException("EventQueue: No event ever fired");
        return lastEvent.isDelayedEvent;
    }


    /**
     * updates the firing time of an event already in the queue
     * @param id the id of the event/transition
     * @param newTime the new firing time
     */
    public void updateEvent(int id, double newTime) {
        if (transitionToQueueNode[id] == null)
            throw new RuntimeException("Event " + id + " is not in the queue");
        EventQueueNode node = transitionToQueueNode[id];
        if (newTime < node.time) {
            node.time = newTime;
            decreasedKey(node.position);
        }
        else if (newTime > node.time) {
            node.time = newTime;
            heapify(node.position);
        }
    }

    /**
     * Adds a non-delayed event to the queue
     * @param id
     * @param time
     */
    public void addEvent(int id, double time) {
        if (transitionToQueueNode[id] != null)
            throw new RuntimeException("Event " + id + " already in the queue");
        EventQueueNode node = new EventQueueNode(id,time,false);
        transitionToQueueNode[id] = node;
        addNode(node);
    }

    /**
     * Adds a delayed (unremovable and unmodifiable) event
     * @param id the id of the transition
     * @param time the firing time
     */
    public void addDelayedEvent(int id, double time) {
        EventQueueNode node = new EventQueueNode(id,time,true);
        addNode(node);
    }

    /**
     * Adds a node to the queue
     * @param node
     */
    private void addNode(EventQueueNode node) {
        if (queueSize == queue.length) //full queue, doubling it
            queue = Arrays.copyOf(queue, 2*queue.length);
        queue[queueSize] = node;
        node.position = queueSize;
        queueSize++;
        this.decreasedKey(node.position);
    }

    /**
     * removes a non-delayed event with identification id.
     * @param id
     */
    public void removeEvent(int id) {
        EventQueueNode node = transitionToQueueNode[id];
        if (node != null) {
            int n = node.position;
            queueSize--;
            if (n < queueSize) {
                swap(n,queueSize);
                //queue[queueSize] = null;
                heapify(n);
            }
            //else
                //queue[queueSize] = null;
            transitionToQueueNode[id] = null;
            if (queue.length > GlobalOptions.minimumSizeEventQueue && queueSize < queue.length/4)
                queue = (EventQueueNode[])cern.colt.Arrays.trimToCapacity(queue, queue.length/2);
        }
    }

    /**
     *
     * @return true if the event queue is empty
     */
    public boolean isEmpty() {
        return (queueSize==0);
    }

    /**
     * empties the event queue
     */
    public void reset(int numberOfTransitions) {
        lastEvent = null;
        queueSize = 0;
        queue = new EventQueueNode[GlobalOptions.minimumSizeEventQueue];
        Arrays.fill(queue, null);
        transitionToQueueNode = new EventQueueNode[numberOfTransitions];
        Arrays.fill(transitionToQueueNode, null);
    }

    /**
     * returns the index containing the parent of a node. If the node is the root, the index is negative!
     * @param node the index of a  node
     * @return the index of the parent node, -1 if node is the root
     */
    private int parent(int node) {
        if (node > 0)
            return (node-1)/2;
        else return -1;
    }

    /**
     * return 
     * @param node
     * @return the index of the left son of a node
     */
    private int left(int node) {
        return 2*node+1;
    }

    /**
     *
     * @param node
     * @return the index of the right son of a node
     */
    private int right(int node) {
        return 2*node+2;   
    }

    /**
     * Swaps elements i and j of the queue.
     * @param i
     * @param j
     */
    private void swap(int i, int j) {
        EventQueueNode ni = queue[i];
        EventQueueNode nj = queue[j];
        ni.position = j;
        nj.position = i;
        queue[i] = nj;
        queue[j] = ni;
    }

    /**
     * Fixes the heap for a node possibly violating the heap property below
     * @param n
     */
    private void heapify (int n)  {
        int l,r,smallest;
        l = left(n);
        if (l < queueSize && queue[l].time < queue[n].time)
            smallest = l;
        else
            smallest = n;
        r = right(n);
        if (r < queueSize && queue[r].time < queue[smallest].time)
            smallest = r;
        if (smallest != n) {
            swap(n,smallest);
            heapify(smallest);
        }
    }

    /**
     * fix the heap for a node with decresed key, possibly violating the
     * heap propery above.
     * @param n
     */
    private void decreasedKey(int n) {
        int p = parent(n);
        if (p > -1 && queue[p].time > queue[n].time) {
            swap(n,p);
            decreasedKey(p);
        }
    }

    @Override
    public String toString() {
        String s = "[";
        for (int i=0;i<this.queueSize;i++)
            s+= (i>0?",":"") + queue[i].toString();
        s += "]";
        return s;
    }



    public class EventQueueNode {
        public int id;
        public double time;
        public boolean isDelayedEvent;
        public int position;

        public EventQueueNode(int id, double time, boolean isDelayedEvent) {
            this.id = id;
            this.time = time;
            this.isDelayedEvent = isDelayedEvent;
        }

        @Override
        public String toString() {
            return "(" + id + "," + time + "," + isDelayedEvent + ")";
        }


    }

}
