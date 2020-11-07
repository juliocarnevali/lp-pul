
package networks;

import java.util.HashSet;

/**
 *
 * @author Julio Carnevali
 */
public class Edge {
    public int source;
    public int target;
    public double weight;
    
    public Edge(int source, int target){
        this.source = source;
        this.target = target;
        this.weight = 1.0;
    }
    public Edge(int source, int target, double weight){
        this.source = source;
        this.target = target;
        this.weight = weight;
    }
    
    public Edge clone(){
        return new Edge(source,target,weight);
    }
    
    
    @Override
    public boolean equals(Object edge) { 
        if(edge == null)
            return false;
        if (this.source == ((Edge)edge).source &&
            this.target == ((Edge)edge).target &&
            Math.abs(this.weight - ((Edge)edge).weight) < 0.00001){
            return true; 
        } 
        return false; 
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.source;
        hash = 47 * hash + this.target;
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.weight) ^ (Double.doubleToLongBits(this.weight) >>> 32));
        return hash;
    }
    
    
}
