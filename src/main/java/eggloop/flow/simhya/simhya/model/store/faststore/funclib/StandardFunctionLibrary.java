/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.model.store.faststore.funclib;

import java.util.ArrayList;
import eggloop.flow.simhya.simhya.model.store.*;
import eggloop.flow.simhya.simhya.model.store.faststore.*;
import java.util.HashMap;
import eggloop.flow.simhya.simhya.matheval.SymbolArray;
/**
 *
 * @author Luca
 */
public class StandardFunctionLibrary implements FunctionLibrary {
    private HashMap<String,FunctionDefinition> functions;


    public StandardFunctionLibrary() {
        this.functions = new HashMap();
        registerFunctions();
    }

    /**
     * 
     */
    private void registerFunctions() {
        FunctionDefinition f;

        //Mass action function
        f = new FunctionDefinition("ma");
        f.addArgument(ArgType.PARAMETER);
        f.addArgument(ArgType.VARIABLE);
        f.addArgument(ArgType.VARIABLE);
        functions.put("ma", f);

        //Linear Function
        f = new FunctionDefinition("lin");
        f.addArgument(ArgType.PARAMETER);
        f.addArgument(ArgType.VARIABLE);
        functions.put("lin", f);

        //Constant Function
        f = new FunctionDefinition("con");
        f.addArgument(ArgType.NUMBER);
        functions.put("con", f);
    }

    public Function getLibraryFunction(String name, ArrayList<String> args, Store store) {
        if (name.equals("ma")) {
            int p = store.getParametersReference().getSymbolId(args.get(0));
            int v1 = store.getVariablesReference().getSymbolId(args.get(1));
            int v2 = store.getVariablesReference().getSymbolId(args.get(2));
            return new MassActionFunction(store,p,v1,v2);

        }
        else if (name.equals("lin")) {
            int p = store.getParametersReference().getSymbolId(args.get(0));
            int v1 = store.getVariablesReference().getSymbolId(args.get(1));
            return new LinearFunction(store,p,v1);
        }
        else if (name.equals("con")) {
            double d = Double.parseDouble(args.get(0));
            return new ConstantFunction(d);
        }
        else
            throw new StoreException("The is no standard library function " + name);
    }

    public Function getLibraryFunction(String name, ArrayList<String> args, Store store, SymbolArray localVars) {
        if (name.equals("ma")) {
            int p = store.getParametersReference().getSymbolId(args.get(0));
            return new MassActionFunction(store,p,args.get(1),args.get(2));

        }
        else if (name.equals("lin")) {
            int p = store.getParametersReference().getSymbolId(args.get(0));
            return new LinearFunction(store,p,args.get(1));
        }
        else if (name.equals("con")) {
            double d = Double.parseDouble(args.get(0));
            return new ConstantFunction(d);
        }
        else
            throw new StoreException("The is no standard library function " + name);
    }

    public boolean isArgumentCorrect(String name, int index, String arg, Store store) {
        return this.functions.get(name).checkArgument(index, arg, store);
    }

    public boolean isArgumentCorrect(String name, int index, String arg, Store store, SymbolArray localVars) {
        return this.functions.get(name).checkArgument(index, arg, store, localVars);
    }

    public boolean isLibraryFunction(String name, int args) {
        return ( this.functions.containsKey(name) && this.functions.get(name).args == args );

    }

    public String getArgumentType(String name, int index) {
        return functions.get(name).getType(index).toString();
    }


    
}
