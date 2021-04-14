/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.hype;
import java.util.ArrayList;
import eggloop.flow.simhya.simhya.matheval.Expression;

/**
 *
 * @author Luca
 */
public class ResetList {
    ArrayList<AtomicReset> resetList;

    public ResetList() {
        resetList = new ArrayList<AtomicReset>();
    }

    public void addReset(AtomicReset r) {
        resetList.add(r);
    }

    public void addReset(String variable, boolean local, Expression reset) {
        resetList.add(new AtomicReset(variable,local,reset));
    }
    
    public void addReset(String variable, boolean local, String reset) {
        resetList.add(new AtomicReset(variable,local,reset));
    }
    
    public void add(ResetList r) {
        for (AtomicReset a : r.resetList)
            this.addReset(a);
    }

    @Override
    public String toString() {
        String s = "";
        for (int i=0;i<resetList.size();i++)
            s += resetList.get(i).toString() + (i==resetList.size()-1 ? "" : "; ");
        return s;
    }
    
    public ArrayList<AtomicReset> getList() {
    	return this.resetList;
    }
    
    public ResetList copyResetList(HypeModel model, int index) {
    	ResetList ret = new ResetList();
    	for(int i=0; i< this.resetList.size();i++) {
    		ret.addReset(this.resetList.get(i).copyAtomicReset(model, index));
    	}
    	return ret;
    }
    
    public ResetList copyResetListWithInteraction(HypeModel model, int i, int j) {
    	ResetList ret = new ResetList();
    	for(int index=0; index< this.resetList.size();index++) {
    		ret.addReset(this.resetList.get(index).copyAtomicReset(model, i, j));
    	}
    	return ret;
    }
    
    public ResetList copyResetListWithRecursiveInteraction(HypeModel model, int i, int j, int k) {
    	ResetList ret = new ResetList();
    	for(int index=0; index< this.resetList.size();index++) {
    		ret.addReset(this.resetList.get(index).copyAtomicReset(model, i, j, k));
    	}
    	return ret;
    }
    

    /**
     * substitutes local variables in a parameterized reset list!
     * @param localVariables
     * @param globalSymbols
     * @param isConstant
     * @return
     */
    public ResetList substitute(ArrayList<String> localVariables, ArrayList<String> globalSymbols, ArrayList<Boolean> isConstant) {
        ResetList newList = new ResetList();
        for (AtomicReset r : this.resetList)
            newList.addReset(r.substitute(localVariables, globalSymbols, isConstant));
        return newList;
    }



}
