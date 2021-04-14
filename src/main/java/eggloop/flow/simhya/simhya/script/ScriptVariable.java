/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.script;

/**
 *
 * @author luca
 */
public abstract class ScriptVariable {
    String name;

    public ScriptVariable(String name) {
        this.name = name;
    }

    



    boolean isModelVariable() { return false; }
    boolean isCollectorVariable() { return false; }
    boolean isStatisticsVariable() { return false; }
    boolean isStatisticsExplorationVariable() { return false; }
    boolean isTrajectoryExplorationVariable() { return false; }
    boolean isModelCheckingVariable() { return false; }
    boolean isModelCheckingOutputVariable() { return false; }

}
