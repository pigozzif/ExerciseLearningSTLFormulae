/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.script;

import com.eggloop.flow.simhya.simhya.dataprocessing.StatisticsExploratorDataCollector;

/**
 *
 * @author luca
 */
public class StatisticsExplorationVariable extends ScriptVariable {
    StatisticsExploratorDataCollector statisticsExplorationData;

    public StatisticsExplorationVariable(String name, StatisticsExploratorDataCollector statisticsExplorationData) {
        super(name);
        this.statisticsExplorationData = statisticsExplorationData;
    }

    @Override
    boolean isStatisticsExplorationVariable() { return true; }



}
