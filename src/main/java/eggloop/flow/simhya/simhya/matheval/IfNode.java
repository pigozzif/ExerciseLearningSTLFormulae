/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;

import org.sbml.jsbml.ASTNode;

/**
 *
 * @author Luca
 */
public class IfNode extends ExpressionNode {

    public IfNode() {
        super(3);
    }
    
  
  
    public IfNode(int numberOfChildren) {
        super(numberOfChildren);
    }

    @Override
    public boolean isLogicalExpression() {
        return children[1].isLogicalExpression() && children[2].isLogicalExpression();
    }
    
    
    

    @Override
    String getExpressionString(boolean fullNumberRepresentation) {
        String s = "";
        s += "if(" + children[0].getExpressionString(false);
        s += "," + children[1].getExpressionString(false);
        s += "," + children[2].getExpressionString(false) + ")";
        return s;
    }

    @Override
    void computeValue() {
        children[0].computeValue();
        if (children[0].value == 1.0) {
            children[1].computeValue();
            super.value = children[1].value;
        } else {
            children[2].computeValue();
            super.value = children[2].value;
        }
    }

    @Override
    void computeValue(SymbolArray varReference) {
        children[0].computeValue(varReference);
        if (children[0].value == 1.0) {
            children[1].computeValue(varReference);
            super.value = children[1].value;
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
            if (children[0].value == 1.0) {
                children[1].computeValue(evaluationCode);
                super.value = children[1].value;
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
            if (children[0].value == 1.0) {
                children[1].computeValue(varReference,evaluationCode);
                super.value = children[1].value;
            } else {
                children[2].computeValue(varReference,evaluationCode);
                super.value = children[2].value;
            }
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        children[0].computeValueLocal(localVariables);
        if (children[0].value == 1.0) {
            children[1].computeValueLocal(localVariables);
            super.value = children[1].value;
        } else {
            children[2].computeValueLocal(localVariables);
            super.value = children[2].value;
        }
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        children[0].computeValueLocal(localVariables,globalVariables);
        if (children[0].value == 1.0) {
            children[1].computeValueLocal(localVariables,globalVariables);
            super.value = children[1].value;
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
            if (children[0].value == 1.0) {
                children[1].computeValueLocal(localVariables,evaluationCode);
                super.value = children[1].value;
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
            if (children[0].value == 1.0) {
                children[1].computeValueLocal(localVariables,globalVariables,evaluationCode);
                super.value = children[1].value;
            } else {
                children[2].computeValueLocal(localVariables,globalVariables,evaluationCode);
                super.value = children[2].value;
            }
        }
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        IfNode newNode = new IfNode();
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n1,n2,n3,n;
        n1 = children[0].convertToJSBML();
        n2 = children[1].convertToJSBML();
        n3 = children[2].convertToJSBML();
        n = new ASTNode(ASTNode.Type.FUNCTION_PIECEWISE);
        n.addChild(n2);
        n.addChild(n1);
        n.addChild(n3);
        return n;
    }

    
    
    @Override
    void computeContinuousChangeStatus() {
        children[0].computeContinuousChangeStatus();
        if (children[0].canChangeContinuously)
            this.canChangeContinuously = true;
        else {
            if (children[0].value == 1.0) {
                //check the true part of the expression
                children[1].computeContinuousChangeStatus();
                this.canChangeContinuously = children[1].canChangeContinuously;
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
            if (children[0].value == 1.0) {
                //check the true part of the expression
                children[1].computeContinuousChangeStatus(varReference);
                this.canChangeContinuously = children[1].canChangeContinuously;
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
            if (children[0].value == 1.0) {
                //check the true part of the expression
                children[1].computeContinuousChangeStatusLocal(localVariables);
                this.canChangeContinuously = children[1].canChangeContinuously;
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
            if (children[0].value == 1.0) {
                //check the true part of the expression
                children[1].computeContinuousChangeStatusLocal(localVariables,globalVariables);
                this.canChangeContinuously = children[1].canChangeContinuously;
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
        String s = "";
        s += "(" + children[0].toJavaCode();
        s += " ? " + c1;
        s += " : " + c2 + ")";
        return s;
    }
    
   

    @Override
    String toMatlabCode() {
        String e = children[0].toMatlabCode();
        String t = children[1].toMatlabCode();
        String f = children[2].toMatlabCode();
        String s = "( " + e + " * " + t + " + (1 - " + e + " ) * " + f + " )";
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
        IfNode node = new IfNode();
        for (int i =0; i<this.numberOfChildren;i++)
            node.addChild(children[i].clone(newEval));
        return newEval.checkNodeDefinition(node);
    }
    
    
    
}
