/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eggloop.flow.simhya.simhya.matheval;
import com.eggloop.flow.simhya.simhya.matheval.function.*;
import com.eggloop.flow.simhya.simhya.matheval.operator.*;
import java.util.HashMap;
import java.util.ArrayList;
import com.eggloop.flow.simhya.simhya.matheval.parser.*;


/**
 *
 * @author Luca
 */
public class Evaluator {
    private HashMap<String,FunctionDefinition> functionLookupTable;
    private HashMap<String,OperatorDefinition> operatorLookupTable;
    private HashMap<String,ExpressionNode> expressionNodeLookupTable;
    private ArrayList<Expression> expressionLookupTable;
    private ArrayList<String> dynamicFunctions;
    int evaluationCode;
    private SymbolArray variables;
    private SymbolArray constants;
    private ExpressionSymbolArray expressionVariables;
    private MathParser parser;
    

    public Evaluator() {
        this.functionLookupTable = new HashMap<String,FunctionDefinition>();
        this.operatorLookupTable = new HashMap<String,OperatorDefinition>();
        this.expressionNodeLookupTable = new HashMap<String,ExpressionNode>();
        this.expressionLookupTable = new ArrayList<Expression>();
        this.variables = new SymbolArray();
        this.constants = new SymbolArray();
        this.dynamicFunctions = new ArrayList<String>();
        this.expressionVariables = new ExpressionSymbolArray();
        evaluationCode = 0;
        registerOperators();
        registerBuiltinFunctions();
        parser = new MathParser(this);
    }


    public int parse(String expression) throws ParseException, TokenMgrError, NumberFormatException{
        Expression exp = parser.parse(expression);
        if (exp != null) {
            int id = exp.getId();
            return id;
        } else return -1;
    }

    public int parse(String expression, SymbolArray localVariables) throws ParseException, TokenMgrError, NumberFormatException{
        Expression exp = parser.parseWithLocalVariables(expression, localVariables);
        if (exp != null) {
            int id = exp.getId();
            return id;
        } else return -1;
        
    }

    public Expression parseExpression(String expression) throws ParseException, TokenMgrError, NumberFormatException {
        return parser.parse(expression);
    }
    
