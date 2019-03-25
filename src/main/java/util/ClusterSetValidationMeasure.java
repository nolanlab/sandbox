/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import sandbox.clustering.ClusterSet;

/**
 *
 * @author Nikolay
 */
public interface ClusterSetValidationMeasure {

    public String getName();

    public double getMeasure();

    public double getPValue();

    public double[] getMeasureBounds();

    public String getDescription();

    public String getAdditionalInfo();

    public ClusterSet getClusterSet();

    public boolean showPValue();

    public double[] getHighlightThresholds();
}
