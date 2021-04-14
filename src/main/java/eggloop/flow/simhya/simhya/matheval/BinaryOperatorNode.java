/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;
import org.sbml.jsbml.ASTNode;
import eggloop.flow.simhya.simhya.matheval.operator.*;
import eggloop.flow.simhya.simhya.matheval.function.Log;
import eggloop.flow.simhya.simhya.matheval.function.Max;
import eggloop.flow.simhya.simhya.matheval.function.Min;
/**
 *
 * @author Luca
 */
public class BinaryOperatorNode extends OperatorNode {

    public BinaryOperatorNode(OperatorDefinition op) {
        super(op, 2);
    }


    @Override
    public boolean canBeConvertedToNumericalExpression() {
        if (operator instanceof And || operator instanceof Or)
            return children[0].canBeConvertedToNumericalExpression()
                && children[1].canBeConvertedToNumericalExpression();
        else if (operator instanceof Greater ||
                 operator instanceof GreaterOrEqual ||
                 operator instanceof Less ||
                 operator instanceof LessOrEqual ||
                 operator instanceof Equal ||
                 operator instanceof NotEqual)
            return true;
        else
            return false;
    }


    @Override
    public ExpressionNode convertToNumericalExpression(Evaluator eval) {
         if (operator instanceof And || operator instanceof Or) {
             ExpressionNode n1 = children[0].convertToNumericalExpression(eval);
             ExpressionNode n2 = children[1].convertToNumericalExpression(eval);
             ExpressionNode n;
             if (operator instanceof Or)
                n = new BinaryFunctionNode(new Max());
             else
                n = new BinaryFunctionNode(new Min());
             n.addChild(n1);
             n.addChild(n2);
             n = eval.checkNodeDefinition(n);
             return n;
         }
         else if (operator instanceof Greater ||
                  operator instanceof GreaterOrEqual ) {
              ExpressionNode n = new BinaryOperatorNode(new Minus());
              //(a>=b) becomes (a-b)
              n.addChild(children[0]);
              n.addChild(children[1]);
              n = eval.checkNodeDefinition(n);
              return n;
         }
         else if (operator instanceof Less ||
                  operator instanceof LessOrEqual) {
             ExpressionNode n = new BinaryOperatorNode(new Minus());
             //(a<=b) becomes (b-a)
              n.addChild(children[1]);
              n.addChild(children[0]);
              n = eval.checkNodeDefinition(n);
              return n;
         }
         else if (operator instanceof Equal ||
                 operator instanceof NotEqual) {
             //we replace equality/inequality with if(a==b,1,-1)/if(a!=b,1,-1)
             ExpressionNode n = new IfNode();
             n.addChild(this);
             n.addChild(new NumericNode(1));
             n.addChild(new NumericNode(-1));
             n = eval.checkNodeDefinition(n);
             return n;
//             throw new EvalException("Cannot convert into numerical form " +
//                "an equality or an inequality");
         }
         else
             throw new EvalException("Cannot convert into numerical form " +
                "a non-logical expression");             
    }

   @Override
    void computeValue() {
        children[0].computeValue();
        children[1].computeValue();
        super.value = operator.compute(children[0].value, children[1].value);
    }

    @Override
    void computeValue(SymbolArray varReference) {
        children[0].computeValue(varReference);
        children[1].computeValue(varReference);
        super.value = operator.compute(children[0].value, children[1].value);
    }

