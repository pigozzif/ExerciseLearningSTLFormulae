/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya;

import cern.colt.Arrays;
import eggloop.flow.simhya.simhya.gui.CommandPromptInteractor;
import eggloop.flow.simhya.simhya.matheval.Expression;
import eggloop.flow.simhya.simhya.matlab.Repressilator;
import eggloop.flow.simhya.simhya.matlab.SimHyAModel;
import eggloop.flow.simhya.simhya.utils.OSchecker;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Locale;




/**
 *
 * @author Luca
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {


        setLibraryPath();

        boolean test = false;
        if (test)
            test();
        else {
            CommandPromptInteractor interactor;
            interactor = new CommandPromptInteractor();
            EventQueue.invokeLater(interactor);
        }
    }


    private static void setLibraryPath() throws Exception  {

        String p = new java.io.File("").getAbsolutePath() +
                java.io.File.separator + "native" + java.io.File.separator;
        if (OSchecker.isWindows())
            if (OSchecker.is64bit())
                p += "win64";
            else
                p += "win32";
        else if (OSchecker.isMac())
            p += "macos";
        else if (OSchecker.isUnix())
            if (OSchecker.is64bit())
                p += "linux64";
            else
                p += "linux32";
        else
            throw new Exception("Cannot identify operating system. Cannot load 3D native libraries.");


        final java.lang.reflect.Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        //get array of paths
        final String[] paths = (String[])usrPathsField.get(null);

        //add the new path
        final String[] newPaths = java.util.Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length-1] = p;
        usrPathsField.set(null, newPaths);
        usrPathsField.setAccessible(false);
    }


    public static void test() {

        SimHyAModel m = new SimHyAModel();
        Repressilator r = new Repressilator(0.1,0.20,12,10000);
        double[][] y = r.simulate(10000);






//        FormulaPopulation p = new FormulaPopulation(10);
//        //FormulaGenOps op = p.getGeneticOperators();
//
//        p.addVariable("flow", -10000, 10000);
//        p.addVariable("flow1", -500, 500);
//
//        NormalBreathSimulator sim2 = new NormalBreathSimulator("data/normal_model/");
//        sim2.setFinalExpirationPhaseOnly(true);
//        IneffectiveBreathSimulator sim1 = new IneffectiveBreathSimulator("data/ineffective_model/");
//        sim1.setFinalExpirationPhaseOnly(true);
//
//        p.addFormulaParameter("p1", -300);
//        p.addFormulaParameter("p2", 0);
//        p.addFormulaParameter("t1", 0.5);
//        p.addFormulaParameter("t2", 0.6);
//
//       Formula f = p.getGeneticOperators().debugFormula(true);
//       p.addFormula(f);
//       System.out.println(f.toString());
//       String [] times = {"t1","t2"};
//       double [] vals = p.getParameterValues(times);
//       System.out.println("t1 " + vals[0]);
//       System.out.println("t2 " + vals[1]);
//

//        int k = p.getFormula(0).getFormulaSize();
//        System.out.println("size " + k);
//        double xxx = p.evaluateFitness(0, 0.97, 0.02, 0, 0, 1000);
//        System.out.println("fitness " + xxx);
//
//        p.initialiseNewGeneration();
//
//        p.selectFormula(0);
//        p.selectFormula(1);
//        p.crossoverNewGeneration(0,1);
//        int[] res = p.modelCheck(sim1, f, 100, 100);
//        //p.modelCheck(sim2, f, 100, 100);
//
//        int g=0,b=0,u=0;
//        for (int i=0;i<res.length;i++) {
//            if (res[i] == 1)
//                g++;
//            else if (res[i]== 0)
//                b++;
//            else u++;
//        }
//        System.out.println("g " + g);
//        System.out.println("b " + b);
//        System.out.println("u " + u);
//
//        vals[0] = 0.6;
//        vals[1] = 0.5;
//        p.setParameters(times, vals);
//        res = p.modelCheck(sim1, f, 100, 100);
//        //p.modelCheck(sim2, f, 100, 100);
//
//        g=0; b=0; u=0;
//        for (int i=0;i<res.length;i++) {
//            if (res[i] == 1)
//                g++;
//            else if (res[i]== 0)
//                b++;
//            else u++;
//        }
//        System.out.println("g " + g);
//        System.out.println("b " + b);
//        System.out.println("u " + u);

//        String [] pars = {"T1","T2","xxx"};
//        double [] vv = {0,1,-300};
//        Formula F = p.loadFormula("P=?[ F[T1,T2] {flow1 <= xxx} ]", pars, vv);
//        System.out.println(F.toString());
//
//        int[] res = p.modelCheck(sim1, f, 100, 100);
//        //p.modelCheck(sim2, f, 100, 100);
//
//        int g=0,b=0,u=0;
//        for (int i=0;i<res.length;i++) {
//            if (res[i] == 1)
//                g++;
//            else if (res[i]== 0)
//                b++;
//            else u++;
//        }
//        System.out.println("g " + g);
//        System.out.println("b " + b);
//        System.out.println("u " + u);

        //double[][] x = loadTrace("ostia.txt",4);


//        double[][] x = sim1.simulate(100);
//        Plot2DTrajectory pt = new Plot2DTrajectory(x,0,1);
//        Line2DChart chart = new Line2DChart(pt,"time","f");
//        //chart.show();
//
//
//
//
//
//        x = sim2.simulate(100);
//        pt = new Plot2DTrajectory(x,0,1);
//        chart = new Line2DChart(pt,"time","f");
//        chart.show();
//
//
//
//
//        int[] b = p.modelCheck(sim1, 0, 100, 1);
//
//        int s = 0, u = 0;
//        for (int i=0;i<b.length;i++) {
//            if (b[i]==1) s++;
//            else if (b[i] == -1) u++;
//        }
//        System.out.println("Successes: " + s);
//        System.out.println("Undefined: " + u);





//        boolean b = p.modelCheck(x, 0);
//        System.out.println("I checked the formula and found " + b);
//
//        String [] pp = {"p1"}; double[] vv = {200};
//        p.setParameters(pp,vv);
//
//        b = p.modelCheck(x, 0);
//        System.out.println("I checked the formula and found " + b);


//        SimHyAModel m = new SimHyAModel();
//        m.loadModel("SIR.txt");
//        m.setSSA();
//
//        double[][] x = m.simulate(100);
//
//
//        FormulaPopulation p = new FormulaPopulation(2);
//        //FormulaGenOps op = p.getGeneticOperators();
//
//
//        p.addVariable("S", 0, 100);
//        p.addVariable("I", 0, 100);
//        p.addVariable("R", 0, 100);
//
//        p.generateInitialPopulation();
//
//
//        System.out.println("Random formula 1");
//        System.out.println(p.getFormula(0).toString());
//        System.out.println("Random formula 2");
//        System.out.println(p.getFormula(1).toString());
//
//        boolean b = p.modelCheck(x, 0);
//        System.out.println("I checked the formula and found " + b);
//
//        p.initialiseNewGeneration();
//        p.selectFormula(0);
//        p.selectFormula(1);
//        p.crossoverNewGeneration(0, 1);
//
//        p.mutateNewGeneration(0);
//        p.finaliseNewGeneration();
//
//        System.out.println("Random formula 1");
//        System.out.println(p.getFormula(0).toString());
//        System.out.println("Random formula 2");
//        System.out.println(p.getFormula(1).toString());


    }

    private static void contains(Expression exp, String symbol) {
        if (exp.containsSymbol(symbol))
                System.out.println(exp.toString() + " contains symbol " + symbol);
        if (exp.containsConstant(symbol))
                System.out.println(exp.toString() + " contains constant " + symbol);
        if (exp.containsGlobalVariable(symbol))
                System.out.println(exp.toString() + " contains global variable " + symbol);
        if (exp.containsExpressionVariable(symbol))
                System.out.println(exp.toString() + " contains expression variable " + symbol);
        if (exp.containsBoundVariable(symbol))
                System.out.println(exp.toString() + " contains bound variable " + symbol);
    }


    public static void printSingleTrace(double[] x, String file) {
        try {
            int m;
            m = x.length;
            java.io.PrintWriter p = new java.io.PrintWriter(file);
            for (int j=0;j<m;j++)
                p.print(String.format(Locale.US, "%.5f", x[j]) + (j<m-1?",":"") );
            p.close();

        } catch (Exception e) {
            System.err.println("Cannot open file: " + e.getMessage());
            System.exit(1);
        }



    }


    public static void printTrace(double[][] x, String file) {
        try {
            int n,m;
            n = x.length;
            if (n==0)
                throw new RuntimeException("Empty trace");
            m = x[0].length;
            java.io.PrintWriter p = new java.io.PrintWriter(file);
            for (int i=0;i<n;i++) {
                for (int j=0;j<m;j++)
                    p.print(String.format(Locale.US, "%.5f", x[i][j]) + (j<m-1?",":"") );
                p.print( (i<n-1?"\n":"")  );
            }
            p.close();

        } catch (Exception e) {
            System.err.println("Cannot open file: " + e.getMessage());
            System.exit(1);
        }



    }


    public static double[][] loadTrace(String file, int dim) {
        /*
         * carica file leggendo linea per linear
           poi usa scanner per separare con token , i vari numeri
         * caricali in arraylist e poi converti in double[]
         */
        double [][] x = new double[dim][];

        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
            for (int i=0;i<dim;i++) {
                String line = reader.readLine();
                java.util.Scanner scanner = new java.util.Scanner(line);
                scanner.useDelimiter(",");
                ArrayList<Double> xx = new ArrayList<Double>();
                while (scanner.hasNext()) {
                    xx.add(scanner.nextDouble());
                }
                int n = xx.size();
                x[i] = new double[n];
                for (int j=0;j<n;j++)
                    x[i][j] = xx.get(j);
            }
            return x;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }


}