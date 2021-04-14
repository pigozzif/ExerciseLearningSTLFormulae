package eggloop.flow.learning;

import eggloop.flow.expr.Context;
import eggloop.flow.expr.Variable;
import eggloop.flow.mitl.MiTL;
import eggloop.flow.mitl.MitlPropertiesList;
import eggloop.flow.model.Trajectory;
import eggloop.flow.parsers.MitlFactory;
import eggloop.flow.utils.files.Utils;

import java.util.Arrays;
import java.util.function.UnaryOperator;

public class CheckFormula {
    private static final UnaryOperator<String> FILE_PATH = Utils.getFilePath(eggloop.flow.learning.Learning.class);
    private static double[] ds2Times = Utils.readVectorFromFile(FILE_PATH.apply("temporal/synthTime_12.txt"));
    private static double[][][] ds2SpatialValues = Utils.readMatrixMultiFromFile(ds2Times.length, FILE_PATH.apply("temporal/synthData_12.txt"));

    //::phi:: G[Tl_176, Tu_176] ((y >= Theta_3 | z <= Theta_6))  ::score:: 1.90375763520218 ::parameters:: [11.0, 11.0, 48.990792215517644, 37.53885223004586]
    public static void main(String[] args) {

        String[] variables = new String[]{"y", "z"};
        String[] parameters = new String[]{"Tl_176", "Tu_176", "Theta_3", "Theta_6"};
        double[] parametersValues = new double[]{11.0, 11.0, 48.990792215517644, 37.53885223004586};
        String formula = "G[Tl_176, Tu_176] ((y >= Theta_3 | z <= Theta_6))";
        int positiveClassified = check(ds2Times, ds2SpatialValues, variables, parameters, formula, parametersValues, 0);
        System.out.println("TOTAL:" + ds2SpatialValues.length);
        System.out.println("POSITIVE CLASSIFIED:" + positiveClassified);
    }

    private static int check(double[] times, double[][][] trajectories, String[] variables, String[] parameters, String formula, double[] formulaParameters, double atTime) {
        int[] b = new int[trajectories.length];
        Context ns = new Context();
        for (String s : variables) {
            new Variable(s, ns);
        }
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < parameters.length; j++) {
            builder.append("const double ").append(parameters[j]).append("=").append(formulaParameters[j]).append(";\n");
        }
        builder.append(formula).append("\n");
        MitlFactory factory = new MitlFactory(ns);
        String text = builder.toString();
        MitlPropertiesList l = factory.constructProperties(text);
        MiTL prop = l.getProperties().get(0);
        for (int i = 0; i < trajectories.length; i++) {
            Trajectory x = new Trajectory(times, ns, trajectories[i]);
            b[i] = prop.evaluate(x, atTime) ? 1 : 0;
        }
        return Arrays.stream(b).sum();
    }
}