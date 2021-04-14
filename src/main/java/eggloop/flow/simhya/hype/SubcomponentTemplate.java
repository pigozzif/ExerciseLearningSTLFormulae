/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.hype;
import eggloop.flow.simhya.simhya.matheval.Expression;
import java.util.ArrayList;


/**
 *
 * @author luca
 */
public class SubcomponentTemplate {
    String name;
    int instances;
    ArrayList<String> boundVars;
    ArrayList<FlowTemplate> flows;
    int flowNumber;
    ArrayList<ArrayList<String>> events;
    int initialFlow;

    public SubcomponentTemplate(String name, ArrayList<String> boundVars) {
        this.name = name;
        this.boundVars = boundVars;
        this.instances = 0;
        this.flows = new ArrayList<FlowTemplate>();
        this.flowNumber = 0;
        this.events = new ArrayList<ArrayList<String>>();
        this.initialFlow = -1;
    }
    
    
    public void addFlow(Expression rate, Expression function, boolean increment, 
            boolean initial, ArrayList<String> events) {
        FlowTemplate f = new FlowTemplate(rate, function, increment);
        int code = this.flowNumber;
        this.flows.add(f);
        if (initial) {
            if (this.initialFlow >= 0)
                throw new HypeException("Initial flow for subcomponent " + name + " already defined");
            this.initialFlow = code;
        }
        this.events.add(events);
        this.flowNumber++;
    }
    
    
    public Subcomponent instantiate(String influence, String variable, ArrayList<String> globalSymbols, SymbolTable symbols) {
        if (this.boundVars.size() != globalSymbols.size())
            throw new HypeException("Incorrect number of parameters passed to subcomponent " + name);
        //correctly define the subsets of symbols to replace
        ArrayList<String> toReplace = new ArrayList<String>();
        ArrayList<String> replacing = new ArrayList<String>();
        //checking global symbols, if one of them is not defined, return an error!
        for (String s : globalSymbols) {
            if (!symbols.isSymbol(s) && !symbols.isEvent(s))
                throw new HypeException("Symbol " + s + " passed to subcomponent " + name + " during instantiation is unknown");
        }
        for (int i=0;i<this.boundVars.size();i++) {
            if (symbols.isSymbol(globalSymbols.get(i))) {
                toReplace.add(boundVars.get(i));
                replacing.add(globalSymbols.get(i));
            }
        }
        //generates new subcomponent, replaces events, and instantiate flows!
        Subcomponent sub = new Subcomponent(name+"."+instances,influence,variable);
        for (int i=0;i<this.flowNumber;i++) {
            //binding events!
            ArrayList<String> ev = new ArrayList<String>();
            for (String e : this.events.get(i)) {
                if (boundVars.contains(e)) 
                    ev.add(globalSymbols.get(boundVars.indexOf(e)));
                else
                    ev.add(e);
            }
            sub.addFlow(this.flows.get(i).instantiate(toReplace, replacing), 
                    initialFlow == i, ev);
        }
        instances++;
        return sub;
    }  
}
