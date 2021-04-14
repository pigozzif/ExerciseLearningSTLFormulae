/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking;

import cern.colt.Arrays;
import eggloop.flow.simhya.simhya.model.flat.FlatModel;
import eggloop.flow.simhya.simhya.model.store.Store;
import eggloop.flow.simhya.simhya.model.store.faststore.FastStore;
import eggloop.flow.simhya.simhya.matheval.Expression;
import java.util.HashMap;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.modelchecking.mtl.MTLformula;
import eggloop.flow.simhya.simhya.modelchecking.mtl.parser.MTLlexer;
import eggloop.flow.simhya.simhya.modelchecking.mtl.parser.MTLparser;
import java.io.StringReader;
import java.io.FileReader;
import eggloop.flow.simhya.simhya.dataprocessing.DataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.HybridDataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.OdeDataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.StochasticDataCollector;
import eggloop.flow.simhya.simhya.dataprocessing.Trajectory;
import eggloop.flow.simhya.simhya.modelchecking.parser.SMClexer;
import eggloop.flow.simhya.simhya.modelchecking.parser.SMCparser;
import eggloop.flow.simhya.simhya.simengine.Simulator;
import eggloop.flow.simhya.simhya.simengine.SimulatorFactory;
import eggloop.flow.simhya.simhya.simengine.ode.IntegratorType;
import eggloop.flow.simhya.simhya.simengine.ode.OdeSimulator;
//import eggloop.flow.simhya.simhya.simengine.ode.OdeSimulatorWithEvents;

/*
 * TO DO: add command to model check a formula and to run a parameter exploration on one 
 * or more formulas. Use parameterExploration routines. 
 * Add options, like the possibility of model checking the pointwise semantics on ODE 
 * or on CTMC trajectories, and maybe the possibility (?here?) of a quantitative pointwise
 * semantics for ODE.
 * 
 * I need a class for collecting model checking results, supporting also visualization!
 * 
 * 
 */

/**
 *
 * @author Luca
 */
public class SMCenvironment {
    HashMap<String,Expression> predicatesToExpressions;
    ArrayList<MTLformula> MTLformulas;
    HashMap<String,MTLformula> namesToMTLformulas;
    public SMCoptions options = new SMCoptions();
    public SMCsimulationOptions simoptions = new SMCsimulationOptions();
    private FlatModel model;
    private Store modelStore;
    private FastStore localStore;
    private int numberOfFormulas;
    private MTLlexer MTLlexer;
    private MTLparser MTLparser;
    private boolean init;
    
    private Simulator simulator;
    private DataCollector collector;
    
    double lastExecutionTime;
    
    
    

    public SMCenvironment(FlatModel model) {
        this.model = model;
        this.modelStore = model.getStore();
        this.reset();
    }
    
    public SMCenvironment(Store store) {
        this.model = null;
        this.modelStore = store;
        this.reset();
    }
    
    private void reset() {
        this.localStore = new FastStore();
        this.numberOfFormulas = 0;
        this.MTLformulas = new ArrayList<MTLformula>();
        this.predicatesToExpressions = new HashMap<String,Expression>();
        this.namesToMTLformulas = new HashMap<String,MTLformula>();
        this.MTLlexer = null;
        this.MTLparser = null;
        this.init = false;
    }
    
    public void loadEnvironment(String file) {
        if (init)
            reset();
        try {
            FileReader in = new FileReader(file);
            SMClexer lexer = new SMClexer(in);
            SMCparser parser = new SMCparser(lexer,this);
//            parser.debug_parse();
            parser.parse();            
            in.close();
            localStore.finalizeVariableInitialization();
            localStore.finalizeInitialization();
            init = true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot load SMC environment " + file);
        }
    }
    
    
    public ArrayList<String> getFormulaeParameterNames() {
        return this.localStore.getNameOfAllParameters();
    }
    
    public double[] getFormulaeParameterValues() {
        return this.localStore.getParametersValues();
    }
    
    
    public void setFormulaeParameters(String[] names, double[] values) {
        if (names.length != values.length)
            throw new RuntimeException("names and values vector length differ!");
        for (int i=0;i<names.length;i++) {
            int id = this.localStore.getParameterID(names[i]);
            this.localStore.setParameterValue(id, values[i]);
        }
        this.localStore.getParametersReference().evaluateAndStoreInitialValueExpressions();
        //recompute metric bounds on formulae
        for (MTLformula f : this.MTLformulas)
            f.recomputeBounds(this.localStore.getParametersReference());
    }
    
    
    
