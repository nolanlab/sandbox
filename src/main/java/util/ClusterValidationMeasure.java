/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import sandbox.clustering.Cluster;

/**
 *
 * @author Nikolay
 */
public interface ClusterValidationMeasure {

    public String getName();

    public double getMeasure();

    public double getPValue();

    public double[] getMeasureBounds();

    public String getDescription();

    public String getAdditionalInfo();

    public Cluster getCluster();

    public void compute();

    public boolean isComputed();

    public boolean showPValue();

    public double[] getHighlightThresholds();
}
