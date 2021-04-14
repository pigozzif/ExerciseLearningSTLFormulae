/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.script;


import eggloop.flow.simhya.simhya.dataprocessing.TrajectoryExploratorDataCollector;

/**
 *
 * @author luca
 */
public class TrajectoryExplorationVariable extends ScriptVariable {
    TrajectoryExploratorDataCollector trajectoryExplorationData;

    public TrajectoryExplorationVariable(String name, TrajectoryExploratorDataCollector trajectoryExplorationData) {
        super(name);
        this.trajectoryExplorationData = trajectoryExplorationData;
    }

    @Override
    boolean isTrajectoryExplorationVariable() { return true; }
}