    private void initMTLparser(String formula) {
        if (MTLlexer == null || MTLparser == null) {
            MTLlexer = new MTLlexer(new StringReader(formula));
            MTLparser = new MTLparser(MTLlexer);
        } else {
            MTLlexer.yyreset(new StringReader(formula));
        }
    }
    
    /**
     * Adds an MTL formula to the model checking environment with the given name 
     * @param formula
     * @return 
     */
    public boolean addMTLformula(String id, String formula) {
        initMTLparser(formula);
        if (this.namesToMTLformulas.containsKey(id))
            return false;
        try {
            MTLformula f = (MTLformula)(MTLparser.parse().value);
            f.initalize(modelStore, localStore.getParametersReference(), predicatesToExpressions);
            f.setName(id);
            this.namesToMTLformulas.put(id, f);
            this.MTLformulas.add(f);
            this.numberOfFormulas++;
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse formula " + id);
        }
    }
    
    /**
     * Adds an MTL formula to the model checking environment with no name 
     * @param formula
     * @return 
     */
    public boolean addMTLformula(String formula) {
        String id = "_formula_" + numberOfFormulas;
        return this.addMTLformula(id, formula);
    }
    
    /**
     * Add a parameter for the model checking environment. 
     * Parameters are stored in a local store.
     * @param name name of the parameter
     * @param definition defining expression
     * @return 
     */
    public boolean addParameter(String name, String definition) {
        if (localStore.getParametersReference().containsSymbol(name))
            return false;
        Expression exp = localStore.parseExpression(definition);
        if (exp.isNumericConstant())
            localStore.addParameter(name, exp.computeValue());
        else
            localStore.addParameter(name, exp.computeValue(), exp);
        return true;
    }
    
    /**
     * Adds an atomic proposition definition to the model checking environment.
     * 
     *
     * @param name
     * @param definition
     * @return 
     */
    public boolean addAtomicProposition(String name, String definition) {
        /*
         * TO DO: allow boolean connectives in the definition of the expression,
         * but only of the form && || !, so that the expression itself can 
         * be parsed by the store. 
         * OR EVEN BETTER: implement a method to define compound atomic predicates in the 
         * file, but keeping track of individual atomic predicates by a special naming convention.
         */
        if (this.predicatesToExpressions.containsKey(name))
            return false;
        Expression exp = modelStore.parseExpression(definition, localStore.getParametersReference());
        exp.setLocalSymbols(localStore.getParametersReference());
        this.predicatesToExpressions.put(name, exp);
        return true;
    }
    
    /*
     * REMEMBER TO RECOMPUTE EXPRESSIONS OF LOCAL PARAMETERS
     */
    
    
    public void setFinalTime(double tf) {
        this.simoptions.finalTime = tf;
    }
    
    public void print_debug() {
        System.out.println("****Parameters****");
        for (String s : this.localStore.getParametersReference().getNameOfAllSymbols()) {
            int id = localStore.getParametersReference().getSymbolId(s);
            System.out.println(s + " = " + localStore.getParametersReference().getValue(id));
        }
        System.out.println("\n****Atomic Predicates****");
        for (String s : this.predicatesToExpressions.keySet()) {
            Expression exp = this.predicatesToExpressions.get(s);
            System.out.println(s + " := " + exp.toString());
        }
        System.out.println("\n****MTL Formulae****");
        for (String s : this.namesToMTLformulas.keySet()) {
            MTLformula f = this.namesToMTLformulas.get(s);
            System.out.println(":"+s+":"); 
            System.out.println(f.toFormulaTree());
            System.out.println("****");
        }
    }
    
    /*
     * TO DO: set up options for model checking
     * set up parameter exploration routines
     * 
     */
    
    public void adaptFinalTime(ArrayList<String> formulaNames) {
        double time = 0.0;
        for (String formulaName : formulaNames) {
            MTLformula formula = this.namesToMTLformulas.get(formulaName);
            if (formula == null)
                throw new RuntimeException("SMC: formula " + formulaName + " does not exist");
            time = Math.max(time,formula.getTimeDepth());
        }
        simoptions.finalTime = 1.05*time;
    }
    
    public boolean isFormula(String name) {
        return this.namesToMTLformulas.containsKey(name);
    }
    
