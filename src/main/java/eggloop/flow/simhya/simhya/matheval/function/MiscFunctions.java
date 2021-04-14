/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.matheval.function;

/**
 *
 * @author Luca
 */
public class MiscFunctions {
    static double log2 = Math.log(2);
    
    
    public static double min(double  arg1, double arg2) {
        return Math.min(arg1, arg2);
    }
    
    public static double min(double... args) {
        double m = Double.POSITIVE_INFINITY;
        for (int i=0;i<args.length;i++)
            if (args[i] < m)
                m = args[i];
        return m;
    }
    
    public static double max(double  arg1, double arg2) {
        return Math.max(arg1, arg2);
    }
    
    public static double max(double... args) {
        double m = Double.NEGATIVE_INFINITY;
        for (int i=0;i<args.length;i++)
            if (args[i] > m)
                m = args[i];
        return m;
    }
    
    public static double log2(double x) {
        return Math.log(x)/log2;
    }
}
