/************************************
* Instructions:                     *
*  - Use the "setConf" function to  *
* set the configuration of the alg. *
*  - Then use the "run" function in *
* order to get the positive and     *
* negative documents.               *
************************************/


package PUL.PULP;

import PUL.PULAlg;
import auxiliar.PosNeg;
import similarity_matrix.SimilarityMatrix;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import networks.Edge;
import networks.Graph;
import networks.regularizer.GFHF;
import networks.constructor.KNN;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Julio Carnevali
 */
public class PULP extends PULAlg{
    public SimilarityMatrix sm;
    public Graph netGraph;
    public HashMap<Integer,String> lpGraph;
    
    public HashSet<Integer> posOriginal;
    public HashSet<Integer> pos;
    public HashSet<Integer> rp;
    public HashSet<Integer> rn;
    
    public String datasetName;
    public String method;
    public double networkParameter;
    public boolean isCosine;
    public double alpha;
    public int m;
    public double lambda;
    
    public String fileNameNet;
    
    public ArrayList<HashMap<Integer,Double>> W;// lista de adjacencias -> de pesos
    public double[][] Wm;
    
    public PULP() throws Exception{
        reset();
    }
    
    public void reset(){
        sm = null;
        netGraph = null;
        lpGraph = null;

        posOriginal = null;
        pos = null;
        rp = null;
        rn = null;
        
        datasetName = "";
        method = "";
        networkParameter = 0.0;
        isCosine = true;
        alpha = 0.0;
        m = 0;
        lambda = 0.0;

        fileNameNet = "";

        W = null;
        Wm = null;
    }
    
    public void setConf(String datasetName, String method, double networkParameter, 
            boolean isCosine, int m, double lambda, double alpha){
        if(!this.datasetName.equals(datasetName) || this.isCosine != isCosine){
            reset();
            this.datasetName = datasetName;
            this.isCosine = isCosine;
        }
        if(!this.method.equals(method)){
            this.method = method;
            
            this.fileNameNet = "";
            
            this.netGraph = null;
            this.lpGraph = null;
            
            W = null;
            Wm = null;
        }
        if(Math.abs(this.networkParameter - networkParameter) > 0.001){
            this.networkParameter = networkParameter;
            
            this.fileNameNet = "";
            
            this.netGraph = null;
            this.lpGraph = null;
            
            W = null;
            Wm = null;
        }
        if(this.m != m){
            this.m = m;
            
            this.lpGraph = null;
        }
        if(Math.abs(this.lambda - lambda) > 0.001){
            this.lambda = lambda;
            
            this.lpGraph = null;
        }
        if(Math.abs(this.alpha - alpha) > 0.001){
            this.alpha = alpha;
            
            this.lpGraph = null;
            
            W = null;
            Wm = null;
        }
        
    }
    
    @Override
    public PosNeg run(ArrayList<Integer> positives, String classe, double instPerClass, int numRep) throws Exception {
        HashSet<Integer> posOriginal = new HashSet(positives);
        HashSet<Integer> pos = (HashSet)posOriginal.clone();
        HashSet<Integer> rp = new HashSet();
        HashSet<Integer> rn = new HashSet();
        
        build();
        
        long t0 = System.currentTimeMillis();
        extractReliablePosAndNegMatrix(posOriginal, pos, rp, rn);
        this.posOriginal = posOriginal;
        this.pos = pos;
        this.rp = rp;
        this.rn = rn;
        
        HashMap<Integer,String> labels = new HashMap();
        for(int i : pos)
            labels.put(i, "positive");
        for(int i : rn)
            labels.put(i, "negative");
        
        labels = new GFHF("teste", netGraph, labels).getLabels();
        
        lpGraph = labels;
        
        PosNeg pn = new PosNeg();
        pn.positives = getPositives(labels);
        pn.negativesExtracted = new ArrayList(rn);
        
        return pn;
    }
    
    public ArrayList<Integer> getPositives(HashMap labels) throws Exception{
        ArrayList<Integer> interestObj = new ArrayList();
        
        for(int v : netGraph.getVertices()){
            if(labels.get(v).equals("positive"))
                interestObj.add(v);
        }
        
        return interestObj;
    }
    
