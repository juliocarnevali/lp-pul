
package arff_reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author julio
 */
public class ArffReader {
    final String RELATION = "@RELATION ";
    final String ATTRIBUTE = "@ATTRIBUTE ";
    final String DATA = "@DATA";
    final String RELATION2 = "@relation ";
    final String ATTRIBUTE2 = "@attribute ";
    final String DATA2 = "@data";
    
    public String relation;
    public ArrayList<String> attributes = new ArrayList<String>();
    public ArrayList<ArrayList<String>> attributesType = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    public ArrayList<HashMap<Integer,Double>> dataHM = new ArrayList();
    public ArrayList<String> classes = new ArrayList();
    
    public ArffReader(String fileName, boolean isHM) throws Exception{    
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        
        Boolean isDataStarted = false;
        String line = br.readLine();
        String[] aux;
        
        while(line != null){
            if(!line.isEmpty() && line.charAt(0) != '%'){
                if(line.startsWith(RELATION) || line.startsWith(RELATION2)){
                    relation = line.replace(RELATION, "");
                    relation = line.replace(RELATION2, "");
                }
                if(line.startsWith(ATTRIBUTE) || line.startsWith(ATTRIBUTE2)){
                    line = line.replace(ATTRIBUTE, "");
                    line = line.replace(ATTRIBUTE2, "");
                    aux = line.split("\t");
                    if(aux.length == 1)
                        aux = line.split(" ");
                    
                    for(int i = 0; i < aux.length; i++){
                        aux[i] = aux[i].trim();
                    }
                    
                    attributes.add(aux[0]);
                    ArrayList<String> al = new ArrayList();
                    if(aux[1].contains(",")){
                        aux[1] = aux[1].replace("{", "");
                        aux[1] = aux[1].replace("}", "");
                        aux = aux[1].split(",");
                        
                        for(int i = 0; i < aux.length; i++){
                            al.add(aux[i]);
                        }
                        
                    }else{
                        al.add(aux[1]);
                    }
                    attributesType.add(al);
                }
                
                if(isDataStarted){
                    ArrayList<String> al = new ArrayList();
                    HashMap<Integer,Double> hm = new HashMap();
                    
                    line = line.replace("{", "");
                    line = line.replace("}", "");
                    aux = line.split(",");
                    
                    for(int i = 0; i < aux.length-1; i++){
                        if(isHM){
                            String[] aux2 = aux[i].split(" ");
                            if(aux2.length > 1 && Double.valueOf(aux2[1]) > 0.0)
                                hm.put(Integer.valueOf(aux2[0]), Double.valueOf(aux2[1]));
                        }else{
                            al.add(aux[i]);
                        }
                    }
                    
                    if(isHM){
                        dataHM.add(hm);
                    }else{
                        data.add(al);
                    }
                    
                    classes.add(aux[aux.length-1].trim());
                }
                
                if(line.equals(DATA) || line.equals(DATA2)){
                    isDataStarted = true;
                }
            }
            
            line = br.readLine();
        }
    }
   
}
