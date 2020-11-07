
package PUL.PEPUC;
import java.util.ArrayList;
import java.util.HashMap;
/**
 *
 * @author julio
 */
public class LLGC_PEPUC {
    public ArrayList<Integer> order;
    public ArrayList<Integer> positives;
    
    public double[] F;
    public double[] f;
    public ArrayList<HashMap<Integer,Double>> weights;// array index is the source vertex, and the hashmap key is the target vertex. finally the hashmap value is the weight
    public double alpha;
    
          
    public LLGC_PEPUC(double alpha, ArrayList<HashMap<Integer,Double>> weights, ArrayList<Integer> positives) throws Exception{
        this.positives = positives;
        this.weights = weights;
        this.alpha = alpha;
        
        long t0 = System.currentTimeMillis();
    }
    
    public double[] run(){
        this.order = new ArrayList();
        for(int i = 0; i < weights.size(); i++){
            order.add(i);
        }
        
        F = new double[weights.size()];
        f = new double[weights.size()];
        
        for(int i : positives){
            F[i] = 1.0;
            f[i] = 1.0;
        }
        
        HashMap<Integer,Double> graus = new HashMap();
        for(int i = 0; i < weights.size(); i++){
            HashMap<Integer,Double> hm = weights.get(i);
            graus.put(i, 0.0);
            for(double d : hm.values()){
                graus.put(i, (graus.get(i)+d));
            }
        }
        
        for(int v = 0; v < weights.size(); v++){
            HashMap<Integer, Double> hm = weights.get(v);
            
            for(int v2 : hm.keySet()){
                hm.put(v2, (hm.get(v2)/(Math.sqrt(graus.get(v))*Math.sqrt(graus.get(v2)))));
            }
        }
        
        execute();
        
        return f;
    }


    public void execute(){
        boolean control = true;
        double previousDif = 0.0;
        int counter = 0;
        int ite = 0;
        int maxIte = 1000;
        while(control){
            double[] fTemp = new double[weights.size()];
            for(int v : order){
                HashMap<Integer,Double> hm = weights.get(v);
                for(int v2 : hm.keySet()){
                    fTemp[v] += alpha * f[v2] * hm.get(v2);
                }
                fTemp[v] += (1-alpha) * F[v];
            }

            double dif = 0.0;
            for(int v : order){
                dif += Math.abs(f[v] - fTemp[v]);
                f[v] = fTemp[v];
            }
            
            if(Math.abs(dif - previousDif) < 0.0001){
                counter++;
            }else{
                counter = 0;
                previousDif = dif;
            }
            
            ite++;
            
            if(dif == 0.0 || counter == 100 || ite == maxIte)
                control = false;

        }
    }
   
}
