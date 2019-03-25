/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import sandbox.clustering.Cluster;
import sandbox.clustering.ClusterSet;
import sandbox.clustering.Datapoint;
import sandbox.clustering.DistanceMeasure;
import sandbox.clustering.Dataset;


/**
 *
 * @author Nikolay
 */
public class ClusterSetValidationMeasureCollection {

    private static final int MIN_CLUSTER_SIZE = 2;

    public static class ClusterSilhouetteIndexFactory extends ClusterSetValidationFactory {

      

        @Override
        public ClusterSetValidationMeasure getClusterSetValidationMeasure(ClusterSet cs) {

            
            int MAX_SIZE = 500;
            int [][] clusters = new int[cs.getClusters().length][];
            
            for (int i = 0; i < cs.getClusters().length; i++) {
               Cluster c = cs.getClusters()[i];
               clusters[i] = new int[c.size()];
                for (int j = 0; j < clusters[i].length; j++) {
                   clusters[i][j] = c.getClusterMembers()[j].getDatapoint().getID();
                }
                int leftout = 0;
                if(c.size()>MAX_SIZE){
                    double sampling_rate = MAX_SIZE/(double)c.size();
                    for (int j = 0; j < c.size(); j++) {
                        if(Math.random()> sampling_rate){
                         clusters[i][j]=-1; 
                         leftout = 0;
                        }
                    }
                }
               Arrays.sort(clusters[i]);
               int firstidx = 0;
                for (; firstidx< clusters[i].length; firstidx++) {
                    if(clusters[i][firstidx]>=0) break;
                }
               
               clusters[i] = Arrays.copyOfRange(clusters[i], firstidx, clusters[i].length);
            }
           
            
           Datapoint[] dp = cs.getDataset().getDatapoints();

            ArrayList<Double> alSilhouettes = new ArrayList<>();

            for (int i = 0; i < clusters.length; i++) {
                int [] currCluster = clusters[i];
                for (int currID:currCluster) {
                    
                double [] currVec = dp[currID].getVector();
                
                double bI = Double.MAX_VALUE;
                double aI = -Double.MAX_VALUE;

                for (int j = 0; j < clusters.length; j++) {
                    double avgDist = 0;
                    double cnt = 0;
                    boolean thisCluster = i==j;
                    int [] otherCluster = clusters[j];
               
                        for (int otherID : otherCluster) {
                            
                            if (currID!=otherID) {
                                double val = dm.getDistance(currVec, dp[otherID].getVector());
                                if (Double.isNaN(val)) {
                                    logger.print("NaN for",dp[currID], dp[otherID]);
                                }
                                avgDist += val;
                                cnt++;
                            } else {
                                thisCluster = true;
                            }
                        }
                        avgDist /= cnt;
                        if (Double.isNaN(avgDist)) {
                            logger.print("avgDist NaN for ",i,j);
                            logger.print(bI);
                            logger.print(aI);
                        }
                   
                    if (thisCluster) {
                        aI = avgDist;
                    } else {
                        bI = Math.min(avgDist, bI);
                    }
                }
                alSilhouettes.add((bI - aI) / Math.max(bI, aI));
                }
            }
            double[] silhouettes = new double[alSilhouettes.size()];
            for (int i = 0; i < silhouettes.length; i++) {
                silhouettes[i] = alSilhouettes.get(i);
            }
            return new ClusterSilhouetteIndex(cs, silhouettes);
        }
        
        private DistanceMeasure dm;

        public ClusterSilhouetteIndexFactory(DistanceMeasure dm) {
            this.dm= dm;
        }
    }

    public static class ClusterSilhouetteIndex implements ClusterSetValidationMeasure {

        ClusterSet cs;
        double pVal;
        double avgClusterSilhouetteIndex;
        double sdClusterSilhouetteIndex;
        double[] clusterSilohuetteIndices;

        public ClusterSilhouetteIndex(ClusterSet cs, double[] clusterSilohuetteIndices) {
            this.cs = cs;
            NormalStatistics ns = new NormalStatistics(clusterSilohuetteIndices);
            this.avgClusterSilhouetteIndex = ns.mean;
            this.sdClusterSilhouetteIndex = ns.SD;
            this.clusterSilohuetteIndices = Arrays.copyOf(clusterSilohuetteIndices, clusterSilohuetteIndices.length);
            pVal = 0;
            for (double d : clusterSilohuetteIndices) {
                if (d > 0) {
                    pVal++;
                }
            }
            pVal /= clusterSilohuetteIndices.length;
        }

        @Override
        public String getAdditionalInfo() {
            return "None";
        }

        @Override
        public ClusterSet getClusterSet() {
            return cs;
        }

        @Override
        public String getDescription() {
            return "Cluster Silhouette Index - google it!";
        }

        @Override
        public double[] getHighlightThresholds() {
            return new double[]{0.7, 1.0};
        }

        @Override
        public double[] getMeasureBounds() {
            return new double[]{-1.0, 1.0};
        }

        @Override
        public double getMeasure() {
            return avgClusterSilhouetteIndex;
        }

        @Override
        public String getName() {
            return "Cluster Silhouette Index";
        }

        @Override
        public double getPValue() {
            return pVal;
        }

        @Override
        public boolean showPValue() {
            return false;
        }
    }

    public static class PPIEnrichmentValidationMeasure implements ClusterSetValidationMeasure {

        ClusterSet cs;
        double logPVal;
        double correlBTWClusters;
        double numPairsWithin;
        double numInteractionsWithin;
        double likelihoodRatio;
        double fractionSignificantClusters;

        public double getFractionSignificantClusters() {
            return fractionSignificantClusters;
        }

        @Override
        public boolean showPValue() {
            return true;
        }

        @Override
        public double getPValue() {
            return Math.exp(logPVal);
        }

        public double getLogPVal() {
            return logPVal;
        }

        @Override
        public String getName() {
            return "Enrichment of clusters in protein-protein interactions";
        }

        @Override
        public double getMeasure() {
            return numInteractionsWithin / numPairsWithin;
        }

        public double getInteractionsWithin() {
            return numInteractionsWithin;
        }

        public double getInteractionsTotal() {
            return numPairsWithin;
        }

        public double getLikelihoodRatio() {
            return likelihoodRatio;
        }

        @Override
        public double[] getMeasureBounds() {
            return new double[]{0, Double.POSITIVE_INFINITY};
        }

        public PPIEnrichmentValidationMeasure(ClusterSet cs, double numPairsWithin, double numInteractionsWithin, double logPVal, double likelihoodRatio, double fractionSignClusters) {
            this.cs = cs;
            this.numInteractionsWithin = numInteractionsWithin;
            this.numPairsWithin = numPairsWithin;
            this.logPVal = logPVal;
            this.likelihoodRatio = likelihoodRatio;
            this.fractionSignificantClusters = fractionSignClusters;
        }

        @Override
        public String getAdditionalInfo() {
            return "";
        }

        @Override
        public String getDescription() {
            return "This measure quantifies the enrichment of the set of pairs of genes that are grouped in one cluster in protein-protein interactions";
        }

        @Override
        public double[] getHighlightThresholds() {
            return new double[]{0, 0};
        }

        @Override
        public ClusterSet getClusterSet() {
            return cs;
        }
    }

    
}
