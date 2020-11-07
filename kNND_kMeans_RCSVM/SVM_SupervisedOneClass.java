/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TCTAlgorithms.InductiveSupervisedOneClass;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import wlsvm.WLSVM;

/**
 *
 * @author rafael
 */
public class SVM_SupervisedOneClass extends OneClassSupervisedClassifier{

    WLSVM  classifier;
    
    int classeReferencia;
    
    int kernelType;
    double gamma;
    double nu;
    double c;
    
    @Override
    public void buildClassifier(Instances dataTrain) throws Exception {
        
        int numTrain = dataTrain.numInstances();
        int numTerms = dataTrain.numAttributes();
        
        classeReferencia = (int)dataTrain.instance(0).classValue();
        
        classifier = new WLSVM();
        
        c = 1;
        
        String options = "-S 2 ";
        options += "-K " + kernelType + " ";
        if(kernelType == 1){
            options += "-D " + 2 + " ";
        }
        options += "-G " + gamma + " ";
        options += "-N " + nu + " ";
        options += "-C " + c + " ";
        options += "-R 0.0 -M 40.0 -E 0.001 -z -P 0.1 -seed 1";
       // String options = ( "-S 2 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1" );
        
        String[] optionsArray = options.split( " " );
        classifier.setOptions( optionsArray );
        
        /*classifier.setSVMType(2);
        classifier.setKernelType(kernelType);
        classifier.setGamma(gamma);
        classifier.setNu(nu);*/
        
        //gerando a Instancia Fictícia;
        //Acho que vou ter que copiar o dataTrain... já colocar a classe 0 nas novas instancias;
        
        // ================================================================
        // Generating a binary dataset for each class: positive label (0) for the current class and negative label (0) for others
        
        //========================
        // Create list to hold nominal values "first", "second", "third" 
        List newClassValues = new ArrayList(2); 
        newClassValues.add("normal"); 
        newClassValues.add("outlier"); 
        FastVector test = new FastVector();
        test.addElement("normal"); 
        test.addElement("outlier");
        // Create nominal attribute "position" 
        Attribute position = new Attribute("position", test);
        //============================

        
        /*Instances dataTarget = new Instances(dataTrain,0);
        Attribute featureNewClass = new Attribute("att_class", test);
        dataTarget.insertAttributeAt(featureNewClass, numTerms);
        Attribute classAtt = dataTarget.attribute(dataTarget.numAttributes()-1); //Setting the last feature as class
        dataTarget.setClass(classAtt);
        dataTarget.deleteAttributeAt(numTerms-2);

        for(int inst=0;inst<numTrain;inst++){
            Instance instance = dataTrain.instance(inst);
            Instance instanceClasse = new Instance(numTerms); 
            instanceClasse.setDataset(dataTarget);
            for(int term=0;term<numTerms-1;term++){
                instanceClasse.setValue(term, instance.value(term));
            }
            instanceClasse.setClassValue(0);
            dataTarget.add(instanceClasse);
        }*/
        
         
        
        /*instanceOutlier.insertAttributeAt(featureNewClass, numTerms);
        classAtt = dataTarget.attribute(dataTarget.numAttributes()-1); //Setting the last feature as class
        dataTarget.setClass(classAtt);
        instanceOutlier.setClassValue(1);*/
        
        
        
        // ================================================================
        
        
        
        
        
        
        /*LibSVM classifier = new LibSVM();*/
        
        //String options = ( "-S 2 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1 -seed 1" );
        
        //String[] optionsArray = options.split( " " );
        //classifier.setOptions( optionsArray );
        
        //teste.setSVMType(value);
        //classifier = new SMO();
        //classifier.setKernel(new RBFKernel());
        //classifier.setC(1);
        
        classifier.buildClassifier(dataTrain);
        //classifier.buildClassifier(dataTarget);
    }
    
    @Override
    public double getScore(Instance test) {
        try {
            return classifier.distributionForInstance(test)[0];
        } catch (Exception ex) {
            Logger.getLogger(SVM_SupervisedOneClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public double classifyInstance(Instance test){
        //double score = getScore(test);
       
        //System.out.println("Classe Ref: " + classeReferencia);
        //System.out.println("Classe Inst: " + test.classValue());
        
        try {
            //System.out.println("Classificacao: " + classifier.classifyInstance(test));
            //System.out.println("Score0: " + classifier.distributionForInstance(test)[0]);
            
           
            if(classifier.classifyInstance(test) == 1){
                return 0;
            }else{
                return 1;
            }
            
            //return classifier.classifyInstance(test);
            //return classifier.classifyInstance(test);
            /*if(classifier.distributionForInstance(test)[0] > 0){
                return 0;
            }else{
                return 1;
            }*/
        } catch (Exception ex) {
            Logger.getLogger(SVM_SupervisedOneClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 1;
        
        /*if(score >= this.getThreshold()){
            return 0;
        }else{
            return 1;
        }*/
    }

    public WLSVM getClassifier() {
        return classifier;
    }

    public void setClassifier(WLSVM classifier) {
        this.classifier = classifier;
    }

    public int getClasseReferencia() {
        return classeReferencia;
    }

    public void setClasseReferencia(int classeReferencia) {
        this.classeReferencia = classeReferencia;
    }

    public int getKernelType() {
        return kernelType;
    }

    public void setKernelType(int kernelType) {
        this.kernelType = kernelType;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getNu() {
        return nu;
    }

    public void setNu(double nu) {
        this.nu = nu;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }
    
}