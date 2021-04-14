package eggloop.flow.parsers;

import eggloop.flow.expr.*;
import eggloop.flow.mitl.*;
import eggloop.flow.modelChecking.SignalFunction;
import eggloop.flow.modelChecking.SignalFunctionType;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;

import java.util.ArrayList;

public class MitlFactory {

	private MitlPropertiesList propertiesList = new MitlPropertiesList();
	private Context modelNamespace;
	private ArrayList<String> errors = new ArrayList<String>();
	private ArrayList<SignalFunction> signalFunctions = new ArrayList<SignalFunction>();

	public MitlFactory(Context modelNamespace) {
		this.modelNamespace = modelNamespace;
	}

	public ArrayList<String> getErrors() {
		return errors;
	}

	public ArrayList<SignalFunction> getFunctionsOfSignals() {
		return signalFunctions;
	}

	public MitlPropertiesList constructProperties(String text) {
		propertiesList = new MitlPropertiesList();

		MiTLLexer lex = new MiTLLexer(new ANTLRStringStream(text));
		CommonTokenStream tokens = new CommonTokenStream(lex);
		eggloop.flow.parsers.MiTLParser parser = new eggloop.flow.parsers.MiTLParser(tokens);
		eggloop.flow.parsers.MiTLParser.eval_return r = null;
		try {
			r = parser.eval();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		Tree ast = (Tree) r.getTree();

		if (ast.getType() == 0)
			constructModel(ast);
		else if (ast.getType() == eggloop.flow.parsers.MiTLParser.CONST)
			constructDeclaration(ast);
		else
			constructMiTLStatement(ast);

		return propertiesList;
	}

	public void constructDeclaration(String text) throws RecognitionException {
		MiTLLexer lex = new MiTLLexer(new ANTLRStringStream(text));
		CommonTokenStream tokens = new CommonTokenStream(lex);
		eggloop.flow.parsers.MiTLParser parser = new eggloop.flow.parsers.MiTLParser(tokens);
		eggloop.flow.parsers.MiTLParser.declaration_return r = parser.declaration();
		Tree ast = (Tree) r.getTree();
		constructDeclaration(ast);
	}

	public boolean constructProperty(String text) throws RecognitionException {
		MiTLLexer lex = new MiTLLexer(new ANTLRStringStream(text));
		CommonTokenStream tokens = new CommonTokenStream(lex);
		eggloop.flow.parsers.MiTLParser parser = new eggloop.flow.parsers.MiTLParser(tokens);
		eggloop.flow.parsers.MiTLParser.exprOR_return r = parser.exprOR();
		Tree ast = (Tree) r.getTree();
		constructMiTLStatement(ast);

		// TODO: return the error message
		return parser.getNumberOfSyntaxErrors() == 0;
	}

	//

	// ======================================================

	//

	private void constructModel(Tree astRoot) {
		for (int i = 0; i < astRoot.getChildCount(); i++) {
			final Tree statement = astRoot.getChild(i);
			final int type = statement.getType();
			if (type == eggloop.flow.parsers.MiTLParser.CONST)
				constructDeclaration(statement);
			else
				constructMiTLStatement(statement);
		}
	}

	private void constructDeclaration(Tree node) {
		final int type = node.getChild(0).getType();
		final String name = node.getChild(1).getText();
		if (modelNamespace.containsVariable(name))
			throw new IllegalStateException("Variable already declared!");

		switch (type) {
		case eggloop.flow.parsers.MiTLParser.DOUBLE:
			propertiesList.addConstant(name, "double");
			break;
		case eggloop.flow.parsers.MiTLParser.INT:
			propertiesList.addConstant(name, "int");
			break;
		case eggloop.flow.parsers.MiTLParser.BOOL:
			propertiesList.addConstant(name, "bool");
			break;
		default:
			return;
		}
		if (node.getChildCount() == 3)
			propertiesList.setConstant(name, node.getChild(2).getText());
	}

	private void constructMiTLStatement(Tree node) {
		MiTL mitl = constructMiTL(node);
		propertiesList.addProperty(mitl);
	}

	//

	// ======================================================

	//

	private RelationalExpression constructRelational(Tree node) {
		final int type = node.getType();
		final ArithmeticExpression l = constructExpression(node.getChild(0));
		final ArithmeticExpression r = constructExpression(node.getChild(1));
		switch (type) {
		case eggloop.flow.parsers.MiTLParser.EQ:
			return new RelationalExpression(RelationalOperator.EQ, l, r);
		case eggloop.flow.parsers.MiTLParser.NEQ:
			return new RelationalExpression(RelationalOperator.NEQ, l, r);
		case eggloop.flow.parsers.MiTLParser.GT:
			return new RelationalExpression(RelationalOperator.GT, l, r);
		case eggloop.flow.parsers.MiTLParser.GE:
			return new RelationalExpression(RelationalOperator.GE, l, r);
		case eggloop.flow.parsers.MiTLParser.LT:
			return new RelationalExpression(RelationalOperator.LT, l, r);
		case eggloop.flow.parsers.MiTLParser.LE:
			return new RelationalExpression(RelationalOperator.LE, l, r);
		default:
			return null;
		}
	}

	private ArithmeticExpression constructExpression(Tree node) {
		if (node == null)
			return null;
		final int type = node.getType();
		switch (type) {
		case eggloop.flow.parsers.MiTLParser.INTEGER:
		case eggloop.flow.parsers.MiTLParser.FLOAT:
			return new ArithmeticConstant(Double.parseDouble(node.getText()));

		case eggloop.flow.parsers.MiTLParser.ID:
			if (node.getChildCount() == 0) {
				final String name = node.getText();
				Variable var = propertiesList.getConstant(name);
				if (var != null)
					return var;
				return modelNamespace.getVariable(name);
			} else
				return constructFunction(node);

		case eggloop.flow.parsers.MiTLParser.PLUS:
		case eggloop.flow.parsers.MiTLParser.MULT:
		case eggloop.flow.parsers.MiTLParser.DIV:
			return constructBinExpression(node);

		case eggloop.flow.parsers.MiTLParser.MINUS:
			final int childCount = node.getChildCount();
			if (childCount == 1)
				return new ArithmeticUnaryExpr(ArithmeticUnaryOperator.UMINUS,
						constructExpression(node.getChild(0)));
			else if (childCount == 2)
				return constructBinExpression(node);
		}
		return null;
	}

	private ArithmeticExpression constructFunction(Tree node) {
		final String name = node.getText();
		final int n = node.getChildCount();
		ArithmeticExpression[] args = new ArithmeticExpression[n];
		for (int i = 0; i < n; i++)
			args[i] = constructExpression(node.getChild(i));

		// function of a signal
		SignalFunctionType signalType = null;
		for (SignalFunctionType t : SignalFunctionType.values())
			if (name.equals(t.toString())) {
				signalType = t;
				break;
			}
		if (signalType != null) {
			try {
				signalType.setArguments(args);
			} catch (Exception e) {
				errors.add(signalType.usage());
			}
			SignalFunction sf = new SignalFunction(name, modelNamespace,
					signalType);
			signalFunctions.add(sf);
			return sf;
		}

		// regular arithmetic function
		ArithmeticFunctionType type = null;
		for (ArithmeticFunctionType t : ArithmeticFunctionType.values())
			if (name.equals(t.toString())) {
				type = t;
				break;
			}
		if (type == null)
			errors.add("Function \"" + name + "\" is not defined!");
		return new ArithmeticFunction(type, args[0]);
	}

	private ArithmeticExpression constructBinExpression(Tree node) {
		final int type = node.getType();
		final ArithmeticExpression l = constructExpression(node.getChild(0));
		final ArithmeticExpression r = constructExpression(node.getChild(1));
		switch (type) {
		case eggloop.flow.parsers.MiTLParser.PLUS:
			return new ArithmeticBinaryExpr(ArithmeticBinaryOperator.PLUS, l, r);
		case eggloop.flow.parsers.MiTLParser.MINUS:
			return new ArithmeticBinaryExpr(ArithmeticBinaryOperator.MINUS, l,
					r);
		case eggloop.flow.parsers.MiTLParser.MULT:
			return new ArithmeticBinaryExpr(ArithmeticBinaryOperator.MULT, l, r);
		case eggloop.flow.parsers.MiTLParser.DIV:
			return new ArithmeticBinaryExpr(ArithmeticBinaryOperator.DIVIDE, l,
					r);
		default:
			return null;
		}
	}

	private MiTL constructMiTL(Tree node) {
		final int type = node.getType();
		switch (type) {
		case eggloop.flow.parsers.MiTLParser.U:
			return constructUntil(node);
		case eggloop.flow.parsers.MiTLParser.F:
			return constructFinally(node);
		case eggloop.flow.parsers.MiTLParser.G:
			return constructGlobally(node);

		case eggloop.flow.parsers.MiTLParser.AND:
			return new MitlConjunction(constructMiTL(node.getChild(0)),
					constructMiTL(node.getChild(1)));
		case eggloop.flow.parsers.MiTLParser.OR:
			return new MitlDisjunction(constructMiTL(node.getChild(0)),
					constructMiTL(node.getChild(1)));
		case eggloop.flow.parsers.MiTLParser.NOT:
			return new MitlNegation(constructMiTL(node.getChild(0)));

		case eggloop.flow.parsers.MiTLParser.TRUE:
			return new MitlTrue();
		case eggloop.flow.parsers.MiTLParser.FALSE:
			return new MitlFalse();
		default:
			return new MitlAtomic(constructRelational(node));
		}
	}

	private MitlUntil constructUntil(Tree node) {
		final double t1;
		final double t2;
		final MiTL f1 = constructMiTL(node.getChild(0));
		final Tree timebound = node.getChild(1);
		if (timebound.getType() == eggloop.flow.parsers.MiTLParser.COMMA) {
			t1 = constructExpression(timebound.getChild(0)).evaluate();
			t2 = constructExpression(timebound.getChild(1)).evaluate();
		} else {
			t1 = 0;
			t2 = constructExpression(timebound).evaluate();
		}
		final MiTL f2 = constructMiTL(node.getChild(2));
		return new MitlUntil(f1, t1, t2, f2);
	}

	private MitlFinally constructFinally(Tree node) {
		final double t1;
		final double t2;
		final Tree timebound = node.getChild(0);
		if (timebound.getType() == eggloop.flow.parsers.MiTLParser.COMMA) {
			t1 = constructExpression(timebound.getChild(0)).evaluate();
			t2 = constructExpression(timebound.getChild(1)).evaluate();
		} else {
			t1 = 0;
			t2 = constructExpression(timebound).evaluate();
		}
		final MiTL f = constructMiTL(node.getChild(1));
		return new MitlFinally(t1, t2, f);
	}

	private MitlGlobally constructGlobally(Tree node) {
		final double t1;
		final double t2;
		final Tree timebound = node.getChild(0);
		if (timebound.getType() == MiTLParser.COMMA) {
			t1 = constructExpression(timebound.getChild(0)).evaluate();
			t2 = constructExpression(timebound.getChild(1)).evaluate();
		} else {
			t1 = 0;
			t2 = constructExpression(timebound).evaluate();
		}
		final MiTL f = constructMiTL(node.getChild(1));
		return new MitlGlobally(t1, t2, f);

	}
}
