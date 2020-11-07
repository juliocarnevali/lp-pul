/************************************
* Instructions:                     *
*  - Use the "setConf" function to  *
* set the configuration of the alg. *
*  - Then use the "run" function in *
* order to get the positive and     *
* negative documents.               *
************************************/

package PUL;

import auxiliar.PosNeg;
import auxiliar.IndexValue;
import similarity_matrix.SimilarityMatrix;
import networks.distance.Dijkstra;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import networks.Graph;
import networks.regularizer.GFHF;
import networks.regularizer.LLGC;
import networks.constructor.EN;
import networks.constructor.KNN;
import networks.constructor.MutualKNN;

/**
 *
 * @author Julio Carnevali
 */
public class LP_PUL extends PULAlg{
    public SimilarityMatrix sm;
    public Graph netGraph;
    public HashMap<Integer,String> lpGraph;
    
    public HashMap<String,HashMap<Integer,String>> communitiesForEachExtraction;
    
    public String datasetName;
    public String networkConstructor; //mknn, knn, en
    public double networkParameter;
    public boolean isCosine;//cosine or euclidean
    public String lp_alg;//gfhf,llgc
    public int neg;//absolute number of negative documents to be extracted
    public double alpha;//alpha of llgc algorithm
    
    public String fileNameNet;
   
    public LP_PUL(){
        reset();
    }
    
    public void reset(){
        this.sm = null;
        this.netGraph = null;
        this.lpGraph = null;
        
        this.communitiesForEachExtraction = new HashMap();
        
        this.datasetName = "";
        this.networkConstructor = "";
        this.networkParameter = 0.0;
        this.isCosine = true;
        this.lp_alg = "";
        this.neg = 0;
        this.alpha = 0.0;
        
        this.fileNameNet = "";
    }
    
    public void setConf(String datasetName, String networkConstructor, double networkParameter, 
            boolean isCosine, String lp_alg, int neg, double alpha){
        if(!this.datasetName.equals(datasetName) || this.isCosine != isCosine){
            reset();
            this.datasetName = datasetName;
            this.isCosine = isCosine;
        }
        if(!this.networkConstructor.equals(networkConstructor)){
            this.networkConstructor = networkConstructor;
            
            this.fileNameNet = "";
            
            this.communitiesForEachExtraction = new HashMap();
            
            this.netGraph = null;
            this.lpGraph = null;
        }
        if(Math.abs(this.networkParameter - networkParameter) > 0.001){
            this.networkParameter = networkParameter;
            
            this.fileNameNet = "";
            
            this.communitiesForEachExtraction = new HashMap();
            
            this.netGraph = null;
            this.lpGraph = null;
        }
        if(!this.lp_alg.equals(lp_alg)){
            this.lp_alg = lp_alg;
            
            this.lpGraph = null;
        }
        if(this.neg != neg){
            this.neg = neg;
            
            this.lpGraph = null;
        }
        if(Math.abs(this.alpha - alpha) > 0.001){
            this.alpha = alpha;
            
            this.lpGraph = null;
        }
    }
    
    @Override
    public PosNeg run(ArrayList<Integer> positives, String classe, double instPerClass, int numRep) throws Exception{
        build();
        
        ArrayList<Integer> rn = new ArrayList();
        
        long t0 = System.currentTimeMillis();
        HashMap labels = extractNegativesJava(positives, classe, instPerClass, numRep, rn);
        //System.out.println("Extraction: "+(System.currentTimeMillis()-t0));
        this.lpGraph = labels;
        
        PosNeg pn = new PosNeg();
        pn.positives = getPositives(labels);
        pn.negativesExtracted = rn;
        return pn;
    }
    
    public ArrayList<Integer> getPositives(HashMap<Integer,String> labels) throws Exception{
        ArrayList<Integer> interestObj = new ArrayList();
        
        for(int v : netGraph.getVertices()){
            if(labels.get(v).equals("positive"))
                interestObj.add(v);
        }
        
        return interestObj;
    }
    
    
    public SimilarityMatrix getMatrix() throws Exception{
        if(sm == null){
            long t0 = System.currentTimeMillis();
            sm = new SimilarityMatrix(datasetName, isCosine);
        }
        return sm;
    }
    
