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
public abstract class ClusterValidationFactory {

    public abstract ClusterValidationMeasure getClusterValidationMeasure(Cluster c);
}
