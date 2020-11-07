
package networks.regularizer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import networks.Edge;
import networks.Graph;
/**
 *
 * @author julio
 */
public class LLGC {
    public Graph G;
    public ArrayList<Integer> order;
    public String fn;
    public HashMap<Integer,String> labels;
    
    public int qttClasses;
    public double[][] F;
    public double[][] f;
    public ArrayList<HashMap<Integer,Double>> weights;// array index is the source vertex, and the hashmap key is the target vertex. finally the hashmap value is the weight
    public double alpha;
    
    public HashMap getLabels(){
        return labels;
    }
    
    public Graph getGraph(){
        return G;
    }
    
    public String getFileName(){
        return fn;
    }
                
    public LLGC(String datasetName, Graph G, HashMap<Integer,String> labels, double alpha) throws Exception{
        this.labels = labels;
        this.alpha = alpha;
        File dir = new File("saida/networks/communities/");
        if(!dir.exists())
            dir.mkdirs();
        
        String algName = "llgc";
        
        String fileName = dir.getPath() + "/" +algName+ "-" +(datasetName.split("/")[datasetName.split("/").length-1]);
        this.fn = fileName;
        
        this.G = G;
        
        long t0 = System.currentTimeMillis();
        run();
    }
    
    public void run(){
        this.order = new ArrayList();
        initialize();
    }
    
    public void initialize(){
        HashSet<String> hs = new HashSet();
        ArrayList<Integer> vs = new ArrayList();
        for(int i : G.getVertices()){
            order.add(i);
            
            String com = labels.get(i);
            if(com != null){
                hs.add(com);
                vs.add(i);
            }
        }
        
        qttClasses = hs.size();
        
        F = new double[G.getVertices().size()][qttClasses];
        f = new double[G.getVertices().size()][qttClasses];
        
        ArrayList<String> hs2 = new ArrayList(hs);
        
        for(int i : vs){
            F[i][hs2.indexOf(labels.get(i))] = 1.0;
            f[i][hs2.indexOf(labels.get(i))] = 1.0;
        }
        
        HashMap<Integer,Double> graus = G.getDegrees();
        weights = new ArrayList();
        for(int v = 0; v < G.getVertices().size(); v++){
            HashMap<Integer, Double> hm = new HashMap();
            weights.add(hm);
            
            for(Edge edge : G.getEdgesFromVert(v)){
                int v2 = edge.source;
                if(v2 == v)
                    v2 = edge.target;
                
                hm.put(v2, (edge.weight/(Math.sqrt(graus.get(v))*Math.sqrt(graus.get(v2)))));
            }
        }
        
        execute();
        
        for(int v : G.getVertices()){
            ArrayList<Integer> majorLabel = new ArrayList();
            double majorLabelValue = -1.0;
            for(int classe = 0; classe < qttClasses; classe++){
                //System.out.println(majorLabelValue +" "+ f[v][classe]);
                if(majorLabelValue < f[v][classe]){
                    majorLabel = new ArrayList();
                    majorLabel.add(classe);
                    majorLabelValue = f[v][classe];
                }else if(majorLabelValue == f[v][classe]){
                    majorLabel.add(classe);
                    if(majorLabelValue == 0.0){
                        majorLabel.clear();
                        majorLabel.add(hs2.indexOf("negative"));
                    }
                }
            }
            Collections.shuffle(majorLabel, new Random(v));
            labels.put(v, hs2.get(majorLabel.get(0)));
        }
        
    }


    public void execute(){
        boolean control = true;
        double previousDif = 0.0;
        int counter = 0;
        int ite = 0;
        int maxIte = 1000;
        while(control){
            double[][] fTemp = new double[G.getVertices().size()][qttClasses];
            for(int v : order){
                HashMap<Integer,Double> hm = weights.get(v);
                for(int v2 : hm.keySet()){
                    for(int classe = 0; classe < qttClasses; classe++){
                        fTemp[v][classe] += alpha * f[v2][classe] * hm.get(v2);
                    }
                }
                for(int classe = 0; classe < qttClasses; classe++){
                    fTemp[v][classe] += (1-alpha) * F[v][classe];
                }
            }

            double dif = 0.0;
            for(int v : order){
                for(int classe = 0; classe < qttClasses; classe++){
                    dif += Math.abs(f[v][classe] - fTemp[v][classe]);
                    f[v][classe] = fTemp[v][classe];
                }
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
