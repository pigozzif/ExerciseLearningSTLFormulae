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
public class ComponentTemplate {
    String name;
    int instances;
    ArrayList<String> boundVars;
    ArrayList<BindingSubcomponent> subcomponents;
    ArrayList<BindingComponent> components;

    public ComponentTemplate(String name, ArrayList<String> boundVars) {
        this.name = name;
        this.instances = 0;
        this.boundVars = boundVars;
        this.components = new ArrayList<BindingComponent>();
        this.subcomponents = new ArrayList<BindingSubcomponent>();      
    }
    
    
    public void addSubcomponent(BindingSubcomponent comp) {
        this.subcomponents.add(comp);
    }
    
    public void addComponent(BindingComponent comp) {
        this.components.add(comp);
    }
    
    
    public Component instantiate(ArrayList<String> globalSymbols, HashMap<String,String> influenceToVars, SymbolTable symbols) {
        if (this.boundVars.size() != globalSymbols.size())
            throw new HypeException("Incorrect number of parameters passed to component " + name);
        Component comp = new Component(this.name + "." + instances);
        for (BindingSubcomponent c : this.subcomponents)
            comp.addSubcomponent(c.instantiate(globalSymbols, influenceToVars, symbols));
        for (BindingComponent c : this.components)
            comp.addComponent(c.instantiate(globalSymbols, influenceToVars, symbols));
        instances++;
        return comp;
    }
    
}
