/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval;
import java.util.ArrayList;
import java.util.List;
import org.sbml.jsbml.ASTNode;


/**
 *
 * @author Luca
 */
public class Expression {
    private ExpressionNode root;
    private int id;
    private Evaluator eval;
    private String strExpr;
    /**
     * Local symbols, can be either bounded variables, local variables or local parameters. 
     * They are all dealt in the same way, and have to be replaced to or their value
     * has to be passed to the expression for calculations.
     */
    private SymbolArray localSymbols;
    private boolean hasLocalSymbols;

    public Expression(ExpressionNode root, Evaluator eval, boolean registerExpression) {
        this.root = root;
        this.eval = eval;
        if (registerExpression)
            this.id = eval.addExpression(this);
        else 
            this.id = -1;
        this.localSymbols = null;
        this.hasLocalSymbols = false;
    }
    
    
    public Expression(ExpressionNode root, Evaluator eval, SymbolArray localVariables, boolean registerExpression) {
        this.root = root;
        this.eval = eval;
        if (registerExpression)
            this.id = eval.addExpression(this);
        else 
            this.id = -1;
        this.localSymbols = localVariables;
        this.hasLocalSymbols = true;
    }

    /**
     * gets the ID code of the expression (can be used to retrieve it from the evaluator)
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * sets the id code of the expression. To be used by the evaluator.
     * @param id
     */
    void setId(int id) {
        this.id = id;
    }


    
    