    public Expression parseExpression(String expression, SymbolArray localVariables) throws ParseException, TokenMgrError, NumberFormatException {
        return parser.parseWithLocalVariables(expression, localVariables);
    }

    
    public Expression getExpression(int id) {
        if (id <0 || id >= this.expressionLookupTable.size())
            throw new EvalException("There is no expression with token " + id);
        return this.expressionLookupTable.get(id);
    }

    
    /**
     * registers builtin functions in the function table;
     */
    private void registerBuiltinFunctions() {
        functionLookupTable.put("abs", new Abs());
        functionLookupTable.put("acos", new Acos());
        functionLookupTable.put("asin", new Asin());
        functionLookupTable.put("atan", new Atan());
        functionLookupTable.put("cbrt", new Cbrt());
        functionLookupTable.put("ceil", new Ceil());
        functionLookupTable.put("cos", new Cos());
        functionLookupTable.put("cosh", new Cosh());
        functionLookupTable.put("exp", new Exp());
        functionLookupTable.put("floor", new Floor());
        functionLookupTable.put("log", new Log());
        functionLookupTable.put("log10", new Log10());
        functionLookupTable.put("log2", new Log2());
        functionLookupTable.put("max", new Max());
        functionLookupTable.put("min", new Min());
        functionLookupTable.put("rexp", new RandExp());
        functionLookupTable.put("rnorm", new RandNorm());
        functionLookupTable.put("sign", new Sign());
        functionLookupTable.put("sin", new Sin());
        functionLookupTable.put("sinh", new Sinh());
        functionLookupTable.put("sqrt", new Sqrt());
        functionLookupTable.put("tanh", new Tanh());
        functionLookupTable.put("tan", new Tan());
        functionLookupTable.put("uniform", new Uniform());
        functionLookupTable.put("pnorm", new ProbNorm());
        functionLookupTable.put("rbeta", new RandBeta());
        functionLookupTable.put("rgamma", new RandGamma());
        functionLookupTable.put("rweibull", new RandWeibull());
        functionLookupTable.put("rpoisson", new RandPoisson());
        functionLookupTable.put("rbinom", new RandBinom());
        functionLookupTable.put("rhyperg", new RandHypergeom());
        functionLookupTable.put("rgeom", new RandGeom());
    }
    /**
     * Registers operators in the operator lookup table;
     */
    private void registerOperators() {
        this.operatorLookupTable.put("+", new Plus());
        this.operatorLookupTable.put("-", new Minus());
        this.operatorLookupTable.put("*", new Multiply());
        this.operatorLookupTable.put("/", new Divide());
        this.operatorLookupTable.put("%", new Mod());
        this.operatorLookupTable.put("^", new Power());
        this.operatorLookupTable.put("&&", new And());
        this.operatorLookupTable.put("||", new Or());
        this.operatorLookupTable.put("!", new Not());
        this.operatorLookupTable.put("==", new Equal());
        this.operatorLookupTable.put("!=", new NotEqual());
        this.operatorLookupTable.put("<", new Less());
        this.operatorLookupTable.put("<=", new LessOrEqual());
        this.operatorLookupTable.put(">", new Greater());
        this.operatorLookupTable.put(">=", new GreaterOrEqual());
        this.operatorLookupTable.put("plus", new Plus());
        this.operatorLookupTable.put("minus", new Minus());
        this.operatorLookupTable.put("multiply", new Multiply());
        this.operatorLookupTable.put("divide", new Divide());
        this.operatorLookupTable.put("mod", new Mod());
        this.operatorLookupTable.put("power", new Power());
        this.operatorLookupTable.put("and", new And());
        this.operatorLookupTable.put("or", new Or());
        this.operatorLookupTable.put("not", new Not());
        this.operatorLookupTable.put("equal", new Equal());
        this.operatorLookupTable.put("notequal", new NotEqual());
        this.operatorLookupTable.put("less", new Less());
        this.operatorLookupTable.put("lessorequal", new LessOrEqual());
        this.operatorLookupTable.put("greater", new Greater());
        this.operatorLookupTable.put("greaterorequal", new GreaterOrEqual());
    }

