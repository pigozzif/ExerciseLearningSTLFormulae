/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking.mtl;
import eggloop.flow.simhya.simhya.dataprocessing.chart.Plot2DTrajectory;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.model.store.Store;
import eggloop.flow.simhya.simhya.modelchecking.mtl.signal.BooleanSignal;

import java.util.HashMap;
import java.util.ArrayList;


/**
 *
 * @author Luca
 */
public abstract class MTLnode {
    static long counter = 0;
    
    MTLnode left; //for binary nodes
    MTLnode right; //for binary nodes
    MTLnode child; //for unary nodes
    MTLnode parent; //parent node
    
    boolean isTopNode;
    boolean isExplorableTopNode;
    Truth [] truthValue;
    int traceLength;
    long id;
    int subtreeSize;
    int internalSubtreeSize;
    int booleanNodesSubtree;
    int temporalOpsSubtree;
    int height;
    int leaves;
    BooleanSignal signal = null;
    
    boolean init = false;

    public MTLnode() {
        this.child = null;
        this.left = null;
        this.right = null;
        this.parent = null;
        id = counter++;
    }
    
    public MTLnode(long id) {
        this.child = null;
        this.left = null;
        this.right = null;
        this.parent = null;
        this.id = id;
    }
    
    
    public static long getNextID() {
        return counter;
    }
    
    public void setChildren(MTLnode left, MTLnode right) {
        this.left = left;
        this.right = right;
        this.child = null;
    }
    
    public void setChild(MTLnode child) {
        this.child = child;
        this.left = null;
        this.right = null;
    }
    
    
    public void setID(long id) {
        this.id = id;
    }
    
    public long getID() {
        return this.id;
    }
    
    public void setParent(MTLnode parent) {
        this.parent = parent;
    }

    public void initalize(Store store, SymbolArray localSymbols, HashMap predicatesToExpression) {
        if (left != null) 
            left.initalize(store,localSymbols,predicatesToExpression);
        if (right != null)
            right.initalize(store,localSymbols,predicatesToExpression);
        if (child != null)
            child.initalize(store,localSymbols,predicatesToExpression);
    }
    
    public void recomputeBounds(SymbolArray localSymbols) {
        if (left != null) 
            left.recomputeBounds(localSymbols);
        if (right != null)
            right.recomputeBounds(localSymbols);
        if (child != null)
            child.recomputeBounds(localSymbols);
    }
    
    
    public String toFormulaTree(int depth) {
        String s = "";
        if (left != null) 
            s += left.toFormulaTree(depth+1);
        if (right != null)
            s += right.toFormulaTree(depth+1);
        if (child != null)
            s += child.toFormulaTree(depth+1);
        return s;
    }
    public abstract  String toSign();


    /**
     * This functions sets the boolean flag for top nodes
     */
    void setTopNodeInfo() {
        this.setTopNodeInfo(false);
    }
    
    void setTopNodeInfo(boolean belowModalNode) {
        isTopNode = !belowModalNode;
        if (left != null) 
            left.setTopNodeInfo(belowModalNode);
        if (right != null)
            right.setTopNodeInfo(belowModalNode);
        if (child != null)
            child.setTopNodeInfo(belowModalNode);
    }
    
    /**
     * This function checks if the formula has explorable top nodes, 
     * according to the given parameter, and sets the corresponding flag.
     * It requires that the top node information is set.
     * @param parameter  a string for a parameter to explore, or null if one wants 
     * to compare current bounds (check is done on parametricInterval and is syntactic: 
     * all parameters have to coincide, not just their value).
     * 
     */
    void testAndSetExplorableTopNodes(String parameter) {
        ParametricInterval pint = this.setExplorableTopNodes(parameter);
        if (pint != null)
            this.setExplorableTopNodes(parameter, true);
        else this.setExplorableTopNodes(parameter, false);
    }
    
    /**
     * This recursively checks that top intervals of modal operators
     * are compatible for exploration. Returns null if they are not, 
     * the top interval that can be explored if they are compatible
     * @param parameter
     * @return 
     */
    ParametricInterval setExplorableTopNodes(String parameter) {
        return null;
    }
    
    /**
     * This recursively sets the explorableTopNode flag, which is true for all nodes 
     * which can contain a set of points as output, depending on the topmost metric intervals
     * @param parameter
     * @param value 
     */
    void setExplorableTopNodes(String parameter, boolean value) {
        if (this.isTopNode) {
            boolean loc = false;
            if (left != null) {
                left.setExplorableTopNodes(parameter, value);
                loc = loc || left.isExplorableTopNode;
            }
            if (right != null) {
                right.setExplorableTopNodes(parameter, value);
                loc = loc || right.isExplorableTopNode;
            }
            if (child != null) {
                child.setExplorableTopNodes(parameter, value);
                loc = loc || child.isExplorableTopNode;
            }
            this.isExplorableTopNode = value;
        }
    }
            
