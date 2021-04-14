/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matlab.genetic;

import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.modelchecking.mtl.*;
import eggloop.flow.simhya.simhya.modelchecking.mtl.parser.MTLlexer;
import eggloop.flow.simhya.simhya.modelchecking.mtl.parser.MTLparser;
import eggloop.flow.simhya.simhya.utils.RandomGenerator;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author luca
 */
public class FormulaGenOps {
    SymbolArray parameters;
    FormulaPopulation reference_population;
    private MTLlexer MTLlexer;
    private MTLparser MTLparser;

    public FormulaGenOps(FormulaPopulation reference_population, SymbolArray parameters) {
        this.reference_population = reference_population;
        this.parameters = parameters;
    }





    /*
     * needs:
     * 1. variable names and ranges
     * 2. upper and lower temporal bounds (per operator)
     * 3. distribution on operators for the formula generation
     * 4. distribution on operators for mutation?
     * 5. distribution on nodes for cross over?
     */


    /**
     * Generates a random formula with a random number of atomic nodes.
     *
     * @return
     */
    public Formula randomFormula() {
        int n;
        if (GeneticOptions.init__random_number_of_atoms)
            n = 1 + RandomGenerator.nextGeometric(1 / GeneticOptions.init__average_number_of_atoms);
        else
            n = GeneticOptions.init__fixed_number_of_atoms;
        MTLnode root = this.generateRandomFormula(n);
        MTLformula f = new MTLformula(root);
        f.initalize(this.reference_population.store, parameters, null);

        Formula F = new Formula(f);
        return F;
    }

    List<Formula> atomicGeneticFormula(int n) {
        List<Formula> res = new ArrayList<>();
        List<MTLnode> mtLnodes = generateAtomicGeneticFormula(n);

        List<MTLformula> f = new ArrayList<>();
        for (MTLnode mtLnode : mtLnodes) {
            MTLformula e = new MTLformula(mtLnode);
            e.initalize(this.reference_population.store, parameters, null);
            f.add(e);
        }
        for (MTLformula mtLformula : f) {
            res.add(new Formula(mtLformula));
        }
        return res;
    }


    public Formula loadFormula(String formula) {
        initMTLparser(formula);
        try {
            MTLformula f = (MTLformula) (MTLparser.parse().value);
            f.initalize(this.reference_population.store, parameters, null);
            Formula F = new Formula(f);
            return F;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cannot parse formula");
        }

    }


    public Formula debugFormula(boolean single) {
        String t1 = "t1", t2 = "t2", p1 = "p1", p2 = "p2";
        String atom1 = "flow1" + " <= " + p1;
        String atom2 = "flow" + " <= " + p2;
        MTLatom n1, n2 = null;
        MTLand n3 = null;
        MTLglobally n4 = null;
        n1 = new MTLatom(atom1, true);
        if (!single) {
            n2 = new MTLatom(atom2, true);
            n3 = new MTLand(n1, n2);
        }
        ParametricInterval i = new ParametricInterval();
        i.setLower(t1);
        i.setUpper(t2);
        if (!single)
            n4 = new MTLglobally(i, n3);
        else
            n4 = new MTLglobally(i, n1);
        MTLformula f = new MTLformula(n4);
        f.initalize(this.reference_population.store, parameters, null);
        Formula F = new Formula(f);
        return F;
    }


    private void initMTLparser(String formula) {
        if (MTLlexer == null || MTLparser == null) {
            MTLlexer = new MTLlexer(new StringReader(formula));
            MTLparser = new MTLparser(MTLlexer);
        } else {
            MTLlexer.yyreset(new StringReader(formula));
        }
    }

    private List<MTLnode> generateAtomicGeneticFormula(int n) {
        ArrayList<MTLnode> res = new ArrayList<>();
        ArrayList<MTLnode> init = new ArrayList<>();
        ArrayList<MTLnode> or = new ArrayList<>();
        ArrayList<MTLnode> and = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            init.add(newAtomicNodeCustom(true, i, true));
            init.add(newAtomicNodeCustom(true, i, false));
        }
        List<int[]> index = combinatorics2(init.size());
        for (int[] e : index) {
            MTLand e1 = new MTLand(init.get(e[0]), init.get(e[1]));
            e1.initalize(reference_population.store, parameters, null);
            and.add(e1);
        }
        //or
        for (int[] e : index) {
            MTLor e1 = new MTLor(init.get(e[0]), init.get(e[1]));
            e1.initalize(reference_population.store, parameters, null);
            or.add(e1);
        }

