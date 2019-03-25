/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import umontreal.iro.lecuyer.probdist.ChiDist;
import umontreal.iro.lecuyer.probdistmulti.MultiNormalDist;
import static sandbox.clustering.Score.ScoringMethod.Chi2;
import static sandbox.clustering.Score.ScoringMethod.Euclidean_Length;
import static sandbox.clustering.Score.ScoringMethod.PSS;
import static sandbox.clustering.Score.ScoringMethod.Similarity_To_Mode;
import util.MatrixOp;
import util.logger;

/**
 *
 * @author Nikolay
 */
public class Scorer {

    private MultiNormalDist noiseDist;
    private double[][] invCovarianceMtx;
    private static Score.ScoringMethod[] scoresToCompute = new Score.ScoringMethod[]{
        Score.ScoringMethod.Euclidean_Length, Score.ScoringMethod.Chi2, Score.ScoringMethod.PSS, Score.ScoringMethod.Similarity_To_Mode, Score.ScoringMethod.LDA
    };

    public Score.ScoringMethod[] getScoresToCompute() {
        return scoresToCompute;
    }

    public Scorer deepCopy() {
        return new Scorer(invCovarianceMtx, new MultiNormalDist(noiseDist.getMu(), noiseDist.getSigma()));
    }

    private Scorer(double[][] invCovarianceMtx, MultiNormalDist noiseDist) {
        this.invCovarianceMtx = invCovarianceMtx;
        this.noiseDist = noiseDist;
    }

    public Scorer(Dataset ds) {
        DenseDoubleMatrix2D cov = new MahalonobisSpace(ds).getCovarianceMtx();
        try {
            setNoiseCovarianceMatrix(cov);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Matrix is singular")) {
                JOptionPane.showMessageDialog(null, "Covariance matrix of the dataset is singular and the PSS scores cannot be computed.\n"
                        + "The reasons for this may be:\n"
                        + "1) Duplicated columns (parameters) present in the dataset\n"
                        + "2) Number of profiles(measurements) is less than the number of columns (parameters) ");
            }
        }
    }

    public double getLDA(double[] center, double[] vector) {
        if (invCovarianceMtx == null) {
            return 0;
        }
        double[] modCenter = MatrixOp.toUnityLen(MatrixOp.mult(invCovarianceMtx, MatrixOp.toUnityLen(center)));
        return MatrixOp.mult(modCenter, vector);
    }

    public double getChi2Prob(double[] vec) {
        if (invCovarianceMtx == null) {
            return 0;
        }
        double ml = Math.sqrt(MatrixOp.mult(vec, MatrixOp.mult(invCovarianceMtx, vec)));
        return Double.isNaN(ml) ? 0.0 : ChiDist.cdf(invCovarianceMtx.length, ml);
    }

    public double getEuclideanLength(double[] vec) {
        return Math.sqrt(MatrixOp.mult(vec, vec));
    }

    public double getCosineToMode(double[] mode, double[] vec) {
        return MatrixOp.getEuclideanCosine(mode, vec);
    }

    public void computeScores(Cluster c, Score.ScoringMethod[] scoresToCompute) {
       
            for (Score.ScoringMethod s : scoresToCompute) {
                switch (s) {
                    case Euclidean_Length:
                        for (ClusterMember cm : c.getClusterMembers()) {
                            cm.getScores().put(s, new Score(s, getEuclideanLength(cm.getDatapoint().getVector())));
                        }
                        break;
                    case Similarity_To_Mode:
                        double maxSim = c.getClusterSet().getDistanceMeasure().getSimilarityBounds()[0];
                        String nnPID = null;
                        DistanceMeasure dm = c.getClusterSet().getDistanceMeasure();
                        for (ClusterMember cm : c.getClusterMembers()) {
                            double sim = dm.getSimilarity(cm.getDatapoint().getVector(), c.getMode().getVector());
                            if (sim > maxSim) {
                                maxSim = sim;
                                nnPID = cm.getDatapointName();
                            }
                            cm.getScores().put(s, new Score(s, sim));
                        }
                        c.setCaption(nnPID);
                        break;
                    case Chi2:
                        for (ClusterMember cm : c.getClusterMembers()) {
                            cm.getScores().put(s, new Score(s, getChi2Prob(cm.getDatapoint().getVector())));
                        }
                        break;
                    case PSS:
                        //if(!computePSS) break;
                        for (ClusterMember cm : c.getClusterMembers()) {
                            cm.getScores().put(s, new Score(s, getPSS(c.getMode().getVector(), cm.getDatapoint().getVector())));
                        }
                        break;
                    case LDA:
                        for (ClusterMember cm : c.getClusterMembers()) {
                            cm.getScores().put(s, new Score(s, getLDA(c.getMode().getVector(), cm.getDatapoint().getVector())));
                        }
                        break;
                }
                c.addComputedScore(s);
            }


        
    }

    public double getPSS(double[] mode, double[] vector) {
        if (invCovarianceMtx == null) {
            return 0;
        }
        if (vector.length != invCovarianceMtx.length) {
            throw new IllegalArgumentException("Vector dimensionality doesn't match the mode: " + vector.length + " vs " + noiseDist.getDimension());
        }
        double[] vec = vector;
        return getProbabilityMuAbove(mode, vec, 0, Math.sqrt(getDotProduct(vec, vec)) * 20);//expectedNoiseProjection.get(cm.getCluster()));
    }

    public final void setNoiseCovarianceMatrix(DenseDoubleMatrix2D covMtx) {
        noiseDist = new MultiNormalDist(new double[covMtx.columns()], covMtx.toArray());
        invCovarianceMtx = Algebra.DEFAULT.inverse(covMtx).toArray();
    }

    private double getProbabilityMuAbove(double[] mu, double[] vec, double thrs, double integrationLimit) {
        double step = integrationLimit / 2000;

        double projLen = -integrationLimit;
        double integralAbove = 0;
        double integralTotal = 0;
        double normConst = noiseDist.density(noiseDist.getMu());
        double[] noise = new double[mu.length];
        double[] proj = new double[mu.length];
        double[] noiseCovProd = new double[mu.length];

        do {
            MatrixOp.mult(mu, -projLen, proj);
            noise = MatrixOp.sum(vec, proj);
            MatrixOp.mult(invCovarianceMtx, noise, noiseCovProd);
            double prob = normConst * Math.exp(-0.5 * getDotProduct(noise, noiseCovProd));

            integralTotal += prob;
            if (projLen > thrs) {
                integralAbove += prob;
            }
            projLen += step;
        } while (projLen < integrationLimit);
        double d = integralAbove / integralTotal;
        if (d > 1.0) {
            d = 1.0;
        }
        if (Double.isNaN(d)) {
            d = 1.0;
        }
        return d;
    }

    private double getDotProduct(double[] vec1, double[] vec2) {
        double res = 0;
        for (int i = 0; i < vec2.length; i++) {
            res += vec1[i] * vec2[i];
        }
        return res;
    }
}
