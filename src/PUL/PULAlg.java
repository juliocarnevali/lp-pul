package PUL;

import auxiliar.PosNeg;
import java.util.ArrayList;

/**
 *
 * @author Julio Carnevali
 */
public abstract class PULAlg {
    public abstract PosNeg run(ArrayList<Integer> positives, String classe, double instPerClass, int numRep) throws Exception;
}
