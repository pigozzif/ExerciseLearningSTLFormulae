package eggloop.flow.simhya.simhya.matlab.genetic;

import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.matlab.BasicSimulator;
import eggloop.flow.simhya.simhya.matlab.SimHyAModel;
import eggloop.flow.simhya.simhya.model.store.faststore.FastStore;
import eggloop.flow.simhya.simhya.modelchecking.SMCestimator;
import eggloop.flow.simhya.simhya.modelchecking.SMCfrequentistEstimator;
import eggloop.flow.simhya.simhya.modelchecking.mtl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * @author luca
 */
public class FormulaPopulation {
    ArrayList<Formula> population;
    ArrayList<Formula> newPopulation;
    TreeSet<Solution> bestSolutions;

    int size;
    FitnessFunction fitness;
    HashMap<String, Double> lowerBound, upperBound;


    FastStore store;
    SymbolArray parameters;
    ArrayList<String> variables;
    FormulaGenOps operators;
    SimHyAModel model1, model2;


    public FormulaPopulation(int size) {
        this.size = size;
        this.population = new ArrayList<Formula>();
        fitness = null;
        model1 = null;
        model2 = null;
        lowerBound = new HashMap<String, Double>();
        upperBound = new HashMap<String, Double>();
        parameters = new SymbolArray();
        store = new FastStore();
        variables = new ArrayList<String>();
        operators = new FormulaGenOps(this, parameters);
        this.bestSolutions = new TreeSet<Solution>();
        this.setFitness(GeneticOptions.fitness_type);
    }


    public FormulaPopulation(SimHyAModel m1, SimHyAModel m2, int size) {
        this.size = size;
        this.population = new ArrayList<Formula>();
        fitness = null;
        lowerBound = new HashMap<String, Double>();
        upperBound = new HashMap<String, Double>();
        parameters = new SymbolArray();
        model1 = m1;
        model2 = m2;
        store = new FastStore();
        variables = new ArrayList<String>();
        operators = new FormulaGenOps(this, parameters);
        this.bestSolutions = new TreeSet<Solution>();
        this.setFitness(GeneticOptions.fitness_type);
    }


    public double getLowerBound(String var) {
        return this.lowerBound.get(var);
    }

    public double getUpperBound(String var) {
        return this.upperBound.get(var);
    }


    public int getVariableNumber() {
        return this.variables.size();
    }


    public String getVariable(int i) {
        return this.variables.get(i);
    }


    public void addVariable(String V, double lower, double upper) {
        store.addVariable(V, 0);
        this.variables.add(V);
        this.lowerBound.put(V, lower);
        this.upperBound.put(V, upper);
    }


    /**
     * Adds a parameter to the parameter array, if not present, and replaces its
     * value otherwise.
     *
     * @param P
     * @param v
     */
    public void addFormulaParameter(String P, double v) {
        if (this.parameters.containsSymbol(P)) {
            int id = this.parameters.getSymbolId(P);
            this.parameters.setValue(id, v);
        } else {
            this.parameters.addSymbol(P, v);
        }

    }


    public FormulaGenOps getGeneticOperators() {
        return operators;
    }


    public int addRandomFormula() {
        Formula f = operators.randomFormula();
        this.newPopulation.add(f);
        return this.newPopulation.indexOf(f);
    }
    public void addGeneticInitFormula(int n) {
        List<Formula> formulas = operators.atomicGeneticFormula(n);
        this.population.addAll(formulas);

    }

    public void addRandomInitFormula() {
        Formula f = operators.randomFormula();
        this.population.add(f);
    }


    public void removeFormulaNewPopulation(int i) {
        if (this.newPopulation.size() <= i)
            throw new RuntimeException("There are less than " + i + " elements in the new pop!");
        this.newPopulation.remove(i);
    }


    public void addGoodSolution(Formula f, double fitness) {
        if (this.bestSolutions.size() < GeneticOptions.solution_set_size) {
            Formula f1 = operators.duplicate(f);
            this.bestSolutions.add(new Solution(fitness, f1));
        } else {
            Solution s = this.bestSolutions.first();
            if (s.fitness < fitness) {
                this.bestSolutions.remove(s);
                Formula f1 = operators.duplicate(f);
                this.bestSolutions.add(new Solution(fitness, f1));
            }
        }
    }

