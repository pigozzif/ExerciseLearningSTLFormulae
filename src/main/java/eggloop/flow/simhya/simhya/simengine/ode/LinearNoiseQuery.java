/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.simengine.ode;
import eggloop.flow.simhya.simhya.matheval.Expression;
import umontreal.iro.lecuyer.probdist.NormalDist;
import java.util.ArrayList;
import java.util.Locale;
import eggloop.flow.simhya.simhya.model.store.Store;
import eggloop.flow.simhya.simhya.dataprocessing.Trajectory;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;

/**
 *
 * @author Luca
 */
public class LinearNoiseQuery {
    Store store;
    String linearExpression;
    Expression averageExpression;
    Expression stdevExpression;
    double size;
    double lower;
    double upper;
    ArrayList<String> varNames;
    boolean onPopulation;
    
    /**
     * Constructs a query for a linear noise model
     * @param store the store
     * @param average a string contaning a linear expression on system variables
     * of the form a1*X1 + ... + ak*Xk. 
     * @param size the size of the system
     * @param lower the lower bound for the expression
     * @param upper the upper bound for the expression
     * @param onPop if true the comparison is done on population numbers
     * @param varNames the name of system variables 
     */
    public LinearNoiseQuery(Store store, String average, String stdev, double size, 
            double lower, double upper, boolean onPop, ArrayList<String> varNames) {
        this.store = store;
        this.size = size;
        this.onPopulation = onPop;
        this.lower = lower;
        this.upper = upper;
        this.varNames = varNames;
        this.generateExpressions(average,stdev);
    }

