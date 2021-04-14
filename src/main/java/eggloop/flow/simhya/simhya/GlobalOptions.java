/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya;

import eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;

/**
 *
 * @author Luca
 */
public class GlobalOptions {

    public static final String version = "0.8";

    public static int transitionThresholdForRateTree = 50;
    public static int maximumSequenceOfInstantaneousTransitions = 100;
    public static int initialArraySize = 1000;
    public static int minimumSizeEventQueue = 10000;
    public static boolean alwaysComputeRateAfterGuard = true;
    public static final double TOLERANCE = 1e-10;
    public static int thresholdForUsingDependencyGraph = 2;

    //chart options
    public static int ChartFrameWidth = 1000;
    public static int ChartFrameHeight = 700;
    public static int ChartFileWidth = 1024;
    public static int ChartFileHeight = 768;
    public static int XYchartFrameWidth = 1000;
    public static int XYchartFrameHeight = 700;
    public static int XYchartFileWidth = 1024;
    public static int XYchartFileHeight = 768;
    public static int HistChartFrameWidth = 1000;
    public static int HistChartFrameHeight = 700;
    public static int HistChartFileWidth = 1024;
    public static int HistChartFileHeight = 768;
    public static org.jfree.data.statistics.HistogramType histogramType = org.jfree.data.statistics.HistogramType.FREQUENCY;
    public static int averageBinSizeInInterquantileRange = 10;


    public static double defaultAbsoluteTolerance = 1e-6;
    public static double defaultRelativeTolerance = 1e-6;
    public static double defaultMinStepSize = 1e-8;
    public static double defaultMaxStepSize = 1.0;
    public static double defaultMaxStepIncrementForEvents = 0.1;
    public static double defaultMaxErrorForEvents = 1e-5;
    public static int defaultMaxIterationforEvents = 50;
    public static IntegratorType defaultIntegratorType = IntegratorType.DP85;

    public static int samplePoints = 1000;
    
    
    public static boolean compileMath = false;

}