    public ArrayList<String> getFormulaNames() {
        ArrayList<String> formulae = new ArrayList<String>();
        for (String s : this.namesToMTLformulas.keySet()) 
            formulae.add(s);
        return formulae;
    }
    
    
    /**
     * Model checks the formula with given name, with parameters and options as previously set
     * @param formulaName
     * @return 
     */
    public SMCoutput modelCheck(String formulaName) {
        MTLformula formula = this.namesToMTLformulas.get(formulaName);
        if (formula == null)
            throw new RuntimeException("SMC: formula " + formulaName + " does not exist");
        ArrayList<MTLformula> list = new ArrayList<MTLformula>();
        list.add(formula);
        SMCoutput out = runModelChecking(list).get(0);
        return out;
    } 
    
    
    public ArrayList<SMCoutput> modelCheck(ArrayList<String> formulaNames) {
        ArrayList<MTLformula> list = new ArrayList<MTLformula>();
        for (String formulaName : formulaNames) {
            MTLformula formula = this.namesToMTLformulas.get(formulaName);
            if (formula == null)
                throw new RuntimeException("SMC: formula " + formulaName + " does not exist");
            list.add(formula);
        }
        return runModelChecking(list);
    }
    
    public MTLformula getMTLformula(String formulaName){
        MTLformula formula = this.namesToMTLformulas.get(formulaName);
        if (formula == null)
            throw new RuntimeException("SMC: formula " + formulaName + " does not exist");
        return formula;
    }
    
    /**
     * Model checks the formula with given id, with parameters and options as previously set
     * @param formulaID
     * @return 
     */
    public SMCoutput modelCheck(int formulaID) {
        MTLformula formula = this.MTLformulas.get(formulaID);
        if (formula == null)
            throw new RuntimeException("SMC: formula " + formulaID + " does not exist");
        ArrayList<MTLformula> list = new ArrayList<MTLformula>();
        list.add(formula);
        SMCoutput out = runModelChecking(list).get(0);
        return out;
    } 
    
    
    /**
     * Actually does the model checking
     * @param formulas
     * @return 
     */
    private ArrayList<SMCoutput> runModelChecking(ArrayList<MTLformula> formulas) {
        long time = System.nanoTime();
        //if (simulator == null || simulator.)
        this.initialiseSimulator();
        
        //2. initialize model checking for formula
        int n = formulas.size();
        ArrayList<SMCcontroller> controllers = new ArrayList<SMCcontroller>();
        for (MTLformula f : formulas) {
            if (f.estimate()) {
                SMCestimator c = null;
                switch (options.estimationType) {
                    case FREQUENTIST_FIXED :
                        c = new SMCfrequentistEstimator();
                        c.setFixed(options.samples);
                        c.setConfidence(options.confidence);
                        break;
                    case FREQUENTIST_CHERNOFF :
                        c = new SMCfrequentistEstimator();
                        c.setError(options.error);
                        c.setConfidence(options.confidence);
                        c.setChernoff();
                        break;
                    case FREQUENTIST_ADAPTIVE :
                        c = new SMCfrequentistEstimator();
                        c.setError(options.error);
                        c.setConfidence(options.confidence);
                        c.setAdaptive(options.adaptiveStep);
                        break;
                    case BAYESIAN_FIXED :
                        c = new SMCbayesianEstimator();
                        c.setFixed(options.samples);
                        c.setConfidence(options.confidence);
                        c.setPrior(options.priorGood, options.priorBad);
                        break;
                    case BAYESIAN_ADAPTIVE :
                        c = new SMCbayesianEstimator();
                        c.setError(options.error);
                        c.setConfidence(options.confidence);
                        c.setAdaptive(options.adaptiveStep);
                        c.setPrior(options.priorGood, options.priorBad);
                        break;
                    default:
                        throw new RuntimeException("What a mess, this seems quite impossible!");
                }
                c.setMaxCalls(options.maxRuns);
                if (options.confidenceType == ConfidenceBoundsMethod.WILSON)
                    c.useWilson();
                else if (options.confidenceType == ConfidenceBoundsMethod.NORMAL)
                    c.useNormal();
                if (options.regularise) 
                    c.regularise(options.priorGood, options.priorBad);
                c.setFormula(f.getName());
                controllers.add(c);
                f.initializeForEstimate(c);
            } else {
                SMCtest c = null;
                switch (options.testType) {
                    case WALD :
                        c = new SMCtestWald(f.getThreshold());
                        break;
                    case BAYESIAN :
                        c = new SMCtestBayesian(f.getThreshold());
                        break;
                    default:
                        throw new RuntimeException("What a mess 2, this seems quite impossible!");
                }
                c.setTestPower(options.significance, options.power);
                c.setThresholdTolerance(options.epsilon);
                c.setFormula(f.getName());
                controllers.add(c);
                f.initializeForModelChecking(c);
            }
 
            if (simoptions.continuousOutput())
                f.setPiecewiseLinearSignals();
            else
                f.setPiecewiseConstantSignals();
            
        }
        

        //3. simulate until stop condition is true. Possibly add max runs threshold
        boolean[] finished = new boolean[n];
        boolean stop;
        for (int i=0;i<n;i++) finished[i] = false;
        do {
            collector.newTrajectory();
            simulator.resetModel(true);
            simulator.reinitialize();
            simulator.run();
            Trajectory traj = collector.getLastFullTrajectory();
            
            stop = true;
            for (int i=0;i<n;i++) {
                if (!finished[i]) {
                    switch (options.type) {
                        case  POINTWISE :
                            finished[i] = formulas.get(i).modelCheckNextTrajectoryPointwiseSemantics(traj);
                            break;
                        case SIGNAL :
                            finished[i] = formulas.get(i).modelCheckNextTrajectorySignalSemantics(traj);
                            if (finished[i]) {
                                formulas.get(i).storeSignalInController();
                            }
                            break;
                        case  ROBUST:
                            break;
                        default :
                            throw new RuntimeException("What a mess 3, this seems quite impossible!");
                    }
                }
                stop = stop && finished[i];
            } 
        } while (!stop);
              
        //4. return the SMCoutput of the formula.   
        ArrayList<SMCoutput> list = new ArrayList<SMCoutput>();
        for (int i=0;i<n;i++) {
            SMCoutput out = controllers.get(i);
            list.add(out);
        }
        time = System.nanoTime() - time;
        this.lastExecutionTime = (double)time/1000000000;
        return list;
    }

    
    
    
    
