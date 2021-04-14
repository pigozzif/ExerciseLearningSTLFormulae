package eggloop.flow.modelChecking;

import eggloop.flow.expr.ArithmeticExpression;
import eggloop.flow.expr.Context;
import eggloop.flow.expr.Variable;

import java.util.Set;

public class SignalFunction extends ArithmeticExpression {

	private String name;
	private Variable auxiliaryVariable = null;
	private SignalFunctionType type;

	public SignalFunction(String name, Context context, SignalFunctionType type) {
		this.name = name;
		final String var = type.getVariable().getName();
		this.auxiliaryVariable = new Variable(var + "__" + name, context);
		this.type = type;
	}

	public double[] evaluateSignal(final double[] t, final double[] x) {
		return type.evaluateSignal(t, x);
	}

	public Variable getVariable() {
		return type.getVariable();
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public double evaluate() {
		if (auxiliaryVariable == null)
			throw new IllegalStateException(
					"Auxiliary variable has not been initialised!");
		return auxiliaryVariable.evaluate();
	}

	@Override
	public Set<Variable> getVariables() {
		return null;
	}

	@Override
	public String toString() {
		return name + "(" + type.getVariable().getName() + ")";
	}

}