    public int addVariable(String name, double value) {
        if (variables.containsSymbol(name) || constants.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new EvalException("Symbol  " + name + " already defined.");
        int id = variables.addSymbol(name, value);
        return id;
    }

    public int addVariable(String name, double value, Expression expression) {
        if (variables.containsSymbol(name) || constants.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new EvalException("Symbol  " + name + " already defined.");
        if (expression == null)
            throw new EvalException("Expression undefined.");
        int id = variables.addSymbol(name, value, expression);
        return id;
    }

    public int addConstant(String name, double value) {
        if (variables.containsSymbol(name) || constants.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new EvalException("Symbol " + name + " already defined.");
        int id = constants.addSymbol(name, value);
        return id;
    }

    public int addConstant(String name, double value, Expression expression) {
        if (variables.containsSymbol(name) || constants.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new EvalException("Symbol " + name + " already defined.");
        if (expression == null)
            throw new EvalException("Expression undefined.");
        int id = constants.addSymbol(name, value, expression);
        return id;
    }

    public int addExpressionVariable(String name, Expression exp) {
        if (variables.containsSymbol(name) || constants.containsSymbol(name) || expressionVariables.containsSymbol(name))
            throw new EvalException("Symbol " + name + " already defined.");
        int id = expressionVariables.addSymbol(name, exp);
        return id;
    }

    public void registerFunction(String name, FunctionDefinition func) {
        if (this.functionLookupTable.containsKey(name) || name.equals("if"))
            throw new EvalException("There is already a function with name " + name);
        this.functionLookupTable.put(name, func);
        if (func instanceof DynamicFunction)
            this.dynamicFunctions.add(name);
    }
    
    ArrayList<String> getDynamicFunctionNames() {
        return this.dynamicFunctions;
    }

    public boolean isVariable(String name) {
        return variables.symbolLookupTable.containsKey(name);
    }


    public boolean isConstant(String name) {
        return constants.symbolLookupTable.containsKey(name);
    }


    public boolean isExpressionVariable(String name) {
        return expressionVariables.containsSymbol(name);
    }

    public boolean isFunction(String name) {
        return name.equals("if") || this.functionLookupTable.containsKey(name);
    }

    public int getVariableCode(String name) {
        return variables.symbolLookupTable.get(name);
    }

    public int getConstantCode(String name) {
        return constants.symbolLookupTable.get(name);
    }

    public int getExpressionVariableCode(String name) {
        return expressionVariables.symbolLookupTable.get(name);
    }

    public SymbolArray getVariableReference() {
        return this.variables;
    }

    public SymbolArray getConstantReference() {
        return this.constants;
    }

    public ExpressionSymbolArray getExpressionVariableReference() {
        return this.expressionVariables;
    }


    /**
     * Sets the value of the variable with specified id
     * @param id the id of the variable
     * @param value a new double value
     * @throws {@link EvalException} if there is no variable with specified id
     */
    public void setVariableValue(int id, double value) {
        if (id >= variables.numberOfSymbols || id < 0)
            throw new EvalException("There is no variable with id " + id);
        variables.values[id] = value;
    }
    /**
     * Sets the value of the variable with specified name
     * @param name the name of the variable
     * @param value a new double value
     * @throws {@link EvalException} if there is no variable with specified id
     */
    public void setVariableValue(String name, double value) {
        Integer id = variables.symbolLookupTable.get(name);
        if (id == null)
            throw new EvalException("Variable " + name + " undefined.");
        variables.values[id] = value;
    }
    /**
     * Sets the value of the constant with specified id
     * @param id the id of the constant
     * @param value a new double value
     * @throws {@link EvalException} if there is no constant with specified id
     */
    public void setConstantValue(int id, double value) {
        if (id >= constants.numberOfSymbols || id < 0)
            throw new EvalException("There is no constant with id " + id);
        constants.values[id] = value;
    }
    /**
     * Sets the value of the constant with specified name
     * @param name the name of the constant
     * @param value a new double value
     * @throws {@link EvalException} if there is no constant with specified id
     */
    public void setConstantValue(String name, double value) {
        Integer id = constants.symbolLookupTable.get(name);
        if (id == null)
            throw new EvalException("Constant " + name + " undefined.");
        constants.values[id] = value;
    }
    /**
     * Sets the value of the expression variable with specified id
     * @param id the id of the variable
     * @param exp an expression
     * @throws {@link EvalException} if there is no variable with specified id
     */
    public void setExpressionVariableValue(int id, Expression exp) {
        if (id >= expressionVariables.numberOfSymbols || id < 0)
            throw new EvalException("There is no expression variable with id " + id);
        expressionVariables.values[id] = exp;
    }
    /**
     * Sets the value of the expression variable with specified name
     * @param name the name of the variable
     * @param exp an expression
     * @throws {@link EvalException} if there is no variable with specified id
     */
    public void setExpressionVariableValue(String name, Expression exp) {
        Integer id = expressionVariables.symbolLookupTable.get(name);
        if (id == null)
            throw new EvalException("expression variable " + name + " undefined.");
        expressionVariables.values[id] = exp;
    }
    /**
     * Returns the current value of the variable with specified id.
     * @param id the integer code of the variable
     * @return a double value
     * @throws {@link EvalException} if there is no variable with specified id
     */
    public double getVariableValue(int id) {
         if (id >= variables.numberOfSymbols || id < 0)
            throw new EvalException("There is no variable with id " + id);
        return variables.values[id];
    }
    /**
     * Returns the current value of the specified variable
     * @param name the name of the variable
     * @return a double value
     * @throws {@link EvalException} if the variable name is not defined
     */
    public double getVariableValue(String name) {
        Integer id = variables.symbolLookupTable.get(name);
        if (id == null)
            throw new EvalException("Variable " + name + " undefined.");
        return variables.values[id];
    }
    /**
     * Returns the current value of the constant with specified id.
     * @param id the integer code of the constant
     * @return a double value
     * @throws {@link EvalException} if there is no constant with specified id
     */
    public double getConstantValue(int id) {
        if (id >= constants.numberOfSymbols || id < 0)
            throw new EvalException("There is no constant with id " + id);
        return constants.values[id];
    }
    /**
     * Returns the current value of the specified constant
     * @param name the name of the constant
     * @return a double value
     * @throws {@link EvalException} if the constant name is not defined
     */
    public double getConstantValue(String name) {
        Integer id = constants.symbolLookupTable.get(name);
        if (id == null)
            throw new EvalException("Constant " + name + " undefined.");
        return constants.values[id];
    }
    /**
     * Returns the current value of the variable with specified id.
     * @param id the integer code of the variable
     * @return a double value
     * @throws {@link EvalException} if there is no variable with specified id
     */
    public double getExpressionVariableValue(int id) {
         if (id >= expressionVariables.numberOfSymbols || id < 0)
            throw new EvalException("There is no expression variable with id " + id);
        return expressionVariables.values[id].computeValue();
    }
    /**
     * Returns the current value of the specified variable
     * @param name the name of the variable
     * @return a double value
     * @throws {@link EvalException} if the variable name is not defined
     */
    public double getExpressionVariableValue(String name) {
        Integer id = expressionVariables.symbolLookupTable.get(name);
        if (id == null)
            throw new EvalException("Expression variable " + name + " undefined.");
        return expressionVariables.values[id].computeValue();
    }

    public String getVariableName(int id) {
        if (id >= variables.numberOfSymbols || id < 0)
            throw new EvalException("There is no variable with id " + id);
        return variables.names.get(id);
    }

    public String getConstantName(int id) {
        if (id >= constants.numberOfSymbols || id < 0)
            throw new EvalException("There is no constant with id " + id);
        return constants.names.get(id);
    }

    public String getExpressionVariableName(int id) {
        if (id >= expressionVariables.numberOfSymbols || id < 0)
            throw new EvalException("There is no expression variable with id " + id);
        return expressionVariables.names.get(id);
    }
    /**
     * Returns an arraylist containing the name of all variables
     * @return
     */
    public ArrayList<String> getNameOfAllVariables() {
        return (ArrayList<String>)variables.names.clone();
    }
    /**
     * Returns an arraylist containing the name of all constants
     * @return
     */
    public ArrayList<String> getNameOfAllConstants() {
        return (ArrayList<String>)constants.names.clone();
    }

    /**
     * Returns an arraylist containing the name of all expression variables
     * @return
     */
    public ArrayList<String> getNameOfAllExpressionVariables() {
        return (ArrayList<String>)expressionVariables.names.clone();
    }



    /**
     * gets the current evaluation code
     * @return
     */
    public int getEvaluationCode() {
        return this.evaluationCode;
    }

    /**
     * changes the evaluation code and returns the new one.
     * @return
     */
    public int newEvaluationCode() {
        this.evaluationCode++;
        return this.evaluationCode;
    }


    /**
     * Checks if an expression node has already been defined. This is done by comparing
     * the whole expression below the node. If so, returns the equivalent node. 
     * Otherwise, it adds the new node to the lookup table and returns it.
     * BoundedVariableNodes are ignored!
     * @param node
     * @return
     */
    public ExpressionNode checkNodeDefinition(ExpressionNode node) {
        String expression = node.getExpressionString(false);
        ExpressionNode n = this.expressionNodeLookupTable.get(expression);
        if (n != null)
            return n;
        else {
            this.expressionNodeLookupTable.put(expression, node);
            return node;
        }
    }




    public FunctionDefinition getFunctionDefinition(String name) {
        FunctionDefinition f = this.functionLookupTable.get(name);
        if (f == null)
            throw new EvalException("Function " + name + " is not defined.");
        return f;
    }

    public OperatorDefinition getOperatorDefinition(String name) {
        OperatorDefinition op = this.operatorLookupTable.get(name);
        if (op == null)
            throw new EvalException("Operator " + name + " is not defined.");
        return op;
    }


    int addExpression(Expression e) {
        int id = this.expressionLookupTable.size();
        this.expressionLookupTable.add(e);
        e.setId(id);
        return id;
    }

    public void replaceInitialValueExpression(int id, Expression newExpr) {
        variables.replaceInitialValueExpression(id, newExpr);
    }

    public void replaceInitialValueExpression(String name, Expression newExpr) {
        variables.replaceInitialValueExpression(name, newExpr);
    }

    
    /**
     * wraps the given symbol in an expression.
     * @param name
     * @return 
     */
    public Expression generateExpressionForSymbol(String name) {
        if (variables.containsSymbol(name)) {
            ExpressionNode n = new VariableNode(variables.getSymbolId(name),variables);
            n = this.checkNodeDefinition(n);
            return new Expression(n,this,false);
        } else if (constants.containsSymbol(name)) {
            ExpressionNode n = new ConstantNode(constants.getSymbolId(name),constants);
            n = this.checkNodeDefinition(n);
            return new Expression(n,this,false);
        } else if (expressionVariables.containsSymbol(name)) {
            int id = expressionVariables.getSymbolId(name);
            Expression exp = expressionVariables.getExpression(id);
            ExpressionNode node = new ExpressionVariableNode(name,exp);
            node = this.checkNodeDefinition(node);
            return new Expression(node,this,false);
        } else 
            throw new EvalException("Symbol " + name + " has not been defined");
    }
    
    /**
     * Wraps the given symbol list in expressions
     * @param names
     * @return 
     */
    
    public ArrayList<Expression> generateExpressionForSymbol(ArrayList<String> names) {
        ArrayList<Expression> list = new ArrayList<Expression>();
        for (String s : names)
            list.add(this.generateExpressionForSymbol(s));
        return list;
    }
    
    
    @Override
    public Evaluator clone() {
        Evaluator newEval = new Evaluator();
        /*
         * Ordine:
         * aggiungere function definition? ho bisogno della definition list? meglio va
         * duplicare expression di exp vars 
         * rimpiazzare expr su expr vars node su function definition
         * e poi initial expressions di vars e params
         * ritornare la minkia
         */
        for (int i=0;i<this.variables.numberOfSymbols;i++)
            newEval.addVariable(variables.getName(i), variables.values[i]);
        for (int i=0;i<this.constants.numberOfSymbols;i++)
            newEval.addConstant(constants.getName(i), constants.values[i]);
        for (int i=0;i<this.expressionVariables.numberOfSymbols;i++)
            newEval.addExpressionVariable(this.expressionVariables.getName(i), null);
        for (String f : this.dynamicFunctions) {
            //extract name from function def
            FunctionDefinition fdef = this.getFunctionDefinition(f);
            Expression exp = ((DynamicFunction)fdef).getFunctionDefinition().clone(newEval);
            DynamicFunction newf = new DynamicFunction(f,fdef.getArity(),exp);
            newEval.registerFunction(f, newf);
        }
        for (int i=0;i<this.expressionVariables.numberOfSymbols;i++) {
            //replacing expressions in expression variables
            Expression exp = expressionVariables.values[i].clone(newEval);
            newEval.expressionVariables.values[i] = exp;
        }
        for (int i=0;i<this.variables.numberOfSymbols;i++)
            //setting initia value expressions correctly
            if (variables.initialValueExpression.get(i) != null) {
                Expression exp = variables.initialValueExpression.get(i).clone(newEval);
                newEval.variables.initialValueExpression.set(i, exp);
            }
        for (int i=0;i<this.constants.numberOfSymbols;i++)
            if (constants.initialValueExpression.get(i) != null) {
                Expression exp = constants.initialValueExpression.get(i).clone(newEval);
                newEval.constants.initialValueExpression.set(i, exp);
            }
        return newEval;
    }
    
    
    /**
     * Clones an expression with respect to this evaluator
     * @param exp
     * @return 
     */
    public Expression cloneExpression(Expression exp) {
        return exp.clone(this);
    }
    
    
    public String functionDefinitionsToMatlabCode() {
        String s = "";
        for (String f : this.dynamicFunctions) {
            FunctionDefinition F = this.getFunctionDefinition(f);
            s += F.toMatlabFunction() + "\n";
        }
        return s;
    }
    
    public String expressionDefinitionsToMatlabCode() {
        String s = "";
        int n = this.expressionVariables.numberOfSymbols;
        for (int i=0;i<n;i++) {
            s += "expr_" + this.expressionVariables.getName(i) + " = " 
                    + this.expressionVariables.getExpression(i).toMatlabCode() + ";\n";
        }
        return s;
    }
    
    
}
