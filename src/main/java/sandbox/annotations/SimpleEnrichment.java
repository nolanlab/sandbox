/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import util.HypergeometricDist;

/**
 *
 * @author Nikolay
 */
public class SimpleEnrichment {

    public static EnrichmentInfo getEnrichment(int[] population, int[] sample, int[] annotation, String annotationName) {

        

        int countSuccessPop = 0;
        int countPop = population.length;
        int countSample = sample.length;
        
        Arrays.sort(population);
        Arrays.sort(sample);
        Arrays.sort(annotation);
        
        int countSuccessSample = 0;

        for (int s : sample) {
            if (Arrays.binarySearch (annotation, s) >= 0) {
                countSuccessSample++;
            }
        }

        for (int s : population) {
            if (Arrays.binarySearch (annotation, s) >= 0) {
                countSuccessPop++;
            }
        }

        double pVal = HypergeometricDist.pValHyperGeom(countPop, countSuccessPop, countSample, countSuccessSample);

        if (countSuccessSample == 0) {
            pVal = 0.5;
        }

        EnrichmentInfo ei = new EnrichmentInfo(annotationName, countSuccessSample, countSuccessPop, pVal, new int[0]);

        return ei;
    }
}
