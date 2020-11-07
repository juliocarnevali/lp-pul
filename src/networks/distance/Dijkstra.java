
package networks.distance;

import auxiliar.IndexValue;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import networks.Edge;
import networks.Graph;

/**
 *
 * @author Julio Carnevali
 */
public class Dijkstra {
    private Graph G;
    private Set<Integer> settled; 
    private PriorityQueue<IndexValue> unsettled;//<ArrayList<(0-vertex,1-weight)>>
    
    private int start;
    
    public Dijkstra(Graph G){
        this.G = G;
    }
    
    public double[] execute(int start){
        this.start = start;
        
        settled = new HashSet<Integer>();
        unsettled = new PriorityQueue((o1, o2) -> {
            if(((IndexValue)o1).value > ((IndexValue)o2).value) return 1;
            else if(((IndexValue)o1).value < ((IndexValue)o2).value) return -1;
            else return 0;
        });
        unsettled.add(new IndexValue(start,0.0));
        
        return run();
    }
    
    private double[] run(){
        int alreadyAchieved = 0;
        double[] distances = new double[G.getVertices().size()];
        while(!unsettled.isEmpty() && G.getVertices().size() != alreadyAchieved){
            long t1 = System.currentTimeMillis();
            
            IndexValue currentV = unsettled.poll();
            int x = currentV.index;
            double distance = currentV.value;
            
            if(!settled.add(x))
                continue;
            
            distances[x] = distance;
            alreadyAchieved++;
            
            for(Edge edge : G.getEdgesFromVert(x)){
                int y = edge.target;
                if(y == x)
                    y = edge.source;
                
                if(settled.contains(y))
                    continue;
                
                IndexValue indVal = new IndexValue(y,distance+G.getEdgeInvertedWeight(edge));
                
                unsettled.add(indVal);
            }
        }
        return distances;
    }
   
}
