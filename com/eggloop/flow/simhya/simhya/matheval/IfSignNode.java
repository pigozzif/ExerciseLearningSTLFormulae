/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eggloop.flow.simhya.simhya.matheval;

import com.eggloop.flow.simhya.simhya.matheval.operator.Greater;
import com.eggloop.flow.simhya.simhya.matheval.operator.Less;
import org.sbml.jsbml.ASTNode;

/**
 * This is an if node checking the sign of an expression exp, and returning 
 * the value of the first argument when exp >0 , of the second if exp=0,
 * of the third if exp <0 
 * @author Luca
 */
public class IfSignNode extends ExpressionNode {

    public IfSignNode() {
        super(4);
    }
    
  
    @Override
    public boolean isLogicalExpression() {
        return children[1].isLogicalExpression() && children[2].isLogicalExpression()
                && children[3].isLogicalExpression();
    }
    
    
    

    @Override
    String getExpressionString(boolean fullNumberRepresentation) {
        String s = "";
        s += "ifsign(" + children[0].getExpressionString(false);
        s += "," + children[1].getExpressionString(false);
        s += "," + children[2].getExpressionString(false);
        s += "," + children[3].getExpressionString(false) + ")";
        return s;
    }

    @Override
    void computeValue() {
        children[0].computeValue();
        if (children[0].value > 0.0) {
            children[1].computeValue();
            super.value = children[1].value;
        } else if (children[0].value < 0.0) {
            children[3].computeValue();
            super.value = children[3].value;
        } else {
            children[2].computeValue();
            super.value = children[2].value;
        }
        
    }

    @Override
    void computeValue(SymbolArray varReference) {
        children[0].computeValue(varReference);
        if (children[0].value > 0.0) {
            children[1].computeValue(varReference);
            super.value = children[1].value;
        } else if (children[0].value < 0.0) {
            children[3].computeValue(varReference);
            super.value = children[3].value;
        } else {
            children[2].computeValue(varReference);
            super.value = children[2].value;
        }
    }

    @Override
    void computeValue(int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(evaluationCode);
            if (children[0].value > 0.0) {
                children[1].computeValue(evaluationCode);
                super.value = children[1].value;
            } else if (children[0].value < 0.0) {
                children[3].computeValue(evaluationCode);
                super.value = children[3].value;
            } else {
                children[2].computeValue(evaluationCode);
                super.value = children[2].value;
            }
        }
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValue(varReference,evaluationCode);
            if (children[0].value > 0.0) {
                children[1].computeValue(varReference,evaluationCode);
                super.value = children[1].value;
            } else if (children[0].value < 0.0) {
                children[3].computeValue(varReference,evaluationCode);
                super.value = children[3].value;
            } else {
                children[2].computeValue(varReference,evaluationCode);
                super.value = children[2].value;
            }
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        children[0].computeValueLocal(localVariables);
        if (children[0].value > 0.0) {
            children[1].computeValueLocal(localVariables);
            super.value = children[1].value;
        } else if (children[0].value < 0.0) {
            children[3].computeValueLocal(localVariables);
            super.value = children[3].value;
        } else {
            children[2].computeValueLocal(localVariables);
            super.value = children[2].value;
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        children[0].computeValueLocal(localVariables,globalVariables);
        if (children[0].value > 0.0) {
            children[1].computeValueLocal(localVariables,globalVariables);
            super.value = children[1].value;
        } else if (children[0].value < 0.0) {
            children[3].computeValueLocal(localVariables,globalVariables);
            super.value = children[3].value;
        } else {
            children[2].computeValueLocal(localVariables,globalVariables);
            super.value = children[2].value;
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables,evaluationCode);
            if (children[0].value > 0.0) {
                children[1].computeValueLocal(localVariables,evaluationCode);
                super.value = children[1].value;
            } else if (children[0].value < 0.0) {
                children[3].computeValueLocal(localVariables,evaluationCode);
                super.value = children[3].value;
            } else {
                children[2].computeValueLocal(localVariables,evaluationCode);
                super.value = children[2].value;
            }
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
        if (random || super.evaluationCode != evaluationCode) {
            super.evaluationCode = evaluationCode;
            children[0].computeValueLocal(localVariables,globalVariables,evaluationCode);
            if (children[0].value > 0.0) {
                children[1].computeValueLocal(localVariables,globalVariables,evaluationCode);
                super.value = children[1].value;
            } else if (children[0].value < 0.0) {
                children[3].computeValueLocal(localVariables,globalVariables,evaluationCode);
                super.value = children[3].value;
            } else {
                children[2].computeValueLocal(localVariables,globalVariables,evaluationCode);
                super.value = children[2].value;
            }
        }
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        IfSignNode newNode = new IfSignNode();
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        BinaryOperatorNode ng = new BinaryOperatorNode(new Greater());
        ng.addChild(children[0]);
        ng.addChild(new NumericNode(0.0,false));
        BinaryOperatorNode nl = new BinaryOperatorNode(new Less());
        nl.addChild(children[0]);
        nl.addChild(new NumericNode(0.0,false));
        
        ASTNode n0g,n0l,n1,n2,n3,n;
        n = new ASTNode(ASTNode.Type.FUNCTION_PIECEWISE);
        n0g = ng.convertToJSBML();
        n0l = nl.convertToJSBML();
        n1 = children[1].convertToJSBML();
        n2 = children[2].convertToJSBML();
        n3 = children[3].convertToJSBML();
        //piecewise def is value if condition, value if condition, ... , value (else case)
        n.addChild(n1);
        n.addChild(n0g);
        n.addChild(n3);
        n.addChild(n0l);
        n.addChild(n2);
        return n;
    }

    
    