    /**
     *
     * @return a list of variables ID of the function
     */
    public ArrayList<Integer> getListOfVariables() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        root.getListOfVariables(list);
        return list;
    }

    public boolean isLogicalExpression() {
        return root.isLogicalExpression();
    }
    
    
    public boolean isStrictInequality() {
        return root.isStrictInequality();
    }
    
    public void setLocalSymbols(SymbolArray localSymbols) {
        if (this.hasLocalSymbols)
            throw new EvalException("Local variables already defined");
        this.hasLocalSymbols = true;
        this.localSymbols = localSymbols;
    }
    
    
    public SymbolArray getLocalSymbolsReference() {
        return this.localSymbols;
    }
    
    /**
     * Returns the number of local symbols
     */
    public int getNumberOfLocalSymbols() {
        if (this.localSymbols != null)
            return this.localSymbols.numberOfSymbols;
        else return 0;
    }
    
    /**
     * returns the name of local symbol i.
     * @param i
     * @return 
     */
    public String getLocalSymbolName(int i) {
        if (this.localSymbols != null)
            return this.localSymbols.getName(i);
        else return null;
    }
    
    
    /**
     * Sets a reference to local variables. It checks that dimension and 
     * variable names coincide!!!
     * @param localVars 
     */
    public void setLocalSymbolsReference(SymbolArray localVars) {
        if (this.hasLocalSymbols) {
            if (localVars.numberOfSymbols != localSymbols.numberOfSymbols)
                throw new EvalException("Symbol array dimension mismatch!");
            for (int i=0;i<localVars.getNumberOfSymbols();i++)
                if (!localVars.getName(i).equals(localSymbols.getName(i)))
                   throw new EvalException("Symbol name mismatch. Found " + localVars.getName(i) +
                                "; required " + localSymbols.getName(i));
            this.localSymbols = localVars;
        }
    }
    
    /**
     * Sets the loval variable reference without doing any consistency control.
     * Use with extreme care!
     * @param localVars a SymbolArray of variables
     */
    public void setLocalSymbolsReferenceQuick(SymbolArray localVars) {
        if (this.hasLocalSymbols)
            this.localSymbols = localVars;
    }
    /**
     * Sets the new value of local variables. 
     * @param localSymbs a double array
     */
    public void setLocalSymbolsValue(double[] localSymbs) {
        if (this.hasLocalSymbols) {
            if (localSymbs.length != localSymbols.numberOfSymbols)
                throw new EvalException("Value and Symbol Array Dimension Mismatch!");
            localSymbols.values = java.util.Arrays.copyOf(localSymbs, localSymbs.length);
        }
    }
    
    /**
     * Sets the new value of local variables. 
     * @param localSymbs a double array
     */
    public void setLocalSymbolsValue(SymbolArray localSymbs) {
         if (this.hasLocalSymbols) {
            if (localSymbs.numberOfSymbols != localSymbols.numberOfSymbols)
                throw new EvalException("Value and Symbol Array Dimension Mismatch!");
            localSymbols.values = java.util.Arrays.copyOf(localSymbs.values, localSymbs.numberOfSymbols);
         }
    }
    
    /**
     * Sets the value of a local variable to the expression
     * @param symbolName the local variable to set the value of
     * @param value the new value
     */
    public void setLocalSymbolValue(String symbolName, double value) {
         if (this.hasLocalSymbols) {
             int id = localSymbols.getSymbolId(symbolName);
             localSymbols.setValue(id, value);
         }
    }

    public double computeValue() {
        if (!this.hasLocalSymbols)
            root.computeValue();
        else
            root.computeValueLocal(localSymbols);
        return root.value;
        
    }
    public double computeValue(SymbolArray varReference){
        if (!this.hasLocalSymbols)
            root.computeValue(varReference);
        else
            root.computeValueLocal(localSymbols,varReference);
        return root.value;
    }
    public double computeValueCache(){
        if (!this.hasLocalSymbols)
            root.computeValue(eval.evaluationCode);
        else
            root.computeValueLocal(localSymbols,eval.evaluationCode);
        return root.value;
    }
    public double computeValueCache(SymbolArray varReference){
        if (!this.hasLocalSymbols)
            root.computeValue(varReference,eval.evaluationCode);
        else
            root.computeValueLocal(localSymbols,varReference,eval.evaluationCode);
        return root.value;
    }
    public boolean isRandom() {
        return root.random;
    }
    public boolean isNumericConstant() {
        return root.isNumericConstant();
    }

    ExpressionNode getRoot() {
        return this.root;
    }
    
    @Override
    public String toString() {
        return root.getExpressionString();
    }

    public boolean canBeConvertedToNumericalExpression() {
        return root.canBeConvertedToNumericalExpression();
    }


    public Expression convertToNumericalExpression() {
        if (!root.canBeConvertedToNumericalExpression())
            return null;
        ExpressionNode n = root.convertToNumericalExpression(eval);
        Expression exp;
        if (this.localSymbols!=null)
            exp = new Expression(n,eval,localSymbols, true);
        else
            exp = new Expression(n,eval, true);
        return exp;
    }
    
    public boolean hasLocalSymbols() {
        return this.hasLocalSymbols;
    }

    /**
     * Substitutes a symbol (either variable, local variable or constant) by a global symbol,
     * i.e. by a global variable or a constant! 
     * @param symbolToReplace
     * @param replacingGlobalSymbol
     * @return
     */
     public Expression substitute(String symbolToReplace, String replacingGlobalSymbol) {
         return this.substitute(symbolToReplace,eval.generateExpressionForSymbol(replacingGlobalSymbol));
     }


     /**
     * Substitutes a list of symbols (either variables, local variables or constants) by global symbols,
     * i.e. by global variables or constants!
     * @param symbolsToReplace
     * @param replacingGlobalSymbols
     * @return
     */
     public Expression substitute(ArrayList<String> symbolsToReplace, ArrayList<String> replacingGlobalSymbols) {
         return this.substitute(symbolsToReplace,eval.generateExpressionForSymbol(replacingGlobalSymbols));
     }
    
    /**
     * Substitutes a symbol (either variable, local variable or constant) 
     * by an expression.
     * Expression variables are replaced by their expressions and then substituted.
     * Replacing expressions cannot containt local variables.
     * @param symbolToReplace the name of the symbol to replace
     * @param replacingExpression the replacing expression
     * @return a new expression, or the current expression if no subsitution occurred.
     */
    public Expression substitute(String symbolToReplace, Expression replacingExpression) {
        if (replacingExpression.hasLocalSymbols)
            throw new EvalException("Cannot replace symbols with expressions containing local variables");
        ExpressionNode newRoot = root.substitute(symbolToReplace, replacingExpression, eval);
        //preparing to deal with local variables
        SymbolArray newLocalVar = localSymbols.clone(true);
        //remove local variables that have been substituted
        if (newLocalVar.containsSymbol(symbolToReplace)) { 
            newLocalVar.removeSymbol(symbolToReplace);
            newRoot.updateLocalVariableIds(newLocalVar);
        }
        return checkNewRootAfterSubstitution(newRoot,newLocalVar);
    }

    
    /**
     * Substitutes a list of symbols (either variable, local variable or constant) 
     * by a list of expressions.
     * Expression variables are replaced by their expressions and then substituted.
     * Replacing expressions cannot containt local variables.
     * @param symbolToReplace the list of names of symbols to replace
     * @param replacingExpression the list of replacing expressions
     * @return a new expression, or the current expression if no subsitution occurred.
     */
    public Expression substitute(List<String> symbolsToReplace, List<Expression> replacingExpressions) {
        for (Expression e : replacingExpressions)
            if (e.hasLocalSymbols)
                throw new EvalException("Cannot replace symbols with expressions containing local variables");
        ExpressionNode newRoot = root.substitute(symbolsToReplace, replacingExpressions, eval);
        SymbolArray newLocalVar = null;
        if (this.hasLocalSymbols) {
            //preparing to deal with local variables
            newLocalVar = localSymbols.clone(true);
            //remove local variables that have been substituted
            boolean removed = false;
            for (String var : symbolsToReplace)
            if (newLocalVar.containsSymbol(var)) {
                newLocalVar.removeSymbol(var);
                removed = true;
            }
            if (removed)
                newRoot.updateLocalVariableIds(newLocalVar);
        }
        return checkNewRootAfterSubstitution(newRoot,newLocalVar);
    }

    
    private Expression checkNewRootAfterSubstitution(ExpressionNode newRoot,SymbolArray newLocalVar) {
        if (newRoot != this.root) {
            Expression newExp;
            if (this.hasLocalSymbols)
                newExp = new Expression(newRoot,this.eval,this.localSymbols, true);
            else 
                newExp = new Expression(newRoot,this.eval, true);
            //we need to add local variable information to newly generated expression
            if (newLocalVar != null)
                newExp.setLocalSymbols(newLocalVar);
            return newExp;
        } else return this;
    }
    
    /**
     * Replaces one local variable or parameter by a global one
     * @param local the name of the local symbol
     * @param global the name of the global symbol
     * @return 
     */
    public Expression replaceLocalSymbolByGlobalOne(String local,String global) {
        Expression globalVarExp=null,newExp;
        try {
            globalVarExp = eval.getExpression(eval.parse(global));
        } catch (Exception e) {
            throw new EvalException("Error while replacing local variable " + local + " by "
                    + global + " in " + this.toString());
        }
        newExp = this.substitute(local, globalVarExp);
        return newExp;
    }
    
    /**
     * Replaces a list local variable or parameter by global ones
     * @param local the list of names of the local symbols
     * @param global the list of names of the global symbols
     * @return 
     */
    public Expression replaceLocalSymbolsByGlobalOnes(List<String> locals, List<String> globals) {
        if (locals.size() != globals.size())
            throw new EvalException("Local and global variable list size mismatch");
        ArrayList<Expression> globalVarExps = new ArrayList<Expression>();
        for (String var : globals)
            try {
                globalVarExps.add(eval.getExpression(eval.parse(var)));
            } catch (Exception e) {
                throw new EvalException("Error while replacing local variable " + locals + " by "
                        + globals + " in " + this.toString());
            }
        Expression newExp = this.substitute(locals, globalVarExps);
        return newExp;
    }
    
    
    /**
     * returns true if the expression represents just a global variable node
     * @return 
     */
    public boolean isVariable() {
        if (root instanceof VariableNode) 
            return true;
        else return false;
    }
    
    /**
     * returns the id of the global variable, if the expression is just a  global variable
     * node. Otherwise returns -1.
     * @return 
     */
    public int getVariableCode() {
        if (root instanceof VariableNode)  {
            return ((VariableNode)root).varIndex;
        }
        else return -1;
    }
    
    
    /**
     * Converts the expression to an ASTNode,
     * a representation of a MathML formula used by JSBML
     * @return 
     */
    public ASTNode convertToJSBML() {
        return root.convertToJSBML();
    }
    
    /**
     * Converts the expression into a MathML string. 
     * It uses JSBML library. Not all function types are 
     * supported
     * @return 
     */
    public String convertToMathML() {
        ASTNode n = root.convertToJSBML();
        return n.toMathML();
    }

    /**
     * Computes the continuity change status of the expression.
     * This is true if the expression can change value when continuous variables
     * change their value. It is false otherwise
     * 
     * It requires the value of the expression to be already computed.
     * @return 
     */
    public boolean computeContinuousChangeStatus() {
        if (!this.hasLocalSymbols)
            root.computeContinuousChangeStatus();
        else
            root.computeContinuousChangeStatusLocal(localSymbols);
        return root.canChangeContinuously;
    }
    
    /**
     * Computes the continuity change status of the expression.
     * This is true if the expression can change value when continuous variables
     * change their value. It is false otherwise
     * 
     * It requires the value of the expression to be already computed.
     * @param varReference a reference to global variables.
     * @return 
     */
    public boolean computeContinuousChangeStatus(SymbolArray varReference){
        if (!this.hasLocalSymbols)
            root.computeContinuousChangeStatus(varReference);
        else
            root.computeContinuousChangeStatusLocal(localSymbols,varReference);
        
   //     System.out.println("Expression " + this.toString() + " change cont? " + root.canChangeContinuously);
        return root.canChangeContinuously;
    }
    
    
    /**
     * Applies some simplification rules and returns a new expression.
     * @return 
     */
    public Expression simplify() {
        throw new RuntimeException("Not implemented yet");
    }
    
    /**
     * Differentiate the expression w.r.t symbol x.
     * @param x
     * @return the differentiated expression
     */
    public Expression differentiate(String x) {
        ExpressionNode diffRoot = root.differentiate(x, eval);
        return checkNewRootAfterSubstitution(diffRoot,null);
    }
    
    
    /**
     * differentiate the expression w.r.t x but returns the node of the expression tree
     * @param x
     * @return 
     */
     public ExpressionNode differentiateNode(String x) {
        return root.differentiate(x, eval);
    }
    
     /**
      * Checks if the expression is differentiable w.r.t. x. 
      * x makes difference if it is or not an expression node
      * @param x
      * @return 
      */
     public boolean isDifferentiable(String x) {
         return root.isDifferentiable(x);
     }
     
    /**
     * Checks if the expression contains a symbol (variable, constant
     * local variable or expression variable) with the given name.
     * Recursively checks expressions defined by expression variables,
     * but not user defined functions.
     * @param symbol
     * @return 
     */
    public boolean containsSymbol(String symbol) {
        return root.containsSymbol(symbol);
    }
    
     /**
     * Checks if the expression contains a variable with the given name.
     * Recursively checks expressions defined by expression variables,
     * but not user defined functions.
     * @param variable
     * @return 
     */
    public boolean containsGlobalVariable(String variable) {
        return root.containsGlobalVariable(variable);
    }
    
     /**
     * Checks if the expression contains a constant with the given name.
     * Recursively checks expressions defined by expression variables,
     * but not user defined functions.
     * @param constant
     * @return 
     */
    public boolean containsConstant(String constant) {
        return root.containsConstant(constant);
    }
    
     /**
     * Checks if the expression contains an expression with the given name.
     * It does not check recursively expressions defined by expression variables,
     * and user defined functions.
     * @param expVar
     * @return 
     */
    public boolean containsExpressionVariable(String expVar) {
        return root.containsExpressionVariable(expVar);
    }
    
     /**
     * Checks if the expression contains a bound variable with the given name.
     * Recursively checks expressions defined by expression variables,
     * but not user defined functions.
     * @param boundVar
     * @return 
     */
    public boolean containsBoundVariable(String boundVar) {
        return root.containsBoundVariable(boundVar);
    }
    
    
    public String toJavaCode() {
        return root.toJavaCode();
    }
    
    
    public String toMatlabCode() {
        return root.toMatlabCode();
    }
    
    
    /**
     * Generates a java class implementing the expression,
     * that can be dynamically compiled and linked to the code.
     * @return 
     */
    public String toJavaClass() {
        String s = "";
        String className = "Expression" + this.id;
        
        s += "package compmath;\n\n";
        s += "import simhya.matheval.CompiledExpression;\n";
        s += "import simhya.matheval.SymbolArray;\n";
        s += "import simhya.matheval.EvalException;\n";
        s += "import simhya.matheval.function.RandomGenerator;\n";
        s += "import simhya.matheval.function.MiscFunctions;\n\n";
        s += "public class " + className + " implements CompiledExpression {\n";
        s += "\tprivate boolean logical;\n";
        s += "\tprivate double[] variable;\n";
        s += "\tprivate double[] parameter;\n\n";
        s += "\tpublic " + className+"() {\n";
        s += "\t\tlogical = " + (this.isLogicalExpression() ? "true"  : "false") + ";\n";
	s += "\t\tvariable = null;\n";
	s += "\t\tparameter = null;\n\t}\n\n";
	s += "\tpublic void setVariableReference(double[] vars) {\n";
	s += "\t\tvariable = vars;\n\t}\n\n";
        s += "\tpublic void setParameterReference(double[] params) {\n";
	s += "\t\tparameter = params;\n\t}\n\n";
        s += "\tpublic boolean isLogical() {\n";
	s += "\t\treturn logical;\n\t}\n\n";
        if (this.isLogicalExpression()) {
            s += "\tpublic double evaluate() {\n";
            s += "\t\treturn (" + toJavaCode() +  " ? 1.0 : 0.0);\n\t}\n\n"; 
            s += "\tpublic double evaluate(SymbolArray vars) {\n";
            s += "\t\tdouble [] variable = vars.getReferenceToValuesArray();\n";
            s += "\t\treturn (" + toJavaCode() +  " ? 1.0 : 0.0);\n\t}\n\n";
            s += "\tpublic double evaluate(double[] variable) {\n";
            s += "\t\treturn (" + toJavaCode() +  " ? 1.0 : 0.0);\n\t}\n\n"; 
            s += "\tpublic boolean evaluateBool() {\n";
            s += "\t\treturn " + toJavaCode() +  ";\n\t}\n\n"; 
            s += "\tpublic boolean evaluateBool(SymbolArray vars) {\n";
            s += "\t\tdouble [] variable = vars.getReferenceToValuesArray();\n";
            s += "\t\treturn " + toJavaCode() +  ";\n\t}\n\n";
            s += "\tpublic boolean evaluateBool(double[] variable) {\n";
            s += "\t\treturn " + toJavaCode() +  ";\n\t}\n\n"; 
        } else {
            s += "\tpublic double evaluate() {\n";
            s += "\t\treturn " + toJavaCode() + ";\n\t}\n\n"; 
            s += "\tpublic double evaluate(SymbolArray vars) {\n";
            s += "\t\tdouble [] variable = vars.getReferenceToValuesArray();\n";
            s += "\t\treturn " + toJavaCode() + ";\n\t}\n\n";
            s += "\tpublic double evaluate(double[] variable) {\n";
            s += "\t\treturn " + toJavaCode() + ";\n\t}\n\n";
            s += "\tpublic boolean evaluateBool() {\n";
            s += "\t\tthrow new EvalException(\"Compiled expression is not logical.\");\n\t}\n\n"; 
            s += "\tpublic boolean evaluateBool(SymbolArray vars) {\n";
            s += "\t\tthrow new EvalException(\"Compiled expression is not logical.\");\n\t}\n\n";
            s += "\tpublic boolean evaluateBool(double[] variable) {\n";
            s += "\t\tthrow new EvalException(\"Compiled expression is not logical.\");\n\t}\n\n"; 
        }

        ExpressionSymbolArray expressions = eval.getExpressionVariableReference();
        for (int i=0;i<expressions.numberOfSymbols;i++) {
            Expression e = expressions.getExpression(i);
            String name = "_expr_" + expressions.getName(i);
            if (e.isLogicalExpression()) {
                s += "\tpublic boolean " + name + "() {\n";
                s += "\t\treturn " + e.toJavaCode() + ";\n\t}\n\n";
            } else {
                s += "\tpublic double " + name + "() {\n";
                s += "\t\treturn " + e.toJavaCode() + ";\n\t}\n\n";
            }
        }
        
        ArrayList<String> dynamicFunctions = eval.getDynamicFunctionNames();
        for (String f : dynamicFunctions)
            s += eval.getFunctionDefinition(f).toJavaMethod();
        
        s +=  "}\n";
        return s;
    }
    
    
    /**
     * compiles the expression and returns a complied expression object, 
     * which can be used to fast evaluation of the expression.
     * @return 
     */
    public CompiledExpression compileExpression() {
        try {
            String className = "Expression" + id;
            String dynFile = "dyncode/compmath/"+className+".java";
            String tempDir = "temp/dynBin";
            //step one: generate the Java class implementing the expression.
            String classText = toJavaClass();
            java.io.PrintWriter p = new java.io.PrintWriter(dynFile);
            p.print(classText);
            p.close();
            //step two: compile the class
            String cpath;
            if ((new java.io.File("SimHyA.jar")).exists())
                    cpath = "SimHyA.jar";
            else
                cpath = "dist/SimHyA.jar";
            java.io.PrintWriter outComp = new java.io.PrintWriter("debug.txt");
            javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
            int errorCode = compiler.run(null, null, null, "-classpath", cpath, "-d", tempDir, dynFile);
            
//            int errorCode = com.sun.tools.javac.Main.compile(new String[] {
//                "-classpath", cpath,
//                "-d", tempDir, dynFile },outComp); 
            if (errorCode > 0) {
                throw new EvalException("Cannot compile to bytecode the expression " + this.toString() + 
                        "\nCompiler error code: " + errorCode);
            }
            
            //step three: load the class and create an instance of it.
            java.io.File classesDir = new java.io.File(tempDir+"/");
            // The parent classloader
            ClassLoader parentLoader = CompiledExpression.class.getClassLoader();
            // Load class "sample.PostmanImpl" with our own classloader.
        
            java.net.URLClassLoader loader = new java.net.URLClassLoader(
                    new java.net.URL[] { classesDir.toURL() }, parentLoader);
            Class cls = loader.loadClass("compmath."+className);
            CompiledExpression compExp = (CompiledExpression) cls.newInstance();
            compExp.setVariableReference(eval.getVariableReference().getReferenceToValuesArray());
            compExp.setParameterReference(eval.getConstantReference().getReferenceToValuesArray());
            return compExp;   
        } catch(Exception e) {
            throw new EvalException(e.toString() + ": " + e.getMessage());
        }
    }

    
    public Expression clone(Evaluator newEval) {
        //to clone an expression:
        //1 clone the expression tree
        ExpressionNode newRoot;
        newRoot = root.clone(newEval);
        if (this.hasLocalSymbols) {
            return new Expression(newRoot,newEval,localSymbols.clone(false),true);
        } else {
            return new Expression(newRoot,newEval,true);
        }
    }
   
    
    /**
     * Returns true if the expression is the constant zero
     * @return 
     */
    public boolean isConstantZero() {
        return this.root.isZero();
    }
    
    
    public void setStrExpr(String strExpr) {
		this.strExpr = strExpr;
	}


    public String getStrExpr() {
            return strExpr;
    }
    
    
    
    
}
