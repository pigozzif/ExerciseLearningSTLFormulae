package com.eggloop.flow.learning;

import com.eggloop.flow.simhya.simhya.matlab.genetic.Formula;
import com.eggloop.flow.simhya.simhya.matlab.genetic.FormulaPopulation;

import java.util.*;
import java.util.stream.IntStream;


public class GeneticPopulation {

    private List<Formula> rankFormulae;
    private List<double[]> rankParameters;
    private List<Double> rankScore;

    GeneticPopulation(List<Formula> rankFormulae, List<double[]> rankParameters, List<Double> rankScore) {
        this.rankFormulae = rankFormulae;
        this.rankParameters = rankParameters;
        this.rankScore = rankScore;
    }

    private List<Formula> getRankFormulae() {
        return rankFormulae;
    }

    private List<double[]> getRankParameters() {
        return rankParameters;
    }

    private List<Double> getRankScore() {
        return rankScore;
    }

    void sort() {
        int[] sortedIndices = IntStream.range(0, rankScore.size())
                .boxed().sorted(Comparator.comparingDouble(rankScore::get))
                .mapToInt(ele -> ele).toArray();
        List<Formula> rankFormulaePOrd = new ArrayList<>();
        List<double[]> rankParametersPOrd = new ArrayList<>();
        List<Double> rankScorePOrd = new ArrayList<>();
        for (int sortedIndice : sortedIndices) {
            rankFormulaePOrd.add(rankFormulae.get(sortedIndice));
            rankParametersPOrd.add(rankParameters.get(sortedIndice));
            rankScorePOrd.add(rankScore.get(sortedIndice));
        }
        rankFormulae = rankFormulaePOrd;
        rankParameters = rankParametersPOrd;
        rankScore = rankScorePOrd;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rankFormulae.size(); i++) {
            builder.append("::phi:: ").append(rankFormulae.get(i).toString()).append(" ::score:: ").append(rankScore.get(i)).append(" ::parameters:: ").append(Arrays.toString(rankParameters.get(i))).append("\n");
        }
        return builder.toString();
    }

    Formula getBestFromula() {
        int lastFormulaOfArray = rankFormulae.size() - 1;
        return rankFormulae.get(lastFormulaOfArray);
    }

    double[] getBestFormulaParameters() {
        int lastFormulaOfArray = rankFormulae.size() - 1;
        return rankParameters.get(lastFormulaOfArray);
    }

    GeneticPopulation getBestHalf() {
        rankFormulae = rankFormulae.subList(rankScore.size() - rankScore.size() / 2, rankScore.size());
        rankParameters = rankParameters.subList(rankScore.size() - rankScore.size() / 2, rankScore.size());
        rankScore = rankScore.subList(rankScore.size() - rankScore.size() / 2, rankScore.size());
        return new GeneticPopulation(rankFormulae,rankParameters,rankScore);
    }

    void addall(GeneticPopulation betsHalfGeneration) {
        rankFormulae.addAll(betsHalfGeneration.getRankFormulae());
        rankParameters.addAll(betsHalfGeneration.getRankParameters());
        rankScore.addAll(betsHalfGeneration.getRankScore());
    }

    void geneticOperations(Random ran, FormulaPopulation pop) {
        List<Double> rankScoreParents = getRankScore();
        List<Formula> rankFormulaeParents = getRankFormulae();
        double[] cumScore = cum(rankScoreParents);
        pop.initialiseNewGeneration();
        for (int i = 0; i < rankFormulaeParents.size(); i++) {
            //first parent (a)
            int a = extract(cumScore, ran);
            int b = a;
            //second parent (b)
            while (b == a) {
                b = extract(cumScore, ran);
            }
            double v = ran.nextDouble();
            if (v > 0.3) {
                int indexNewA = pop.addNewFormula(rankFormulaeParents.get(a));
                int indexNewB = pop.addNewFormula(rankFormulaeParents.get(b));
                //recombination operator, modify both newA and newB
                pop.crossoverNewGeneration(indexNewA, indexNewB);
            } else if (v > 0.0) {
                //mutation newA
                int indexNewA = pop.addNewFormula(rankFormulaeParents.get(a));
                int indexNewB = pop.addNewFormula(rankFormulaeParents.get(b));
                pop.mutateNewGeneration(indexNewB);
                pop.mutateNewGeneration(indexNewA);
            } else {
                //mutation newA
                int indexNewA = pop.addNewFormula(rankFormulaeParents.get(a));
                int indexNewB = pop.addNewFormula(rankFormulaeParents.get(b));
                pop.unionNewGeneration(indexNewA, indexNewB);
            }
        }
    }

    private static int extract(double[] cum, Random ran) {
        double r = ran.nextDouble();
        for (int i = 0; i < cum.length; i++) {
            if (cum[i] > r) {
                return i - 1;
            }
        }
        return cum.length - 1;
    }

    private static double[] cum(List<Double> w) {
        double[] res = new double[w.size() + 1];
        for (int i = 1; i < res.length; i++) {
            res[i] = res[i - 1] + w.get(i - 1);
        }
        return Arrays.stream(res).map(s -> s / res[res.length - 1]).toArray();
    }

}