    public void extractReliablePosAndNeg(HashSet<Integer> posOriginal, HashSet<Integer> pos, 
            HashSet<Integer> rp, HashSet<Integer> rn){
        
        PriorityQueue<ArrayList> values = new PriorityQueue((o1, o2) -> {
            if((Double)((ArrayList)o1).get(1) > (Double)((ArrayList)o2).get(1)) return -1;
            else if((Double)((ArrayList)o1).get(1) < (Double)((ArrayList)o2).get(1)) return 1;
            else return 0;
        });
        
        int qttToExtractForEachIte = (int)(lambda / m * posOriginal.size());
        if(qttToExtractForEachIte <= 0)
            qttToExtractForEachIte = 1;
        
        for(int ite = 0; ite < m; ite++){
            values.clear();
            for(int i = 0; i < W.size(); i++){
                if(pos.contains(i))
                    continue;
                ArrayList al = new ArrayList();
                al.add(i);
                al.add(meanSimilarity(i,pos));
                values.add(al);
            }
            for(int i = 0; i < qttToExtractForEachIte; i++){
                rp.add((int)values.poll().get(0));
            }
            pos.addAll(rp);
        }
        //neg
        values = new PriorityQueue((o1, o2) -> {
            if((Double)((ArrayList)o1).get(1) > (Double)((ArrayList)o2).get(1)) return 1;
            else if((Double)((ArrayList)o1).get(1) < (Double)((ArrayList)o2).get(1)) return -1;
            else return 0;
        });
        for(int i = 0; i < W.size(); i++){
            if(pos.contains(i))
                continue;
            ArrayList al = new ArrayList();
            al.add(i);
            al.add(meanSimilarity(i,pos));
            values.add(al);
        }
        for(int i = 0; i < pos.size(); i++){
            rn.add((int)values.poll().get(0));
        }
    }
    
    public double meanSimilarity(int i, HashSet<Integer> pos){
        double value = 0.0;
        HashMap<Integer,Double> hmi = W.get(i);
        for(int p : pos){
            if(hmi.containsKey(p))
                value += hmi.get(p);
        }
        value /= pos.size();
        
        return value;
    }
    
    public void extractReliablePosAndNegMatrix(HashSet<Integer> posOriginal, HashSet<Integer> pos, 
            HashSet<Integer> rp, HashSet<Integer> rn){
        
        PriorityQueue<ArrayList> values = new PriorityQueue((o1, o2) -> {
            if((Double)((ArrayList)o1).get(1) > (Double)((ArrayList)o2).get(1)) return -1;
            else if((Double)((ArrayList)o1).get(1) < (Double)((ArrayList)o2).get(1)) return 1;
            else return 0;
        });
        
        int qttToExtractForEachIte = (int)(lambda / m * posOriginal.size());
        
        for(int ite = 0; ite < m; ite++){
            values.clear();
            for(int i = 0; i < Wm.length; i++){
                if(pos.contains(i))
                    continue;
                ArrayList al = new ArrayList();
                al.add(i);
                al.add(meanSimilarityMatrix(i,pos));
                values.add(al);
            }
            for(int i = 0; i < qttToExtractForEachIte; i++){
                rp.add((int)values.poll().get(0));
            }
            pos.addAll(rp);
        }
        //neg
        values = new PriorityQueue((o1, o2) -> {
            if((Double)((ArrayList)o1).get(1) > (Double)((ArrayList)o2).get(1)) return 1;
            else if((Double)((ArrayList)o1).get(1) < (Double)((ArrayList)o2).get(1)) return -1;
            else return 0;
        });
        for(int i = 0; i < Wm.length; i++){
            if(pos.contains(i))
                continue;
            ArrayList al = new ArrayList();
            al.add(i);
            al.add(meanSimilarityMatrix(i,pos));
            values.add(al);
        }
        
        for(int i = 0; i < pos.size(); i++){
            rn.add((int)values.poll().get(0));
        }
    }
    
    public double meanSimilarityMatrix(int i, HashSet<Integer> pos){
        double value = 0.0;
        for(int p : pos){
            value += Wm[i][p];
        }
        value /= pos.size();
        
        return value;
    }
    
