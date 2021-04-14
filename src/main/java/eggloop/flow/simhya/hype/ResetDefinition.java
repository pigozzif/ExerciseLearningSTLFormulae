/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.hype;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.matheval.Evaluator;

/**
 *
 * @author Luca
 */
public class ResetDefinition {
    String name;
    ArrayList<String> parameters;
    int arity;
    ResetList resets;

    
    public ResetDefinition(String name, ArrayList<String> parameters, ResetList resets) {
        this.name = name;
        this.parameters = parameters;
        this.arity  = parameters.size();
        this.resets = resets;
    }

    /**
     * Instantiate the reset definition to a proper reset list
     * @param eval
     * @param globalSymbols
     * @return
     */
    public ResetList instantiate(Evaluator eval, ArrayList<String> globalSymbols) {
        if (globalSymbols.size() != arity)
            throw new HypeException("Reset definition " + name + " requires " + arity
                    + " parameters, not " + globalSymbols.size());
        ArrayList<Boolean> isParameter = new ArrayList<Boolean>();
        for (String s : globalSymbols) {
            if (eval.isConstant(s) || eval.isExpressionVariable(s))
                isParameter.add(true);
            else if (eval.isVariable(s))
                isParameter.add(false);
            else throw new HypeException("Unknown symbol " + s);
        }
        return this.resets.substitute(parameters, globalSymbols, isParameter);
    }


}
