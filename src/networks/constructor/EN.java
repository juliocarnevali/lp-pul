
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
public class EN {
    
    private Graph G;
    private String fileName;
    
    public String getFileName(){
        return fileName;
    }
    
    public Graph getGraph(){
        return G;
    }
    
    public EN(String datasetName, double e, SimilarityMatrix sm) throws Exception{
        File dir = new File("saida/networks/");
        if(!dir.exists())
            dir.mkdirs();
        
        String fileName = dir.getPath()+"/rede-en"+e+"-"+datasetName;
        this.fileName = fileName;
        
        if((new File(fileName+".pajek")).exists()){
            System.out.println("Network already built.");
            G = new Graph(fileName+".pajek");
            return;
        }
        
        System.out.println("eN...");
        
        G = new Graph();

        ArrayList<Integer> index = new ArrayList();
        for(int i = 0; i < sm.matrix.length; i++)
            index.add(i);

        G.add_nodes_from(index);

        eNGraph(sm, e, G);
        
        G.writePajek(fileName+".pajek");

        System.out.println("Vertices: " + G.getVertices().size());
        System.out.println("Edges: " + G.getEdges().size());

    }
    
    public void eNGraph(SimilarityMatrix sm, double e, Graph G){
        HashMap<Integer, ArrayList<IndexValue>> nearestE = new HashMap();
        
        for(int i = 0; i < sm.matrix.length; i++){
            PriorityQueue<IndexValue> orderedList = sm.getOrderedListOfIndex(i);
            ArrayList<IndexValue> nearestEofI = new ArrayList();
            for(int j = 0; orderedList.size() > 0; j++){
                IndexValue indVal = orderedList.poll();
                if(e > indVal.value){
                    break;
                }
                nearestEofI.add(indVal);
            }
            nearestE.put(i, nearestEofI);
        }
        ArrayList<Edge> validEdges = new ArrayList();
        for(int i : nearestE.keySet()){
            for(IndexValue indVal : nearestE.get(i)){
                Edge edge = null;
                if(indVal.index > i){
                    edge = new Edge(i,indVal.index,indVal.value);
                }else{
                    edge = new Edge(indVal.index,i,indVal.value);
                }
                validEdges.add(edge);
            }
        }
        G.add_edges_from(validEdges);
    }
   
    
}
