
package networks.constructor;

import auxiliar.IndexValue;
import similarity_matrix.SimilarityMatrix;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import networks.Edge;
import networks.Graph;

/**
 *
 * @author Julio Carnevali
 */
public class MutualKNN {
    
    private Graph G;
    private String fileName;
    
    public String getFileName(){
        return fileName;
    }
    
    public Graph getGraph(){
        return G;
    }
    
    public MutualKNN(String datasetName, int k, SimilarityMatrix sm) throws Exception{
        File dir = new File("saida/networks/");
        if(!dir.exists())
            dir.mkdirs();
        
        String fileName = dir.getPath()+"/rede-mknn"+k+"-"+datasetName;
        this.fileName = fileName;
        
        if((new File(fileName+".pajek")).exists()){
            System.out.println("Network already built.");
            G = new Graph(fileName+".pajek");
            return;
        }
        
        System.out.println("Mutual KNN...");
        
        G = new Graph();

        ArrayList<Integer> index = new ArrayList();
        for(int i = 0; i < sm.matrix.length; i++)
            index.add(i);

        G.add_nodes_from(index);

        mutualKNNGraph(sm, k, G);
        
        G.writePajek(fileName+".pajek");

        System.out.println("Vertices: " + G.getVertices().size());
        System.out.println("Edges: " + G.getEdges().size());

    }
    
    public void mutualKNNGraph(SimilarityMatrix sm, int k, Graph G){
        HashMap<Integer, ArrayList<IndexValue>> nearestK = new HashMap();
        
        for(int i = 0; i < sm.matrix.length; i++){
            PriorityQueue<IndexValue> orderedList = sm.getOrderedListOfIndex(i);
            ArrayList<IndexValue> nearestKofI = new ArrayList();
            for(int j = 0; j < k && orderedList.size() > 0; j++)
                nearestKofI.add(orderedList.poll());
            nearestK.put(i, nearestKofI);
        }
        HashMap<Edge, Integer> edges = new HashMap();
        ArrayList<Edge> validEdges = new ArrayList();
        for(int i : nearestK.keySet()){
            for(IndexValue indVal : nearestK.get(i)){
                double weight = indVal.value;
                if(weight <= 0.0)
                    continue;
                Edge edge = null;
                if(indVal.index > i){
                    edge = new Edge(i,indVal.index,weight);
                }else{
                    edge = new Edge(indVal.index,i,weight);
                }
                if(edges.containsKey(edge)){
                    validEdges.add(edge);
                    edges.put(edge, edges.get(edge)+1);
                }else
                    edges.put(edge, 0);
            }
        }
        G.add_edges_from(validEdges);
    }
    
}