    public double getLastExecutionTime() {
        return lastExecutionTime;
    }
    
    
    
    
    public ArrayList<SMCoutput> modelCheck(ArrayList<String> formulaNames, String parameter, int points, double[] values) {
        ArrayList<MTLformula> list = new ArrayList<MTLformula>();
        for (String formulaName : formulaNames) {
            MTLformula formula = this.namesToMTLformulas.get(formulaName);
            if (formula == null)
                throw new RuntimeException("SMC: formula " + formulaName + " does not exist");
            list.add(formula);
        }
        return runModelChecking(list,parameter,points,values);
        

    }
    
    
    
    /**
     * Actually does the model checking
     * @param formulas
     * @return 
     */
    private ArrayList<SMCoutput> runModelChecking(ArrayList<MTLformula> formulas, String parameter, int points, double[] values) {
        long time = System.nanoTime();
        if (simulator == null)
            this.initialiseSimulator();
        
        //2. initialize model checking for formula
        int n = formulas.size();
        ArrayList<SMCcontroller> controllers = new ArrayList<SMCcontroller>();
        for (MTLformula f : formulas) {
            if (f.estimate()) {
                SMCestimator c = null;
                switch (options.estimationType) {
                    case FREQUENTIST_FIXED :
                        c = new SMCfrequentistEstimator();
                        c.setFixed(options.samples);
                        c.setConfidence(options.confidence);
                        break;
                    case FREQUENTIST_CHERNOFF :
                        c = new SMCfrequentistEstimator();
                        c.setError(options.error);
                        c.setConfidence(options.confidence);
                        c.setChernoff();
                        break;
                    case FREQUENTIST_ADAPTIVE :
                        c = new SMCfrequentistEstimator();
                        c.setError(options.error);
                        c.setConfidence(options.confidence);
                        c.setAdaptive(options.adaptiveStep);
                        break;
                    case BAYESIAN_FIXED :
                        c = new SMCbayesianEstimator();
                        c.setFixed(options.samples);
                        c.setConfidence(options.confidence);
                        c.setPrior(options.priorGood, options.priorBad);
                        break;
                    case BAYESIAN_ADAPTIVE :
                        c = new SMCbayesianEstimator();
                        c.setError(options.error);
                        c.setConfidence(options.confidence);
                        c.setAdaptive(options.adaptiveStep);
                        c.setPrior(options.priorGood, options.priorBad);
                        break;
                    default:
                        throw new RuntimeException("What a mess, this seems quite impossible!");
                }
                c.setMaxCalls(options.maxRuns);
                if (options.confidenceType == ConfidenceBoundsMethod.WILSON)
                    c.useWilson();
                else if (options.confidenceType == ConfidenceBoundsMethod.NORMAL)
                    c.useNormal();
                if (options.regularise) 
                    c.regularise(options.priorGood, options.priorBad);
                c.setFormula(f.getName());
                if (!f.containsParameter(parameter)) {                    
                    controllers.add(c);
                    f.initializeForEstimate(c);
                } else {
                    SMCestimateTrajectory ee = new SMCestimateTrajectory(points,values,parameter);
                    ee.setEstimators(c);
                    ee.setFormula(f.getName());
                    controllers.add(ee);
                    f.initializeForEstimate(ee);
                }
            } else {
                SMCtest c = null;
                switch (options.testType) {
                    case WALD :
                        c = new SMCtestWald(f.getThreshold());
                        break;
                    case BAYESIAN :
                        c = new SMCtestBayesian(f.getThreshold());
                        break;
                    default:
                        throw new RuntimeException("What a mess 2, this seems quite impossible!");
                }
                c.setTestPower(options.significance, options.power);
                c.setThresholdTolerance(options.epsilon);
                c.setFormula(f.getName());
                controllers.add(c);
                f.initializeForModelChecking(c);
            }
 
            if (simoptions.continuousOutput())
                f.setPiecewiseLinearSignals();
            else
                f.setPiecewiseConstantSignals();
            
        }
        

        //3. simulate until stop condition is true. Possibly add max runs threshold
        boolean[] finished = new boolean[n];
        boolean stop;
        for (int i=0;i<n;i++) finished[i] = false;
        do {
            collector.newTrajectory();
            simulator.resetModel(true);
            simulator.reinitialize();
            simulator.run();
            Trajectory traj = collector.getLastFullTrajectory();
            
            
            String[] pp = {parameter};
            double[] vv = new double[1];
            //outer loop: on parameter values. Inner loop on formulae?
            for (int j=0;j<points;j++) {
                //WARNING: così resetto i parametri per tutte le formule
                vv[0] = values[j];
                this.setFormulaeParameters(pp, vv);
                for (int i=0;i<n;i++) { 
                    if (!finished[i] && controllers.get(i).isExplorator()) {
                        controllers.get(i).setCurrent(j);
                        switch (options.type) {
                            case  POINTWISE :
                                formulas.get(i).modelCheckNextTrajectoryPointwiseSemantics(traj);
                                finished[i] = controllers.get(i).stop();
                                break;
                            case SIGNAL :
                                formulas.get(i).modelCheckNextTrajectorySignalSemantics(traj);
                                finished[i] = controllers.get(i).stop();
                                if (finished[i])
                                    formulas.get(i).storeSignalInController();
                                break;
                            case  ROBUST:
                                break;
                            default :
                                throw new RuntimeException("What a mess 3, this seems quite impossible!");
                        }
                    }
                    else if (!finished[i] && j == points-1) {
                        switch (options.type) {
                            case  POINTWISE :
                                finished[i] = formulas.get(i).modelCheckNextTrajectoryPointwiseSemantics(traj);
                                break;
                            case SIGNAL :
                                finished[i] = formulas.get(i).modelCheckNextTrajectorySignalSemantics(traj);
                                if (finished[i])
                                    formulas.get(i).storeSignalInController();
                                break;
                            case  ROBUST:
                                break;
                            default :
                                throw new RuntimeException("What a mess 3, this seems quite impossible!");
                        }
                    }
                    
                }
            }
            //check stop condition;
            stop = true;
            for (int i=0;i<n;i++)
                stop = stop && finished[i];
        } while (!stop);
              
        //4. return the SMCoutput of the formula.   
        ArrayList<SMCoutput> list = new ArrayList<SMCoutput>();
        for (int i=0;i<n;i++) {
            SMCoutput out = controllers.get(i);
            list.add(out);
        }
        time = System.nanoTime() - time;
        this.lastExecutionTime = (double)time/1000000000;
        return list;
    }
    
    
    public void resetSimulator() {
        this.simulator = null;
        this.collector = null;
    } 
    
   
    void initialiseSimulator() {
        switch (simoptions.simType) {
            case SSA : 
                this.initSSASimulator(simoptions.finalTime);
                break;
            case GB: 
                this.initGBSimulator(simoptions.finalTime);
                break;
            case ODE: 
                this.initODESimulator(simoptions.finalTime);
                break;
            case HYBRID: 
                this.initHybridSimulator(simoptions.finalTime);
                break;
            case LN: 
                this.initLNSimulator(simoptions.finalTime);
                break;
            default :
                throw new RuntimeException("Some incredible mess happened here!!!");
        }
    }
    
    
    void initSSASimulator(double finalTime) {
        if (model == null)
            throw new RuntimeException("Load model first");
        
        collector = new StochasticDataCollector(model);
        collector.storeStrategySMC();
        collector.clearAll();
        simulator = SimulatorFactory.newSSAsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
    }
    