    private ArrayList<Formula> getSolutionFormulae() {
        ArrayList<Formula> list = new ArrayList<Formula>();
        for (Solution s : this.bestSolutions) {
            list.add(s.formula);
        }
        return list;
    }


    private void setFitness(String type) {
        if (type.equals("regularised_logodds")) {
            fitness = new RegularisedLogOddRatioFitness();
        } else if (type.equals("logodds")) {
            fitness = new LogOddRatioFitness();
        } else {
            fitness = new LogOddRatioFitness();
        }
    }


    public Formula getFormula(int i) {
        return this.population.get(i);
    }

    public void addFormula(Formula f) {
        this.population.add(f);
    }


    public void setFormula(Formula f, int i) {
        if (this.population.size() > i)
            this.population.set(i, f);
    }


    public void addFormula(String f) {
        Formula F = this.operators.loadFormula(f);
        this.population.add(F);
    }


    public void setFormula(String f, int i) {
        if (this.population.size() > i) {
            Formula F = this.operators.loadFormula(f);
            this.population.set(i, F);
        }
    }


    public double evaluateFitness(int i, double p1, double p2, double undef1, double undef2, int runs) {
        if (this.population.size() > i) {
            int n = this.population.get(i).getFormulaSize();
            return this.fitness.compute(p1, p2, n, undef1, undef2, runs);
        } else return Double.NEGATIVE_INFINITY;
    }


    public double evaluateFitness(Formula f, double p1, double p2, double undef1, double undef2, int runs) {
        int n = f.getFormulaSize();
        return this.fitness.compute(p1, p2, n, undef1, undef2, runs);
    }

    /**
     * Evaluates fitness for formula i, for a loaded model, if  any
     *
     * @param i
     * @return
     */
//    public double evaluateFitness(int i, double finaltime, int runs) {
//        if (this.model1 != null && this.model2 != null && this.population.size() > i) {
//            int n = this.population.get(i).getFormulaSize();
//            double p1,p2;
//            MTLformula f = this.population.get(i).formula;
//            //check fomrula on model 1
//            f.initalize(model1.getModel().getStore(), parameters, null);
//            p1 = 0;
//            for (int j=0;j<runs;j++) {
//              double[][] x = model1.simulate(finaltime);
//              SMCestimator est = new SMCestimator(1,false);
//              f.initializeForEstimate(est);
//              f.modelCheckNextTrajectoryPointwiseSemantics(x);
//              if (est.getLast().isUndefined())
//                   throw new RuntimeException("Trajectory is not lasting enough and cannot be checked. Simulate longer");
//              p1 += ( est.getLast().isTrue() ? 1 : 0 );
//            }
//            p1 /= runs;
//            //ceck formula on model 2
//            f.initalize(model2.getModel().getStore(), parameters, null);
//            p2 = 0;
//            for (int j=0;j<runs;j++) {
//              double[][] x = model2.simulate(finaltime);
//              SMCestimator est = new SMCestimator(1,false);
//              f.initializeForEstimate(est);
//              f.modelCheckNextTrajectoryPointwiseSemantics(x);
//              if (est.getLast().isUndefined())
//                   throw new RuntimeException("Trajectory is not lasting enough and cannot be checked. Simulate longer");
//              p2 += ( est.getLast().isTrue() ? 1 : 0 );
//            }
//            p2 /= runs;
//            return this.fitness.compute(p1, p2,n);
//        } else return Double.NEGATIVE_INFINITY;
//    }
    public int getPopulationSize() {
        return population.size();
    }

    public int getNewPopulationSize() {
        return newPopulation.size();
    }

    /**
     * Model checks formula i
     *
     * @param trajectory
     * @param i
     * @return
     */
    public int modelCheck(double[][] trajectory, int i) {
        if (i < this.population.size()) {
            SMCestimator est = new SMCfrequentistEstimator();
            est.setFixed(1);
            MTLformula f = this.population.get(i).formula;
            f.initializeForEstimate(est);
            f.modelCheckNextTrajectoryPointwiseSemantics(trajectory);
            Truth c = est.getLast();
            if (c.isTrue()) return 1;
            else if (c.isFalse()) return 0;
            else return -1;
        } else
            throw new RuntimeException("Formula " + i + " does not exist.");
    }