    /**
     * Recursively sets the initialisation status of a node
     * @param status 
     */
    private void setInitStatus(boolean status) {
        if (this.child != null)
            this.child.setInitStatus(status);
        if (this.left != null)
            this.left.setInitStatus(status);
        if (this.right != null)
            this.right.setInitStatus(status);
        this.init = status;
    }
    
    boolean checkConsistencyOfTimeBounds() {
        boolean truth = true;
        if (this.child != null)
            truth = truth && child.checkConsistencyOfTimeBounds();
        if (this.left != null)
            truth = truth && left.checkConsistencyOfTimeBounds();
        if (this.right != null)
            truth = truth && right.checkConsistencyOfTimeBounds();
        return truth;
    }
    
    
    boolean containsParameter(String param) {
        boolean answer = false;
        if (this.child != null)
            answer = answer || child.containsParameter(param);
        if (this.left != null)
            answer = answer || left.containsParameter(param);
        if (this.right != null)
            answer = answer || right.containsParameter(param);
        return answer;
    }
    
            
    //replace void with an output data structure,
    Truth[] modelCheckPointwiseSemantics(double [] deltaTimes, boolean varyInterval, boolean varyRightBound, int points) {
        if (!init) 
            throw new RuntimeException("Formula not initialized for model checking");
        this.recursiveLabelingPointwiseSemantics(deltaTimes, varyInterval, varyRightBound, points);       
        this.setInitStatus(false);
        return this.truthValue;
    }
    
    //pass suitable parameters
    void initForModelCheckingPointwiseSemantics(int points, int traceLength) {
        //init recusrively
        if (this.child != null)
            this.child.initForModelCheckingPointwiseSemantics(points, traceLength);
        if (this.left != null)
            this.left.initForModelCheckingPointwiseSemantics(points, traceLength);
        if (this.right != null)
            this.right.initForModelCheckingPointwiseSemantics(points, traceLength);
        //init this node
        if (!init) {
            this.traceLength = traceLength;
            if (!this.isTopNode)
                this.truthValue = new Truth[traceLength];
            else {
                if (this.isExplorableTopNode)
                    this.truthValue = new Truth[points];
                else 
                    this.truthValue = new Truth[1];
            }
            init = true;
        }
    }
    
    
    
    
    
    
    
    
    
    /**
     * recursively labels the nodes of the formula with the truth values.
     */
    void recursiveLabelingPointwiseSemantics(double [] deltaTimes, 
            boolean varyInterval, boolean varyRightBound, int points) {
        if (left != null)
            left.recursiveLabelingPointwiseSemantics(deltaTimes,varyInterval,varyRightBound,points);
        if (right!=null)
            right.recursiveLabelingPointwiseSemantics(deltaTimes,varyInterval,varyRightBound,points);
        if (child != null)
            child.recursiveLabelingPointwiseSemantics(deltaTimes,varyInterval,varyRightBound,points);
        if (isTopNode)
            this.labelingTopFormulaPointwiseSemantics(deltaTimes,varyInterval && isExplorableTopNode, //labels parametrically the top node only if it is explorable. 
                    varyRightBound,points);
        else
            this.labelingFormulaPointwiseSemantics(deltaTimes);
    }
    
    abstract void labelingTopFormulaPointwiseSemantics(double [] deltaTimes,  
            boolean varyInterval, boolean varyRightBound, int points);
    /**
     * Labels a non-top formula. 
     * @param deltaTimes is the array containing the time difference between any state
     */
    abstract void labelingFormulaPointwiseSemantics(double [] deltaTimes);
    
    
    
    
    //replace void with an output data structure,
    Truth[] modelCheckSignalSemantics(double t0, double tf, boolean varyInterval, boolean varyRightBound, int points) {
        if (!init) 
            throw new RuntimeException("Formula not initialized for model checking");
        this.recursiveLabelingSignalSemantics(t0,tf, varyInterval, varyRightBound, points);       
        this.setInitStatus(false);
        return this.truthValue;
    }
    
