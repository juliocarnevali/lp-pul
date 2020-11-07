/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TCTAlgorithms.InductiveSupervisedOneClass;

import TCTNetworkGeneration.Proximity;
import TCTStructures.NeighborHash;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author rafael
 */
public class KMeans_Parametric_SupervisedOneClass extends PrototypeBasedClusteringClassifier{
    
    private NeighborHash[][] centroids;
    
    private double[] cohesions;
    
    private NeighborHash[] adjacencyListDocTerm;
    
    private NeighborHash[] bestCentroids;
    
    double[][] docTopic;

    int numTerms;
    
    public KMeans_Parametric_SupervisedOneClass(){
        super();
        this.setK(5);
        this.setNumMaxIterations(100);
        this.setChangeRate(0);
        this.setCosine(false);
    }
    
    public void buildClassifier(Instances dataTrain){

        this.numTerms = dataTrain.numAttributes() - 1;
        int numDocs = dataTrain.numInstances();
        
        if(numDocs < this.getK()){
            this.setK(numDocs);
        }
        
        // Criando a estrutura em hash para calcular as similaridades
        adjacencyListDocTerm = new NeighborHash[numDocs];
        for(int doc=0;doc<numDocs;doc++){
            adjacencyListDocTerm[doc] = new NeighborHash();
        }
        
        // Normalizando
        double[] totalWeightDoc = new double[numDocs];
        for(int doc=0;doc<numDocs;doc++){
            for(int term=0;term<numTerms;term++){
                if(dataTrain.instance(doc).value(term) > 0){
                    totalWeightDoc[doc] += dataTrain.instance(doc).value(term);
                }    
            }
        }

        for(int doc=0;doc<numDocs;doc++){
            for(int term=0;term<numTerms;term++){
                double value = dataTrain.instance(doc).value(term);
                if(value > 0){
                //if((value > 0) && (totalWeightDoc[doc] > 0)){
                    //value = value / totalWeightDoc[doc]; // caso queira normalizar (se não é só comentar)
                    adjacencyListDocTerm[doc].AddNeighbor(term,value); 
                //}    
                }
            }
        }
        
        
        
        // Criando os centroides aleatoriamente;
        /*centroids = new NeighborHash[this.getK()];
        for(int topic=0;topic<this.getK();topic++){
            centroids[topic] = new NeighborHash();
            for(int term=0;term<numTerms;term++){
                double value = Math.random() * maxValueTerms[term];
                centroids[topic].AddNeighbor(term,value);
            }
        }*/

        ArrayList<Integer>[] docsPerTopic = new ArrayList[this.getK()];
        for(int topic=0;topic<this.getK();topic++){
            docsPerTopic[topic] = new ArrayList<Integer>();
        }
        
        
        cohesions = new double[this.getNumTrials()];
        centroids = new NeighborHash[this.getNumTrials()][this.getK()];
        
        for(int trial=0;trial<this.getNumTrials();trial++){
            Random random = new Random(trial);
            //Criando os centroides na moeda
            centroids[trial] = new NeighborHash[this.getK()];
            for(int doc=0;doc<numDocs;doc++){
                docsPerTopic[random.nextInt(this.getK())].add(doc);
            }
            computeCentroids(docsPerTopic,trial);

            docTopic = new double[numDocs][this.getK()];

            int numIt = 0;
            boolean sair = false;

            while(sair == false){

                //System.out.println("Iteration: " + (numIt + 1));

                double acmDiff = 0;


                for(int topic=0;topic<this.getK();topic++){
                    docsPerTopic[topic] = new ArrayList<Integer>();
                }

                // Calculando a proximidade de cada documento para cada centroide
                for(int doc=0;doc<numDocs;doc++){
                    double maxValue = -1;
                    int idMaxValue = -1;
                    double[] tempTopic = new double[this.getK()];
                    for(int topic=0;topic<this.getK();topic++){
                        double prox = 0;
                        if(this.isCosine()){
                            prox = Proximity.computeCosine(adjacencyListDocTerm[doc],centroids[trial][topic]);
                        }else if(this.isPearson()){
                            prox = Proximity.computePearson(adjacencyListDocTerm[doc],centroids[trial][topic]);
                        }else{
                            prox = Proximity.computeEuclidean(adjacencyListDocTerm[doc],centroids[trial][topic]);
                        }
                        
                        //
                        if(prox > maxValue){
                            maxValue = prox;
                            idMaxValue = topic;
                        }
                    }

                    tempTopic[idMaxValue] = 1;
                    docsPerTopic[idMaxValue].add(doc);
                    for(int topic=0;topic<this.getK();topic++){
                        if(Math.abs(tempTopic[topic] - docTopic[doc][topic]) == 1){
                            acmDiff++;
                        }
                        docTopic[doc][topic] = tempTopic[topic];
                    }
                }

                acmDiff = (acmDiff / (double)numDocs) * 100;

                //System.out.println("- % of changes: " + acmDiff);

                // Recalculando os centroides;
                computeCentroids(docsPerTopic,trial);
                for(int topic=0;topic<this.getK();topic++){
                    // Frequencia total dos termos dos documentos de um determinado tópico
                    double[] avgTerms = new double[numTerms];
                    for(int doc=0;doc<docsPerTopic[topic].size();doc++){
                        int idDoc = docsPerTopic[topic].get(doc);
                        HashMap<Integer,Double> neighbors =  adjacencyListDocTerm[idDoc].getNeighbors();
                        Object[] keys = neighbors.keySet().toArray();
                        for(int term=0;term<keys.length;term++){
                            int idTerm = (Integer)keys[term];
                            avgTerms[idTerm] += neighbors.get(idTerm);
                        }
                    }

                    //Tirando a média das frequências dos termos por tópico;
                    for(int term=0;term<numTerms;term++){
                        if(docsPerTopic[topic].size() == 0){
                            avgTerms[term] = 0;
                        }else{
                            avgTerms[term] = (double)avgTerms[term] / (double)docsPerTopic[topic].size();
                        }
                    }

                    //Criando o novo centróide do tópico
                    centroids[trial][topic] = new NeighborHash();
                    if(this.isCosine()){
                        for(int term=0;term<numTerms;term++){
                            if(avgTerms[term] > 0){
                                centroids[trial][topic].AddNeighbor(term,avgTerms[term]);
                            }
                        }
                    }else{
                        for(int term=0;term<numTerms;term++){
                            centroids[trial][topic].AddNeighbor(term,avgTerms[term]);
                        }
                    }
                    

                }

                // Calculando a cohesão -- Não interfere no processo de agrupamento -- pode ser comentada
                double cohesion = 0;
                for(int topic=0;topic<this.getK();topic++){
                    for(int doc=0;doc<docsPerTopic[topic].size();doc++){
                        int idDoc = docsPerTopic[topic].get(doc);
                        double prox = 0;
                        if(this.isCosine()){
                            prox = Proximity.computeCosine(adjacencyListDocTerm[doc],centroids[trial][topic]);
                        }else if(this.isPearson()){
                            prox = Proximity.computePearson(adjacencyListDocTerm[doc],centroids[trial][topic]);
                        }else{
                            prox = Proximity.computeEuclidean(adjacencyListDocTerm[doc],centroids[trial][topic]);
                        }
                        cohesion += prox;
                    }
                }
                //System.out.println("- Total Cohesion: " + cohesion);

                if(acmDiff <= this.getChangeRate()){
                    sair = true;
                }

                numIt++;
                if(numIt >= this.getNumMaxIterations()){
                    sair = true;
                    
                }
                
                if(sair == true){
                    cohesions[trial] = cohesion;
                    //System.out.println("Aqui - " + cohesion);
                }
                
            }
        }
        
        //Getting the best centroids
        double maxCohesion = Double.MIN_VALUE;
        int idMaxCohesion = 0;
        for(int trial=0;trial<this.getNumTrials();trial++){
            if(cohesions[trial] > maxCohesion){
                maxCohesion = cohesions[trial];
                idMaxCohesion = trial;
            }
        }
        
        bestCentroids = centroids[idMaxCohesion];
        
    }