    public int[] modelCheck(BasicSimulator sim, int i, int runs, double tf) {
        if (i < this.population.size()) {
            int[] b = new int[runs];
            SMCestimator est = new SMCfrequentistEstimator();
            est.setFixed(runs);
            MTLformula f = this.population.get(i).formula;
            f.initializeForEstimate(est);
            for (int j = 0; j < runs; j++) {
                double[][] x = sim.simulate(tf);
                f.modelCheckNextTrajectoryPointwiseSemantics(x);
                Truth c = est.getLast();
                if (c.isTrue()) b[j] = 1;
                else if (c.isFalse()) b[j] = 0;
                else b[j] = -1;
            }
            return b;
        } else
            throw new RuntimeException("Formula " + i + " does not exist.");
    }

    public int modelCheck(double[][] trajectory, Formula F) {
        //warning. The formula may not be part of the current population  
        //or best solution set. This is unchecked.
        SMCestimator est = new SMCfrequentistEstimator();
        est.setFixed(1);
        MTLformula f = F.formula;
        f.initializeForEstimate(est);
        f.modelCheckNextTrajectoryPointwiseSemantics(trajectory);
        Truth c = est.getLast();
        if (c.isTrue()) return 1;
        else if (c.isFalse()) return 0;
        else return -1;
    }

    public int[] modelCheckSimple(double[][] trajectory, Formula F) {
        int[] b = new int[trajectory.length];
        SMCestimator est = new SMCfrequentistEstimator();
        est.setFixed(trajectory.length);
        MTLformula f = F.formula;
        f.initializeForEstimate(est);
        for (int j = 0; j < trajectory.length; j++) {
            double[][] x = new double[][]{trajectory[j]};
            f.modelCheckNextTrajectoryPointwiseSemantics(x);
            Truth c = est.getLast();
            if (c.isTrue()) b[j] = 1;
            else if (c.isFalse()) b[j] = 0;
            else
                b[j] = -1;
        }
        return b;
    }

    public int[] modelCheck(BasicSimulator sim, Formula F, int runs, double tf) {
        int[] b = new int[runs];
        SMCestimator est = new SMCfrequentistEstimator();
        est.setFixed(runs);
        MTLformula f = F.formula;
        f.initializeForEstimate(est);
        for (int j = 0; j < runs; j++) {
            double[][] x = sim.simulate(tf);
            f.modelCheckNextTrajectoryPointwiseSemantics(x);
            Truth c = est.getLast();
            if (c.isTrue()) b[j] = 1;
            else if (c.isFalse()) b[j] = 0;
            else
                b[j] = -1;
        }
        return b;
    }


    public void generateInitialPopulation() {
        this.population = new ArrayList<Formula>();
        for (int i = 0; i < size; i++) {
            Formula f = operators.randomFormula();
            this.population.add(f);
        }
    }


    public void initialiseNewGeneration() {
        this.newPopulation = new ArrayList<Formula>();
    }

    /**
     * Selects formula i of the old generation and copies it to the new generation
     *
     * @param i
     * @return the index of the formula in the new generation
     */
    public int selectFormula(int i) {
        if (i < 0 || i >= this.population.size())
            return -1;
        Formula f = operators.duplicate(this.population.get(i));
        this.newPopulation.add(f);
        return this.newPopulation.indexOf(f);
    }

    public int addNewFormula(Formula f) {
        Formula ff = operators.duplicate(f);
        this.newPopulation.add(ff);
        return this.newPopulation.indexOf(ff);
    }


    public Formula getFormulaNewGeneration(int i) {
        return this.newPopulation.get(i);
    }


    public void crossoverNewGeneration(int i, int j) {
        if (this.newPopulation.size() > Math.max(i, j)) {
            operators.crossover(this.newPopulation.get(i), this.newPopulation.get(j));
        }
    }

    public void unionNewGeneration(int i, int j) {
        if (this.newPopulation.size() > Math.max(i, j)) {
            operators.union(this.newPopulation.get(i), this.newPopulation.get(j));
        }
    }

