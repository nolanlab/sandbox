/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import sandbox.clustering.Cluster;

/**
 *
 * @author Nikolay
 */
public class ClusterWrapper {

    Cluster cl;

    public ClusterWrapper(Cluster cl) {
        this.cl = cl;
    }

    public Cluster getCluster() {
        return cl;
    }

    @Override
    public String toString() {
        return (cl.getCaption().length() < 2) ? String.valueOf(cl.getID()) : String.valueOf(cl.getID()) + ", " + cl.getCaption();
    }
}
