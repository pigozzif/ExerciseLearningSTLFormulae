/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.dataprocessing;

/**
 *
 * @author luca
 */
public interface EventDataCollector {

    
    public void putNextTimedEvent(double time, double[] y, String name);
    public void putNextInstantaneousEvent(double time, double[] y, String name);
    public void saveEventList(String filename);
    

}