    public void computeCentroids(ArrayList<Integer>[] docsPerTopic, int trial){
        
        // Recalculando os centroides;
        for(int topic=0;topic<this.getK();topic++){
            // Frequencia total dos termos dos documentos de um determinado tópico
            double[] avgTerms = new double[numTerms];
            for(int doc=0;doc<docsPerTopic[topic].size();doc++){
                int idDoc = docsPerTopic[topic].get(doc);
                HashMap<Integer,Double> neighbors =  adjacencyListDocTerm[idDoc].getNeighbors();
                Object[] keys = neighbors.keySet().toArray();
                for(int term=0;term<keys.length;term++){
                    int idTerm = (Integer)keys[term];
                    avgTerms[idTerm] += neighbors.get(idTerm);
                }
            }

            //Tirando a média das frequências dos termos por tópico;
            for(int term=0;term<numTerms;term++){
                if(docsPerTopic[topic].size() == 0){
                    avgTerms[term] = 0;
                }else{
                    avgTerms[term] = (double)avgTerms[term] / (double)docsPerTopic[topic].size();
                }
            }

            //Criando o novo centróide do tópico
            centroids[trial][topic] = new NeighborHash();
            if(this.isCosine()){
                for(int term=0;term<numTerms;term++){
                    if(avgTerms[term] > 0){
                        centroids[trial][topic].AddNeighbor(term,avgTerms[term]);
                    }
                }    
            }else{
                for(int term=0;term<numTerms;term++){
                        centroids[trial][topic].AddNeighbor(term,avgTerms[term]);
                }
            }    
            
            /*if(centroids[trial][topic].getNeighbors().size() < numTerms){
                System.out.println("Aqui kcta 3");
            }*/

        }
        
    }
   
    
    @Override
    public double getScore(Instance test) {
        
        NeighborHash newInstance = new NeighborHash();
        NeighborHash testInstance = new NeighborHash();
        
        for(int term=0;term<numTerms;term++){
            double value = test.value(term);
            if((value > 0)){
                newInstance.AddNeighbor(term,value); 
            }    
        }
        
        //Parei aqui
        double maxValue = -1;
        for(int topic=0;topic<this.getK();topic++){
            double prox = 0;
            if(this.isCosine()){
                prox = Proximity.computeCosine(newInstance,bestCentroids[topic]);
            }else if(this.isPearson()){
                prox = Proximity.computePearson(newInstance,bestCentroids[topic]);
            }else{
                prox = Proximity.computeEuclidean(newInstance,bestCentroids[topic]);
            }
            //double prox = Proximity.ComputePearson(newInstance,bestCentroids[topic]);
            if(prox > maxValue){
                maxValue = prox;
            }
        }
        
        return maxValue;
        
    }
    
 
    public NeighborHash[] getAdjacencyListDocTerm(){
        return this.adjacencyListDocTerm;
    }

    public NeighborHash[][] getCentroids() {
        return centroids;
    }

    public void setCentroids(NeighborHash[][] centroids) {
        this.centroids = centroids;
    }

    public double[] getCohesions() {
        return cohesions;
    }

    public void setCohesions(double[] cohesions) {
        this.cohesions = cohesions;
    }

    public NeighborHash[] getBestCentroids() {
        return bestCentroids;
    }

    public void setBestCentroids(NeighborHash[] bestCentroids) {
        this.bestCentroids = bestCentroids;
    }

    public double[][] getDocTopic() {
        return docTopic;
    }

    public void setDocTopic(double[][] docTopic) {
        this.docTopic = docTopic;
    }

    public int getNumTerms() {
        return numTerms;
    }

    public void setNumTerms(int numTerms) {
        this.numTerms = numTerms;
    }

    
    
}
