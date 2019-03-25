/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.annotations;

import sandbox.clustering.Datapoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import sandbox.clustering.Dataset;
import java.util.HashSet;
import util.util;

/**
 *
 * @author Nikolay
 */
public class Annotation {

    private final String annotationName;
    private final HashMap<String, int[]> hmProfilesForTerm;
    private Dataset dataset;
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public Annotation replaceTerms(HashMap<String, String> termMap, String name) {
        Annotation res = new Annotation(dataset, name);
        for (String s : termMap.keySet()) {
            res.addTerm(this.getDpIDsForTerm(s), termMap.get(s));
        }
        return res;
    }

    public int getID() {
        return id;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    @Override
    public String toString() {
        return (annotationName == null) ? "<null>" : annotationName;
    }

    @Override
    public Object clone() {
        Annotation res = new Annotation(dataset, annotationName);
        for (String s : this.getTerms()) {
            res.addTerm(this.getDpIDsForTerm(s), s);
        }
        return res;
    }

    public Annotation(Dataset nd, String annotationName) {
        this.annotationName = annotationName;
        hmProfilesForTerm = new HashMap<>();
        this.dataset = nd;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setID(int id) {
        this.id = id;
    }

    public Dataset getBaseDataset() {
        return dataset;
    }

    public static Annotation merge(Annotation ann1, Annotation ann2, String newAnnotationName) {
        if (!ann1.getBaseDataset().equals(ann2.getBaseDataset())) {
            throw new IllegalArgumentException("The annotations belong to different datasets. \n" + ann1 + ":" + ann1.getBaseDataset() + "\n" + ann2 + ":" + ann2.getBaseDataset());
        }
        Annotation res = new Annotation(ann1.getBaseDataset(), newAnnotationName);
        for (String s : ann1.getTerms()) {
            res.addTerm(ann1.getDpIDsForTerm(s), "[" + ann1.getAnnotationName() + "]: " + s);
        }
        for (String s : ann2.getTerms()) {
            res.addTerm(ann2.getDpIDsForTerm(s), "[" + ann2.getAnnotationName() + "]: " + s);
        }
        return res;
    }

    public int[] getDpIDsForTerm(String term) {
        return hmProfilesForTerm.get(term);
    }

    public String[] getTermsForDpID(int dpID) {
        ArrayList<String> al = new ArrayList<>();
        for (String t : hmProfilesForTerm.keySet()) {
            if (Arrays.binarySearch(hmProfilesForTerm.get(t), dpID) >= 0) {
                al.add(t);
            }
        }
        return al.toArray(new String[al.size()]);

    }

    public void addTerm(List<Integer> dpIDs, String term) {
        int[] st = new int[dpIDs.size()];
        Integer [] arr = dpIDs.toArray(new Integer[dpIDs.size()]);
        for (int i = 0; i < st.length; i++) {
            st[i] = arr[i];
        }
        Arrays.sort(st);
        addTerm(st, term);
    }

    public void addTerm(int[] dpIDs, String term) {
        if (hmProfilesForTerm.get(term) != null) {
            int[] existing = hmProfilesForTerm.get(term);
            HashSet<Integer> hs = new HashSet<>(dpIDs.length + existing.length);
            for (int i : dpIDs) {
                hs.add(i);
            }

            for (int i : existing) {
                hs.add(i);
            }

            int[] out = new int[hs.size()];
            int k = 0;
            for (int i : hs) {
                out[k++] = i;
            }
            hmProfilesForTerm.put(term, out);
        } else {
            dpIDs = Arrays.copyOf(dpIDs, dpIDs.length);
            Arrays.sort(dpIDs);
            hmProfilesForTerm.put(term, dpIDs);
        }
    }

    public boolean containsAnnotationPair(String term, int pid) {
        return Arrays.binarySearch(hmProfilesForTerm.get(term), pid) >= 0;
    }

    public String[] getTerms() {
        return hmProfilesForTerm.keySet().toArray(new String[hmProfilesForTerm.size()]);
    }

    public enum EntityType {
        GENES, OLIGOS, CHEMICALS, CUSTOM, DRUGS
    }

    byte[][] getAnnotationVectors(Datapoint[] dp) {
        HashSet<String> termsToUse = new HashSet<>();
        for (Datapoint d : dp) {
            termsToUse.addAll(Arrays.asList(getTermsForDpID(d.getID())));
        }
        HashMap<Integer, Integer> hmIdx = new HashMap<>();
        for (int i = 0; i < dp.length; i++) {
            hmIdx.put(dp[i].getID(), i);
        }
        String[] terms = termsToUse.toArray(new String[termsToUse.size()]);
        byte[][] mtx = new byte[dp.length][termsToUse.size()];
        for (int k = 0; k > terms.length; k++) {
            for (int d : getDpIDsForTerm(terms[k])) {
                mtx[hmIdx.get(d)][k] = 1;
            }
        }
        return mtx;
    }

}
