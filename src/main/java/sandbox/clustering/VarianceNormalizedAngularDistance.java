/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import java.util.Arrays;
import util.MatrixOp;
import util.NormalStatistics;

/**
 *
 * @author Nikolay
 */
public class VarianceNormalizedAngularDistance extends AngularDistance {

    private double[] sdvec;
    private double[] sd2vec;
    private int dim;

    @Override
    public VarianceNormalizedAngularDistance clone() {
        VarianceNormalizedAngularDistance dist = new VarianceNormalizedAngularDistance(Arrays.copyOf(sdvec, sdvec.length), Arrays.copyOf(sd2vec, sd2vec.length), dim);
        return dist;
    }

    @Override
    public boolean supportsPrototyping() {
        return true;
    }

    @Override
    public double[] getPrototype(double[][] vectors, double[] weights) {
        if (vectors.length == 0) {
            return null;
        }
        if (vectors.length != weights.length) {
            throw new IllegalArgumentException("The weights array and vectors array have different lengths");
        }
        double[] prototype = new double[vectors[0].length];
        double sumW = 0;
        for (int i = 0; i < weights.length; i++) {
            double[] vec = MatrixOp.toUnityLen(vectors[i]);
            for (int j = 0; j < prototype.length; j++) {
                prototype[j] += vec[j] * weights[i];
            }
            sumW += weights[i];
        }
        for (int j = 0; j < prototype.length; j++) {
            prototype[j] /= sumW;
        }
        return prototype;
    }

    @Override
    public double[] getPrototype(double[][] vectors) {
        if (vectors.length == 0) {
            return null;
        }

        double[] prototype = new double[vectors[0].length];
        double sumW = 0;
        for (int i = 0; i < vectors.length; i++) {
            double[] vec = MatrixOp.toUnityLen(vectors[i]);
            for (int j = 0; j < prototype.length; j++) {
                prototype[j] += vec[j];
            }
            sumW++;
        }
        for (int j = 0; j < prototype.length; j++) {
            prototype[j] /= sumW;
        }
        return prototype;
    }

    //private EuclideanDistance ed = new EuclideanDistance();
    @Override
    public double similarityToDistance(double similarity) {
        return (1 - similarity) / 2.0;
    }

    @Override
    public double distanceToSimilarity(double distance) {
        return 1 - (distance * 2);
    }

    @Override
    public double getDistance(double[] vec1, double[] vec2, String name1, String name2) {
        return getDistance(vec1, vec2);
    }

    @Override
    public void init(Dataset nd) {
        this.dim = nd.getNumDimensions();
        this.sdvec = new double[dim];
        this.sd2vec = new double[dim];
        for (int i = 0; i < sdvec.length; i++) {
            double[] val = new double[nd.size()];
            for (int j = 0; j < val.length; j++) {
                val[j] = nd.getDatapoints()[j].getVector()[i];
            }
            sdvec[i] = new NormalStatistics(val).getSD();
            sd2vec[i] = sdvec[i] * sdvec[i];
        }
    }

    private VarianceNormalizedAngularDistance(double[] sdvec, double[] sd2vec, int dim) {
        this.sdvec = sdvec;
        this.sd2vec = sd2vec;
        this.dim = dim;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public double getDistance(double[] vector1, double[] vector2) {
        return (1 - getSimilarity(vector1, vector2)) / 2.0;
    }

    @Override
    public double getDistance(DenseDoubleMatrix1D vector1, DenseDoubleMatrix1D vector2) {
        return (1 - getSimilarity(vector1, vector2)) / 2.0;
    }

    @Override
    public double[] getDistanceBounds() {
        return new double[]{MIN_DISTANCE, MAX_DISTANCE};
    }

    @Override
    public String getDescription() {
        return "Variance-normalized version of the Angular Distance, which divides every parameter by its SD in the total dataset, such that all parameters are brought to same scale and contribute equally to the clustering";
    }

    @Override
    public String getName() {
        return "Variance-Normalized Angular Distance";
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public double[] getSimilarityBounds() {
        return new double[]{MIN_SIMILARITY, MAX_SIMILARITY};
    }
    private static final double MIN_DISTANCE = 0;
    private static final double MAX_DISTANCE = 1.0; //2.0* Math.PI;
    private static final double MIN_SIMILARITY = -1;
    private static final double MAX_SIMILARITY = 1;

    @Override
    public double getSimilarity(DenseDoubleMatrix1D vector1, DenseDoubleMatrix1D vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Supplied vectors are of a different size");
        }
        double cos = 0;
        for (int i = 0; i < dim; i++) {
            cos += (vector1.getQuick(i) * vector2.getQuick(i)) / sd2vec[i];
        }
        cos /= getNormalizedLen(vector1.toArray()) * getNormalizedLen(vector2.toArray());
        return Math.min(MAX_SIMILARITY, Math.max(MIN_SIMILARITY, cos));
    }

    private double getNormalizedLen(double[] vec) {
        if (vec.length != sdvec.length) {
            throw new IllegalArgumentException("Supplied vector has a different size from the SD vector");
        }
        double len = 0;
        for (int i = 0; i < dim; i++) {
            len += (vec[i] * vec[i]) / sd2vec[i];
        }
        return Math.sqrt(len);
    }

    public VarianceNormalizedAngularDistance() {
    }

    @Override
    public double getSimilarity(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Supplied vectors are of a different size");
        }
        double cos = 0;
        for (int i = 0; i < dim; i++) {
            cos += (vector1[i] * vector2[i]) / sd2vec[i];
        }
        cos /= getNormalizedLen(vector1) * getNormalizedLen(vector2);
        return Math.min(MAX_SIMILARITY, Math.max(MIN_SIMILARITY, cos));
    }
}
