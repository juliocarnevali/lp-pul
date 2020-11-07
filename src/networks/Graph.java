/*****************************
 * ONLY FOR UNDIRECTED GRAPH *
 *****************************/
package networks;

import similarity_matrix.SimilarityMatrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author julio
 */
public class Graph {
    HashMap<Integer,String> verticesCommunity;      //dict(vertex    ->  Community)
    HashMap<Integer, ArrayList<Edge>> verticesEdges;  //dict(vertex    ->  edges)
    HashMap<Integer, ArrayList> verticesComp;   //dict(vertex    ->  comp)
    HashMap<ArrayList<Integer>, ArrayList<Edge>> components;   //dict(comp      ->  edges)
    
    int qttOfDocs = -1;
    
    private Graph(HashMap<Integer,String> verticesCommunity, 
            HashMap<Integer, ArrayList<Edge>> verticesEdges, 
            HashMap<Integer, ArrayList> verticesComp, 
            HashMap<ArrayList<Integer>, ArrayList<Edge>> components){
        
        this.verticesCommunity = verticesCommunity;
        this.verticesEdges = verticesEdges;
        this.verticesComp = verticesComp;
        this.components = components;
    }
    
    public Graph(){
        verticesEdges = new HashMap();
        verticesComp = new HashMap();
        components = new HashMap();
        verticesCommunity = new HashMap();
    }
    
    public Graph(String pajekFile) throws Exception{
        verticesEdges = new HashMap();
        verticesComp = new HashMap();
        components = new HashMap();
        verticesCommunity = new HashMap();
        
        BufferedReader br = new BufferedReader(new FileReader(pajekFile));
        
        String line = br.readLine();
        
        boolean isVert = false;
        boolean isEdge = false;
        
        int counter = 0;
        int qttVert = 0;
        int edges = 0;
        
        while(line != null){
            if(!line.startsWith("#") && !line.isEmpty()){
                if(counter == qttVert)
                    isVert = false;

                if(isVert){
                    counter++;
                    String[] s = line.split(" ");
                    add_node(Integer.valueOf(s[0]));
                    if(s.length >= 3){
                        setCommunityFromVertice(Integer.valueOf(s[0]), s[2]);
                    }
                }

                if(isEdge){
                    edges++;
                    //if(edges%10000 == 0)
                        //System.out.println(edges);
                    String[] s = line.split(" ");
                    Edge edge = new Edge(Integer.valueOf(s[0]),Integer.valueOf(s[1]),1.0);
                    if(s.length >= 3){
                        edge.weight = Double.valueOf(s[2]);
                    }
                    add_edge(edge);
                }

                if(line.contains("*Vertices")){
                    isVert = true;
                    isEdge = false;

                    qttVert = Integer.valueOf(line.split(" ")[1]);
                }
                if(line.contains("*Edges")){
                    isVert = false;
                    isEdge = true;
                }
            }else{
                if(line.contains("#qttOfDocs")){
                    qttOfDocs = Integer.valueOf(line.split(" ")[1]);
                }
            }
            line = br.readLine();
        }
    }
    
    private HashMap<Integer, ArrayList<Edge>> cloneVerticesEdges(){
        HashMap<Integer, ArrayList<Edge>> verticesEdges = new HashMap();
        for(int v : this.verticesEdges.keySet()){
            ArrayList al = new ArrayList();
            for(Edge edge : this.verticesEdges.get(v)){
                al.add(edge.clone());
            }
            verticesEdges.put(v, al);
        }
        return verticesEdges;
    }
    private HashMap<Integer, ArrayList> cloneVerticesComp(){
        HashMap<Integer, ArrayList> verticesComp = new HashMap();
        for(int v : this.verticesComp.keySet()){
            verticesComp.put(v, (ArrayList)this.verticesComp.get(v).clone());
        }
        return verticesComp;
    }
    
    private HashMap<ArrayList<Integer>, ArrayList<Edge>> cloneComponents(){
        HashMap<ArrayList<Integer>, ArrayList<Edge>> components = new HashMap();
        for(ArrayList comp : this.components.keySet()){
            ArrayList al = new ArrayList();
            for(Edge edge : this.components.get(comp)){
                al.add(edge.clone());
            }
            components.put((ArrayList)comp.clone(), al);
        }
        return components;
    }
    
    public Graph clone(){
        return new Graph((HashMap<Integer,String>)verticesCommunity.clone(), cloneVerticesEdges(), cloneVerticesComp(), cloneComponents());
    }
    
    public int getQttOfDocs(){
        if(qttOfDocs > 0)
            return qttOfDocs;
        
        return verticesCommunity.size();
    }
    
