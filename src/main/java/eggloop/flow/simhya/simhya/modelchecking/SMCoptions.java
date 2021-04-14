/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

/**
 * SMC default options
 * @author luca
 */
public class SMCoptions {
    public SMCtype type = SMCtype.POINTWISE;
    public TestMethod testType = TestMethod.WALD;
    public EstimationMethod estimationType = EstimationMethod.FREQUENTIST_ADAPTIVE;
    public ConfidenceBoundsMethod confidenceType = ConfidenceBoundsMethod.WILSON;
    public long samples = 10000;
    public double confidence = 0.95;
    public double error = 0.01;
    public int adaptiveStep = 10;
    public long maxRuns = 100000;
    public double priorGood = 1;
    public double priorBad = 1;
    public boolean regularise = false;
    
    public double power = 0.95;
    public double significance = 0.01;
    public double epsilon = 0.01;
    
    
          
    
    public boolean Bayesian() {
        if (estimationType == EstimationMethod.BAYESIAN_ADAPTIVE || estimationType == EstimationMethod.BAYESIAN_FIXED  )
            return true;
        else return false;
    }
    
    
}