    void initGBSimulator(double finalTime) {
        if (model == null)
            throw new RuntimeException("Load model first");
        
        collector = new StochasticDataCollector(model);
        collector.storeStrategySMC();
        collector.clearAll();
        simulator = SimulatorFactory.newGBsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
    }
    
    
    
    void initHybridSimulator(double finalTime) {
        if (model == null)
            throw new RuntimeException("Load model");
        
        collector = new HybridDataCollector(model);
        collector.storeStrategySMC();
        collector.clearAll();
        simulator = SimulatorFactory.newHybridSimulator(model, collector, false);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
       
    }
    
    void initODESimulator(double finalTime) {
        if (model == null)
            throw new RuntimeException("Load model first");
        
        collector = new OdeDataCollector(model);
        collector.storeStrategySMC();
        collector.clearAll();
        simulator = SimulatorFactory.newODEsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        if (simoptions.integrator == IntegratorType.EULER) {
            if (simulator instanceof OdeSimulator) {
                OdeSimulator sim = (OdeSimulator)simulator;
                sim.setMinimumStepSize(simoptions.stepSize);
                sim.setIntegrator(IntegratorType.EULER);
            }
//            else if (simulator instanceof OdeSimulatorWithEvents) {
//                OdeSimulatorWithEvents sim = (OdeSimulatorWithEvents)simulator;
//                sim.setMinimumStepSize(simoptions.stepSize);
//                sim.setIntegrator(IntegratorType.EULER);
//            }
        }
        simulator.initialize();
    }
    
