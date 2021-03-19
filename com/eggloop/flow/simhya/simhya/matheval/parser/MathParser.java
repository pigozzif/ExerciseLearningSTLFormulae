/* Generated By:JavaCC: Do not edit this line. MathParser.java */
package com.eggloop.flow.simhya.simhya.matheval.parser;

import com.eggloop.flow.simhya.simhya.matheval.*;
import com.eggloop.flow.simhya.simhya.matheval.function.*;
import java.util.ArrayList;
import java.util.Random;

public class MathParser implements MathParserConstants {
    private Evaluator evaluator;
    private boolean parsingWithLocalVariables;
    private boolean parsingFunctionDefinition;
    private String functionName;
    private int functionArity;
    private SymbolArray localVariables;
    private Random rand = new Random();
    private int uniqueId;

    public MathParser(Evaluator e) {
        this(new java.io.StringReader(""));
        evaluator = e;
    }

    public Expression parse( String expression ) throws ParseException, TokenMgrError, NumberFormatException {
        Expression exp = null;
        parsingWithLocalVariables = false;
        parsingFunctionDefinition = false;
        localVariables = null;
        uniqueId = 0;
        this.ReInit(new java.io.StringReader(expression));
        exp = this.Start();
        return exp;
    }


    public Expression parseWithLocalVariables(String expression,
                       SymbolArray localVars) throws ParseException, TokenMgrError, NumberFormatException {
        Expression exp = null;
        parsingWithLocalVariables = true;
        localVariables = localVars;
        parsingFunctionDefinition = false;
        uniqueId = rand.nextInt();
        this.ReInit(new java.io.StringReader(expression));
        exp = this.Start();
        return exp;
    }

/***********************************************
GRAMMAR RULES
***********************************************/
  final public Expression Start() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    ExpressionNode node;
    parsingFunctionDefinition = false;
    DynamicFunction f;
    Token t;
    if (getToken(1).kind == IDENTIFIER && getToken(2).kind == LR && !evaluator.isFunction(getToken(1).image)) {
      FunctionDef();
      jj_consume_token(DEF);
            if (parsingWithLocalVariables)
                {if (true) throw new ParseException("Parser is parsing with local variables.\u005cn"
                    + "Parsing function definition is not allowed");}
            parsingWithLocalVariables = true;
            parsingFunctionDefinition = true;
      node = Expression();
      jj_consume_token(0);
            Expression exp = new Expression(node,evaluator, true);
            exp.setLocalSymbols(localVariables);
            f = new DynamicFunction(functionName, functionArity, exp);
            evaluator.registerFunction(functionName,f);
            {if (true) return null;}
    } else if (getToken(2).kind == DEF) {
      t = jj_consume_token(IDENTIFIER);
      jj_consume_token(DEF);
      node = Expression();
      jj_consume_token(0);
            Expression exp = new Expression(node,evaluator, true);
            //MANAGE LOCAL VARIABLES HERE!!!

            try { evaluator.addExpressionVariable(t.image,exp); }
            catch(EvalException e) { {if (true) throw new ParseException("Cannot define expression variable " + t.image +":\u005cn" + e);}}
            {if (true) return null;}
    } else if (jj_2_1(1)) {
      node = Expression();
      jj_consume_token(0);
            Expression exp = new Expression(node,evaluator, true);
            //MANAGE LOCAL VARIABLES HERE!!!

            {if (true) return exp;}
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode Expression() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    ExpressionNode node;
    node = OrExpression();
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode OrExpression() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    Token t;
    ExpressionNode node,n1,n2;
    node = AndExpression();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      t = jj_consume_token(OR);
      n2 = AndExpression();
            n1 = node;
            if (!n1.isLogicalExpression())
                 {if (true) throw new ParseException("First disjunct is a non-logical expression: " + n1.getExpressionString());}
            if (!n2.isLogicalExpression())
                 {if (true) throw new ParseException("Second disjunct is a non-logical expression: " + n2.getExpressionString());}
            node = new BinaryOperatorNode(evaluator.getOperatorDefinition(t.image));
            node.addChild(n1);
            node.addChild(n2);
            node = evaluator.checkNodeDefinition(node);
    }
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode AndExpression() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    Token t;
    ExpressionNode node,n1,n2;
    node = RelationalExpression();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
      t = jj_consume_token(AND);
      n2 = RelationalExpression();
            n1 = node;
            if (!n1.isLogicalExpression())
                 {if (true) throw new ParseException("First conjuct is a non-logical expression: " + n1.getExpressionString());}
            if (!n2.isLogicalExpression())
                 {if (true) throw new ParseException("Second conjunct is a non-logical expression: " + n2.getExpressionString());}
            node = new BinaryOperatorNode(evaluator.getOperatorDefinition(t.image));
            node.addChild(n1);
            node.addChild(n2);
            node = evaluator.checkNodeDefinition(node);
    }
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode RelationalExpression() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    Token t;
    ExpressionNode node,n1,n2;
    node = AddExpression();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case GT:
    case LT:
    case EQ:
    case LE:
    case GE:
    case NEQ:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case EQ:
        t = jj_consume_token(EQ);
        break;
      case NEQ:
        t = jj_consume_token(NEQ);
        break;
      case LT:
        t = jj_consume_token(LT);
        break;
      case LE:
        t = jj_consume_token(LE);
        break;
      case GT:
        t = jj_consume_token(GT);
        break;
      case GE:
        t = jj_consume_token(GE);
        break;
      default:
        jj_la1[2] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      n2 = AddExpression();
                n1 = node;
                node = new BinaryOperatorNode(evaluator.getOperatorDefinition(t.image));
                node.addChild(n1);
                node.addChild(n2);
                node = evaluator.checkNodeDefinition(node);
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode AddExpression() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    Token t;
    ExpressionNode node,n1,n2;
    node = MultExpression();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
      case MINUS:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_3;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        t = jj_consume_token(PLUS);
        break;
      case MINUS:
        t = jj_consume_token(MINUS);
        break;
      default:
        jj_la1[5] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      n2 = MultExpression();
            n1 = node;
            node = new BinaryOperatorNode(evaluator.getOperatorDefinition(t.image));
            node.addChild(n1);
            node.addChild(n2);
            node = evaluator.checkNodeDefinition(node);
    }
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode MultExpression() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    Token t;
    ExpressionNode node,n1,n2;
    node = UnaryExpression();
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case MUL:
      case DIV:
      case MOD:
        ;
        break;
      default:
        jj_la1[6] = jj_gen;
        break label_4;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case MUL:
        t = jj_consume_token(MUL);
        break;
      case DIV:
        t = jj_consume_token(DIV);
        break;
      case MOD:
        t = jj_consume_token(MOD);
        break;
      default:
        jj_la1[7] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      n2 = UnaryExpression();
            n1 = node;
            node = new BinaryOperatorNode(evaluator.getOperatorDefinition(t.image));
            node.addChild(n1);
            node.addChild(n2);
            node = evaluator.checkNodeDefinition(node);
    }
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode UnaryExpression() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    Token t;
    ExpressionNode node,n1;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PLUS:
    case MINUS:
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PLUS:
        t = jj_consume_token(PLUS);
        break;
      case MINUS:
        t = jj_consume_token(MINUS);
        break;
      default:
        jj_la1[8] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      n1 = PowerExpression();
                    node = new UnaryOperatorNode(evaluator.getOperatorDefinition(t.image));
                    node.addChild(n1);
                    node = evaluator.checkNodeDefinition(node);
      break;
    case NOT:
      t = jj_consume_token(NOT);
      n1 = UnaryExpression();
                    if (!n1.isLogicalExpression())
                        {if (true) throw new ParseException("Trying to negate a non-logical expression: " + n1.getExpressionString());}
                    node = new UnaryOperatorNode(evaluator.getOperatorDefinition(t.image));
                    node.addChild(n1);
                    node = evaluator.checkNodeDefinition(node);
      break;
    default:
      jj_la1[9] = jj_gen;
      if (jj_2_2(1)) {
        node = PowerExpression();
      } else {
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode PowerExpression() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    Token t;
    ExpressionNode node,n1,n2;
    node = BasicExpression();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case POWER:
      t = jj_consume_token(POWER);
      n2 = UnaryExpression();
            n1 = node;
            node = new BinaryOperatorNode(evaluator.getOperatorDefinition(t.image));
            node.addChild(n1);
            node.addChild(n2);
            node = evaluator.checkNodeDefinition(node);
      break;
    default:
      jj_la1[10] = jj_gen;
      ;
    }
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode BasicExpression() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    ExpressionNode node;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INTEGER:
    case FLOAT:
    case FLOAT_SCIENTIFIC:
      node = Number();
      break;
    default:
      jj_la1[11] = jj_gen;
      if (getToken(1).kind == IDENTIFIER && getToken(2).kind == LR) {
        node = Function();
      } else {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case IDENTIFIER:
          node = Symbol();
          break;
        case LR:
          jj_consume_token(LR);
          node = Expression();
          jj_consume_token(RR);
          break;
        default:
          jj_la1[12] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode Number() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    ExpressionNode node;
    Token t;
    int n;
    double d;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INTEGER:
      t = jj_consume_token(INTEGER);
                n = Integer.parseInt(t.image);
                node = new NumericNode(n);
                node = evaluator.checkNodeDefinition(node);
      break;
    case FLOAT:
      t = jj_consume_token(FLOAT);
                d = Double.parseDouble(t.image);
                node = new NumericNode(d,false);
                node = evaluator.checkNodeDefinition(node);
      break;
    case FLOAT_SCIENTIFIC:
      t = jj_consume_token(FLOAT_SCIENTIFIC);
                d = Double.parseDouble(t.image);
                node = new NumericNode(d,true);
                node = evaluator.checkNodeDefinition(node);
      break;
    default:
      jj_la1[13] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
          {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode Function() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    ExpressionNode node, n;
    ArrayList<ExpressionNode> args = new ArrayList<ExpressionNode>();
    Token name;
    FunctionDefinition f;
    boolean ifFunction;
    name = jj_consume_token(IDENTIFIER);
            if (name.image.equals("if")) {
                ifFunction = true;
                f = null;
            }
            else if(evaluator.isFunction(name.image)) {
                ifFunction = false;
                f = evaluator.getFunctionDefinition(name.image);

            } else
                {if (true) throw new ParseException("Function " + name.image + " is not defined");}
    jj_consume_token(LR);
    if (jj_2_3(1)) {
      n = Expression();
                args.add(n);
      label_5:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case COMMA:
          ;
          break;
        default:
          jj_la1[14] = jj_gen;
          break label_5;
        }
        jj_consume_token(COMMA);
        n = Expression();
                    args.add(n);
      }
    } else {
      ;
    }
    jj_consume_token(RR);
            int argNumber = args.size();
            if (ifFunction) {
                if (argNumber != 3)
                    {if (true) throw new ParseException("Invalid number of arguments for function " + name.image);}
                node = new IfNode();
            } else {
                if (!f.isArityCongruent(argNumber))
                    {if (true) throw new ParseException("Invalid number of arguments for function " + name.image);}
                if (f.isBuiltinFunction()) {
                    switch(argNumber) {
                        case 0:
                            node = new NullaryFunctionNode(f);
                            break;
                        case 1:
                            node = new UnaryFunctionNode(f);
                            break;
                        case 2:
                            node = new BinaryFunctionNode(f);
                            break;
                        default:
                            node = new NaryFunctionNode(f,argNumber);
                            break;
                    }
                } else {
                    node = new DynamicFunctionNode(f,argNumber);
                }
            }
            for (int i=0;i<argNumber;i++) {
                n = args.get(i);
                node.addChild(n);
            }
            node = evaluator.checkNodeDefinition(node);
            {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public ExpressionNode Symbol() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    ExpressionNode node;
    Token t;
    SymbolArray vars = evaluator.getVariableReference();
    SymbolArray consts = evaluator.getConstantReference();
    ExpressionSymbolArray exps = evaluator.getExpressionVariableReference();
    t = jj_consume_token(IDENTIFIER);
            if (parsingWithLocalVariables && localVariables.isDefined(t.image)) {
                int id = localVariables.getSymbolId(t.image);
                VariableNode n1;
                if (parsingFunctionDefinition)
                    n1 = new BoundVariableNode(id,t.image,functionName);
                else
                    n1 = new BoundVariableNode(id,t.image,uniqueId);
                node = evaluator.checkNodeDefinition(n1);
            }
            else if (vars.isDefined(t.image)) {
                int id = vars.getSymbolId(t.image);
                VariableNode n1 = new VariableNode(id,vars);
                node = evaluator.checkNodeDefinition(n1);
            }
            else if (consts.isDefined(t.image)) {
                int id = consts.getSymbolId(t.image);
                node = new ConstantNode(id,consts);
                node = evaluator.checkNodeDefinition(node);
            }
            else if (exps.isDefined(t.image)) {
                int id = exps.getSymbolId(t.image);
                Expression exp = exps.getExpression(id);
                node = new ExpressionVariableNode(t.image,exp);
                node = evaluator.checkNodeDefinition(node);
            }
            else
                {if (true) throw new ParseException("Symbol " + t.image + " is not defined");}
            {if (true) return node;}
    throw new Error("Missing return statement in function");
  }

  final public void FunctionDef() throws ParseException, NumberFormatException, RuntimeException, ParseException {
    Token t;
    t = jj_consume_token(IDENTIFIER);
        if (evaluator.isFunction(t.image))
            {if (true) throw new ParseException("Function " + t.image + " already defined");}
        functionName = t.image;
        functionArity = 0;
        localVariables = new SymbolArray();
    jj_consume_token(LR);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENTIFIER:
      t = jj_consume_token(IDENTIFIER);
            try { localVariables.addSymbol(t.image,0.0); }
            catch(EvalException e) { {if (true) throw new ParseException("Error in local parameter definition of function " + functionName +
                                        ": " + e.getMessage());} }
            functionArity++;
      label_6:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case COMMA:
          ;
          break;
        default:
          jj_la1[15] = jj_gen;
          break label_6;
        }
        jj_consume_token(COMMA);
        t = jj_consume_token(IDENTIFIER);
                try { localVariables.addSymbol(t.image,0.0); }
                catch(EvalException e) { {if (true) throw new ParseException("Error in local parameter definition of function " + functionName +
                                            ": " + e.getMessage());} }
                functionArity++;
      }
      break;
    default:
      jj_la1[16] = jj_gen;
      ;
    }
    jj_consume_token(RR);
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_3R_16() {
    if (jj_3R_20()) return true;
    return false;
  }

  private boolean jj_3_2() {
    if (jj_3R_8()) return true;
    return false;
  }

  private boolean jj_3R_24() {
    if (jj_3R_25()) return true;
    return false;
  }

  private boolean jj_3R_18() {
    if (jj_scan_token(IDENTIFIER)) return true;
    return false;
  }

  private boolean jj_3R_9() {
    if (jj_3R_11()) return true;
    return false;
  }

  private boolean jj_3R_15() {
    if (jj_scan_token(LR)) return true;
    return false;
  }

  private boolean jj_3R_14() {
    if (jj_3R_19()) return true;
    return false;
  }

  private boolean jj_3R_13() {
    if (jj_3R_18()) return true;
    return false;
  }

  private boolean jj_3R_27() {
    if (jj_scan_token(NOT)) return true;
    return false;
  }

  private boolean jj_3R_12() {
    if (jj_3R_17()) return true;
    return false;
  }

  private boolean jj_3R_7() {
    if (jj_3R_9()) return true;
    return false;
  }

  private boolean jj_3R_10() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_12()) {
    jj_scanpos = xsp;
    jj_lookingAhead = true;
    jj_semLA = getToken(1).kind == IDENTIFIER && getToken(2).kind == LR;
    jj_lookingAhead = false;
    if (!jj_semLA || jj_3R_13()) {
    jj_scanpos = xsp;
    if (jj_3R_14()) {
    jj_scanpos = xsp;
    if (jj_3R_15()) return true;
    }
    }
    }
    return false;
  }

  private boolean jj_3R_23() {
    if (jj_scan_token(FLOAT_SCIENTIFIC)) return true;
    return false;
  }

  private boolean jj_3R_26() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(21)) {
    jj_scanpos = xsp;
    if (jj_scan_token(22)) return true;
    }
    return false;
  }

  private boolean jj_3R_19() {
    if (jj_scan_token(IDENTIFIER)) return true;
    return false;
  }

  private boolean jj_3R_20() {
    if (jj_3R_24()) return true;
    return false;
  }

  private boolean jj_3R_11() {
    if (jj_3R_16()) return true;
    return false;
  }

  private boolean jj_3R_25() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_26()) {
    jj_scanpos = xsp;
    if (jj_3R_27()) {
    jj_scanpos = xsp;
    if (jj_3_2()) return true;
    }
    }
    return false;
  }

  private boolean jj_3R_22() {
    if (jj_scan_token(FLOAT)) return true;
    return false;
  }

  private boolean jj_3_1() {
    if (jj_3R_7()) return true;
    return false;
  }

  private boolean jj_3_3() {
    if (jj_3R_7()) return true;
    return false;
  }

  private boolean jj_3R_21() {
    if (jj_scan_token(INTEGER)) return true;
    return false;
  }

  private boolean jj_3R_8() {
    if (jj_3R_10()) return true;
    return false;
  }

  private boolean jj_3R_17() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_21()) {
    jj_scanpos = xsp;
    if (jj_3R_22()) {
    jj_scanpos = xsp;
    if (jj_3R_23()) return true;
    }
    }
    return false;
  }

  /** Generated Token Manager. */
  public MathParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  /** Whether we are looking ahead. */
  private boolean jj_lookingAhead = false;
  private boolean jj_semLA;
  private int jj_gen;
  final private int[] jj_la1 = new int[17];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x100000,0x80000,0x7e000,0x7e000,0x600000,0x600000,0x3800000,0x3800000,0x600000,0x4600000,0x8000000,0xe0,0x10000200,0xe0,0x1000,0x1000,0x200,};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[3];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public MathParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public MathParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new MathParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public MathParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new MathParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public MathParser(MathParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(MathParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 17; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = jj_lookingAhead ? jj_scanpos : token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[31];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 17; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 31; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 3; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }

}