/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TCTAlgorithms.InductiveSupervisedOneClass;

import TCTStructures.IndexValue;
import TCTStructures.NeighborHash;
import java.util.ArrayList;
import weka.core.Instance;

/**
 *
 * @author Marcos Gôlo
 */
public class KNNDensity_SupervisedOneClass extends KNN_BasedSupervisedOneClass{
    
    @Override
    public double classifyInstance(Instance test){
        
        //System.out.println("Classe: " + dataTest.classValue());
        //System.out.println("Desity: " + densityX);
        double score = getScore(test);
        if(score >getThreshold()){
            return 0;
        }else{
            return 1;
        }
        
    }

    @Override
    public double getScore(Instance test) {
        
        
        NeighborHash ninst = newInstance(test);
        ArrayList<IndexValue> indexDensity = calculaSimilaridade(ninst);
        double score  = sumDensity(indexDensity);
        score=score/getK();
        
        return score;
        
    }
    
    
    
    
}