    //pass suitable parameters
    void initForModelCheckingSignalSemantics(int points) {
        //init recusrively
        if (this.child != null)
            this.child.initForModelCheckingSignalSemantics(points);
        if (this.left != null)
            this.left.initForModelCheckingSignalSemantics(points);
        if (this.right != null)
            this.right.initForModelCheckingSignalSemantics(points);
        //init this node
        if (!init) {
            if (!this.isTopNode)
                this.truthValue = new Truth[1];
            else {
                if (this.isExplorableTopNode)
                    this.truthValue = new Truth[points];
                else 
                    this.truthValue = new Truth[1];
            }
            signal = null;
            init = true;
        }
    }
    
    
    
    
    
    
    
    
    
    /**
     * recursively labels the nodes of the formula with the truth values.
     */
    void recursiveLabelingSignalSemantics(double t0, double tf,boolean varyInterval, boolean varyRightBound, int points) {
        if (left != null)
            left.recursiveLabelingSignalSemantics(t0,tf,varyInterval,varyRightBound,points);
        if (right!=null)
            right.recursiveLabelingSignalSemantics(t0,tf,varyInterval,varyRightBound,points);
        if (child != null)
            child.recursiveLabelingSignalSemantics(t0,tf,varyInterval,varyRightBound,points);
        if (isTopNode)
            this.labelingTopFormulaSignalSemantics(t0, tf, varyInterval && isExplorableTopNode, //labels parametrically the top node only if it is explorable. 
                    varyRightBound,points);
        else
            this.labelingFormulaSignalSemantics(t0,tf);
    }
    
    abstract void labelingTopFormulaSignalSemantics(double t0, double tf, boolean varyInterval, boolean varyRightBound, int points);
    /**
     * Labels a non-top formula. 
     * @param tf is the array containing the time difference between any state
     */
    abstract void labelingFormulaSignalSemantics(double t0, double tf);
    
    
    
    
    
    
    
    public abstract NodeType getType();
    
    void collectAtomicNodes(ArrayList<MTLatom> list) {
        if (left != null)
            left.collectAtomicNodes(list);
        if (right != null)
            right.collectAtomicNodes(list);
        if (child != null)
            child.collectAtomicNodes(list);
    }
    
    
    public abstract MTLnode duplicate(); 
    
    
    
    public void swap(MTLnode node) {
        MTLnode p,l,r,c;
        boolean b;
        p = this.parent;
        this.parent = node.parent;
        node.parent = p;
        b = this.isTopNode;
        this.isTopNode = node.isTopNode;
        node.isTopNode = b;
        b = this.isExplorableTopNode;
        this.isExplorableTopNode = node.isExplorableTopNode;
        node.isExplorableTopNode = b;
        if (p != null) {
            if (p.left == this)
                p.left = node;
            else if (p.right == this)
                p.right = node;
            else if (p.child == this)
                p.child = node;
        }
        p = this.parent;
        if (p != null) {
            if (p.left == node)
                p.left = this;
            else if (p.right == node)
                p.right = this;
            else if (p.child == node)
                p.child = this;
        }
    }
    
    public MTLnode replace(MTLnode node) {
        MTLnode p,l,r,c;
        boolean b;
        p = this.parent;
        l = this.left;
        r = this.right;
        c = this.child;
        this.parent = null;
        this.left = null;
        this.right = null;
        this.child = null;
        node.parent = p;
        node.left = l;
        node.right = r;
        node.child = c;
        b = this.isTopNode;
        node.isTopNode = b;
        b = this.isExplorableTopNode;
        node.isExplorableTopNode = b;
        if (p != null) {
            if (p.left == this)
                p.left = node;
            else if (p.right == this)
                p.right = node;
            else if (p.child == this)
                p.child = node;
        }
        return node;
    }
    
    
    void collectSubtreeNodes(ArrayList<MTLnode> list) {
        if (child != null) child.collectSubtreeNodes(list);
        if (left != null) left.collectSubtreeNodes(list);
        list.add(this);
        if (right != null) right.collectSubtreeNodes(list);
    }
    
    
    int countTemporalNodes() {
        if (this.child != null)
            this.temporalOpsSubtree = this.child.countBooleanNodes();
        else if (this.left != null && this.right != null)
            this.temporalOpsSubtree = this.left.countBooleanNodes() + this.right.countBooleanNodes();
        else
            this.temporalOpsSubtree = 0;  
        return this.temporalOpsSubtree;
    }
    
    int countBooleanNodes() {
        if (this.child != null)
            this.booleanNodesSubtree = this.child.countBooleanNodes();
        else if (this.left != null && this.right != null)
            this.booleanNodesSubtree = this.left.countBooleanNodes() + this.right.countBooleanNodes();
        else
            this.booleanNodesSubtree = 0;  
        return this.booleanNodesSubtree;
    }
    
    
    int countNodes() {
        if (this.child != null)
            this.subtreeSize = this.child.countNodes() + 1;
        else if (this.left != null && this.right != null)
            this.subtreeSize = this.left.countNodes() + this.right.countNodes() + 1;
        else
            this.subtreeSize = 1;  
        return this.subtreeSize;
    }
    
