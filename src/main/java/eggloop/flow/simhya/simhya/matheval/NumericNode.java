/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.matheval;

import java.util.Locale;
import org.sbml.jsbml.ASTNode;

/**
 *
 * @author Luca
 */
public class NumericNode extends ExpressionNode {
    private Integer intValue;
    private boolean scientificNotation;


    public NumericNode(double value, boolean scientificNotation) {
        super(0);
        super.value = value;
        this.scientificNotation = scientificNotation;
        //then the number is an integer, represent it an such.
        if (value == Math.floor(value)) 
            this.intValue = (int)value;
    }

    public NumericNode(int value) {
        super(0);
        super.value = value;
        this.scientificNotation = false;
        this.intValue = value;
    }


    @Override
    String getExpressionString(boolean fullNumberRepresentation) {
        String s = "", args, argn;
        if (fullNumberRepresentation) {
            args = "%e"; argn = "%f";
        } else { args = "%.4e"; argn = "%.6f"; }
        if (this.intValue != null)
            s += intValue.toString();
        else if (this.scientificNotation)
            s += String.format(Locale.US, args, value);
        else
            s += String.format(Locale.US, argn, value);
        return s;
    }
    
    
    boolean isInteger() {
        return this.intValue != null;
    }
    
    int getIntValue() {
        if (this.intValue != null)
            return intValue;
        else
            return 0;
    }
    

    @Override
    public boolean isNumericConstant() {
        return true;
    }



    @Override
    void computeValue() {
       
    }

    @Override
    void computeValue(SymbolArray varReference) {
        
    }

    @Override
    void computeValue(int evaluationCode) {
        
    }

    @Override
    void computeValue(SymbolArray varReference, int evaluationCode) {

    }

    @Override
    void computeValueLocal(SymbolArray localVariables) {
        
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, int evaluationCode) {
        
    }

    @Override
    void computeValueLocal(SymbolArray localVariables, SymbolArray globalVariables, int evaluationCode) {
       
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        NumericNode newNode = new NumericNode(this.value,true);
        newNode.intValue = this.intValue;
        newNode.scientificNotation = this.scientificNotation;
        this.copyContent(newNode);
        return newNode;
    }

    @Override
    ASTNode convertToJSBML() {
        ASTNode n = new ASTNode(ASTNode.Type.REAL);
        if (this.intValue != null)
            n.setValue(intValue);
        else
            n.setValue(value);
        return n;
    }
    
    @Override
    void computeContinuousChangeStatus() {
        this.canChangeContinuously = false;
    }

    @Override
    void computeContinuousChangeStatus(SymbolArray varReference) {
        this.canChangeContinuously = false;
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables) {
        this.canChangeContinuously = false;
    }

    @Override
    void computeContinuousChangeStatusLocal(SymbolArray localVariables, SymbolArray globalVariables) {
        this.canChangeContinuously = false;
    }

    @Override
    String toJavaCode() {
        return getExpressionString(true);        
    }
    
    

    @Override
    String toMatlabCode() {
        return getExpressionString(true);
    }
    
    @Override
    ExpressionNode differentiate(String x, Evaluator eval) {
        return new NumericNode(0);
    }

    @Override
    public boolean isZero() {
        return value == 0.0;
    }

    @Override
    public boolean isOne() {
        return value == 1.0;
    }
    
    
    
    @Override
    boolean isDifferentiable(String x) {
        return true;
    }
    
    
    @Override
    protected ExpressionNode clone(Evaluator newEval) {
        NumericNode node = new NumericNode(this.value,this.scientificNotation);
        return newEval.checkNodeDefinition(node);
    }
    
}
