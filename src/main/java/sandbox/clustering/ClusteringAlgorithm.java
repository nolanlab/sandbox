/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import sandbox.clustering.DistanceMeasure;
import sandbox.clustering.ClusterSet;
import javax.swing.JPanel;

/**
 *
 * @author Nikolay
 */
public abstract class ClusteringAlgorithm {

    public abstract ClusterSet[] doBatchClustering(Dataset ds, String comment);

    public abstract JPanel getParamControlPanel();

    public abstract String getAlgorithmName();

    public abstract String getAlgorithmDescription();

    public abstract boolean isAlgorithmPublic();
    protected DistanceMeasure dm;

    public ClusteringAlgorithm(DistanceMeasure dm) {
        this.dm = dm;
    }

    @Override
    public String toString() {
        return getAlgorithmName();
    }
}
