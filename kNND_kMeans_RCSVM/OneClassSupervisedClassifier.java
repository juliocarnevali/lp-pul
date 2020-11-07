/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TCTAlgorithms.InductiveSupervisedOneClass;

import weka.classifiers.Classifier;
import weka.core.Instance;

/**
 *
 * @author rafael
 */
public abstract class OneClassSupervisedClassifier extends Classifier{
    
    //The classifier must set 1 to a normal object and 0 to an outlier
    
    private double threshold;

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
    
    public abstract double getScore(Instance test);
    
    @Override
    public double classifyInstance(Instance test) {
        double score = getScore(test);
       
         //System.out.println("Classe: " + test.classValue());
        //System.out.println("Score: " + score);
        
        if(score >= this.getThreshold()){
            return 0;
        }else{
            return 1;
        }
        
    }
    
}
