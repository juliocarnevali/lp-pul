/************************************
* Instructions:                     *
*  - Use the "setConf" function to  *
* set the configuration of the alg. *
*  - Then use the "run" function in *
* order to get the positive and     *
* negative documents.               *
************************************/

package PUL.PEPUC;

import PUL.PULAlg;
import auxiliar.PosNeg;
import auxiliar.IndexValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Julio Carnevali
 */
public class PEPUC extends PULAlg{
    public ArrayList<HashMap<Integer,Double>> dataOriginal;//HashMap<index-of-term,value>
    public int qttAttrOriginal;
    
    public String dataset;
    public int m;
    public double alpha;
    public double lambda;
    public boolean isEM;
    public double em;
    public boolean isCosine;
    
    public boolean last;
    public NaiveBayes nbAlg;
    public EM emAlg;
    
    public HashMap<String,RPRN> hashRPRN;
    public PEPUC(){
        hashRPRN = new HashMap();
        this.dataset = "";
    }
    
    public void setConf(String dataset, ArrayList<HashMap<Integer,Double>> dataOriginal,
                        int qttAttrOriginal, int m, 
                        double lambda, double alpha, boolean isEM, double em, boolean isCosine){
        if(!this.dataset.equals(dataset)){
            this.dataset = dataset;
            hashRPRN = new HashMap();
        }
        this.dataOriginal = dataOriginal;
        this.qttAttrOriginal = qttAttrOriginal;
        this.lambda = lambda;
        this.alpha = alpha;
        this.m = m;
        this.isEM = isEM;
        this.em = em;
        
        this.isCosine = isCosine;
    }
    
    @Override
    public PosNeg run(ArrayList<Integer> positives, String classe, double instPerClass, int numRep) throws Exception{
        ArrayList<Integer> newPos;
        ArrayList<Integer> rn;
        
        String s = lambda+"-"+alpha+"-"+m+"-"+classe+"-"+instPerClass+"-"+numRep;
        if(!hashRPRN.containsKey(s)){
            long t0 = System.currentTimeMillis();
            ArrayList<Integer> PandU = new ArrayList();
            for(int i = 0; i < dataOriginal.size(); i++)
                PandU.add(i);

            newPos = repeatedExtraction(positives,PandU);
            HashMap<Integer,String> classOfUnlabeled = extractRN(PandU, newPos);
            rn = new ArrayList();
            for(int i : classOfUnlabeled.keySet()){
                if(classOfUnlabeled.get(i).equals("-1"))
                    rn.add(i);
            }
            saveOnhashRPRN(s, newPos, rn);
        }else{
            newPos = (ArrayList) hashRPRN.get(s).PandRP.clone();
            rn = (ArrayList) hashRPRN.get(s).RN.clone();
        }
        
        PosNeg pn = new PosNeg();
        pn.negativesExtracted = (ArrayList)rn.clone();
        
        if(isEM){
            pn.positives = finalClassifierEM(newPos, rn);
        }else{
            pn.positives = finalClassifierNB(newPos, rn);
        }
        
        return pn;
        
    }
    
    public synchronized void saveOnhashRPRN(String s, ArrayList<Integer> newPos, ArrayList<Integer> rn){
        RPRN rr = new RPRN();
        rr.PandRP = (ArrayList)newPos.clone();
        rr.RN = (ArrayList)rn.clone();
        hashRPRN.put(s, rr);
    }
    
    public ArrayList<Integer> finalClassifierEM(ArrayList<Integer> newPos, ArrayList<Integer> rn){
        
        ArrayList<HashMap<Integer,Double>> trainSet = new ArrayList();
        ArrayList<Integer> clas = new ArrayList();
        
        ArrayList<HashMap<Integer,Double>> testSet = new ArrayList();
        
        for(int i : newPos){
            trainSet.add(dataOriginal.get(i));
            clas.add(1);
        }
        for(int i : rn){
            trainSet.add(dataOriginal.get(i));
            clas.add(0);
        }
        
        for(int i = 0; i < dataOriginal.size(); i++){
            if(!trainSet.contains(i)){
                testSet.add(dataOriginal.get(i));
            }
        }
        
        EM emAlg = new EM();
        emAlg.setWeightUnlabeled(em);
        emAlg.buildClassifier(trainSet, testSet, clas, qttAttrOriginal, 2);
        
        boolean last = true;
        
        ArrayList<Integer> extractedPos = new ArrayList();
        for(int i = 0; i < dataOriginal.size(); i++){
            double[] f = emAlg.label(dataOriginal.get(i), last);
            if(f[1] > f[0])
                extractedPos.add(i);
        }
        this.emAlg = emAlg;
        this.last = last;
        return extractedPos;
    }
    
