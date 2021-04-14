/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.simengine.paramexplore;

import java.util.ArrayList;
import eggloop.flow.simhya.simhya.dataprocessing.OdeDataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.TrajectoryExploratorDataCollector;
import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.model.flat.LinearNoiseFlatModel;
import eggloop.flow.simhya.simhya.simengine.ode.LinearNoiseQuery;


/**
 *
 * @author luca
 */
public class LinearNoiseParamExplorator extends DeterministicParamExplorator {
    private ArrayList<LinearNoiseQuery> lnQuery;
    private LinearNoiseFlatModel lnModel;

    public LinearNoiseParamExplorator(FlatModel model) {
        super(model);
        if (model.isLinearNoiseApproximation())
            lnModel = (LinearNoiseFlatModel)model;
        else {
            super.model = (LinearNoiseFlatModel)model.generateLinearNoiseApproximation();
            lnModel = (LinearNoiseFlatModel)super.model; 
        }
        this.lnQuery = new ArrayList<LinearNoiseQuery>();
    }

    @Override
    void postProcessSimulation(OdeDataCollector localCollector) {
        //here compute LN queries. Standard exploration is as in Det Expl
        int id = 1;
        for (LinearNoiseQuery q : this.lnQuery) {
            if (this.saveFinalStateOnly)
                q.computeFinalStateOnly(id++, localCollector);
            else q.compute(id++,  localCollector);
        }
    }

    @Override
    void setupDataCollector() {
        this.collector = new TrajectoryExploratorDataCollector(this.paramSet);
        int n;
        if (this.varsToSave != null)
            n = varsToSave.size() + 1 + this.lnQuery.size();
        else
            n = model.getStore().getNumberOfVariables() + 
                    model.getStore().getNumberOfExpressionVariables() + 1
                    + this.lnQuery.size();
        collector.setupSaveOptions(n,saveFinalStateOnly);
        ArrayList<String> vars = getVarNamesForCollector(varsToSave);
        collector.setVariableNames(vars);
        collector.setModelName(model.getName());
    }

    @Override
    ArrayList<String> getVarNamesForCollector(ArrayList<String> varsToSave) {
        ArrayList<String> vars = super.getVarNamesForCollector(varsToSave);
        for (int id=1;id<=this.lnQuery.size();id++) {
            vars.add("ln."+id);
        }
        return vars;
    }
    
    public void addQuery(String average, String stdev, double size, 
            double threshold, boolean greater, boolean onPop) {
        this.lnQuery.add(new LinearNoiseQuery(this.lnModel.getStore(),average,stdev,size,
                threshold,greater,onPop,this.lnModel.getOriginalModelVariables()));        
    }
    
    
    
    public void addQuery(String average, String stdev, double size, 
            double lower, double upper, boolean onPop) {
        this.lnQuery.add(new LinearNoiseQuery(this.lnModel.getStore(),average,stdev,size,
                lower,upper,onPop,this.lnModel.getOriginalModelVariables()));

    }
    

    
}
