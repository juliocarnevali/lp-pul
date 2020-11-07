
package similarity_matrix;

import arff_reader.ArffReader;
import auxiliar.IndexValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 *
 * @author julio
 */
public class SimilarityMatrix{
    
    public double[][] matrix;
    public int qttThreads;
    public boolean isCosine;
    public ArffReader dataset;
    
    public SimilarityMatrix(String datasetName, boolean isCosine) throws Exception {
        System.out.println("MATRIX...");
        
        File dir = new File("saida/matrix/");
        if(!dir.exists())
            dir.mkdirs();
        
        String fileName = dir.getPath()+"/"+datasetName+".matrix";
        
        if((new File(fileName)).exists()){
            load(fileName);
            //System.out.println(matrix[115][145]);
            return;
        }
        long t0 = System.currentTimeMillis();
        qttThreads = 10;
        this.isCosine = isCosine;        
        dataset = new ArffReader("datasets/"+datasetName+".arff", isCosine);
        
        if(isCosine)
            matrix = new double[dataset.dataHM.size()][dataset.dataHM.size()];
        else
            matrix = new double[dataset.data.size()][dataset.data.size()];
        
        ArrayList<Thread> threads = new ArrayList();
        for(int i = 0; i < qttThreads; i++){
            final int thread = i+1;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    
                    int begin = (matrix.length/qttThreads) * (thread-1);
                    int end = (matrix.length/qttThreads) * thread;
                    if(thread == qttThreads)
                        end = matrix.length;

                    System.out.println("Length: "+matrix.length);
                    System.out.println(Thread.currentThread().getName()+": "+thread+" -> "+begin+"-"+end);

                    HashMap<Integer,Double> hm = null;
                    HashMap<Integer,Double> hm2 = null;
                    ArrayList al = null;
                    ArrayList al2 = null; 

                    for(int i = begin; i < end; i++){
                        if(isCosine)
                            hm = dataset.dataHM.get(i);
                        else
                            al = dataset.data.get(i);
                        for(int j = i; j < matrix.length; j++){
                            if(isCosine)
                                hm2 = dataset.dataHM.get(j);
                            else
                                al2 = dataset.data.get(j);

                            if(i == j)
                                matrix[i][j] = -1;
                            else{
                                if(isCosine)
                                    matrix[i][j] = CosineSimilarity(hm, hm2);
                                else
                                    matrix[i][j] = 1/EuclideanDistance(al, al2);

                                matrix[j][i] = matrix[i][j];
                            }
                        }
                    }
                }
            });
            t.start();
            
            threads.add(t);
        }
        
        for(Thread t : threads){
            t.join();
        }
        
        System.out.println("Time taken to calculate matrix: "+(System.currentTimeMillis()-t0));
        System.out.println("Saving matrix...");
        save(fileName);
    }
    
    public PriorityQueue getOrderedListOfIndex(int i){
        PriorityQueue<IndexValue> pq = new PriorityQueue((o1, o2) -> {
            if((Double)((IndexValue)o1).value < (Double)((IndexValue)o2).value) return 1;
            else if((Double)((IndexValue)o1).value > (Double)((IndexValue)o2).value) return -1;
            else return 0;
        });
        for(int j = 0; j < matrix.length; j++){
            if(i == j) continue;

            pq.add(new IndexValue(j,matrix[i][j]));
        }
        return pq;
    }
    
    public void load(String fileName) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        
        String line = br.readLine();
        matrix = new double[Integer.valueOf(line)][Integer.valueOf(line)];
        
        int counter = 0;
        
        line = br.readLine();
        while(line != null){
            String[] aux =  line.split(",");
            for(int j = 0; j < aux.length; j++){
                matrix[counter][j] = Double.valueOf(aux[j]);
            }
            
            counter++;
            line = br.readLine();
        }
        
    }
    
    public void save(String fileName) throws IOException{
        FileWriter fw = new FileWriter(new File(fileName));
        fw.write(matrix.length+"\n");
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix.length; j++){
                fw.write(matrix[i][j]+"");
                if(j < matrix.length-1)
                    fw.write(",");
            }
            fw.write("\n");
        }
        
        fw.close();
    }
    
    public double CosineSimilarity(HashMap<Integer,Double> hm, HashMap<Integer,Double> hm2){
        double sim = 0.0;
        
        HashMap<Integer,Double> hm3;
        
        if(hm2.size() < hm.size()){
            hm3 = hm;
            hm = hm2;
            hm2 = hm3;
        }
        
        double hm_den = 0.0;
        double hm2_den = 0.0;

        for(Double weight : hm.values())
            hm_den += Math.pow(weight,2);

        for(Double weight : hm2.values())
            hm2_den += Math.pow(weight,2);

        hm_den = Math.sqrt(hm_den);
        hm2_den = Math.sqrt(hm2_den);

        for(Integer i : hm.keySet()){
            if(hm2.containsKey(i)){
                sim += hm.get(i) * hm2.get(i);
            }
        }
        
        if(hm_den == 0.0)
            hm_den = 0.0000001;
        if(hm2_den == 0.0)
            hm2_den = 0.0000001;
        
        double ret = (sim / (hm_den * hm2_den));
        
        return ret;
    }
    
    public double EuclideanDistance(ArrayList al, ArrayList al2){
        double distance = 0.0;
        for(int i = 0; i < al.size(); i++){
            distance += Math.pow((Double.valueOf((String) al.get(i)) - Double.valueOf((String) al2.get(i))), 2);
        }
        
        distance = Math.sqrt(distance);
        
        if(distance <= 0.0)
            distance = 0.000001;
        
        return distance;
    }
    
}
