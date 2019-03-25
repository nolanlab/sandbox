/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.dataIO;

import sandbox.clustering.Cluster;
import sandbox.clustering.ClusterMember;
import sandbox.clustering.ClusterSet;
import sandbox.clustering.Dataset;
import sandbox.fcs.ExportFCS;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import util.MatrixOp;
import util.Shuffle;

/**
 *
 * @author Nikolay
 */
public class ClusterSetToFCSExporterWithNNReassignment {

    private final static int MAX_CLUSTER_SAMPLE_SIZE = 1000;

    public static void exportClusterSet(ClusterSet cs, String[] fcsFilePaths, File out, ImportConfigObject ico) throws Exception {

        for (int idx = 0; idx < fcsFilePaths.length; idx++) {
            File fcsFile = new File(fcsFilePaths[idx]);
            DatasetStub stub = DatasetStub.createFromFCS(fcsFile);

            ico.limitRowsPerFile = (int) stub.getRowCount();
            Dataset dsFullFile = DatasetImporter.importDataset(new DatasetStub[]{stub}, ico);

            ExportFCS writer = new ExportFCS();

            Dataset ds = cs.getDataset();
            String[] s = ds.getFeatureNamesCombined();

            
            String [] spn = Arrays.copyOf(stub.getShortColumnNames(), stub.getShortColumnNames().length+1);
        
            String [] lpn = Arrays.copyOf(stub.getLongColumnNames(), stub.getLongColumnNames().length+1);
            
            lpn[stub.getLongColumnNames().length] = "cluster_id";
            spn[stub.getLongColumnNames().length] = cs.toString();
            
           
            int[] clusterIDmap = new int[(int) stub.getRowCount()];
             for (int i = 0; i < clusterIDmap.length; i++) {
                clusterIDmap[i] = -1;
            }
            
            int id = 0;

            for (Cluster c : cs.getClusters()) {
                c.setID(id++);
                for (ClusterMember cm : c.getClusterMembers()) {

                    String filename = fcsFile.getName().trim();

                    if (filename.endsWith(".fcs")) {
                        filename = filename.substring(0, filename.length() - 4);
                    }

                    if (fcsFile.getName().trim().startsWith(cm.getDatapoint().getFilename().trim())) {
                        int evtidx = cm.getDatapoint().getIndexInFile();
                        clusterIDmap[evtidx] = c.getID();
                    }
                }
            }
            Shuffle<double[]> sh = new Shuffle<>();

            HashMap<Cluster, double[][]> clusRepVectors = new HashMap<>();
            for (Cluster c : cs.getClusters()) {
                double[][] vec = new double[c.size()][];
                for (int i = 0; i < vec.length; i++) {
                    vec[i] = c.getClusterMembers()[i].getDatapoint().getVector();
                }
                sh.shuffleArray(vec);
                clusRepVectors.put(c, Arrays.copyOf(vec, Math.min(vec.length, MAX_CLUSTER_SAMPLE_SIZE)));
            }

            for (int i = 0; i < clusterIDmap.length; i++) {
                if (i % 1000 == 0) {
                    System.err.println("remapping " + i);
                }
                if (clusterIDmap[i] < 0) {
                    double[] dp = dsFullFile.getDatapoints()[i].getVector();
                    double maxSim = -1;
                    Cluster closestCluster = null;
                    for (Cluster c : cs.getClusters()) {
                        double[][] vec = clusRepVectors.get(c);
                        for (double[] v : vec) {
                            double cos = MatrixOp.getEuclideanCosine(v, dp);
                            if (cos > maxSim) {
                                maxSim = cos;
                                closestCluster = c;
                            }
                        }
                    }
                    clusterIDmap[i] = closestCluster.getID();
                }
            }

            //double[][] inEvents = fcsFile.getEventCount();
            float[][] events = new float[(int)stub.getRowCount()][stub.getShortColumnNames().length + 1];

            for (int i = 0; i < (int)stub.getRowCount(); i++) {
                for (int j = 0; j < stub.getShortColumnNames().length; j++) {
                    events[i][j] = (float)stub.getRow(i)[j];
                }
                events[i][events[i].length - 1] = clusterIDmap[i];
            }
            
            writer.writeFCSAsFloat(out +File.separator + fcsFile.getName(), events, spn, lpn);
        }
    }
}
