/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eggloop.flow.simhya.simhya.simengine.paramexplore;

/**
 *
 * @author Luca
 */
public class ParamSetKey {
    public int[] configuration = null;
    public int points = 0;

   
    @Override
    public boolean equals(Object obj) {
        ParamSetKey key = (ParamSetKey)obj;
        if (key.points != points)
            return false;
        for (int i=0;i<points;i++)
            if (key.configuration[i] != this.configuration[i])
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i=0;i<points;i++) {
            result *= configuration[i];
            result %= Integer.MAX_VALUE;
        }
        return result;
    }

    /**
     * Extracts from a list of ParamValueSet the current configuration and sets its internal value
     * accordingly
     * @param set a list of {@link  ParamValueSet} objects
     */
    public void defineFrom(java.util.ArrayList<ParamValueSet> set) {
        if (points != set.size()) {
            this.points = set.size();
            this.configuration = new int[points];
        }
        for (int i=0;i<points;i++)
            configuration[i] = set.get(i).currentPoint;
            
    }
}
