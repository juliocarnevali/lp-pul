
package auxiliar;

/**
 *
 * @author Julio Carnevali
 */
public class IndexValue{
    public int index;
    public double value;
    
    public IndexValue(){}
    public IndexValue(int index, double value){
        this.index = index;
        this.value = value;
    }
    
    public String toString(){
        return "index: "+this.index+" value: "+this.value;
    }
    
    public boolean equals(Object indVal) { 
        if(indVal == null)
            return false;
        if (this.index == ((IndexValue)indVal).index &&
            Math.abs(this.value - ((IndexValue)indVal).value) < 0.00001){
            return true; 
        } 
        return false; 
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.index;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        return hash;
    }
}