    int countInternalNodes() {
        if (this.child != null)
            this.internalSubtreeSize = this.child.countInternalNodes() + 1;
        else if (this.left != null && this.right != null)
            this.internalSubtreeSize = this.left.countInternalNodes() + this.right.countInternalNodes() + 1;
        else
            this.internalSubtreeSize = 0;  
        return this.internalSubtreeSize;
    }
    
    int countLeaves() {
        if (this.child != null)
            leaves =  this.child.countLeaves();
        else if (this.left != null && this.right != null)
            leaves =   this.left.countLeaves() + this.right.countLeaves();
        else
            leaves =   1;  
        return leaves;
    }
    
    
    int height() {
        if (this.child != null)
            height = this.child.height() + 1;
        else if (this.left != null && this.right != null)
            height =  Math.max(this.left.height(), this.right.height() ) + 1;
        else
            height =  0;  
        return height;
    }

    public int getInternalSubtreeSize() {
        return internalSubtreeSize;
    }

    public int getSubtreeSize() {
        return subtreeSize;
    }

    public int getHeight() {
        return height;
    }

    public int getLeaves() {
        return leaves;
    }
    
    
    
    
    
    
    public MTLnode changeType(NodeType newtype, boolean keepLeft, MTLnode newLeft, MTLnode newRight, ParametricInterval interval) {
        MTLnode n,p;
        //atomic nodes cannot be replaced
        if (this.child == null && this.left == null && this.right == null)
            return this;
        switch(newtype) {
            case ATOM:
                //doing nothing: can replace only internal nodes with internal nodes.
                n=this;
                break;
            case AND: 
                if (child != null && newLeft != null)
                    n = new MTLand(newLeft,child);
                else if (child != null && newRight != null)
                    n = new MTLand(child,newRight);
                else if (child == null)
                    n = new MTLand(left,right);
                else
                    n = this;
                break;
            case OR:
                if (child != null && newLeft != null)
                    n = new MTLor(newLeft,child);
                else if (child != null && newRight != null)
                    n = new MTLor(child,newRight);
                else if (child == null)
                    n = new MTLor(left,right);
                else
                    n = this;
                break;
            case IMPLY:
                if (child != null && newLeft != null)
                    n = new MTLimply(newLeft,child);
                else if (child != null && newRight != null)
                    n = new MTLimply(child,newRight);
                else if (child == null)
                    n = new MTLimply(left,right);
                else
                    n = this;
                break;
            case NOT:
                if (child == null) {
                    if (keepLeft)
                        n = new MTLnot(left);
                    else 
                        n = new MTLnot(right);
                } else 
                    n = new MTLnot(child);
                break;
            case UNTIL:
                if (child != null && newLeft != null)
                    n = new MTLuntil(newLeft,child);
                else if (child != null && newRight != null)
                    n = new MTLuntil(child,newRight);
                else if (child == null)
                    n = new MTLuntil(left,right);
                else
                    n = this;
                break;
            case WUNTIL:
                if (child != null && newLeft != null)
                    n = new MTLweakUntil(newLeft,child);
                else if (child != null && newRight != null)
                    n = new MTLweakUntil(child,newRight);
                else if (child == null)
                    n = new MTLweakUntil(left,right);
                else
                    n = this;
                break;
            case NEXT:
                if (child == null) {
                    if (keepLeft)
                        n = new MTLnext(left);
                    else 
                        n = new MTLnext(right);
                } else 
                    n = new MTLnext(child);
                break;
            case EVENTUALLY:
                if (child == null) {
                    if (keepLeft)
                        n = new MTLeventually(left);
                    else 
                        n = new MTLeventually(right);
                } else 
                    n = new MTLeventually(child);
                break;
            case GLOBALLY:
                if (child == null) {
                    if (keepLeft)
                        n = new MTLglobally(left);
                    else 
                        n = new MTLglobally(right);
                } else 
                    n = new MTLglobally(child);
                break;
            default:
                n = this;
                break;  
        }
        n.id = this.id;
        n.isTopNode = this.isTopNode;
        n.isExplorableTopNode = this.isExplorableTopNode;
        n.parent = this.parent;
        if (n.parent != null) {
            p = n.parent;
            if (p.left == this)
                p.left = n;
            else if (p.right == this)
                p.right = n;
            else if (p.child == this)
                p.child = n;
        }
        return n;
    }
    
    
    
