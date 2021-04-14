/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval.function;

import org.sbml.jsbml.ASTNode.Type;
import eggloop.flow.simhya.simhya.matheval.ExpressionNode;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
import eggloop.flow.simhya.simhya.matheval.Expression;
import eggloop.flow.simhya.simhya.matheval.EvalException;
import eggloop.flow.simhya.simhya.matheval.Evaluator;

/**
 *
 * @author Luca
 */
public class DynamicFunction extends FunctionDefinition {
    SymbolArray arguments;
    Expression definition;

    public DynamicFunction(String name, int arity, Expression definition) {
        super.name = name;
        super.arity = arity;
        super.minimumArity = 0;
        super.type = FunctionType.DYNAMIC;
        super.randomFunction = definition.isRandom();
        this.definition = definition;
        super.isLogical = definition.isLogicalExpression();
        arguments = SymbolArray.getNewFastEvalSymbolArray(arity);
    }

  

    @Override
    public boolean canBeConvertedToNumericalFunction() {
        return definition.canBeConvertedToNumericalExpression();
    }

    @Override
    public FunctionDefinition convertToNumericalFunction(Evaluator eval) {
        if (!this.canBeConvertedToNumericalFunction()) {
            if (this.isLogical)
                throw new EvalException("Logical function " + this.name + " cannot be converted into a numerical function.");
            else
                throw new EvalException("Function cannot be converted into a numerical function:" +
                    "it is already a numerical function");
        } else {
            String newName = name + "$numeric";
            if (eval.isFunction(newName))
                return eval.getFunctionDefinition(newName);
            Expression newDefinition = definition.convertToNumericalExpression();
            newDefinition.setLocalSymbols(definition.getLocalSymbolsReference().clone(true));
            //System.out.println(newName + " = " + newDefinition.toString());
            
            FunctionDefinition f  = new DynamicFunction(newName,arity,newDefinition);
            eval.registerFunction(newName, f);
            return f;
        }
    }


    
    @Override
    public double compute() {
        throw new UnsupportedOperationException("This is a dynamic function. Call compute(double[] args)");
    }

    @Override
    public double compute(double x) {
        throw new UnsupportedOperationException("This is a dynamic function. Call compute(double[] args)");
    }

    @Override
    public double compute(double x1, double x2) {
        throw new UnsupportedOperationException("This is a dynamic function. Call compute(double[] args)");
    }

    @Override
    public double compute(double[] args) {
        for (int i=0;i<arity;i++)
            arguments.setValue(i, args[i]);
        definition.setLocalSymbolsReferenceQuick(arguments);
        return definition.computeValue();
    }

    @Override
    public boolean computeContinuousChangeStatus(boolean[] argStatus) {
        for (int i=0;i<arity;i++)
            arguments.setContinuousEvolutionStatus(i, argStatus[i]);
        definition.setLocalSymbolsReferenceQuick(arguments);
        return definition.computeContinuousChangeStatus();
    }
    
    

    @Override
    public Type getSBMLType() {
        throw new UnsupportedOperationException("user defined functions are currently unsupported in the traslation to SBML --- cannot convert it to MATHML.");
    }

    @Override
    public String toJavaCode() {
        return "_func_" + this.name;
    }
    

    @Override
    public String toMatlabCode() {
        return "func_" + this.name;
    }
    
    

   
    
   

    @Override
    public String toMatlabFunction() {
        String s = this.toMatlabCode() + " = @(";
        for (int i=1;i<=this.arity;i++)
                s+= (i > 1 ? "," : "") + "arg_" + i;
        s += ") ";
        s += definition.toMatlabCode() + ";";
        return s;
    }

    @Override
    public String toJavaMethod() {
        String s = "";
        
        if (definition.isLogicalExpression()) {
            s += "\tprivate boolean " + this.toJavaCode();
            s += "(double... args) {\n";
            s += "\t\treturn " + definition.toJavaCode() + ";\n";
        }
        else {
            s += "\tprivate double " + this.toJavaCode();
            s += "(double... args) {\n";
            s += "\t\treturn " + definition.toJavaCode() + ";\n";
        }
        s += "\t}\n\n";
        return s;
    }
    
    @Override
    //a function is differentiable iff it si differentiable w.r.t its formal arguments.
    public boolean isDifferentiable(String x, Integer args) {
        for (int i=0;i<this.arity;i++) {
            if (!this.definition.isDifferentiable(this.definition.getLocalSymbolName(i)))
                return false;
        }
        return definition.isDifferentiable(x);
    }

    @Override
    public ExpressionNode differentiate(int arg) {
        String x = this.definition.getLocalSymbolName(arg);
        ExpressionNode n = this.definition.differentiateNode(x);
        return n;
    }
    
    
    public ExpressionNode differentiate(String x) {
        ExpressionNode n = this.definition.differentiateNode(x);
        return n;
    }
    
    /**
     * returns the ith local symbol name
     * @param i
     * @return 
     */
    public String getLocalSymbolName(int i) {
        return this.definition.getLocalSymbolName(i);
    }
 
    
    /**
     * return the function definition!!!!
     * USE WITH EXTREME CARE. 
     * @return 
     */
    public Expression getFunctionDefinition() {
        return this.definition;
    }
    
    
}
