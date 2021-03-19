/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.hype;
import java.util.ArrayList;
import java.util.HashMap;
/**
 *
 * @author Luca
 */
public class SynchronizationNode {
    ArrayList<String> synchronizationSet;
    boolean synchronizeOnAllSharedEvents;
    SynchronizationNode parent;
    SynchronizationNode leftChild;
    SynchronizationNode rightChild;
    SequentialController seqController;
    Controller controller;
    boolean isLeaf;
    boolean ground;
    int leafCode;

    public SynchronizationNode(SequentialController seqController) {
        this.seqController = seqController;
        synchronizeOnAllSharedEvents = false;
        synchronizationSet = null;
        isLeaf = true;
        parent = null;
        leftChild = null;
        rightChild = null;
        ground = false;
        leafCode = -1;
    }

    public SynchronizationNode(Controller controller) {
        this.controller = controller;
        synchronizeOnAllSharedEvents = false;
        synchronizationSet = null;
        isLeaf = true;
        parent = null;
        leftChild = null;
        rightChild = null;
        ground = false;
        leafCode = -1;
    }

    public SynchronizationNode(ArrayList<String> synchronizationSet) {
        this.synchronizationSet = synchronizationSet;
        synchronizeOnAllSharedEvents = false;
        isLeaf = false;
        parent = null;
        leftChild = null;
        rightChild = null;
        ground = false;
        leafCode = -1;
    }

    public SynchronizationNode() {
        synchronizeOnAllSharedEvents = true;
        synchronizationSet = null;
        isLeaf = false;
        parent = null;
        leftChild = null;
        rightChild = null;
        ground = false;
        leafCode = -1;
    }

    public void setLeftChild(SynchronizationNode leftChild) {
        this.leftChild = leftChild;
    }

    public void setParent(SynchronizationNode parent) {
        this.parent = parent;
    }

    public void setRightChild(SynchronizationNode rightChild) {
        this.rightChild = rightChild;
    }
    
    
    /**
     * constructs a ground tree, in which leaves are only constructed by subcomponents!
     * @return 
     */
    SynchronizationNode constructGroundTree() {
        SynchronizationNode n,n1;
        if (this.parent == null)
            n = this.clone();
        else 
            n = this;
        n.ground = true;
        if (n.controller != null) {
            n1 = n.controller.synchRoot.constructGroundTree();
            if (n.parent != null)
                if (n == n.parent.leftChild) {
                    n1.parent = n.parent;
                    n.parent.leftChild = n1;
                } else if (n == n.parent.rightChild) {
                    n1.parent = n.parent;
                    n.parent.rightChild = n1;
                }
            n = n1;
        }
        else if (!n.isLeaf) {
            n.leftChild.constructGroundTree();
            n.rightChild.constructGroundTree();
        }
        return n;
    }
    
    
    @Override
    protected SynchronizationNode clone() {
        SynchronizationNode n = new SynchronizationNode();
        n.controller = this.controller;
        n.isLeaf = this.isLeaf;
        n.ground = this.ground;
        n.leafCode = this.leafCode;
        n.seqController = this.seqController;
        n.synchronizationSet = this.synchronizationSet;
        n.synchronizeOnAllSharedEvents = this.synchronizeOnAllSharedEvents;
        if (this.leftChild != null) {
            n.leftChild = this.leftChild.clone();
            n.leftChild.parent = n;
        }
        if (this.rightChild != null) {
            n.rightChild = this.rightChild.clone();
            n.rightChild.parent = n;
        }
        return n;
    }
    
    
    void computeDerivativeSetsOfSequentialControllers(HashMap<String,SequentialController> seqControllers) {
        if (!this.ground)
            throw new HypeException("Synchronization tree must be grounded before computing derivative sets");
        if (this.seqController != null)
            seqController.computeDerivativeSet(seqControllers);
        else if (this.isLeaf)
            throw new HypeException("Found a leaf with no sequential controller in a ground tree!");
        else {
            this.leftChild.computeDerivativeSetsOfSequentialControllers(seqControllers);
            this.rightChild.computeDerivativeSetsOfSequentialControllers(seqControllers);
        }
    }
    