    public void setQttOfDocs(int docs){
        this.qttOfDocs = docs;
    }
    
    public int getQttOfCommunities(){
        HashSet hs = new HashSet();
        for(String comm: verticesCommunity.values()){
            hs.add(comm);
            //System.out.println("community: "+comm);
        }
        
        return hs.size();
    }
    
    public void writePajek(String local) throws Exception{
        System.out.println("Writing network...");
        FileWriter f = new FileWriter(new File(local));
        
        if(qttOfDocs > 0)
            f.write("#qttOfDocs "+qttOfDocs+"\n");
        
        f.write("*Vertices "+getVertices().size()+"\n");
        
        for(int v : getVertices()){
            
            //f.write(counter + " " +name[row] + "\n");
            if(verticesCommunity.containsKey(v))
                f.write(v + " " +v + " " + verticesCommunity.get(v) + "\n");
            else
                f.write(v + " " +v + "\n");
        }

        f.write("*Edges\n");

        for(Edge edge : getEdges())
            f.write(edge.source + " " + edge.target + " " + edge.weight + "\n");
                    
        f.close();
    }
    
    public void writePajekWithoutEdges(String local) throws Exception{
        FileWriter f = new FileWriter(new File(local));
        
        if(qttOfDocs > 0)
            f.write("#qttOfDocs "+qttOfDocs+"\n");
        
        f.write("*Vertices "+getVertices().size()+"\n");
        int counter = -1;

        for(int row : getVertices()){
            counter += 1;
            
            //f.write(counter + " " +name[row] + "\n");
            if(verticesCommunity.containsKey(counter))
                f.write(counter + " " +counter + " " + verticesCommunity.get(counter) + "\n");
            else
                f.write(counter + " " +counter + "\n");
        }

        f.close();
    }
    
    public void writePajekBipartite(String local) throws Exception{
        if(qttOfDocs > 0){
            for(int i = qttOfDocs; i < getVertices().size(); i++){
                verticesCommunity.remove(i);
            }
            ArrayList<Integer> vertices = new ArrayList();
            for(int i = 0; i < qttOfDocs; i++){
                vertices.add(i);
            }
            Graph g = new Graph();
            g.add_nodes_from(vertices);
            
            verticesComp = g.verticesComp;
            verticesEdges = g.verticesEdges;
            components = g.components;
            
        }
            
        writePajek(local);
    }
    
    
    public void add_nodes_from(ArrayList<Integer> vertices){
        for(int o : vertices){
            if(verticesEdges.containsKey(o))
                continue;
            
            add_node(o);
        }
    }
    
    public void add_nodes_from_edge(Edge edge){
        if(!verticesEdges.containsKey(edge.source))
            add_node(edge.source);
        if(!verticesEdges.containsKey(edge.target))
            add_node(edge.target);
    }
    
    public void add_node(int o){
        ArrayList al = new ArrayList();
        al.add(o);
        verticesEdges.put(o, new ArrayList());
        verticesComp.put(o, al);
        components.put(al, new ArrayList());
    }
    
    public ArrayList<Integer> getAdj(int node){
        HashSet<Integer> adj = new HashSet<Integer>();
        
        for(Edge edge: verticesEdges.get(node)){
            if(edge.source == node){
                adj.add(edge.target);
            }else{
                adj.add(edge.source);
            }
        }
        
        return new ArrayList(adj);
    }
    

    public int getQttComponents(){
        return components.size();
    }

    public Set<ArrayList<Integer>> getComponents(){
        return components.keySet();
    }

    public ArrayList getComponentFromVertex(int vertex){
        return (ArrayList) verticesComp.get(vertex);
    }

    public int getNumberOfEdgesFromComponent(ArrayList comp){
        return ((ArrayList)components.get(comp)).size();
    }

    public Set<Integer> getVertices(){
        return verticesEdges.keySet();
    }
    
    public HashMap<Integer,String> getVerticesCommunity(){
        return verticesCommunity;
    }
    
    public void setVerticesCommunity(HashMap<Integer,String> hm){
        verticesCommunity = hm;
    }
    
    public String getCommunityFromVertice(int v){
        if(verticesCommunity.containsKey(v)) return verticesCommunity.get(v);
        return null;
    }
    
    public boolean verticeXHasCommunity(int v){
        return verticesCommunity.containsKey(v);
    }
    
    public void setCommunityFromVertice(int v, String com){
        verticesCommunity.put(v,com);
    }
    
    
    public double getEdgeInvertedWeight(Edge edge){
        double newWeight = (1 / edge.weight);
        if(newWeight > 0.0)
            return newWeight;
        
        return 0.0001;
    }
    
    
    