    void initLNSimulator(double finalTime) {
        if (model==null)
            throw new RuntimeException("Load model first");
        FlatModel m =  model.generateLinearNoiseApproximation();
        
        collector = new OdeDataCollector(model);
        collector.storeStrategySMC();
        collector.clearAll();
        simulator = SimulatorFactory.newODEsimulator(model, collector);
        simulator.setFinalTime(finalTime);
        simulator.getProgressMonitor().setSilent(true);
        simulator.initialize();
    }
    
      
    
    public void setParameters(ArrayList<String> names, double[] values) {
        ArrayList<String> fp = new  ArrayList<String>();
        double[] fv = new double[values.length];
        int c = 0;
        for (int i=0;i<names.size();i++) {
            String s = names.get(i);
            double v = values[i];
            if (model.containsExplorableParameter(s)) {
                model.changeInitialValueOfParameter(s, v);
            } 
            else if (model.containsExplorableVariable(s)) {
                model.changeInitialValueOfVariable(s, v);
            }
            else {
                fp.add(s);
                fv[c++] = v;
            }
        }
        fv = Arrays.trimToCapacity(fv, c);
        this.setFormulaeParameters(fp.toArray(new String[0]), fv);
        model.computeInitialValues();
    }
    
    
    public void setParameter(String name, double value) {  
        if (model.containsExplorableParameter(name)) {
            model.changeInitialValueOfParameter(name, value);
            model.computeInitialValues();
        } 
        else if (model.containsExplorableVariable(name)) {
            model.changeInitialValueOfVariable(name, value);
            model.computeInitialValues();
        }
        else {
            ArrayList<String> fp = new  ArrayList<String>();
            double[] fv = new double[1];
            fp.add(name);
            fv[0] = value;
            this.setFormulaeParameters(fp.toArray(new String[0]), fv);
        }   
    }
    
    
}