    public void mutateNewGeneration(int i) {
        if (this.newPopulation.size() > i) {
            operators.mutate(this.newPopulation.get(i));
        }
    }

    public void setFormulaNewGeneration(Formula f, int j) {
        if (this.newPopulation.size() > j) {
            operators.mutate(this.newPopulation.set(j, f));
        }
    }

    /**
     * call when the operations to construct the new generation are over.
     */
    public void finaliseNewGeneration() {
        //copy the parameter vector keeping only survived parameters 

        //       System.out.println("*****Finalising generation");

        SymbolArray newParameters = new SymbolArray();
        ArrayList<Formula> listF = this.getSolutionFormulae();
        listF.addAll(this.newPopulation);
        for (Formula F : listF) {
//            System.out.println("Processing fomrula " + F.toString());
            ArrayList<MTLnode> list = F.getAllNodes();
            for (MTLnode n : list) {
                if (n.isAtom()) {
                    String s = this.extractThreshold(n);

//                    System.out.println("Saving parameter " + s);

                    int j = this.parameters.getSymbolId(s);
                    double v = this.parameters.getValue(j);
                    newParameters.addSymbol(s, v);
                } else if (n.isModal()) {
                    ParametricInterval interval = ((MTLmodalNode) n).getParametricInterval();
                    String s = interval.getLowerParameter();

//                    System.out.println("Saving parameter " + s);
                    try {
                        int j = this.parameters.getSymbolId(s);
                        double v = this.parameters.getValue(j);
                        newParameters.addSymbol(s, v);

//                    System.out.println("Saving parameter " + s);

                        s = interval.getUpperParameter();
                        j = this.parameters.getSymbolId(s);
                        v = this.parameters.getValue(j);
                        newParameters.addSymbol(s, v);
                    } catch (Exception ex) {
                        continue;
                    }

                }
            }
        }
        this.parameters = newParameters;
        this.population = this.newPopulation;
        this.operators.parameters = this.parameters;
        newPopulation = null;
        System.gc();

//        System.out.println("*****Finalisation completed. Symbols saved:");
//        for (String s : parameters.getNameOfAllSymbols())
//            System.out.println(s);

    }

    private String extractThreshold(MTLnode n) {
        if (n == null) return null;
        if (n.isAtom()) {
            String s = ((MTLatom) n).getPredicateString();
            int i = s.lastIndexOf("_");
            return GeneticOptions.threshold_name + s.substring(i);
        } else return "";

    }

    public double[] getParameterValues(String[] names) {
        if (names == null) return new double[0];

        double[] vals = new double[names.length];
        for (int i = 0; i < names.length; i++)
            vals[i] = this.parameters.getValue(this.parameters.getSymbolId(names[i]));
        return vals;
    }


    public void setParameters(String[] names, double[] values) {
        if (names == null || values == null) return;
        for (int j = 0; j < names.length; j++) {
            int i = this.parameters.getSymbolId(names[j]);
            this.parameters.setValue(i, values[j]);
        }
        if (this.population != null)
            for (Formula f : this.population)
                f.recomputeBounds(parameters);
        if (this.newPopulation != null)
            for (Formula f : this.newPopulation)
                f.recomputeBounds(parameters);
    }


    public Formula loadFormula(String formula, String[] parameters, double[] values) {
        for (int i = 0; i < parameters.length; i++)
            this.addFormulaParameter(parameters[i], values[i]);
        Formula f = this.operators.loadFormula(formula);
        this.addFormula(f);
        return f;
    }


    class Solution implements Comparable {
        double fitness;
        Formula formula;

        public Solution(double fitness, Formula formula) {
            this.fitness = fitness;
            this.formula = formula;
        }

        public int compareTo(Object t) {
            if (t instanceof Solution) {
                Solution s = (Solution) t;
                if (this.fitness < s.fitness)
                    return -1;
                else if (this.fitness > s.fitness)
                    return 1;
                else return 0;
            } else throw new RuntimeException("Uncomparable objects");
        }

        @Override
        public boolean equals(Object o) {
            return this.compareTo(o) == 0;
        }


    }


}
