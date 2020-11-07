package TCTAlgorithms.InductiveSupervisedOneClass;

import TCTNetworkGeneration.Proximity;
import TCTStructures.IndexValue;
import TCTStructures.NeighborHash;
import java.util.ArrayList;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Marcos Gôlo
 */
public abstract class KNN_BasedSupervisedOneClass extends OneClassSupervisedClassifier {

    private int k;

    private int numTrain; // Number of labeled documents
    private int numTerms; // Number of terms

    NeighborHash[] adjancecyListDocTerm; //Hash structure to store da datase and speed up cosine similarity computation

    @Override
    public void buildClassifier(Instances dataTrain) {
        this.numTrain = dataTrain.numInstances();
        this.numTerms = dataTrain.numAttributes() - 1;

        if(this.getK() > numTrain){
            setK(numTrain);
        }
        
        adjancecyListDocTerm = new NeighborHash[numTrain];
        for (int inst = 0; inst < numTrain; inst++) {
            adjancecyListDocTerm[inst] = new NeighborHash();
        }

        for (int inst = 0; inst < numTrain; inst++) {
            for (int term = 0; term < numTerms; term++) {
                if (dataTrain.instance(inst).value(term) > 0) {
                    adjancecyListDocTerm[inst].AddNeighbor(term, dataTrain.instance(inst).value(term));
                }
            }
        }
    }

    public ArrayList<IndexValue> calculaSimilaridade(NeighborHash newInstance) {
        ArrayList<IndexValue> indexDensity = new ArrayList<IndexValue>();
        for (int inst = 0; inst < getNumTrain(); inst++) {
            double cosine = Proximity.computeCosine(adjancecyListDocTerm[inst], newInstance);
            IndexValue indDensi = new IndexValue(inst, cosine);
            addMedida(indexDensity, k, indDensi);
        }
        return indexDensity;
    }

    private void addMedida(ArrayList<IndexValue> array, int k, IndexValue indDensi) {
        if (array.size() == 0) {
            array.add(indDensi);
        } else {
            int i = 0;
            while (i < array.size() && indDensi.getValue() > array.get(i).getValue()) {
                i++;
            }
            array.add(i, indDensi);
            if (array.size() == k + 1) {
                array.remove(0);
            }
        }
    }

    public NeighborHash newInstance(Instance dataTest) {
        NeighborHash newInstance = new NeighborHash();
        for (int term = 0; term < getNumTerms(); term++) {
            if (dataTest.value(term) > 0) {
                newInstance.AddNeighbor(term, dataTest.value(term));
            }
        }
        return newInstance;
    }

    public double sumDensity(ArrayList<IndexValue> indexDensity) {
        double soma = 0.0;
        for (int i = 0; i < indexDensity.size(); i++) {
            soma += indexDensity.get(i).getValue();
        }
        return soma;
    }

    public int getK() {
        return k;
    }

    public int getNumTrain() {
        return numTrain;
    }

    public int getNumTerms() {
        return numTerms;
    }
    
    public void setK(int k){
        this.k = k;
    }

    @Override
    public abstract double getScore(Instance test);
    
}