    /**
     * Generates all state variables for sequential controllers 
     * and collects them in a list.
     * @return 
     */
    ArrayList<Variable> generateStateVariables() {
        if (!this.ground)
            throw new HypeException("Synchronization tree must be grounded before computing variable list");
        this.setLeafCode(0);
        ArrayList<Variable> list = new ArrayList<Variable>();
        this.generateStateVariablesRec(list);
        return list;
    }
    
    /**
     * recursively collects all state variables
     * @param varList 
     */
    private void generateStateVariablesRec(ArrayList<Variable> varList) {
        if (!this.ground)
            throw new HypeException("Synchronization tree must be grounded before computing variable list");
        if (this.seqController != null)
            varList.addAll(seqController.generateStateVariables(leafCode));
        else if (this.isLeaf)
            throw new HypeException("Found a leaf with no sequential controller in a ground tree!");
        else {
            this.leftChild.generateStateVariablesRec(varList);
            this.rightChild.generateStateVariablesRec(varList);
        }
    }
    
    
    /**
     * numbering leaves from left to right.
     * @param nextCode
     * @return 
     */
    private int setLeafCode(int nextCode) {
        if (this.isLeaf) {
            this.leafCode = nextCode;
            return nextCode + 1;
        } else {
            int i = this.leftChild.setLeafCode(nextCode);
            i = this.rightChild.setLeafCode(i);
            return i;
        }
    }
    
    
    /**
     * generates all transitions for the specified event!
     * @param event
     * @return 
     */
    ArrayList<GuardResetPair> generateTransitionsForEvent(String event) {
        if (!this.ground)
            throw new HypeException("Synchronization tree must be grounded before computing transitions");
        if (this.seqController != null)
            return this.seqController.generateTransitionsForEvent(event);
        else if (this.isLeaf)
            throw new HypeException("Found a leaf with no sequential controller in a ground tree!");
        else {
            ArrayList<GuardResetPair> left = this.leftChild.generateTransitionsForEvent(event);
            ArrayList<GuardResetPair> right = this.rightChild.generateTransitionsForEvent(event);
            if (this.synchronizeOnAllSharedEvents) {
                if (left.isEmpty())
                    return right;
                else if (right.isEmpty())
                    return left;
                else 
                    return productList(left, right);
            }
            else if (this.synchronizationSet.contains(event)) {
                return productList(left,right);
            }
            else 
                return mergeLists(left,right);
        }
    }
    
    /**
     * takes the product of two lists, concatenating in each possible way guards and resets
     * @param first
     * @param second
     * @return 
     */
    private ArrayList<GuardResetPair> productList(ArrayList<GuardResetPair> first, ArrayList<GuardResetPair> second) {
        ArrayList<GuardResetPair> list = new ArrayList<GuardResetPair>();
        if (first.isEmpty() || second.isEmpty())
            return list;
        for (GuardResetPair p1 : first)
            for (GuardResetPair p2 : second) {
                String g = p1.guard + " && " + p2.guard;
                String r = p1.reset;
                r +=  (r.isEmpty() ? "" : (p2.reset.isEmpty() ? "" : "; ")) + p2.reset;
                GuardResetPair p = new GuardResetPair(g,r);
                list.add(p);
            }
        return list;
    }
    
    
    /**
     * merges two lists
     * @param first
     * @param second
     * @return 
     */
    private ArrayList<GuardResetPair> mergeLists(ArrayList<GuardResetPair> first, ArrayList<GuardResetPair> second) {
        ArrayList<GuardResetPair> list = new ArrayList<GuardResetPair>();
        list.addAll(first);
        list.addAll(second);
        return list;
    }

}