    public MTLnode insert(NodeType type) {
        MTLnode n,p;
        p = this.parent;
        switch(type) {
            case AND: case OR: case IMPLY: case UNTIL: case WUNTIL:
                n = null;    
                break;
            case NOT:
                n = new MTLnot(this);
                break;
            case EVENTUALLY:
                n = new MTLeventually(this);
                break;
            case GLOBALLY:
                n = new MTLglobally(this);
                break;
            case NEXT: 
                n = new MTLnext(this);
                break;
            default: 
                n = null;
                break;
                
        }
        if (n != null) {
            n.parent = p;
            if (p != null) {
                if (p.left == this)
                    p.left = n;
                else if (p.right == this)
                    p.right = n;
                else if (p.child == this)
                    p.child = n;
            }
        }
        
        
        return n;
    }
    
    
    public MTLnode delete(boolean keepLeft) {
        MTLnode m,p;
        p = this.parent;
        if (this.child == null && this.left == null && this.right == null)
            return null;
        else if (this.child == null && keepLeft) 
            m = this.left;
        else if (this.child == null && !keepLeft) 
            m = this.right;
        else
            m = this.child;
        m.parent = p;
        if (p != null) {
          if (p.left == this)
            p.left = m;
          else if (p.right == this)
            p.right = m;
          else if (p.child == this)
            p.child = m;
        }
        return m;
    }
    
    
    public boolean isRoot() {
        return parent == null;
    }
    
    public boolean isInternal() {
        return this.child != null || this.left != null || this.right != null;
    }
    
    public boolean isLeaf() {
        return !this.isInternal();
    }
    
    public boolean isBoolean() {
        return (this instanceof MTLand) || (this instanceof MTLor) 
                || (this instanceof MTLimply) || (this instanceof MTLnot);
    }
    
    public boolean isModal() {
        return (this instanceof MTLuntil) || (this instanceof MTLnext) 
                || (this instanceof MTLeventually) || (this instanceof MTLglobally)
                || (this instanceof MTLweakUntil);
    }
    public boolean isBinary() {
        return this.left != null && this.right != null;
    }
    
    public boolean isUnary() {
        return this.child != null;
    }
    
    public boolean isAtom() {
        return false;
    }

   
    public ArrayList<String> getParameters(ArrayList<String> list) {
        if (left != null)
            left.getParameters(list);
        if (right != null)
            right.getParameters(list);
        if (child != null)
            child.getParameters(list);
        return list;
    }
    
    
    
    public double computeTimeDepth() {
        double T = 0;
        if (left != null)
            T += left.computeTimeDepth();
        if (right != null)
            T += right.computeTimeDepth();
        if (child != null)
            T += child.computeTimeDepth();
        return T;  
    }
    
    
    
    public ArrayList<Plot2DTrajectory> getSignals() {
        if (this.signal != null) {
            ArrayList<Plot2DTrajectory> list = new ArrayList<Plot2DTrajectory>();
            this.getSignals(list);
            return list;
        }
        else
            return null;
            
       
    }
    
    private void getSignals(ArrayList<Plot2DTrajectory> list) {
        String name = this.toString();
        double[][] trace = signal.getTrace();
        Plot2DTrajectory T = new Plot2DTrajectory();
        T.name = name;
        T.x = trace[0];
        T.y = trace[1];
        list.add(T);
        if (child != null)
            child.getSignals(list);
        if (left != null)
            left.getSignals(list);
        if (right != null)
            right.getSignals(list);
    }

    
    void printSignals() {
       String name = this.toString();
       String s = signal.toString();
       System.out.print(name + " ::= ");
       System.out.println(s);
       if (child != null)
            child.printSignals();
        if (left != null)
            left.printSignals();
        if (right != null)
            right.printSignals();
    }
    
    
}





/*
 * To do for mutation. 
 * A node that has to mutate does the following. 
 * 1. generates the new target node, and set the correct links. 
 * 2. copy the additional information (intervals), possibly changing it. 
 * 3. if the additional information is not available, generate it from scratch 
 * (ex: intervals for passing from boolean ops to temporal ops).
 * 4. when passing from unary to binary, create a new atomic proposition, 
 * and link it to left or right children of the new node. For booleans, always right,
 * for until, it depends on the previous node. From eventually, attach to left, 
 * from always, attach to right.
 * 
 * 
 * 
 * Insertion and deletion, only of unary nodes. 
 * Create a new node of the desired form, with additional info. Add connections
 * For deletion, remove the node and rewire.
 * 
 * 
 * 
 */