    /**
     * Constructs a query for a linear noise model
     * @param store the store
     * @param average a string contaning a linear expression on system variables
     * of the form a1*X1 + ... + ak*Xk. 
     * @param size the size of the system
     * @param threshold the threshold to compare the expression to
     * @param greater if true, computes the probability of the expression being
     * greater than the threshold, if false checks the prob of being lower.
     * @param onPop if true the comparison is done on population numbers
     * @param varNames the name of system variables 
     */
    public LinearNoiseQuery(Store store, String average, String stdev, double size, 
            double threshold, boolean greater, boolean onPop, ArrayList<String> varNames) {
        this.store = store;
        this.size = size;
        this.onPopulation = onPop;
        if (greater) {
            this.lower = threshold;
            this.upper = Double.POSITIVE_INFINITY;
        } else {
            this.upper = threshold;
            this.lower = Double.NEGATIVE_INFINITY;
        }
        this.varNames = varNames;
        this.generateExpressions(average,stdev);
    }
    
    
    private void generateExpressions(String average,String stdev) {
        //check expression is effectively linear
        this.averageExpression = store.parseExpression(average);
        this.stdevExpression = store.parseExpression(stdev);
        this.linearExpression = average;
        //generate average expression by replacing x with N*x, if the count is on the pop
        //or leaving it untouched if the count is on norm pop
        if (this.onPopulation) {
            ArrayList<Expression> toSub = new ArrayList<Expression>();
            for (String s : this.varNames)
                toSub.add(store.parseExpression("" + this.size + "*" + s));
            this.averageExpression = this.averageExpression.substitute(varNames, toSub);
            toSub = new ArrayList<Expression>();
            for (String s : this.varNames)
                toSub.add(store.parseExpression("sqrt(" + this.size + "* var." + s + ")"));
            this.stdevExpression = this.stdevExpression.substitute(varNames, toSub);
        } else {
            ArrayList<Expression> toSub = new ArrayList<Expression>();
            for (String s : this.varNames)
                toSub.add(store.parseExpression("sqrt(var." + s + "/" + this.size + ")"));
            this.stdevExpression = this.stdevExpression.substitute(varNames, toSub);
        }        
    } 
    
    
    /**
     * computes the query for the given trajectory 
     * @return  the model variable in which data has been saved
     */
    public String compute(int queryId, DataCollector data) {
        Trajectory t = data.getTrajectory(0);
        int n = this.varNames.size();
        int m = n + n*(n+1)/2 ;
        SymbolArray vars = SymbolArray.getNewFastEvalSymbolArray(m);
        
        
        double [] values = new double[t.getPoints()];
        double [] x = new double[m];
        for (int i=0;i<t.getPoints();i++) {
            //for each data point of the trajectory
            //copy it into x
            for (int j=0;j<m;j++)
                x[j] = t.getData(j+1, i);
            vars.setValuesReference(x);
            //compute average and stddev
            double mu = this.averageExpression.computeValue(vars);
            double sigma = this.stdevExpression.computeValue(vars);
            
            //System.out.println("Time " + t.getData(0, i) + ". Average " + mu + " Stdev " + sigma);
            
            
            //compute probability according to normal model
            if (this.lower == Double.NEGATIVE_INFINITY) {
                if (sigma <= 0) 
                    values[i] = (mu < upper ? 1:0);
                else            
                    values[i] = NormalDist.cdf(mu, sigma, this.upper);
            } else if (this.upper == Double.POSITIVE_INFINITY) {
                if (sigma <= 0) 
                    values[i] = (mu > lower ? 1 : 0);
                else
                    values[i] = NormalDist.barF(mu, sigma, this.lower);
            } else {
                if (sigma <= 0) 
                    values[i] = (mu > lower && mu < upper ? 1 : 0);
                else
                    values[i] = NormalDist.cdf(mu, sigma, this.upper) - NormalDist.cdf(mu, sigma, this.lower);
            }
        }
        //adds into to the trajectory
        String v = "ln."+queryId;
        data.addTimeTrace(v, values);
        return v;
    }

    
    /**
     * computes the query for the given trajectory 
     * @return  the model variable in which data has been saved
     */
    public String computeFinalStateOnly(int queryId, DataCollector data) {
        Trajectory t = data.getTrajectory(0);
        int n = this.varNames.size();
        int m = n + n*(n+1)/2;
        SymbolArray vars = SymbolArray.getNewFastEvalSymbolArray(m);
        double  value;
        double [] x = new double[m];
        //copy the final state of the trajectory into x
        for (int j=0;j<m;j++)
            x[j] = t.getFinalStateData(j+1);
        vars.setValuesReference(x);
        //compute average and stddev
        double mu = this.averageExpression.computeValue(vars);
        double sigma = this.stdevExpression.computeValue(vars);
        //compute probability according to normal model
        if (this.lower == Double.NEGATIVE_INFINITY) {
                if (sigma <= 0) 
                    value = (mu < upper ? 1:0);
                else            
                    value = NormalDist.cdf(mu, sigma, this.upper);
            } else if (this.upper == Double.POSITIVE_INFINITY) {
                if (sigma <= 0) 
                    value = (mu > lower ? 1 : 0);
                else
                    value = NormalDist.barF(mu, sigma, this.lower);
            } else {
                if (sigma <= 0) 
                    value = (mu > lower && mu < upper ? 1 : 0);
                else
                    value = NormalDist.cdf(mu, sigma, this.upper) - NormalDist.cdf(mu, sigma, this.lower);
            }
        //adds into the trajectory
        //String v = "ln"+queryId+"."+(Math.floor(size) == size ? (int)size :
        //        String.format(Locale.UK, "%.4f", size));
        String v = "ln."+queryId;
        data.addFinalStateTrace(v, value);
        return v;
    }
    
    
    
    @Override
    public String toString() {
        String s = "Prob{ " + this.linearExpression;
        if (this.lower == Double.NEGATIVE_INFINITY) {
                s += " < " + this.upper;
            }
            else if (this.upper == Double.POSITIVE_INFINITY) {
                s += " > " + this.lower;
            } else {
                s += " in [" + lower +"," + upper + "]";
            }
        s += " }";
        return  s;
    }
    
    
    
    
}
