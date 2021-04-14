/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eggloop.flow.simhya.hype;
import java.util.HashSet;

/**
 *
 * @author luca
 */
public class SymbolTable {
    HashSet<String> symbols;
    HashSet<String> events;
    HashSet<String> influences;

    public SymbolTable() {
        this.symbols = new HashSet<String>();
        this.events = new HashSet<String>();
        this.influences = new HashSet<String>();
    }
    
    public void addSymbol(String name) {
        symbols.add(name);
    }

    public void addEvent(String name) {
        events.add(name);
    }
    
    public void addInfluence(String name) {
        influences.add(name);
    }
    
    
    public boolean isSymbol(String name) {
        return this.symbols.contains(name);
    }
    
    public boolean isEvent(String name) {
        return this.events.contains(name);
    }
    
    public boolean isInfluence(String name) {
        return this.influences.contains(name);
    }
    
}
