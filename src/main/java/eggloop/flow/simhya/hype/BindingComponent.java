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
public class BindingComponent {
    ComponentTemplate componentTemplate;
    Component component;
    int paramNumber;
    ArrayList<String> globalBinding;
    ArrayList<Integer> localBinding;
    
    public BindingComponent(ComponentTemplate component, ArrayList<String> containerParams, 
            ArrayList<String> callingParams) {
        this.component = null;
        this.componentTemplate = component;
        if (component.boundVars.size() != callingParams.size())
            throw new HypeException("Calling a subcomponent with the wrong number of parameters");
        globalBinding = new ArrayList<String>();
        localBinding = new ArrayList<Integer>();
        this.initBindings(containerParams, callingParams);
    }
    
    public BindingComponent(Component component) {
        this.component = component;
        this.componentTemplate = null;
    }
    
    private void initBindings(ArrayList<String> containerParams, 
            ArrayList<String> callingParams) {
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
    }
    
    
    public Component instantiate(ArrayList<String> globalSymbols, HashMap<String,String> influenceToVars, SymbolTable symbols) {
        if (this.component!=null)
            return this.component;
        ArrayList<String> replacingSymbols = new ArrayList<String>();
        for (int i=0;i<paramNumber;i++) {
            if (this.globalBinding.get(i)!=null)
                replacingSymbols.add(globalBinding.get(i));
            else
                replacingSymbols.add(globalSymbols.get(this.localBinding.get(i)));
        }
        return this.componentTemplate.instantiate(replacingSymbols,influenceToVars, symbols);
    }
}
