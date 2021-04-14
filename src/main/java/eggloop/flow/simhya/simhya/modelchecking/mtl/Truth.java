/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.simhya.modelchecking.mtl;

/**
 *
 * @author luca
 */
public enum Truth {
    TRUE,FALSE,UNDEFINED;
    
    public boolean isTrue() {
        return this == TRUE;
    }
    
    public boolean isFalse() {
        return this == FALSE;
    }
    
    public boolean isUndefined() {
        return this == UNDEFINED;
    }
    
    
    public static Truth and(Truth x1, Truth x2) {
        if (x1 == UNDEFINED || x2 == UNDEFINED)
            return UNDEFINED;
        else if (x1 == TRUE && x2 == TRUE)
            return TRUE;
        else return FALSE;
    }
    
    public static Truth or(Truth x1, Truth x2) {
        if (x1 == UNDEFINED || x2 == UNDEFINED)
            return UNDEFINED;
        else if (x1 == TRUE || x2 == TRUE)
            return TRUE;
        else return FALSE;
    }
    
    public static Truth imply(Truth x1, Truth x2) {
        if (x1 == UNDEFINED || x2 == UNDEFINED)
            return UNDEFINED;
        else if (x1 == FALSE || x2 == TRUE)
            return TRUE;
        else return FALSE;
    }
    
    
    public static Truth not(Truth x) {
        if (x == UNDEFINED)
            return UNDEFINED;
        else if (x == TRUE)
            return FALSE;
        else return TRUE;
    }
}
