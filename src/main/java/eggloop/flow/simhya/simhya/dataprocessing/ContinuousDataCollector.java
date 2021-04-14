/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.dataprocessing;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.apache.commons.math.ode.DerivativeException;

/**
 *
 * @author Luca
 */
public interface ContinuousDataCollector extends DataCollector {
    
    public void setInterpolator(StepInterpolator interpolator);
    public void putDataOde(double nextTime) throws DerivativeException;
    public void putFinalStateOde(double time) throws DerivativeException;
    
}
