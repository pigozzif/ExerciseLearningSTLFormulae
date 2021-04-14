/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;

import eggloop.flow.simhya.simhya.matheval.function.Max;
import eggloop.flow.simhya.simhya.matheval.function.Min;
import eggloop.flow.simhya.simhya.matheval.operator.*;
import org.sbml.jsbml.ASTNode;

/**
 * @author Luca
 */
public class UnaryOperatorNode extends OperatorNode {

    public UnaryOperatorNode(OperatorDefinition op) {
        super(op, 1);
    }

    @Override
    public boolean canBeConvertedToNumericalExpression() {
        if (operator instanceof Not)
            return children[0].canBeConvertedToNumericalExpression();
        else
            return false;
    }

    @Override
    public ExpressionNode convertToNumericalExpression(Evaluator eval) {
        if (operator instanceof Not) {
            if (children[0] instanceof OperatorNode) {
                //child must be an instance of an operator node
                OperatorNode child = (OperatorNode) children[0];
                if (child.operator instanceof And || child.operator instanceof Or) {
                    //bring negation inside and change and into or
                    //negate first child
                    ExpressionNode n1 = new UnaryOperatorNode(new Not());
                    n1.addChild(child.children[0]);
                    n1 = n1.convertToNumericalExpression(eval);
                    //negate second child
                    ExpressionNode n2 = new UnaryOperatorNode(new Not());
                    n2.addChild(child.children[1]);
                    n2 = n2.convertToNumericalExpression(eval);
                    //create a new max node for or or a min node for and.
                    ExpressionNode n;
                    if (child.operator instanceof And)
                        n = new BinaryFunctionNode(new Max());
                    else
                        n = new BinaryFunctionNode(new Min());
                    n.addChild(n1);
                    n.addChild(n2);
                    n = eval.checkNodeDefinition(n);
                    return n;
                } else if (child.operator instanceof Greater ||
                        child.operator instanceof GreaterOrEqual) {
                    ExpressionNode n = new BinaryOperatorNode(new Minus());
                    //!(a>=b) becomes (b-a)
                    n.addChild(child.children[1]);
                    n.addChild(child.children[0]);
                    n = eval.checkNodeDefinition(n);
                    return n;
                } else if (child.operator instanceof Less ||
                        child.operator instanceof LessOrEqual) {
                    ExpressionNode n = new BinaryOperatorNode(new Minus());
                    //!(a<=b) becomes (a-b)
                    n.addChild(child.children[0]);
                    n.addChild(child.children[1]);
                    n = eval.checkNodeDefinition(n);
                    return n;
                } else if (child.operator instanceof Equal) {
                    ExpressionNode n = new IfNode();
                    ExpressionNode n1 = new BinaryOperatorNode(new NotEqual());
                    n1.addChild(child.children[0]);
                    n1.addChild(child.children[1]);
                    n1 = eval.checkNodeDefinition(n1);
                    n.addChild(n1);
                    n.addChild(new NumericNode(1));
                    n.addChild(new NumericNode(-1));
                    n = eval.checkNodeDefinition(n);
                    return n;
                } else if (child.operator instanceof NotEqual) {
                    ExpressionNode n = new IfNode();
                    ExpressionNode n1 = new BinaryOperatorNode(new Equal());
                    n1.addChild(child.children[0]);
                    n1.addChild(child.children[1]);
                    n1 = eval.checkNodeDefinition(n1);
                    n.addChild(n1);
                    n.addChild(new NumericNode(1));
                    n.addChild(new NumericNode(-1));
                    n = eval.checkNodeDefinition(n);
                    return n;
                } else
                    throw new EvalException("Unknown logical operator!!1");
            } else {
                //case of negation of  a logical function.
                ExpressionNode n1 = children[0].convertToNumericalExpression(eval);
                ExpressionNode n = new UnaryOperatorNode(new Minus());
                n.addChild(n1);
                n = eval.checkNodeDefinition(n);
                return n;
            }
        } else
            throw new EvalException("Cannot convert into numerical form " +
                    "a non-logical expression");
    }


    @Override
    void computeValue() {
        children[0].computeValue();
        super.value = operator.compute(children[0].value);
    }

    @Override
    void computeValue(SymbolArray varReference) {
        children[0].computeValue(varReference);
        super.value = operator.compute(children[0].value);
    }

    @Override
    void computeValue(int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(evaluationCode);
            super.value = operator.compute(children[0].value);
        }
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(varReference, evaluationCode);
            super.value = operator.compute(children[0].value);
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        children[0].computeValueLocal(localVariables);
        super.value = operator.compute(children[0].value);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        children[0].computeValueLocal(localVariables, globalVariables);
        super.value = operator.compute(children[0].value);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables, evaluationCode);
            super.value = operator.compute(children[0].value);
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables, globalVariables, evaluationCode);
            super.value = operator.compute(children[0].value);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        UnaryOperatorNode newNode = new UnaryOperatorNode(this.operator);
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n1, n;
        n1 = children[0].convertToJSBML();
        n = new ASTNode(operator.getSBMLType());
        n.addChild(n1);
        return n;
    }


    @Override
    void computeContinuousChangeStatus() {
        children[0].computeContinuousChangeStatus();
        this.canChangeContinuously = children[0].canChangeContinuously;
    }

    @Override
    void computeContinuousChangeStatus(SymbolArray varReference) {
        children[0].computeContinuousChangeStatus(varReference);
        this.canChangeContinuously = children[0].canChangeContinuously;
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        children[0].computeContinuousChangeStatusLocal(localVariables);
        this.canChangeContinuously = children[0].canChangeContinuously;
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        children[0].computeContinuousChangeStatusLocal(localVariables, globalVariables);
        this.canChangeContinuously = children[0].canChangeContinuously;
    }

    @Override
    String toJavaCode() {
        String c1 = (!this.isLogicalExpression() && children[0].isLogicalExpression() ?
                "(" + children[0].toJavaCode() + "? 1.0 : 0.0)" :
                children[0].toJavaCode());
        String s = "(" + operator.toJavaCode();
        s += " " + c1 + ")";
        return s;
    }


    @Override
    String toMatlabCode() {
        String c1 = children[0].toMatlabCode();
        String s = "( " + operator.toMatlabCode() + " " + c1 + " )";
        return s;
    }


    @Override
        //null means that one cannot differentiate the expression as it is not numerical
    ExpressionNode differentiate(String x, Evaluator eval) {
        if (this.isLogicalExpression())
            return null;
        ExpressionNode n1 = children[0].differentiate(x, eval);
        if (n1 != null) {
            if (operator.getType() == OperatorType.MINUS) {
                ExpressionNode n = new UnaryOperatorNode(new Minus());
                n.addChild(n1);
                n = eval.checkNodeDefinition(n);
                return n;
            } else
                return null;
        } else return null;

    }

    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        UnaryOperatorNode node = new UnaryOperatorNode(this.operator);
        for (int i = 0; i < this.numberOfChildren; i++)
            node.addChild(children[i].clone(newEval));
        return newEval.checkNodeDefinition(node);
    }

}