        //Ginit
        for (MTLnode mtLnode : init) {
            long id = MTLnode.getNextID();
            ParametricInterval interval = newInterval(id);
            MTLglobally mtLglobally = new MTLglobally(interval, mtLnode);
            mtLglobally.initalize(reference_population.store, parameters, null);
            res.add(mtLglobally);

        }
        //Finit
        for (MTLnode mtLnode : init) {
            long id = MTLnode.getNextID();
            ParametricInterval interval = newInterval(id);
            MTLeventually mtLglobally = new MTLeventually(interval, mtLnode);
            mtLglobally.initalize(reference_population.store, parameters, null);
            res.add(mtLglobally);

        }
        //GO
        for (MTLnode mtLnode : or) {
            long id = MTLnode.getNextID();
            ParametricInterval interval = this.newInterval(id);
            MTLglobally mtLglobally = new MTLglobally(interval, mtLnode);
            mtLglobally.initalize(this.reference_population.store, parameters, null);
            res.add(mtLglobally);
        }
        //FA
        for (MTLnode mtLnode : and) {
            long id = MTLnode.getNextID();
            ParametricInterval interval = newInterval(id);
            MTLeventually mtLglobally = new MTLeventually(interval, mtLnode);
            mtLglobally.initalize(reference_population.store, parameters, null);
            res.add(mtLglobally);
        }
        //U
        for (int[] e : index) {
            long id = MTLnode.getNextID();
            ParametricInterval interval = newInterval(id);
            MTLuntil e1 = new MTLuntil(interval, init.get(e[0]), init.get(e[1]));
            e1.initalize(reference_population.store, parameters, null);
            res.add(e1);
        }
        return res;
    }

    public static List<int[]> combinatorics2(int n) {
        List<int[]> res = new ArrayList<>();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                res.add(new int[]{i, j});
                res.add(new int[]{j, i});
            }
        }
        return res;

    }

    private MTLnode generateRandomFormula(int atomNumber) {
        ArrayList<MTLnode> nodes = new ArrayList<MTLnode>();
        //generate atoms
        for (int i = 0; i < atomNumber; i++) {
            if (RandomGenerator.flipCoin(GeneticOptions.init__prob_of_true_atom))
                nodes.add(new MTLconstantAtom(true));
            else
                nodes.add(this.newAtomicNode(false));
        }
        int count = 0;
        while (true && count < 3) {
            count++;
            //System.out.println("****************************************");
            //System.out.println("Nodes array current status. Elements contained " + nodes.size());
            //for (int i=0;i<nodes.size();i++)
            //    System.out.println(nodes.get(i).toFormulaTree(0));


            //check termination condition
            if (nodes.size() == 1 && nodes.get(0).isModal())
                break;
            //else create a new node

            //choose one or two nodes to merge
            MTLnode c1, c2, n;
            int j;
            j = RandomGenerator.nextInt(nodes.size());
            c1 = nodes.remove(j);
            c2 = null;

            double[] x = {GeneticOptions.init__and_weight * (nodes.size() >= 1 ? 1 : 0),
                    GeneticOptions.init__or_weight * (nodes.size() >= 1 ? 1 : 0),
                    GeneticOptions.init__imply_weight * (nodes.size() >= 1 ? 1 : 0),
                    GeneticOptions.init__not_weight * (c1.getType() == NodeType.NOT ? 0 : 1),
                    GeneticOptions.init__eventually_weight * (c1.getType() == NodeType.EVENTUALLY ? 0 : 1),
                    GeneticOptions.init__globally_weight * (c1.getType() == NodeType.GLOBALLY ? 0 : 1),
                    GeneticOptions.init__until_weight * (nodes.size() >= 1 ? 1 : 0),
                    GeneticOptions.init__eventuallyglobally_weight * (c1.getType() == NodeType.GLOBALLY ? 0 : 1),
                    GeneticOptions.init__globallyeventually_weight * (c1.getType() == NodeType.EVENTUALLY ? 0 : 1)};

            int c = RandomGenerator.sample(x);

            if (!nodes.isEmpty() && (c == 0 || c == 1 || c == 2 || c == 6)) {
                j = RandomGenerator.nextInt(nodes.size());
                c2 = nodes.remove(j);
            }

            switch (c) {
                case 0: //and
                    n = new MTLand(c1, c2);
                    break;
                case 1: //or
                    n = new MTLor(c1, c2);
                    break;
                case 2: //imply
                    n = new MTLimply(c1, c2);
                    break;
                case 3: //not
                    n = new MTLnot(c1);
                    break;
                case 4: //eventually
                    long id = MTLnode.getNextID();
                    ParametricInterval interval = this.newInterval(id);
                    n = new MTLeventually(interval, c1);
                    break;
                case 5: //globally
                    id = MTLnode.getNextID();
                    interval = this.newInterval(id);
                    n = new MTLglobally(interval, c1);
                    break;
                case 6: //until
                    id = MTLnode.getNextID();
                    interval = this.newInterval(id);
                    n = new MTLuntil(interval, c1, c2);
                    break;
                case 7: // eventually globally
                    id = MTLnode.getNextID();
                    interval = this.newInterval(id);
                    MTLnode n1 = new MTLglobally(interval, c1);
                    id = MTLnode.getNextID();
                    interval = this.newInterval(id);
                    n = new MTLeventually(interval, n1);
                    break;
                case 8: //globally eventually
                    id = MTLnode.getNextID();
                    interval = this.newInterval(id);
                    n1 = new MTLeventually(interval, c1);
                    id = MTLnode.getNextID();
                    interval = this.newInterval(id);
                    n = new MTLglobally(interval, n1);
                    break;
                default:
                    n = null;
                    break;
            }
            nodes.add(n);
        }
        return nodes.get(0);
    }


    public Formula mutate(Formula f) {
        ArrayList<MTLnode> nodelist = new ArrayList<MTLnode>();
        double p;
        if (GeneticOptions.mutate__one_node) {
            MTLnode n = f.chooseOneNode();
            nodelist.add(n);
            p = GeneticOptions.mutate__mutation_probability_one_node;
        } else {
            nodelist = f.getAllNodes();
            p = GeneticOptions.mutate__mutation_probability_per_node;
        }
        for (MTLnode n : nodelist) {
            if (RandomGenerator.flipCoin(p)) {
                MTLnode m = this.mutateNode(n);
                if (m != null && m.isRoot())
                    f.setNewRoot(m);
            }
        }
        f.refresh();
        f.formula.initalize(this.reference_population.store, parameters, null);
        return f;
    }


    private double sum(double[] x) {
        double s = 0;
        for (int i = 0; i < x.length; i++)
            s += x[i];
        return s;
    }


    private MTLnode mutateNode(MTLnode n) {
        MTLnode m = null, nn = null;

        double[] x = {GeneticOptions.mutate__insert__weight,
                GeneticOptions.mutate__delete__weight * (n.isInternal() ? 1 : 0),
                GeneticOptions.mutate__replace__weight,
                GeneticOptions.mutate__change__weight * (n.isModal() || n instanceof MTLatom ? 1 : 0)};
        if (sum(x) == 0.0) return null;
        int choice = RandomGenerator.sample(x);
        switch (choice) {
            case 0: //insertion before the node
                double[] x_ins = {GeneticOptions.mutate__insert__eventually_weight * (n.getType() == NodeType.EVENTUALLY ? 0 : 1),
                        GeneticOptions.mutate__insert__globally_weight * (n.getType() == NodeType.GLOBALLY ? 0 : 1),
                        GeneticOptions.mutate__insert__negation_weight * (n.getType() == NodeType.NOT ? 0 : 1)};
                if (sum(x_ins) == 0.0) return null;
                int c_ins = RandomGenerator.sample(x_ins);
                switch (c_ins) {
                    case 0: //eventually
                        nn = n.insert(NodeType.EVENTUALLY);
                        ((MTLmodalNode) nn).setParametricInterval(this.newInterval(nn.getID()));
                        break;
                    case 1: //globally
                        nn = n.insert(NodeType.GLOBALLY);
                        ((MTLmodalNode) nn).setParametricInterval(this.newInterval(nn.getID()));
                        break;
                    case 2: //
                        nn = n.insert(NodeType.NOT);
                        break;
                }
                return nn;
            case 1: //delete the node
                if (n.isBinary()) {
                    if (RandomGenerator.flipCoin(GeneticOptions.mutate__delete__keep_left_node))
                        nn = n.delete(true);
                    else
                        nn = n.delete(false);
                } else if (n.isUnary()) {
                    nn = n.delete(true);
                }
                return nn;
            case 2: //replace the node
                if (n.isModal()) {
                    double[] x_mod_1 = {GeneticOptions.mutate__replace__modal_to_modal_weight,
                            GeneticOptions.mutate__replace__modal_to_bool_weight};
                    if (sum(x_mod_1) == 0.0) return null;
                    int c_mod_1 = RandomGenerator.sample(x_mod_1);
                    switch (c_mod_1) {
                        case 0: //replace a modal node with a modal node
                            double[] x_mod_11 = {GeneticOptions.mutate__replace__eventually_weight * (n instanceof MTLeventually ? 0 : 1),
                                    GeneticOptions.mutate__replace__globally_weight * (n instanceof MTLglobally ? 0 : 1),
                                    GeneticOptions.mutate__replace__until_weight * (n instanceof MTLuntil ? 0 : 1)};
                            if (sum(x_mod_11) == 0.0) return null;
                            int c_mod_11 = RandomGenerator.sample(x_mod_11);
                            ParametricInterval interval = this.newInterval(n.getID());
                            boolean keepLeft = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__keep_left_node);
                            boolean leftNew;
                            if (n instanceof MTLeventually)
                                leftNew = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__new_left_node_for_until_from_eventually);
                            else if (n instanceof MTLglobally)
                                leftNew = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__new_left_node_for_until_from_globally);
                            else
                                leftNew = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__new_left_node_for_until);
                            switch (c_mod_11) {
                                case 0: //eventually
                                    nn = n.changeType(NodeType.EVENTUALLY, keepLeft, null, null, interval);
                                    break;
                                case 1: //globally
                                    nn = n.changeType(NodeType.GLOBALLY, keepLeft, null, null, interval);
                                    break;
                                case 2: //until
                                    m = this.newAtomicNode(true);
                                    nn = n.changeType(NodeType.UNTIL, keepLeft, (leftNew ? m : null), (leftNew ? null : m), interval);
                                    break;
                            }
                            break;
                        case 1:
                            double[] x_mod_12 = {GeneticOptions.mutate__replace__and_weight,
                                    GeneticOptions.mutate__replace__or_weight,
                                    GeneticOptions.mutate__replace__imply_weight,
                                    GeneticOptions.mutate__replace__not_weight};
                            if (sum(x_mod_12) == 0.0) return null;
                            int c_mod_12 = RandomGenerator.sample(x_mod_12);
                            keepLeft = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__keep_left_node);
                            leftNew = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__new_left_node_for_boolean);
                            switch (c_mod_12) {
                                case 0: //and
                                    if (n.isUnary()) {
                                        m = this.newAtomicNode(true);
                                        nn = n.changeType(NodeType.AND, keepLeft, (leftNew ? m : null), (leftNew ? null : m), null);
                                    } else {
                                        nn = n.changeType(NodeType.AND, keepLeft, null, null, null);
                                    }
                                    break;
                                case 1: //or
                                    if (n.isUnary()) {
                                        m = this.newAtomicNode(true);
                                        nn = n.changeType(NodeType.OR, keepLeft, (leftNew ? m : null), (leftNew ? null : m), null);
                                    } else {
                                        nn = n.changeType(NodeType.OR, keepLeft, null, null, null);
                                    }
                                    break;
                                case 2: //imply
                                    if (n.isUnary()) {
                                        m = this.newAtomicNode(true);
                                        nn = n.changeType(NodeType.IMPLY, keepLeft, (leftNew ? m : null), (leftNew ? null : m), null);
                                    } else {
                                        nn = n.changeType(NodeType.IMPLY, keepLeft, null, null, null);
                                    }
                                    break;
                                case 3: //not
                                    m = this.newAtomicNode(true);
                                    nn = n.changeType(NodeType.NOT, keepLeft, null, null, null);
                                    break;
                            }
                            break;
                    }

                } else if (n.isBoolean()) {
                    double[] x_mod_2 = {GeneticOptions.mutate__replace__bool_to_modal_weight,
                            GeneticOptions.mutate__replace__bool_to_bool_weight};
                    if (sum(x_mod_2) == 0.0) return null;
                    int c_mod_2 = RandomGenerator.sample(x_mod_2);
                    switch (c_mod_2) {
                        case 0: //replace a modal node with a modal node
                            double[] x_mod_21 = {GeneticOptions.mutate__replace__eventually_weight,
                                    GeneticOptions.mutate__replace__globally_weight,
                                    GeneticOptions.mutate__replace__until_weight};
                            if (sum(x_mod_21) == 0.0) return null;
                            int c_mod_21 = RandomGenerator.sample(x_mod_21);
                            ParametricInterval interval = this.newInterval(n.getID());
                            boolean keepLeft = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__keep_left_node);
                            boolean leftNew = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__new_left_node_for_until);
                            switch (c_mod_21) {
                                case 0: //eventually
                                    nn = n.changeType(NodeType.EVENTUALLY, keepLeft, null, null, interval);
                                    break;
                                case 1: //globally
                                    nn = n.changeType(NodeType.GLOBALLY, keepLeft, null, null, interval);
                                    break;
                                case 2: //until
                                    if (n.isUnary()) {
                                        m = this.newAtomicNode(true);
                                        nn = n.changeType(NodeType.UNTIL, keepLeft, (leftNew ? m : null), (leftNew ? null : m), interval);
                                    } else {
                                        nn = n.changeType(NodeType.UNTIL, keepLeft, null, null, interval);
                                    }
                                    break;
                            }
                            break;
                        case 1:
                            double[] x_mod_22 = {GeneticOptions.mutate__replace__and_weight * (n instanceof MTLand ? 0 : 1),
                                    GeneticOptions.mutate__replace__or_weight * (n instanceof MTLor ? 0 : 1),
                                    GeneticOptions.mutate__replace__imply_weight * (n instanceof MTLimply ? 0 : 1),
                                    GeneticOptions.mutate__replace__not_weight * (n instanceof MTLnot ? 0 : 1)};
                            if (sum(x_mod_22) == 0.0) return null;
                            int c_mod_22 = RandomGenerator.sample(x_mod_22);
                            keepLeft = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__keep_left_node);
                            leftNew = RandomGenerator.flipCoin(GeneticOptions.mutate__replace__new_left_node_for_boolean);
                            switch (c_mod_22) {
                                case 0: //and
                                    if (n.isUnary()) {
                                        m = this.newAtomicNode(true);
                                        nn = n.changeType(NodeType.AND, keepLeft, (leftNew ? m : null), (leftNew ? null : m), null);
                                    } else {
                                        nn = n.changeType(NodeType.AND, keepLeft, null, null, null);
                                    }
                                    break;
                                case 1: //or
                                    if (n.isUnary()) {
                                        m = this.newAtomicNode(true);
                                        nn = n.changeType(NodeType.OR, keepLeft, (leftNew ? m : null), (leftNew ? null : m), null);
                                    } else {
                                        nn = n.changeType(NodeType.OR, keepLeft, null, null, null);
                                    }
                                    break;
                                case 2: //imply
                                    if (n.isUnary()) {
                                        m = this.newAtomicNode(true);
                                        nn = n.changeType(NodeType.IMPLY, keepLeft, (leftNew ? m : null), (leftNew ? null : m), null);
                                    } else {
                                        nn = n.changeType(NodeType.IMPLY, keepLeft, null, null, null);
                                    }
                                    break;
                                case 3: //not
                                    m = this.newAtomicNode(true);
                                    nn = n.changeType(NodeType.NOT, keepLeft, null, null, null);
                                    break;
                            }
                            break;
                    }

                } else if (n.isLeaf()) {
                    m = this.newAtomicNode(n.getID(), true);
                    nn = n.replace(m);

//                    System.out.println("#####Parameters saved:");
//                    for (String s : parameters.getNameOfAllSymbols())
//                        System.out.println(s);

                }
                return nn;
            case 3:
                long id = n.getID();
                if (n instanceof MTLatom) {
                    String expr = ((MTLatom) n).getPredicateString();
                    String P = GeneticOptions.threshold_name + "_" + id;
                    int k = this.parameters.getSymbolId(P);
                    double v = this.parameters.getValue(k);
                    String V = this.getVariable(expr);
                    double l = this.reference_population.getLowerBound(V);
                    double u = this.reference_population.getUpperBound(V);
                    double delta = (u - l) * GeneticOptions.mutate__change__proportion_of_variation;
                    u = Math.min(v + delta / 2, u);
                    l = Math.max(v - delta / 2, l);
                    v = RandomGenerator.nextDouble(l, u);
                    this.parameters.setValue(k, v);
                } else if (n.isModal()) {
                    String Pl = "Tl_" + id;
                    String Pu = "Tu_" + id;
                    int kl = this.parameters.getSymbolId(Pl);
                    int ku = this.parameters.getSymbolId(Pu);
                    double vl = this.parameters.getValue(kl);
                    double vu = this.parameters.getValue(ku);
                    boolean lower = RandomGenerator.flipCoin(GeneticOptions.mutate__change__prob_lower_bound);
                    double delta = (GeneticOptions.max_time_bound - GeneticOptions.min_time_bound) * GeneticOptions.mutate__change__proportion_of_variation;
                    if (lower) {
                        double u = Math.min(vl + delta / 2, vu);
                        double l = Math.max(vl - delta / 2, GeneticOptions.min_time_bound);
                        double v = RandomGenerator.nextDouble(l, u);
                        this.parameters.setValue(kl, v);
                    } else {
                        double u = Math.min(vu + delta / 2, GeneticOptions.max_time_bound);
                        double l = Math.max(vu - delta / 2, vl);
                        double v = RandomGenerator.nextDouble(l, u);
                        this.parameters.setValue(ku, v);
                    }
                }
                return n;
            default:
                return null;
        }
    }


    public void crossover(Formula f1, Formula f2) {
        MTLnode n1 = f1.chooseInternalNode();
        MTLnode n2 = f2.chooseInternalNode();
//        System.out.println("Formula 1");
//        System.out.println(f1.toString());
//        System.out.println("subformula 1");
//        System.out.println(n1.toFormulaTree(0));
//        System.out.println("Formula 2");
//        System.out.println(f2.toString());
//        System.out.println("subformula 2");
//        System.out.println(n2.toFormulaTree(0));

        if (n1.isRoot())
            f1.setNewRoot(n2);
        if (n2.isRoot())
            f2.setNewRoot(n1);


        n1.swap(n2);
        //update formula in case of root

        f1.refresh();
        f2.refresh();
        f1.formula.initalize(this.reference_population.store, parameters, null);
        f2.formula.initalize(this.reference_population.store, parameters, null);

    }

    public void union(Formula f1, Formula f2) {
        MTLnode n1 = f1.chooseRootNode();
        MTLnode n2 = f2.chooseRootNode();
        MTLnode n3 = new MTLor(n1, n2);
        MTLnode n4 = new MTLand(n1, n2);
//        System.out.println("Formula 1");
//        System.out.println(f1.toString());
//        System.out.println("subformula 1");
//        System.out.println(n1.toFormulaTree(0));
//        System.out.println("Formula 2");
//        System.out.println(f2.toString());
//        System.out.println("subformula 2");
//        System.out.println(n2.toFormulaTree(0));

        //if (n1.isRoot())
        f1.setNewRoot(n3);
        //if (n2.isRoot())
        f2.setNewRoot(n4);


        //n1.swap(n2);
        //update formula in case of root

        f1.refresh();
        f2.refresh();
        f1.formula.initalize(this.reference_population.store, parameters, null);
        f2.formula.initalize(this.reference_population.store, parameters, null);

    }


    public ParametricInterval newInterval(long id) {
        ParametricInterval I = new ParametricInterval();
        String lw = "Tl_" + id;
        String up = "Tu_" + id;
        I.setLower(lw);
        I.setUpper(up);
        //setting upper and lower bound randomly.
        double l, u;
        l = RandomGenerator.nextDouble(GeneticOptions.min_time_bound, GeneticOptions.max_time_bound);
        u = RandomGenerator.nextDouble(l, GeneticOptions.max_time_bound);
        if (this.parameters.isDefined(lw))
            this.parameters.setValue(this.parameters.getSymbolId(lw), l);
        else
            this.parameters.addSymbol(lw, l);
        if (this.parameters.isDefined(up))
            this.parameters.setValue(this.parameters.getSymbolId(up), u);
        else
            this.parameters.addSymbol(up, u);
        return I;
    }

    public MTLnode newAtomicNode(boolean initNode) {
        long id = MTLnode.getNextID();
        String exp = this.generateAtomicExpression(id);
        MTLatom n = new MTLatom(exp, true);
        if (initNode)
            n.initalize(this.reference_population.store, parameters, null);

        //System.out.println("New atomic node created with ID " + id + ". Initialised: " + initNode);

        return n;
    }

    public MTLnode newAtomicNode(long id, boolean initNode) {
        String exp = this.generateAtomicExpression(id);
        MTLatom n = new MTLatom(id, exp, true);
        if (initNode)
            n.initalize(this.reference_population.store, parameters, null);
        //      System.out.println("New atomic node created to replace node with ID " + id + ". Initialised: " + initNode);
        //      System.out.println("Node expression" + n.getPredicateString() + " and id " + n.getID());

        return n;
    }

    public MTLnode newAtomicNodeCustom(boolean initNode, int variables, boolean le) {
        long id = MTLnode.getNextID();
        String exp = this.generateAtomicExpression(variables, le, id);
        MTLatom n = new MTLatom(exp, true);
        if (initNode)
            n.initalize(this.reference_population.store, parameters, null);

        //System.out.println("New atomic node created with ID " + id + ". Initialised: " + initNode);

        return n;
    }

    private String generateAtomicExpression(long id) {
        int k = this.reference_population.getVariableNumber();
        int j = RandomGenerator.nextInt(k);
        String V = this.reference_population.getVariable(j);
        boolean lessthan = RandomGenerator.flipCoin(GeneticOptions.init__prob_of_less_than);
        double l = this.reference_population.getLowerBound(V);
        double u = this.reference_population.getUpperBound(V);
        double v = RandomGenerator.nextDouble(l, u);
        //add threshold to formula parameters
        String th = GeneticOptions.threshold_name + "_" + id;
        if (this.parameters.containsSymbol(th)) {
            int i = this.parameters.getSymbolId(th);
            this.parameters.setValue(i, v);
        } else {
            this.parameters.addSymbol(th, v);
        }
        String exp = V + (lessthan ? " <= " : " >= ") + th;
        return exp;
    }

    private String generateAtomicExpression(int variables, boolean le, long id) {
        String V = this.reference_population.getVariable(variables);
        double l = this.reference_population.getLowerBound(V);
        double u = this.reference_population.getUpperBound(V);
        double v = RandomGenerator.nextDouble(l, u);
        //add threshold to formula parameters
        String th = GeneticOptions.threshold_name + "_" + id;
        if (this.parameters.containsSymbol(th)) {
            int i = this.parameters.getSymbolId(th);
            this.parameters.setValue(i, v);
        } else {
            this.parameters.addSymbol(th, v);
        }
        String exp = V + (le ? " <= " : " >= ") + th;
        return exp;
    }


    public Formula duplicate(Formula F) {
        ArrayList<MTLnode> list0 = F.getAllNodes();
        MTLformula f = F.formula.duplicate();
        ArrayList<MTLnode> list = f.getAllNodes();
        for (int h = 0; h < list.size(); h++) {
            MTLnode n = list.get(h);
            long id = n.getID();
            if (n.isAtom()) {
                //replace substring changing threshold name
                String s = ((MTLatom) n).getPredicateString();
                int i = s.lastIndexOf("_");
                String w = s.substring(0, i);
                w += "_" + id;
                ((MTLatom) n).setPredicateString(w);
                String th = GeneticOptions.threshold_name + "_" + id;
                String z = GeneticOptions.threshold_name + s.substring(i);
                int k = this.parameters.getSymbolId(z);
                double v = this.parameters.getValue(k);
                if (this.parameters.containsSymbol(th)) {
                    int j = this.parameters.getSymbolId(th);
                    this.parameters.setValue(j, v);
                } else {
                    this.parameters.addSymbol(th, v);
                }

            } else if (n.isModal()) {
                ((MTLmodalNode) n).setParametricInterval(this.newInterval(id));
                MTLnode n0 = list0.get(h);
                long id0 = n0.getID();
                String z0, z;
                int k, j;
                double v;
                z0 = "Tu_" + id0;
                z = "Tu_" + id;
                k = this.parameters.getSymbolId(z0);
                v = this.parameters.getValue(k);
                if (this.parameters.containsSymbol(z)) {
                    j = this.parameters.getSymbolId(z);
                    this.parameters.setValue(j, v);
                } else {
                    this.parameters.addSymbol(z, v);
                }
                z0 = "Tl_" + id0;
                z = "Tl_" + id;
                k = this.parameters.getSymbolId(z0);
                v = this.parameters.getValue(k);
                if (this.parameters.containsSymbol(z)) {
                    j = this.parameters.getSymbolId(z);
                    this.parameters.setValue(j, v);
                } else {
                    this.parameters.addSymbol(z, v);
                }
            }
        }
        f.initalize(this.reference_population.store, parameters, null);
        //f.refresh();
        return new Formula(f);
    }

    public Formula duplicate(MTLformula F) {
        MTLformula f = F;
        ArrayList<MTLnode> list = f.getAllNodes();
        for (int h = 0; h < list.size(); h++) {
            MTLnode n = list.get(h);
            long id = n.getID();
            if (n.isAtom()) {
                //replace substring changing threshold name
                String s = ((MTLatom) n).getPredicateString();
                int i = s.lastIndexOf("_");
                String w = s.substring(0, i);
                w += "_" + id;
                ((MTLatom) n).setPredicateString(w);
                String th = GeneticOptions.threshold_name + "_" + id;
                String z = GeneticOptions.threshold_name + s.substring(i);
                int k = this.parameters.getSymbolId(z);
                double v = this.parameters.getValue(k);
                if (this.parameters.containsSymbol(th)) {
                    int j = this.parameters.getSymbolId(th);
                    this.parameters.setValue(j, v);
                } else {
                    this.parameters.addSymbol(th, v);
                }

            } else if (n.isModal()) {
                ((MTLmodalNode) n).setParametricInterval(this.newInterval(id));
                MTLnode n0 = list.get(h);
                long id0 = n0.getID();
                String z0, z;
                int k, j;
                double v;
                z0 = "Tu_" + id0;
                z = "Tu_" + id;
                k = this.parameters.getSymbolId(z0);
                v = this.parameters.getValue(k);
                if (this.parameters.containsSymbol(z)) {
                    j = this.parameters.getSymbolId(z);
                    this.parameters.setValue(j, v);
                } else {
                    this.parameters.addSymbol(z, v);
                }
                z0 = "Tl_" + id0;
                z = "Tl_" + id;
                k = this.parameters.getSymbolId(z0);
                v = this.parameters.getValue(k);
                if (this.parameters.containsSymbol(z)) {
                    j = this.parameters.getSymbolId(z);
                    this.parameters.setValue(j, v);
                } else {
                    this.parameters.addSymbol(z, v);
                }
            }
        }
        f.initalize(this.reference_population.store, parameters, null);
        //f.refresh();
        return new Formula(f);
    }


    private String getVariable(String atomicExpression) {
        int i = atomicExpression.indexOf("<");
        if (i < 0) i = atomicExpression.indexOf(">");
        //there is one space between the variable and the comparison symbol
        return atomicExpression.substring(0, i - 1);
    }


}
