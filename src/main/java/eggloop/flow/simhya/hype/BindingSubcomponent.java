/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.hype;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author luca
 */
public class BindingSubcomponent {
    SubcomponentTemplate componentTemplate;
    Subcomponent component;
    int paramNumber;
    ArrayList<String> globalBinding;
    ArrayList<Integer> localBinding;
    String influenceGlobalBinding;
    int influenceLocalBinding;

    public BindingSubcomponent(SubcomponentTemplate component, ArrayList<String> containerParams, 
            ArrayList<String> callingParams, String callingInfluence) {
        this.componentTemplate = component;
        this.component = null;
        if (component.boundVars.size() != callingParams.size())
            throw new HypeException("Calling a subcomponent with the wrong number of parameters");
        paramNumber = callingParams.size();
        globalBinding = new ArrayList<String>();
        localBinding = new ArrayList<Integer>();
        influenceGlobalBinding= null;
        influenceLocalBinding = -1;
        this.initBindings(containerParams, callingParams, callingInfluence);
    }
    
    public BindingSubcomponent(Subcomponent component) {
        this.component = component;
        this.componentTemplate = null;
    }
    
    private void initBindings(ArrayList<String> containerParams, 
            ArrayList<String> callingParams, String callingInfluence) {
        for (String p : callingParams) {
            if (containerParams.contains(p)) {
                int i = containerParams.indexOf(p);
                this.localBinding.add(i);
                this.globalBinding.add(null);
            } else {
                this.localBinding.add(null);
                this.globalBinding.add(p);
            }    
        }
        if (containerParams.contains(callingInfluence)) {
            int i = containerParams.indexOf(callingInfluence);
            influenceLocalBinding = i;
        } else {
            influenceGlobalBinding = callingInfluence;
        }
    }
    
    public Subcomponent instantiate(ArrayList<String> globalSymbols, HashMap<String,String> influenceToVars, SymbolTable symbols) {
        if (this.component != null)
            return component;
        ArrayList<String> replacingSymbols = new ArrayList<String>();
        for (int i=0;i<paramNumber;i++) {
            if (this.globalBinding.get(i)!=null)
                replacingSymbols.add(globalBinding.get(i));
            else
                replacingSymbols.add(globalSymbols.get(this.localBinding.get(i)));
        }
        String influence,variable;
        if (this.influenceLocalBinding >= 0)
            influence = globalSymbols.get(influenceLocalBinding);
        else 
            influence = this.influenceGlobalBinding;
        variable = influenceToVars.get(influence);
        return this.componentTemplate.instantiate(influence, variable, replacingSymbols, symbols);
    }
    
}