    @Override
    void computeContinuousChangeStatus() {
        children[0].computeContinuousChangeStatus();
        if (children[0].canChangeContinuously)
            this.canChangeContinuously = true;
        else {
            if (children[0].value > 0.0) {
                //check the true part of the expression
                children[1].computeContinuousChangeStatus();
                this.canChangeContinuously = children[1].canChangeContinuously;
            } else if (children[0].value < 0.0) {
                //check the true part of the expression
                children[3].computeContinuousChangeStatus();
                this.canChangeContinuously = children[3].canChangeContinuously;
            } else {
                //check the true part of the expression
                children[2].computeContinuousChangeStatus();
                this.canChangeContinuously = children[2].canChangeContinuously;
            }
        }
    }
    
    @Override
    void computeContinuousChangeStatus(SymbolArray varReference) {
        children[0].computeContinuousChangeStatus(varReference);
        if (children[0].canChangeContinuously)
            this.canChangeContinuously = true;
        else {
            if (children[0].value > 0.0) {
                //check the true part of the expression
                children[1].computeContinuousChangeStatus(varReference);
                this.canChangeContinuously = children[1].canChangeContinuously;
            } else if (children[0].value < 0.0) {
                //check the true part of the expression
                children[3].computeContinuousChangeStatus(varReference);
                this.canChangeContinuously = children[3].canChangeContinuously;
            } else {
                //check the true part of the expression
                children[2].computeContinuousChangeStatus(varReference);
                this.canChangeContinuously = children[2].canChangeContinuously;
            }
        }
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        children[0].computeContinuousChangeStatusLocal(localVariables);
        if (children[0].canChangeContinuously)
            this.canChangeContinuously = true;
        else {
            if (children[0].value > 0.0) {
                //check the true part of the expression
                children[1].computeContinuousChangeStatusLocal(localVariables);
                this.canChangeContinuously = children[1].canChangeContinuously;
            } else if (children[0].value < 0.0) {
                //check the true part of the expression
                children[3].computeContinuousChangeStatusLocal(localVariables);
                this.canChangeContinuously = children[3].canChangeContinuously;
            } else {
                //check the true part of the expression
                children[2].computeContinuousChangeStatusLocal(localVariables);
                this.canChangeContinuously = children[2].canChangeContinuously;
            }
        }
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        children[0].computeContinuousChangeStatusLocal(localVariables,globalVariables);
        if (children[0].canChangeContinuously)
            this.canChangeContinuously = true;
        else {
            if (children[0].value > 0.0) {
                //check the true part of the expression
                children[1].computeContinuousChangeStatusLocal(localVariables,globalVariables);
                this.canChangeContinuously = children[1].canChangeContinuously;
            } else if (children[0].value < 0.0) {
                //check the true part of the expression
                children[3].computeContinuousChangeStatusLocal(localVariables,globalVariables);
                this.canChangeContinuously = children[3].canChangeContinuously;
            } else {
                //check the true part of the expression
                children[2].computeContinuousChangeStatusLocal(localVariables,globalVariables);
                this.canChangeContinuously = children[2].canChangeContinuously;
            }
        }
    }

    @Override
    String toJavaCode() {
        String c1 = (!this.isLogicalExpression() && children[1].isLogicalExpression() ?
                    "(" + children[1].toJavaCode() + "? 1.0 : 0.0)"  : 
                    children[1].toJavaCode() );
        String c2 = (!this.isLogicalExpression() && children[2].isLogicalExpression() ?
                    "(" + children[2].toJavaCode() + "? 1.0 : 0.0)"  : 
                    children[2].toJavaCode() );
        String c3 = (!this.isLogicalExpression() && children[3].isLogicalExpression() ?
                    "(" + children[3].toJavaCode() + "? 1.0 : 0.0)"  : 
                    children[3].toJavaCode() );
        String s = "";
        s += "(" + children[0].toJavaCode() + " > 0.0";
        s += " ? " + c1;
        s += " : (" + children[0].toJavaCode() + " < 0.0 ? " + c3 + " : " + c2 + "))";
        return s;
    }
    

    @Override
    String toMatlabCode() {
        String e = children[0].toMatlabCode();
        String p = children[1].toMatlabCode();
        String z = children[2].toMatlabCode();
        String n = children[2].toMatlabCode();
        String s = "( ";
        s += "( " + e + " > 0 ) * " + p;
        s += "+ ( " + e + " == 0 ) * " + z;
        s += "+ ( " + e + " < 0 ) * " + n;   
        s += " )";
        return s;
    }
    

    @Override
    ExpressionNode differentiate(String x, Evaluator eval) {
        return null;
    }

    @Override
    boolean isDifferentiable(String x) {
        return false;
    }
    
    
    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        IfSignNode node = new IfSignNode();
        for (int i =0; i<this.numberOfChildren;i++)
            node.addChild(children[i].clone(newEval));
        return newEval.checkNodeDefinition(node);
    }
    
    
}