    public synchronized void build() throws Exception{
        if(netGraph == null){
            if(networkConstructor.equals("mknn")){
                MutualKNN mknn = new MutualKNN(datasetName, (int)networkParameter, getMatrix());
                fileNameNet = mknn.getFileName();
                netGraph = mknn.getGraph();
            }else if(networkConstructor.equals("knn")){
                long t0 = System.currentTimeMillis();
                KNN knn = new KNN(datasetName, (int)networkParameter, getMatrix());
                fileNameNet = knn.getFileName();
                netGraph = knn.getGraph();
            }else if(networkConstructor.equals("en")){
                EN en = new EN(datasetName, networkParameter, getMatrix());
                fileNameNet = en.getFileName();
                netGraph = en.getGraph();
            }
        }
    }
    public HashMap extractNegativesJava(ArrayList<Integer> classified, String classe, double instPerClass, int numRep, ArrayList<Integer> rn) throws Exception{
        
        String fileNameAlg = fileNameNet;
        
        ArrayList<Integer> interestObj = new ArrayList();
        HashSet<String> interestCommunities = new HashSet();
        
        for(int v : classified){
            interestObj.add(v);
        }
        
        String fileName = (fileNameAlg.split("/")[fileNameAlg.split("/").length-1]);
        String newFileName = (fileNameAlg.replace(fileName, ""))+"/extractNeg/"+classe+"_"+instPerClass+"_"+neg+"_"+numRep+"_"+fileName;
        File f = (new File(newFileName+".pajek"));
        File dir = new File(f.getParent());
        if(!dir.exists()){
            dir.mkdirs();
        }
        
        HashMap<Integer,String> posANDneg = null;
        if(!communitiesForEachExtraction.containsKey(newFileName)){
            int qttNegs = neg;
            if(qttNegs < 0)
                qttNegs = interestObj.size()*Math.abs(qttNegs);
            
            long t1 = System.currentTimeMillis();
            posANDneg = extractNeg(interestObj, qttNegs);
            long t2 = System.currentTimeMillis();
            setCom(newFileName, posANDneg);
        }else{ 
            posANDneg = ((HashMap)communitiesForEachExtraction.get(newFileName).clone());
        }
        
        for(int i : posANDneg.keySet()){
            if(posANDneg.get(i).equals("negative"))
                rn.add(i);
        }
        
        if(lp_alg.equals("llgc")) return new LLGC(newFileName, netGraph, posANDneg, alpha).getLabels();
        else if(lp_alg.equals("gfhf")) return new GFHF(newFileName, netGraph, posANDneg).getLabels();
        
        return null;
    }
    
    public synchronized void setCom(String newFileName, HashMap commus){
        communitiesForEachExtraction.put(newFileName, (HashMap)(commus.clone()));
    }
    
    public HashMap extractNeg(ArrayList<Integer> interestObj, int qttNegs){
        HashMap<Integer,String> posANDneg = new HashMap();
        
        double[] avgDist = new double[netGraph.getVertices().size()-interestObj.size()];
        ArrayList<Integer> targetV = new ArrayList();
        for(int v : netGraph.getVertices()){
            if(!interestObj.contains(v)) targetV.add(v);
        }
        
        ArrayList<Integer> negatives = new ArrayList();
        if(targetV.size() > 0 && qttNegs < targetV.size()){
            Dijkstra dij = new Dijkstra(netGraph);
            for(int v : interestObj){
                double[] aux = dij.execute(v);
                for(int i = 0; i < avgDist.length; i++){
                    avgDist[i] += aux[targetV.get(i)];
                }
            }
            for(int i = 0; i < avgDist.length; i++){//media
                avgDist[i] /= interestObj.size();
            }

            ArrayList<IndexValue> ordered = new ArrayList();
            for(int i = 0; i < avgDist.length; i++){
                ordered.add(new IndexValue(targetV.get(i),avgDist[i]));
            }
            ordered.sort((IndexValue o1, IndexValue o2)->{
                if((double)o1.value > (double)o2.value) return -1;
                else if((double)o1.value < (double)o2.value) return 1;
                else return 0;
            });
            for(int i = 0; i < qttNegs; i++){
                negatives.add((int)ordered.get(i).index);
            }

        }else{
            negatives = targetV;
        }
        for(int v : interestObj){
            posANDneg.put(v, "positive");
        }
        for(int v : negatives){
            posANDneg.put(v, "negative");
        }
        
        return posANDneg;
    }
    
}