    public ArrayList<Integer> finalClassifierEMChosingBetterOne(ArrayList<Integer> newPos, ArrayList<Integer> rn){
        
        ArrayList<HashMap<Integer,Double>> trainSet = new ArrayList();
        ArrayList<Integer> clas = new ArrayList();
        
        ArrayList<HashMap<Integer,Double>> testSetForEM = new ArrayList();
        ArrayList<Integer> clasTestForEM = new ArrayList();
        
        ArrayList<HashMap<Integer,Double>> testSet = new ArrayList();
        
        Random ra = new Random(newPos.size());
        Collections.shuffle(newPos,ra);
        Collections.shuffle(rn,ra);
        int counter = 0;
        for(int i : newPos){
            if(counter < 0.7 * newPos.size()){
                trainSet.add(dataOriginal.get(i));
                clas.add(1);
            }else{
                testSetForEM.add(dataOriginal.get(i));
                clasTestForEM.add(1);
            }
            counter++;
        }
        int qttOfPosToTest = testSetForEM.size();
        counter = 0;
        for(int i : rn){
            if(counter < 0.7 * rn.size()){
                trainSet.add(dataOriginal.get(i));
                clas.add(0);
            }else{
                testSetForEM.add(dataOriginal.get(i));
                clasTestForEM.add(0);
            }
            counter++;
        }
        
        for(int i = 0; i < dataOriginal.size(); i++){
            if(!trainSet.contains(i)){
                testSet.add(dataOriginal.get(i));
            }
        }
        
        EM emAlg = new EM();
        emAlg.setWeightUnlabeled(em);
        emAlg.buildClassifier(trainSet, testSet, clas, qttAttrOriginal, 2);
        
        //getting the best NB classifier (first or last)
        boolean last = true;
        
        if(getF1ForTestSet(emAlg,testSetForEM, clasTestForEM, qttOfPosToTest, false) > 
           getF1ForTestSet(emAlg,testSetForEM, clasTestForEM, qttOfPosToTest, true))
            last = false;
        
        ArrayList<Integer> extractedPos = new ArrayList();
        for(int i = 0; i < dataOriginal.size(); i++){
            double[] f = emAlg.label(dataOriginal.get(i), last);
            if(f[1] > f[0])
                extractedPos.add(i);
        }
        this.emAlg = emAlg;
        this.last = last;
        return extractedPos;
    }
    
    public double getF1ForTestSet(EM emAlg, ArrayList<HashMap<Integer,Double>> testSetForEM, 
                                  ArrayList<Integer> clasTestForEM, int qttOfPosToTest, boolean last){
        int posOK = 0;
        int posLabeledByMe = 0;
        for(int i = 0; i < testSetForEM.size(); i++){
            HashMap<Integer,Double> query = testSetForEM.get(i);
            double[] f = emAlg.label(query, last);
            if(f[1] > f[0]){
                posLabeledByMe++;
                if(clasTestForEM.get(i) == 1)
                    posOK++;
            }
        }
        double precision = 0.0;
        double recall = 0.0;
        if(posLabeledByMe > 0)
            precision = (double)posOK / posLabeledByMe;
        if(qttOfPosToTest > 0)
            recall = (double)posOK / qttOfPosToTest;
        
        return ((double)2 * ((precision*recall) / (precision+recall)));
    }
    
    public ArrayList<Integer> finalClassifierNB(ArrayList<Integer> newPos, ArrayList<Integer> rn){
        
        ArrayList<HashMap<Integer,Double>> trainSet = new ArrayList();
        ArrayList<String> clas = new ArrayList();
        for(int i : newPos){
            trainSet.add(dataOriginal.get(i));
            clas.add("1");
        }
        for(int i : rn){
            trainSet.add(dataOriginal.get(i));
            clas.add("-1");
        }
        
        NaiveBayes nbAlg = new NaiveBayes();
        nbAlg.train(trainSet, clas, qttAttrOriginal);
        
        ArrayList<Integer> extractedPos = new ArrayList();
        for(int i = 0; i < dataOriginal.size(); i++){
            HashMap<String,Double> hm = nbAlg.execute(dataOriginal.get(i));
            String c = "";
            double bigger = Double.MIN_VALUE;
            if(hm.containsValue(Double.NaN)){
                Random r = new Random(i);
                c = (String) new ArrayList(hm.keySet()).get(r.nextInt(hm.size()));
            }else{
                for(String s : hm.keySet()){
                    if(hm.get(s) > bigger){
                        bigger = hm.get(s);
                        c = s;
                    }
                }
            }
            if(c.equals("1"))
                extractedPos.add(i);
        }
        this.nbAlg = nbAlg;
        return extractedPos;
    }
    
    
    public ArrayList<Integer> repeatedExtraction(ArrayList<Integer> positives, ArrayList<Integer> PandU) throws Exception{
        ArrayList<Integer> newPos = (ArrayList)positives.clone();
        int qttRP = newPos.size();
        for(int i = 0; i < m; i++){
            HashMap<Integer,String> classOfUnlabeled = extractRN(PandU, newPos);
            ArrayList<Integer> rp = enlargeP(classOfUnlabeled, newPos, lambda/(double)m);
            newPos.addAll(rp);
        }
        qttRP = newPos.size() - qttRP;
        return newPos;
    }
    
