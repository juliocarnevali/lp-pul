
package PUL.PEPUC;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Julio Carnevali
 */
public class NaiveBayes {
    private HashMap<String,double[]> p_tk_cj;
    private HashMap<String,Double> p_Cj;
    
    public void train(ArrayList<HashMap<Integer,Double>> X, ArrayList<String> Y, int qttAttr){
        this.p_tk_cj = new HashMap();
        this.p_Cj = new HashMap();
        
        HashMap<String,ArrayList<Integer>> hm = new HashMap();// Y -> its index
        for(int i = 0; i < Y.size(); i++){
            String o = Y.get(i);//class of object i
            
            if(!hm.containsKey(o))
                hm.put(o, new ArrayList());
            
            ArrayList al = (ArrayList)hm.get(o);
            al.add(i);
        }
        
        HashMap results = new HashMap();
        int qttClasses = hm.keySet().size();
        for(String o : hm.keySet()){
            ArrayList<Integer> indexs = ((ArrayList)hm.get(o));
            double p_Cj = ((double)indexs.size()+1) / (Y.size()+qttClasses);
            this.p_Cj.put(o, p_Cj);
            
            double sumOfAllTerms = 0.0;
            double[] sumOfTerms = new double[qttAttr];
            for(int i : indexs){
                HashMap<Integer,Double> txt = X.get(i);
                for(int word : txt.keySet()){
                    sumOfAllTerms += txt.get(word);
                    sumOfTerms[word] += txt.get(word);
                }
            }
            
            double[] p = new double[qttAttr];
            for(int word = 0; word < qttAttr; word++){
                p[word] = ((1+sumOfTerms[word])/(qttAttr+sumOfAllTerms));
            }
            this.p_tk_cj.put(o, p);
        }
        
    }
    
    public HashMap executeBig(HashMap<Integer,Double> query){
        HashMap results = new HashMap();
        BigDecimal norm = BigDecimal.ZERO;
        for(String o : this.p_Cj.keySet()){
            BigDecimal result = new BigDecimal(this.p_Cj.get(o));

            for(int word : query.keySet()){
                result = result.multiply(new BigDecimal(this.p_tk_cj.get(o)[word]));
               
            }
            
            norm = norm.add(result);
            results.put(o, result);
        }
        return results;
    }
    
    public HashMap execute(HashMap<Integer,Double> query){
        HashMap results = new HashMap();
        double norm = 0.0;
        for(String o : this.p_Cj.keySet()){
            double result = this.p_Cj.get(o);

            for(int word : query.keySet()){
                result *= this.p_tk_cj.get(o)[word];
               
            }
            
            norm += (result);
            results.put(o, result);
        }
        for(Object o : results.keySet()){
            results.put(o, (double)results.get(o)/norm);
        }
        
        return results;
    }
    
}
