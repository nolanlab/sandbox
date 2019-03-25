/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JPanel;
import util.MatrixOp;
import util.ProfileAverager;
import util.Shuffle;
import util.logger;

/**
 *
 * @author Nikolay
 */
public class K_medoids extends ClusteringAlgorithm {

    private static KMeansParamPanel paramPanel = new KMeansParamPanel();

    public K_medoids(DistanceMeasure dm) {
        super(dm);
        if (!dm.supportsPrototyping()) {
            throw new IllegalArgumentException("This clustering method requires prototyping");
        }
    }

    @Override
    public String getAlgorithmName() {
        return "K-Medoids";
    }

    @Override
    public String getAlgorithmDescription() {
        return "K-Medoids (PAM) is similar to K-means but the centers of the clusters are always quantized to datapoints";
    }

    @Override
    public boolean isAlgorithmPublic() {
        return true;
    }

    @Override
    public ClusterSet[] doBatchClustering(Dataset ds, String comment) {
        int K[] = paramPanel.getKValues();
        ClusterSet[] css = new ClusterSet[K.length];
        int batchID = 0;

        for (int i = 0; i < css.length; i++) {
            logger.print("K-medoids, K = " + K[i] + " clustering has started");
            css[i] = new ClusterSet(batchID, ds, doClustering(ds, K[i], paramPanel.getPrecision()), dm, getAlgorithmName(), "K = " + K[i], K[i], comment);
        }
        System.gc();
        return css;
    }

    @Override
    public JPanel getParamControlPanel() {
        return paramPanel;
    }

    private class Centroid {

        double[] vec;
        int idx;

        public Centroid(double[] vec, int idx) {
            this.vec = vec;
            this.idx = idx;
        }

        public int getIndex() {
            return idx;
        }

        public double[] getVector() {
            return vec;
        }

        @Override
        protected Centroid clone() {
            return new Centroid(Arrays.copyOf(vec, vec.length), idx);
        }
    }

    //double bestClusters ;
    public Cluster[] doClustering(Dataset nd, final int K, int numIterations) {

        int[] bestAssignments = null;

        double minSumDistOverIterations = Double.MAX_VALUE;
        boolean angular = AngularDistance.class.isAssignableFrom(dm.getClass());
        final Datapoint[] dp = nd.getDatapoints();

        for (int r = 0; r < numIterations; r++) {

            Datapoint[] cent = Arrays.copyOf((new Shuffle<Datapoint>()).shuffleCopyArray(nd.getDatapoints()), K);

            final double[][] centroids = new double[cent.length][];
            final int[] sizes = new int[centroids.length];
            final int[] assignments = new int[nd.size()];
            //final ConcurrentLinkedQueue<NDatapoint>[] lst = new ConcurrentLinkedQueue[numCells];
            for (int i = 0; i < centroids.length; i++) {
                centroids[i] = MatrixOp.copy(cent[i].getVector());
            }

            double currSumDist = Double.MAX_VALUE;
            double minSumDist = Double.MAX_VALUE;

            int it = 0;
            do {
                minSumDist = currSumDist;
                final ConcurrentLinkedQueue<Datapoint> q = new ConcurrentLinkedQueue<>();
                q.addAll(Arrays.asList(nd.getDatapoints()));

                int cpu = Runtime.getRuntime().availableProcessors();
                Thread[] t = new Thread[cpu];
                ThreadGroup tg = new ThreadGroup("NNthreads");
                for (int i = 0; i < t.length; i++) {
                    t[i] = new Thread(tg, new Runnable() {
                        @Override
                        public void run() {
                            Datapoint d;
                            while ((d = q.poll()) != null) {
                                double[] vec = MatrixOp.copy(d.getVector());
                                double maxSim = -1;
                                int nIDX = -1;
                                for (int j = 0; j < K; j++) {
                                    double sim = dm.getSimilarity(vec, centroids[j]);
                                    if (sim > maxSim) {
                                        nIDX = j;
                                        maxSim = sim;
                                    }
                                }
                                if (nIDX == -1) {
                                    nIDX = (int) Math.floor(Math.random() * (K));
                                }
                                assignments[d.getID()] = nIDX;
                            }
                        }
                    });
                    t[i].start();
                }
                do {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {

                        logger.print(e);
                        return null;
                    }
                } while (tg.activeCount() > 0);

                double[][] newCentroids = new double[centroids.length][centroids[0].length];
                Arrays.fill(sizes, 0);
                for (int i = 0; i < assignments.length; i++) {
                    int centIDX = assignments[i];
                    double[] ulv = MatrixOp.copy(dp[i].getVector());//angular ? MatrixOp.toUnityLen() : MatrixOp.copy(d.getVector());
                    double len = angular ? MatrixOp.lenght(ulv) : 1;
                    for (int j = 0; j < ulv.length; j++) {
                        newCentroids[centIDX][j] += ulv[j] / len;
                    }
                    sizes[centIDX]++;
                }
                for (int i = 0; i < centroids.length; i++) {
                    if (sizes[i] > 0) {
                        centroids[i] = newCentroids[i];
                        MatrixOp.mult(centroids[i], 1.0 / (angular ? MatrixOp.lenght(newCentroids[i]) : sizes[i]));
                    }
                }
                currSumDist = 0;
                for (int i = 0; i < assignments.length; i++) {
                    int centIDX = assignments[i];
                    currSumDist += dm.getDistance(centroids[centIDX], dp[i].getVector());
                }
                logger.print("iteration:" + r + " optim_cycle:" + it++);
            } while (currSumDist < minSumDist);

            if (minSumDist < minSumDistOverIterations) {
                minSumDistOverIterations = minSumDist;
                bestAssignments = assignments;
            }
        }
        List<Datapoint>[] cList = new List[K];
        for (int g = 0; g < bestAssignments.length; g++) {
            int cI = bestAssignments[g];
            if (cList[cI] == null) {
                cList[cI] = new LinkedList<>();
            }
            cList[cI].add(dp[g]);
        }
        ArrayList<List<Datapoint>> al = new ArrayList<>();
        for (int i = 0; i < cList.length; i++) {
            if (cList[i] != null) {
                al.add(cList[i]);
            }
        }
        cList = al.toArray(new List[al.size()]);
        List<Cluster> out = new ArrayList<>();
        for (int i = 0; i < cList.length; i++) {
            if (cList[i] == null) {
                continue;
            };
            Datapoint[] dpCl = cList[i].toArray(new Datapoint[cList[i].size()]);
            ProfileAverager paM = new ProfileAverager();
            ProfileAverager paS = new ProfileAverager();

            for (Datapoint d : dpCl) {
                paM.addProfile(d.getVector());
                paS.addProfile(d.getSideVector());
            }

            out.add(new Cluster(dpCl, paM.getAverage(), paS.getAverage(), "", dm));
        }
        return out.toArray(new Cluster[cList.length]);
    }
}
