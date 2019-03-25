/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;

/**
 *
 * @author Nikolay
 */
public class EnrichmentInfo {

    public final String term;
    public final int count;
    public final int total;
    public double enrichment;
    public final ArrayList<String> annotatedItems;

     public EnrichmentInfo(String term, int count, int total, double enrichment, ArrayList<String> annotatedItems) {
        this.term = term;
        this.count = count;
        this.total = total;
        this.enrichment = enrichment;
        this.annotatedItems = annotatedItems;
    }

    @Override
    public String toString() {
        return "Enrichment for term '" + term + ": " + count + " out of " + total + ", pVal = " + enrichment + ", annotated items: " + annotatedItems;
    }
}
