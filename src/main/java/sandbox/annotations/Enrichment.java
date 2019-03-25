/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import util.HypergeometricDist;

/**
 *
 * @author Nikolay
 */
public class Enrichment {

    
    public static EnrichmentInfo[] computeEnrichment(int[] totalSet, int[] sample, Annotation ann) {
        ArrayList<EnrichmentInfo> list = new ArrayList<>();
        for (String f : ann.getTerms()) {
            int totalTermCount = 0;
            int annotatedProf[] = ann.getDpIDsForTerm(f);
            Arrays.sort(annotatedProf);
            /*
             if(f.equals("apoptosis")){
             logger.print("Profiles annotated with 'Apoptosis'");
             logger.print(Arrays.toString(annotatedProf));
             }
             * 
             */
            for (int i = 0; i < totalSet.length; i++) {
                int s = totalSet[i];
                if (Arrays.binarySearch(annotatedProf, s) >= 0) {
                    totalTermCount++;
                }
            }
            /* 
             if(f.equals("apoptosis")){
             logger.print("Total term count for apoptosis: " + totalTermCount);
             }*/

            ArrayList<Integer> annotatedItems = new ArrayList<>();
            int sampleTermCount = 0;
            for (int i = 0; i < sample.length; i++) {
                int s = sample[i];
                if (Arrays.binarySearch(annotatedProf, s) >= 0) {
                    sampleTermCount++;
                    annotatedItems.add(sample[i]);
                }
            }
            int [] out = new int [annotatedItems.size()];
            for (int i = 0; i < out.length; i++) {
                out[i] = annotatedItems.get(i);
            }
            if (sampleTermCount >= 0) {
                Double pVal = HypergeometricDist.pValHyperGeom(totalSet.length, totalTermCount, sample.length, sampleTermCount);
                list.add(new EnrichmentInfo(f, sampleTermCount, totalTermCount, pVal, out));
            }
        }

        Collections.sort(list, new Comparator<EnrichmentInfo>() {
            @Override
            public int compare(EnrichmentInfo o1, EnrichmentInfo o2) {
                return o1.term.compareTo(o2.term);
            }
        });

        return list.toArray(new EnrichmentInfo[list.size()]);
    }
}
