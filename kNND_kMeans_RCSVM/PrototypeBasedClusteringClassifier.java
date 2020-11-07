/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TCTAlgorithms.InductiveSupervisedOneClass;

import weka.core.Instance;


public abstract class PrototypeBasedClusteringClassifier extends Clustering{

    private int k; // K number
    private int numMaxIterations; // Maximum number of Iterations
    private double convergence; 
    private double changeRate; 
    private int numTrials;
    private boolean cosine;
    private boolean pearson;
    private boolean euclidean;

    public PrototypeBasedClusteringClassifier(){
        this.setK(5);
        this.setNumMaxIterations(100);
        this.setChangeRate(0.0);
        this.setNumTrials(10);
        this.setCosine(true);
    }

    public boolean isPearson() {
        return pearson;
    }

    public void setPearson(boolean pearson) {
        this.pearson = pearson;
    }

    public boolean isEuclidean() {
        return euclidean;
    }

    public void setEuclidean(boolean euclidean) {
        this.euclidean = euclidean;
    }
    
    public int getNumTrials() {
        return numTrials;
    }

    public void setNumTrials(int numTrials) {
        this.numTrials = numTrials;
    }
    
    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getNumMaxIterations() {
        return numMaxIterations;
    }

    public void setNumMaxIterations(int numMaxIterations) {
        this.numMaxIterations = numMaxIterations;
    }

    public double getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(double changeRate) {
        this.changeRate = changeRate;
    }
    
    public boolean isCosine() {
        return cosine;
    }

    public void setCosine(boolean cosine) {
        this.cosine = cosine;
    }

    public double getConvergence() {
        return convergence;
    }

    public void setConvergence(double convergence) {
        this.convergence = convergence;
    }
    
    
}