    @Override
    void computeValue(int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(evaluationCode);
            children[1].computeValue(evaluationCode);
            super.value = operator.compute(children[0].value, children[1].value);
        }
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(varReference,evaluationCode);
            children[1].computeValue(varReference,evaluationCode);
            super.value = operator.compute(children[0].value, children[1].value);
        }
    }
    
    @Override
    void computeValueLocal(SymbolArray localVariables) {
        children[0].computeValueLocal(localVariables);
        children[1].computeValueLocal(localVariables);
        super.value = operator.compute(children[0].value, children[1].value);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        children[0].computeValueLocal(localVariables,globalVariables);
        children[1].computeValueLocal(localVariables,globalVariables);
        super.value = operator.compute(children[0].value, children[1].value);
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables,evaluationCode);
            children[1].computeValueLocal(localVariables,evaluationCode);
            super.value = operator.compute(children[0].value, children[1].value);
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables,globalVariables,evaluationCode);
            children[1].computeValueLocal(localVariables,globalVariables,evaluationCode);
            super.value = operator.compute(children[0].value, children[1].value);
        }
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        BinaryOperatorNode newNode = new BinaryOperatorNode(this.operator);
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n1,n2,n;
        n1 = children[0].convertToJSBML();
        n2 = children[1].convertToJSBML();
        n = new ASTNode(operator.getSBMLType());
        n.addChild(n1);
        n.addChild(n2);
        return n;
    }
    
    
    private void setContinuityStatus() {
        ExpressionNode left = children[0];
        ExpressionNode right = children[1];
        
//        System.out.println("Operator " + operator.getStringRepresentation() + "\n"
//                + "left node is " + left.getExpressionString() + " and its cont status is " + left.canChangeContinuously + " and its status is " + left.value + "\n"
//                + "right node is " + right.getExpressionString() + " and its cont status is " + right.canChangeContinuously + " and its status is " + right.value);
        
        if (operator.isLogical()) {
            if (operator.getStringRepresentation().equals("&&")) {
                //and case
                if (!left.canChangeContinuously && !right.canChangeContinuously)
                    this.canChangeContinuously = false;
                else if (!left.canChangeContinuously && left.value == 0.0) 
                    //in this case, the term that cannot be modified by continuous variables
                    //is false, so the truth value of the expression will be false.
                    this.canChangeContinuously = false;
                else if (!right.canChangeContinuously && right.value == 0.0)
                    this.canChangeContinuously = false;
                else
                    this.canChangeContinuously = true; 
            } else {
                //or case
                if (!left.canChangeContinuously && !right.canChangeContinuously)
                    this.canChangeContinuously = false;
                else if (!left.canChangeContinuously && left.value == 1.0) 
                    //in this case, the term that cannot be modified by continuous variables
                    //is true, so the truth value of the expression will be true.
                    this.canChangeContinuously = false;
                else if (!right.canChangeContinuously && right.value == 1.0)
                    this.canChangeContinuously = false;
                else
                    this.canChangeContinuously = true;
            }
        } else 
            this.canChangeContinuously = left.canChangeContinuously || right.canChangeContinuously;
        
   //     System.out.println("Continuity status of " + this.getExpressionString() +  " is " + this.canChangeContinuously);
        
    }
    
    
    //if the operator is logical
    //and: if left and right are false: false
    //if one false and one true, false if the false one has value false.
    //if both true, then true.
    //or: if both false, then false. If one true, then false if the false one has value true. otherwise true
    //true if both true.
    //other operators: true if one child is true.

    @Override
    void computeContinuousChangeStatus() {
        children[0].computeContinuousChangeStatus();
        children[1].computeContinuousChangeStatus();
        setContinuityStatus();
    }
    
    @Override
    void computeContinuousChangeStatus(SymbolArray varReference) {
        children[0].computeContinuousChangeStatus(varReference);
        children[1].computeContinuousChangeStatus(varReference);
        setContinuityStatus();
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        children[0].computeContinuousChangeStatusLocal(localVariables);
        children[1].computeContinuousChangeStatusLocal(localVariables);
        setContinuityStatus();
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        children[0].computeContinuousChangeStatusLocal(localVariables,globalVariables);
        children[1].computeContinuousChangeStatusLocal(localVariables,globalVariables);
        setContinuityStatus();
    }

    String toJavaCode() {
        String s = "";
        String c1 = (!this.isLogicalExpression() && children[0].isLogicalExpression() ?
                    "(" + children[0].toJavaCode() + "? 1.0 : 0.0)"  : 
                    children[0].toJavaCode() );
        String c2 = (!this.isLogicalExpression() && children[1].isLogicalExpression() ?
                    "(" + children[1].toJavaCode() + "? 1.0 : 0.0)"  : 
                    children[1].toJavaCode() );
        if (operator.functionInJava()) {
            s = operator.toJavaCode() + "(";
            s += c1 + " ";
            s += ","; 
            s += c2 + ")";
        } else {
            s = "(";
            s += c1 + " ";
            s += operator.toJavaCode() + " "; 
            s += c2 + ")";
        }
        return s;
    }
    
    
    @Override
    String toMatlabCode() {
        String c1 = children[0].toMatlabCode();
        String c2 = children[1].toMatlabCode();
        String s = "";
        if (operator.getType() == OperatorType.MODULUS) {
            s = operator.toMatlabCode() + "( " + c1 + " , " + c2 + " )";
        } else {
            s = "( " + c1 + " " +  operator.toMatlabCode() + " " + c2 + " )";
        }
        return s; 
    }
    
    
    

    @Override
    ExpressionNode differentiate(String x, Evaluator eval) {
        
        //checks that the node is differentiable
        if (this.isLogicalExpression())
            return null;
        ExpressionNode derivLeft = children[0].differentiate(x,eval);
        ExpressionNode derivRight = children[1].differentiate(x,eval);
        //check that children are differentiable
        if (derivLeft == null || derivRight == null)
            return null;
        ExpressionNode m,m1,m2,m3,m4,m5;
        switch(operator.getType()) {
            case PLUS:
                if (derivLeft.isZero()) {
                    if (derivRight.isZero())
                        return new NumericNode(0);
                    return derivRight;
                } else if (derivRight.isZero()) {
                    return derivLeft;
                } else if (derivLeft.isNumericConstant() && derivRight.isNumericConstant()) {
                    return eval.checkNodeDefinition(new NumericNode(derivLeft.value + derivRight.value,false));
                } else {
                    if (derivRight instanceof UnaryOperatorNode && 
                            ((UnaryOperatorNode)derivRight).operator.getType() == OperatorType.MINUS ) {
                        //we push the minus into the binary operator
                        m = new BinaryOperatorNode(new Minus());
                        m.addChild(derivLeft);
                        m.addChild(derivRight.children[0]);
                    } else {
                        m = new BinaryOperatorNode(new Plus());
                        m.addChild(derivLeft);
                        m.addChild(derivRight);
                    }
                    m = eval.checkNodeDefinition(m);
                    return m;
                }
            case MINUS:
                if (derivLeft.isZero()) {
                    if (derivRight.isZero())
                        return new NumericNode(0);
                    m = new UnaryOperatorNode(new Minus());
                    m.addChild(derivRight);
                    m = eval.checkNodeDefinition(m);
                    return m;
                } else if (derivRight.isZero()) {
                    return derivLeft;
                } else if (derivLeft.isNumericConstant() && derivRight.isNumericConstant()) {
                    return  eval.checkNodeDefinition(new NumericNode(derivLeft.value - derivRight.value,false));
                } else {
                    if (derivRight instanceof UnaryOperatorNode && 
                            ((UnaryOperatorNode)derivRight).operator.getType() == OperatorType.MINUS ) {
                        //we push the minus into the binary operator, which becomes a plus
                        m = new BinaryOperatorNode(new Plus());
                        m.addChild(derivLeft);
                        m.addChild(derivRight.children[0]);
                    } else {
                        m = new BinaryOperatorNode(new Minus());
                        m.addChild(derivLeft);
                        m.addChild(derivRight);
                    }
                    m = eval.checkNodeDefinition(m);
                    return m;
                }
            case MULTIPLY:
                if (derivLeft.isZero()) {
                    if (derivRight.isZero())
                        return new NumericNode(0);
                    //(a*g(x))' = a*g'(x)
                    if (derivRight.isOne())
                        m = children[0];
                    else {
                        m = new BinaryOperatorNode(new Multiply());
                        m.addChild(children[0]);
                        m.addChild(derivRight);
                        m = eval.checkNodeDefinition(m);
                    }
                    return m;
                } else if (derivRight.isZero()) {
                    //(f(x)*a)' = a*f'(x)
                    if (derivLeft.isOne())
                        m = children[1];
                    else {
                        m = new BinaryOperatorNode(new Multiply());
                        m.addChild(derivLeft);
                        m.addChild(children[1]);
                        m = eval.checkNodeDefinition(m);
                    }
                    return m;
                } else {
                    //(f(x)*g(x))' = f'(x)*g(x) + f(x)*g'(x)
                    //m1 is f'(x)*g(x)
                    if (derivLeft.isOne())
                        m1 = children[1];
                    else {
                        m1 = new BinaryOperatorNode(new Multiply());
                        m1.addChild(derivLeft);
                        m1.addChild(children[1]);
                        m1= eval.checkNodeDefinition(m1);
                    }
                    //m2 is f(x)*g'(x)
                    if (derivRight.isOne())
                        m2 = children[0];
                    else {
                        m2 = new BinaryOperatorNode(new Multiply());
                        m2.addChild(children[0]);
                        m2.addChild(derivRight);
                        m2 = eval.checkNodeDefinition(m2);
                    }
                    //m is m1+m2
                    m = new BinaryOperatorNode(new Plus());
                    m.addChild(m1);
                    m.addChild(m2);
                    m = eval.checkNodeDefinition(m);
                    return m;
                }
            case DIVIDE:
                if (derivRight.isZero()) {
                    if (derivLeft.isZero())
                        return new NumericNode(0);
                    //(f(x)/a)' = f'(x)/a
                    if (children[1].isNumericConstant()) {
                        if ( children[1].isOne()) {
                            m = derivLeft;
                        } else if (derivLeft.isNumericConstant()) {
                            m = new NumericNode(derivLeft.value / children[1].value,false);
                            m = eval.checkNodeDefinition(m);
                        } else {
                           m = new BinaryOperatorNode(new Divide());
                           m.addChild(derivLeft);
                           m.addChild(children[1]); 
                           m = eval.checkNodeDefinition(m);
                        }
                    }
                    else {
                        m = new BinaryOperatorNode(new Divide());
                        m.addChild(derivLeft);
                        m.addChild(children[1]);
                        m = eval.checkNodeDefinition(m);
                    }
                    return m;
                } else if (derivLeft.isZero()) {
                    // (a/g(x))' = - a*g'(x)/g(x)^2 
                    
                    //m2 is g(x)^2
                    m2 = new BinaryOperatorNode(new Power());
                    m2.addChild(children[1]);
                    m2.addChild(new NumericNode(2));
                    m2 = eval.checkNodeDefinition(m2);
                    
                    //m1 is a*g'(x)
                    if (derivRight.isOne()) {
                        m1 = children[0];
                    }
                    else if (children[0].isNumericConstant() && derivRight.isNumericConstant()) {
                        m1 = new NumericNode(children[0].value*derivRight.value, false);
                        m1 = eval.checkNodeDefinition(m1);
                    } else {
                        m1 = new BinaryOperatorNode(new Multiply());
                        m1.addChild(children[0]);
                        m1.addChild(derivRight);
                        m1 = eval.checkNodeDefinition(m1);
                    }
                    //m3 is m1/m2
                    m3 = new BinaryOperatorNode(new Divide());
                    m3.addChild(m1);
                    m3.addChild(m2);
                    m3 = eval.checkNodeDefinition(m3);
                    //m is -m3
                    m = new UnaryOperatorNode(new Minus());
                    m.addChild(m3);
                    m = eval.checkNodeDefinition(m);
                    return m;
                } else {
                    // (f(x)/g(x))' = (f'(x)*g(x) - f(x)*g'(x))/g(x)^2
                    //m4 is g(x)^2
                    m4 = new BinaryOperatorNode(new Power());
                    m4.addChild(children[1]);
                    m4.addChild(new NumericNode(2));
                    m4 = eval.checkNodeDefinition(m4);
                    //m1 is f'(x)*g(x)
                    if (derivLeft.isOne()) {
                        m1 = children[1];
                    } else {
                        m1 = new BinaryOperatorNode(new Multiply());
                        m1.addChild(derivLeft);
                        m1.addChild(children[1]);
                        m1 = eval.checkNodeDefinition(m1);
                    }
                    // m2 is f(x)*g'(x)
                    if (derivRight.isOne()) {
                        m2 = children[0];
                    } else {
                        m2 = new BinaryOperatorNode(new Multiply());
                        m2.addChild(children[0]);
                        m2.addChild(derivRight);
                        m2 = eval.checkNodeDefinition(m2);
                    }
                    // m3 is m1-m2
                    m3 = new BinaryOperatorNode(new Minus());
                    m3.addChild(m1);
                    m3.addChild(m2);
                    m3 = eval.checkNodeDefinition(m3);
                    //m is m3/m4
                    m = new BinaryOperatorNode(new Divide());
                    m.addChild(m3);
                    m.addChild(m4);
                    m = eval.checkNodeDefinition(m);
                    return m;
                }
            case POWER: 
                if (derivRight.isZero()) {
                    if (derivLeft.isZero())
                        return new NumericNode(0);
                    
                    // (f(x)^a)' = a*f'(x)*f(x)^(a-1)
                    if (children[1].isNumericConstant()) {
                        m3 = new NumericNode(children[1].value-1,false);
                        m3 = eval.checkNodeDefinition(m3);
                    } else {
                        m3 = new BinaryOperatorNode(new Minus());
                        m3.addChild(children[1]);
                        m3.addChild(new NumericNode(1));
                        m3 = eval.checkNodeDefinition(m3);
                    }
                    
                    //m2 is f(x)^(a-1)
                    if (m3.isZero()) {
                        m2 = new NumericNode(1);
                    } else if (m3.isOne()) {
                        m2 = children[0];
                    } else {
                        m2 = new BinaryOperatorNode(new Power());
                        m2.addChild(children[0]);
                        m2.addChild(m3);
                        m2 = eval.checkNodeDefinition(m2);
                    }
                    
                    //m1 is a*f'(x)
                    if (derivLeft.isNumericConstant() && children[1].isNumericConstant()) {
                        //simplify expression in case of two numeric constants
                        m1 = new NumericNode(children[1].value*derivLeft.value,false);
                        m1 = eval.checkNodeDefinition(m1);
                    } else {
                        m1 = new BinaryOperatorNode(new Multiply());
                        m1.addChild(derivLeft);
                        m1.addChild(children[1]);
                        m1 = eval.checkNodeDefinition(m1);
                    }
                    //m is m1 * m2
                    m = new BinaryOperatorNode(new Multiply());
                    m.addChild(m1);
                    m.addChild(m2);
                    m = eval.checkNodeDefinition(m);
                    return m;
                } else if (derivLeft.isZero()) {
                    
                    // (a^g(x))' = ln(a)*g'(x)*a^g(x)
                    m = new BinaryOperatorNode(new Multiply());
                    
                    //m2 is ln(a)
                    if (children[0].isNumericConstant()) {
                        m2 = new NumericNode(Math.log(children[0].value),false);
                        m2 = eval.checkNodeDefinition(m2);
                    } else {
                        m2 = new UnaryFunctionNode(new Log());
                        m2.addChild(children[0]);
                        m2 = eval.checkNodeDefinition(m2);
                    }
                    
                    //m1 is ln(a)*g'(x)
                    if (derivRight.isOne()) 
                        m1 = m2;
                    else if (derivRight.isNumericConstant() && m2.isNumericConstant()) {
                        m1 = new NumericNode(derivRight.value*m2.value,false);
                        m1 = eval.checkNodeDefinition(m1);
                    }
                    else {
                        m1 = new BinaryOperatorNode(new Multiply());
                        m1.addChild(m2);
                        m1.addChild(derivRight);
                        m1 = eval.checkNodeDefinition(m1);
                    }
                    
                    //m  is m1 * this
                    m.addChild(m1);
                    m.addChild(this);
                    m = eval.checkNodeDefinition(m);
                    return m;
                } else {
                    //(f(x)^g(x))' = (f'(x)*g(x)/f(x) + g'(x)*ln(f(x)))*f(x)^g(x)
                    
                    //m4 is g'(x)*ln(f(x)))
                    m5 = new UnaryFunctionNode(new Log());
                    m5.addChild(children[0]);
                    m5 = eval.checkNodeDefinition(m5);
                    if (derivRight.isOne()) 
                        m4 = m5;
                    else {
                        m4 = new BinaryOperatorNode(new Multiply());
                        m4.addChild(derivRight);
                        m4.addChild(m5);
                        m4 = eval.checkNodeDefinition(m4);
                    }
                    
                    //m2 is f'(x)*g(x)/f(x)
                    m3 = new BinaryOperatorNode(new Divide());
                    m3.addChild(children[1]);
                    m3.addChild(children[0]);
                    m3 = eval.checkNodeDefinition(m3);
                    if (derivLeft.isOne()) 
                        m2 = m3;
                    else {
                        m2 = new BinaryOperatorNode(new Multiply());
                        m2.addChild(derivLeft);
                        m2.addChild(m3);
                        m2 = eval.checkNodeDefinition(m2);
                    }
                    
                    //m1 is m2+m3
                    m1 = new BinaryOperatorNode(new Plus());
                    m1.addChild(m2);
                    m1.addChild(m3);
                    m1 = eval.checkNodeDefinition(m1);
                    
                    //m is m1 * this
                     m = new BinaryOperatorNode(new Multiply());
                    m.addChild(m1);
                    m.addChild(this);
                    m = eval.checkNodeDefinition(m);
                    return m;
                }
            case MODULUS:
                //derivative undefined
                return null;
            default:
                return null; 
        }
        
        
        
    }
    
    
    
    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        BinaryOperatorNode node = new BinaryOperatorNode(this.operator);
        for (int i =0; i<this.numberOfChildren;i++)
            node.addChild(children[i].clone(newEval));
        return newEval.checkNodeDefinition(node);
    }

    
}
