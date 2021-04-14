/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval.function;


import org.sbml.jsbml.ASTNode.Type;
import eggloop.flow.simhya.simhya.matheval.*;

/**
 *
 * @author Luca
 */
public abstract class FunctionDefinition {
    /**
     * The name of the function. There cannot be two functions with the same name,
     * but there can be static functions with different arities, by setting their arity to -1,
     * and writing the proper code. Remember to implement the dedicated compute methods for zero, one or two
     * arguments.
     */
    String name;
    /**
     * the arity of the function. Can be any number greater or equal than 0, or -1.
     * A function with arity -1 can take any number of arguments greater or equal than {@link minimumArity}.
     */
    int arity;
    /**
     * Type of the function. Builtin functions are of static type (specialized w.r.t. their arity).
     * Functions added within the code, by writing an appropiate class extending FunctionDefinition,
     * are also static. Functions defined at runtime are dynamic.
     */
    FunctionType type;
    /**
     * The minimum arity of a function taking an arbitrary number of arguments.
     * If the function takes a fixed number of arguments, this field is ignored.
     */
    int minimumArity;
    /**
     * The minimum arity of a function taking an arbitrary number of arguments.
     * If the function takes a fixed number of arguments, this field is ignored.
     */
    int maximumArity;
    /**
     * True if the function has random values.
     */
    boolean randomFunction;
    /*
     * True if the function returns a logical value
     */
    boolean isLogical = false;



    /**
     * Checks if the parsed arity of a function is congruent with the arity of the
     * defined function
     * @param parsedArity the parsed arity
     * @return true if the parsed arity is congruent.
     */
    public boolean isArityCongruent(int parsedArity) {
        if (arity >= 0)
            return (parsedArity == arity);
        else
            return (parsedArity >= minimumArity && parsedArity <= maximumArity);
    }


    public int getArity() {
        return arity;
    }
    public int getMinimumArity() {
        return minimumArity;
    }
    public String getName() {
        return name;
    }
    public FunctionType getType() {
        return type;
    }
    public boolean isRandomFunction() {
        return this.randomFunction;
    }
    public boolean isBuiltinFunction() {
        return type != FunctionType.DYNAMIC;
    }
     public boolean isDynamicFunction() {
        return type == FunctionType.DYNAMIC;
    }
     public boolean isLogicalFunction() {
         return this.isLogical;
     }
     
    public boolean canBeConvertedToNumericalFunction() {
        return false;
    }

    public FunctionDefinition convertToNumericalFunction(Evaluator eval) {
        throw new EvalException("Function cannot be converted into a numerical function:" +
                "it is already a numerical function");
    }



    public double compute() {
        throw new UnsupportedOperationException("Wrong arity. Function " + name + " is a " + arity + "-ary function");
    }
    public double compute(double x) {
        throw new UnsupportedOperationException("Wrong arity. Function " + name + " is a " + arity + "-ary function");
    }
    public double compute(double x1, double x2){
        throw new UnsupportedOperationException("Wrong arity. Function " + name + " is a " + arity + "-ary function");
    }
    public double compute(double[] args){
        throw new UnsupportedOperationException("Wrong arity. Function " + name + " is a " + arity + "-ary function");
    }
    
    public boolean computeContinuousChangeStatus(boolean[] argStatus) {
        throw new UnsupportedOperationException("This operation is meaningless for this kind of function");
    } 
    
    public abstract Type getSBMLType();
    
    public abstract String toJavaCode();
    
    public String toJavaMethod() {
        return "";
    }
    
    public abstract String toMatlabCode();
    
    public String toMatlabCode(String[] args) {
        String s = this.toMatlabCode() + "( ";
        for (int i=0;i<args.length;i++) {
            s += (i>0 ? ", " : "") + args[i];
        }
        s += " )";
        return s;
    }
    
    public String toMatlabFunction() {
        return "";
    }
    
   
    
    public abstract boolean isDifferentiable(String x, Integer args);
    
    
    /**
     * differentiates the function w.r.t. the given argument.
     * Return an ExpressionNode, with arg variables 
     * as BoundNodes named X0,X1,....
     * @param arg
     * @return 
     */
    public abstract ExpressionNode differentiate(int arg);
    
    
}