    public ArrayList<Integer> enlargeP(HashMap <Integer,String> classOfUnlabeled, ArrayList<Integer> positives, double lambda) throws Exception{
        ArrayList<Integer> randomP = new ArrayList();
        ArrayList<Integer> pos = (ArrayList)positives.clone();
        
        Random random = new Random(pos.size());
        
        Collections.shuffle(pos, random);
        if(pos.size() > 1)
            randomP = new ArrayList(pos.subList(0, (int)Math.ceil((double)pos.size()/2.0)));
        else
            randomP = (ArrayList)pos.clone();
        
        ArrayList<Integer> net = new ArrayList((ArrayList)randomP.clone());
        int qttOfRN = net.size();
        for(int i : classOfUnlabeled.keySet()){
            if(classOfUnlabeled.get(i).equals("1"))
                net.add(i);
        }
        qttOfRN = net.size()-qttOfRN;
        
        ArrayList<Integer> pl = new ArrayList();
        for(int i : randomP)
            pl.add(net.indexOf(i));
        
        ArrayList<HashMap<Integer,Double>> adjMatrix = new ArrayList();
        
        for(int i = 0; i < net.size(); i++){
            adjMatrix.add(new HashMap());
            for(int j = 0; j < net.size(); j++){
                if(i != j)
                    adjMatrix.get(i).put(j, Math.exp(-(l2Norm(net.get(i), net.get(j)))/2));
            }
        }
        
        LLGC_PEPUC lp = new LLGC_PEPUC(alpha, adjMatrix, pl);
        double[] f = lp.run();
        
        ArrayList<IndexValue> ranking = new ArrayList();
        for(int i = 0; i < f.length; i++){
            if(!pl.contains(i)){
                ranking.add(new IndexValue(net.get(i),f[i]));
            }
        }
        
        ranking.sort(new Comparator<IndexValue>() {
            @Override
            public int compare(IndexValue o1, IndexValue o2) {
                if((double)((o1).value) > (double)((o2).value))
                    return -1;
                else if((double)((o1).value) < (double)((o2).value))
                    return 1;
                else return 0;
            }
        });
        
        ArrayList rp = new ArrayList();
        
        for(int i = 0; i < Math.ceil(lambda*ranking.size()); i++)
            rp.add(ranking.get(i).index);
        
        return rp;
    }

    public double l2Norm(int i, int j){
        double value = 0.0;
        HashMap<Integer,Double> hm1 = dataOriginal.get(i);
        HashMap<Integer,Double> hm2 = dataOriginal.get(j);
        
        HashSet<Integer> hs = new HashSet();
        hs.addAll(hm1.keySet());
        hs.addAll(hm2.keySet());
        
        Iterator it = hs.iterator();
        while(it.hasNext()){
            int word = (int)it.next();
            double aux = 0.0;
            if(hm1.containsKey(word))
                aux += hm1.get(word);
            if(hm2.containsKey(word))
                aux -= hm2.get(word);
            
            value += Math.pow(aux,2);     
        }
            
        value = Math.sqrt(value);
        return value;
    }
    
    public HashMap extractRN(ArrayList<Integer> PandU, ArrayList<Integer> positives){
        NaiveBayes nb = new NaiveBayes();
        
        ArrayList<String> classes = new ArrayList();
        ArrayList<Integer> U = new ArrayList();
        
        ArrayList data = new ArrayList();
        
        for(int i : PandU){
            data.add(dataOriginal.get(i));
            if(positives.contains(i)){
                classes.add("1");
            }else{
                classes.add("-1");
                U.add(i);
            }
        }
        
        nb.train(data, classes, qttAttrOriginal);
        
        //classifying
        HashMap classOfUnlabeled = new HashMap();
        for(int i : U){
            HashMap<String,Double> hm = nb.execute(dataOriginal.get(i));
            if((hm.values().contains(Double.NaN))){
                //System.out.println("entrou: "+i);
                ArrayList al = new ArrayList(hm.keySet());
                Collections.shuffle(al, new Random(i));
                classOfUnlabeled.put(i, al.get(0));
            }else{
                double d = 0.0;
                for(String s : hm.keySet()){
                    if(d < hm.get(s)){
                        d = hm.get(s);
                        classOfUnlabeled.put(i,s);
                    }
                }
            }
        }
        
        return classOfUnlabeled;
        
    }
    
    
    public class RPRN{
        public ArrayList<Integer> PandRP;
        public ArrayList<Integer> RN;
    }
}