    public void calcWm(){
        double [][] values = new double[netGraph.getVertices().size()][netGraph.getVertices().size()];
        double [][] rhs = new double[netGraph.getVertices().size()][netGraph.getVertices().size()];
        
        for(Edge edge : netGraph.getEdges()){
            values[edge.source][edge.target] = 1.0;
            values[edge.target][edge.source] = 1.0;
        }
        
        for(int i = 0; i < rhs.length; i++){
            rhs[i][i] = 1;
        }
        
        for(int i = 0; i < values.length; i++){
            for(int j = 0; j < values.length; j++){
                values[i][j] *= alpha;
                if(i==j)
                    values[i][j] = 1 - values[i][j];
                else
                    values[i][j] = 0 - values[i][j];
            }
        }
        
        RealMatrix A = new Array2DRowRealMatrix(values);
        DecompositionSolver solver = new LUDecomposition(A).getSolver();

        RealMatrix I = new Array2DRowRealMatrix(rhs);
        RealMatrix B = solver.solve(I);
        
        values = B.getData();
        for(int i = 0; i < values.length; i++){
            values[i][i] -= 1;
        }
        
        Wm = values;
    }
    
    public void calcW(){
        W = new ArrayList();
        
        for(int i = 0; i < netGraph.getVertices().size(); i++)
            W.add(new HashMap());
        
        for(int i = 0; i < netGraph.getVertices().size(); i++){
            for(int j = i+1; j < netGraph.getVertices().size(); j++){
                System.out.println("Path: "+i+" -> "+j);
                HashMap<Integer, Integer> qttPaths = new HashMap();//<tamanho, qtt>
                qttOfPaths(-1,i,j,netGraph, new HashSet(), qttPaths);
                if(qttPaths.size() > 0){
                    double value = 0.0;
                    for(int sizes : qttPaths.keySet()){
                        value += (Math.pow(alpha, sizes)) * qttPaths.get(sizes);
                    }
                    W.get(i).put(j, value);
                    W.get(j).put(i, value);
                }
            }
        }
    }
    
    public void qttOfPaths(int previous, int now, int target, Graph G, HashSet myPath, HashMap<Integer,Integer> qttPaths){
        if(previous != -1)
            myPath.add(new ArrayList(){{add(now);add(previous);}});
        
        if(now == target){
            int size = myPath.size();
            if(!qttPaths.containsKey(size))
                qttPaths.put(size, 0);
            qttPaths.put(size, qttPaths.get(size)+1);
            //System.out.println(qttPaths);
            return;
        }
        
        if(myPath.size() >= 7)
            return;
        
        for(int adj : G.getAdj(now)){
            if(!myPath.contains(new ArrayList(){{add(now);add(adj);}}) && !myPath.contains(new ArrayList(){{add(adj);add(now);}}))
                qttOfPaths(now, adj, target, G, (HashSet)myPath.clone(), qttPaths);
        }
    }
    
    public SimilarityMatrix getMatrix() throws Exception{
        if(sm == null){
            sm = new SimilarityMatrix(datasetName, isCosine);
        }
        return sm;
    }
    
    public synchronized void build() throws Exception{
        if(netGraph == null){
            if(method.equals("knn")){
                KNN knn = new KNN(datasetName, (int)networkParameter, getMatrix());
                fileNameNet = knn.getFileName();
                netGraph = knn.getGraph();
            }
        }
        if(Wm == null){
            File folder = new File("saida/pulp/");
            if(!folder.exists())
                folder.mkdirs();
            
            String n = folder.getPath() + "/"+datasetName+"-"+method+networkParameter+"-"+this.alpha+".matrix";
            File f = new File(n);
            if(f.exists()){
                long t0 = System.currentTimeMillis();
                System.out.println("Reading W...");
                Wm = new double[netGraph.getVertices().size()][netGraph.getVertices().size()];
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line = br.readLine();
                int i = 0;
                while(line != null){
                    String[] aux = line.split(" ");
                    for(int j = 0; j < aux.length; j++)
                        Wm[i][j] = Double.valueOf(aux[j]);
                    line = br.readLine();
                    i++;
                }
                //System.out.println("Reading time: "+(System.currentTimeMillis()-t0));
            }else{
                System.out.println("Calculating W...");
                calcWm();
                
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                for(int i = 0; i < Wm.length; i++){
                    for(int j = 0; j < Wm.length; j++){
                        if(j < Wm.length-1)
                            bw.write(Wm[i][j]+" ");
                        else
                            bw.write(Wm[i][j]+"");
                    }
                    if(i < Wm.length-1)
                        bw.write("\n");
                }
                bw.flush();
                bw.close();
            }
        }
    }
}
 