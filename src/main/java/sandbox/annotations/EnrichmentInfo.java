/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.annotations;

import java.util.ArrayList;
import samusik.objecttable.AbstractRowRepresentable;

/**
 *
 * @author Nikolay
 */
public class EnrichmentInfo extends AbstractRowRepresentable {

    public final String term;
    public final int count;
    public final int total;
    public double enrichment;
    public final  int [] annotatedItems;

    @Override
    protected void clearHeaderAndData() {
        return;
    }

    @Override
    public void updateValue(int col, Object value) {
        return;
    }
    private static final String[] headerRow = new String[]{"term", "Count Sample", "Count Total", "P-Value"};
    private static final boolean[] edMask = new boolean[]{false, false, false, false, false};

    @Override
    public String[] getHeaderRow() {
        return headerRow;
    }

    @Override
    public Object[] toRow() {
        return new Object[]{
            term, count, total, enrichment
        };
    }

    @Override
    public boolean[] getEditibilityMask() {
        return edMask;
    }

    public EnrichmentInfo(String term, int count, int total, double enrichment, int[] annotatedItems) {
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