    public ArrayList<Edge> getEdges(){
        ArrayList<Edge> allEdges = new ArrayList();
        for(ArrayList<Edge> edges : components.values()){
            for(Edge edge : edges){
                allEdges.add(new Edge(edge.source,edge.target,edge.weight));
            }
        }
        return allEdges;
    }
    
    public ArrayList<Edge> getEdgesFromVert(int v){
        return verticesEdges.get(v);
    }

    public void add_edges_from(ArrayList<Edge> edges){
        for(Edge edge : edges){
            add_edge(edge);
        }
    }    
    
    public void add_edge(Edge edge){
        Edge edgeMirror = new Edge(edge.target,edge.source,edge.weight);
        
        add_nodes_from_edge(edge);
        if(((ArrayList)verticesEdges.get(edge.source)).contains(edge) || ((ArrayList)verticesEdges.get(edge.source)).contains(edgeMirror))
            return;

        //updating self.verticesEdges
        ((ArrayList)verticesEdges.get(edge.source)).add(edge);
        ((ArrayList)verticesEdges.get(edge.target)).add(edge);

        ArrayList<Integer> compI = verticesComp.get(edge.source);
        ArrayList<Integer> compJ = verticesComp.get(edge.target);
            
        
        if(compI != compJ){
            //updating verticesComp
            ArrayList<Integer> newComp = (ArrayList) compJ.clone();
            newComp.addAll(compI);

            newComp.sort((o1, o2) -> {
                if(o1 > o2)
                    return 1;
                else if(o1 < o2)
                    return -1;
                else return 0;
            });

            for(int v : newComp)
                verticesComp.put(v, newComp);

            //updating components
            components.put(newComp, (ArrayList)(components.get(compI)).clone());//nao tem problema clonar, pq apaga logo abaixo
            ((ArrayList)components.get(newComp)).addAll((ArrayList)components.get(compJ));//nao tem problema adicionar as edges, pq apaga logo abaixo
            ((ArrayList)components.get(newComp)).add(edge);

            components.remove(compI);
            components.remove(compJ);
        }else
            ((ArrayList)components.get(compI)).add(edge);
    }
    
    public void verifyDuplicity(){//verifica se tem comunidades diferentes com o mesmo label
        HashSet<Integer> vertices = new HashSet();
        HashMap<String, HashSet<Integer>> commusConnected = new HashMap();
        for(int v : verticesCommunity.keySet()){
            if(!vertices.contains(v)){
                String commu = getCommunityFromVertice(v);
                HashSet<Integer> commuList = new HashSet();
                commuDuplicated(v, commu, commuList);
                vertices.addAll(commuList);
                
                while(commusConnected.containsKey(commu))
                    commu += "a";
                
                commusConnected.put(commu, commuList);
            }
        }
        if(commusConnected.keySet().size() != getQttOfCommunities()){
            System.out.println("Conflito de comunidades.");
            
            for(String com : commusConnected.keySet()){
                for(int v : commusConnected.get(com)){
                    setCommunityFromVertice(v, com);
                }
            }
        }
    }
    
    private void commuDuplicated(int v, String commu, HashSet<Integer> commuList){
        if(!getCommunityFromVertice(v).equals(commu))
            return;
        if(commuList.contains(v))
            return;
        
        commuList.add(v);
        for(int adj : getAdj(v)){
            commuDuplicated(adj, commu, commuList);
        }
    }
    
    public HashMap<Integer,Double> getDegrees(){
        HashMap<Integer,Double> hm = new HashMap();
        
        for(int v : getVertices()){
            hm.put(v, getDegree(v));
        }
        
        return hm;
    }
    
    public double getDegree(int v){
        double degree = 0.0;
        for(Edge edge : verticesEdges.get(v)){
            degree += edge.weight;
        }
        
        return degree;
    }
    
    public void singleLinkageForComponents(double[][] similarityMatrix) throws Exception{
        System.out.println("SingleLinkage: "+getComponents().size());
        while(getComponents().size() > 1){
            ArrayList<ArrayList<Integer>> comps = new ArrayList(getComponents());
            
            double value = Double.MIN_VALUE;
            int a = 0;
            int b = 0;
            for(int i = 0; i < comps.size(); i++){
                for(int j = i+1; j < comps.size(); j++){
                    for(int m : comps.get(i)){
                        for(int n : comps.get(j)){
                            if(similarityMatrix[m][n] > value){
                                a = m;
                                b = n;
                                value = similarityMatrix[m][n];
                                //System.out.println(value);
                            }
                        }
                    }
                }
            }
            
            Edge edge = new Edge(a,b,value);
            add_edge(edge);

        }
        
    }
    
    
}
