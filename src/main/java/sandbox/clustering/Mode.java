/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import cern.colt.Arrays;
import java.io.Serializable;
import util.logger;

/**
 *
 * @author Nikolay
 */
public class Mode implements Serializable {

    private static final long serialVersionUID = 1L;
    private Cluster cluster;
    private double[] mainVector;
    private double[] sideVector;

    private static boolean useMedian = false;

    public static boolean isUseMedian() {
        return useMedian;
    }

    public static void setUseMedian(boolean useMedian) {
        Mode.useMedian = useMedian;
    }

    public Mode(Cluster cl, double[] vector, double[] sideVector) {
        this.cluster = cl;
        this.mainVector = vector;
        this.sideVector = sideVector;
    }

    public void setVector(double[] vector) {
        this.mainVector = vector;
    }

    public double[] getSideVector() {
        if (sideVector == null) {
            return new double[0];
        }
        return sideVector;
    }

    public double[] getVector() {
        if (useMedian) {
            try {
                mainVector = cluster.getMedianFeatureVec();
            } catch (Exception e) {
                logger.showException(e);
            }
        }
        return mainVector;
    }

    public void setSideVector(double[] sideVector) {
        this.sideVector = sideVector;
    }

    @Override
    public String toString() {
        String s = Arrays.toString(mainVector);
        return getCluster().getCaption() + "," + s.replace("[", "").replace("]", "");
    }

    public Cluster getCluster() {
        return cluster;
    }
}
