/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import sandbox.clustering.Cluster;
import sandbox.clustering.ClusterSet;
import sandbox.clustering.Datapoint;
import sandbox.clustering.Dataset;
import java.sql.SQLException;
import java.util.Map.Entry;

/**
 *
 * @author Nikolay
 */
public class GroupStatisticsInClusters {

    /**
     * @param groups - An array of entries, each containing the group name and
     * group members
     * @param cs - Cluster set to analyze
     * @param relative - if true, group contributions are expressed as
     * percentages of cluster size, if false - as absolute numbers
     * @return NDataset object, where each NDatapoint corresponds to one group
     *
     */
    public static Dataset getGroupStatistics(ClusterSet cs, Entry<String, Datapoint[]>[] groups, boolean relative, double euclideanLenThreshold) throws SQLException {
        Cluster[] cl = cs.getClusters();

        Dataset nd = cs.getDataset();
        Datapoint[] out = new Datapoint[groups.length];
        int dim = nd.getNumDimensions();
        for (int i = 0; i < groups.length; i++) {
            Datapoint[] groupMembers = groups[i].getValue();
            String groupName = groups[i].getKey();
            double[] vec = new double[cl.length +1];
            double unclassif = 0;
            for (int j = 0; j < cl.length; j++) {
                double d = 0;
                for (int k = 0; k < groupMembers.length; k++) {
                    if (cl[j].containsDpID(groupMembers[k].getID()) ) {
                       if(MatrixOp.lenght(groupMembers[k].getVector())>euclideanLenThreshold){
                           d++;
                       }else{
                           unclassif++;
                       }
                    }
                }

                if (relative) {
                    d /= groupMembers.length;
                }
                vec[j] = d;
            }
            vec[cl.length] = unclassif/(relative?groupMembers.length:1);
            
            
          
            if (false) {
                for (int k = 0; k < cl.length; k++) {
                    Cluster c = cl[k];
                    double n = 0;
                    for (Datapoint d : groupMembers) {
                        if (c.containsDpID(d.getID())) {
                            for (int j = 0; j < nd.getFeatureNamesCombined().length; j++) {
                                vec[((j + 1) * cl.length) + k] += (j < dim) ? d.getVector()[j] : d.getSideVector()[j - dim];
                            }
                            n++;
                        }
                    }
                    if (n != 0) {
                        for (int j = 0; j < nd.getFeatureNamesCombined().length; j++) {
                            vec[((j + 1) * cl.length) + k] /= n;
                        }
                    }
                }
            }

            out[i] = new Datapoint(groupName, vec, i);
        }
        String[] paramNames = new String[cl.length+1];
        for (int i = 0;
                i < cl.length;
                i++) {
            paramNames[i] = "Contrib_to_Cluster_" + cl[i].getID();
        }
        paramNames[cl.length] = "unclassif";
        /*
        for (int i = 0;
                i < nd.getFeatureNamesCombined().length; i++) {
            for (int j = 0; j < cl.length; j++) {
                paramNames[((i + 1) * cl.length) + j] = "avg_" + nd.getFeatureNamesCombined()[i] + "_in_Cluster_" + cl[j].getID();
            }
        }*/

        return new Dataset(
                "Statistics per group for CS" + cs.getID(), out, paramNames);
    }
}
