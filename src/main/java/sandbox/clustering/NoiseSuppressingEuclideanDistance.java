/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.JOptionPane;

/**
 *
 * @author Nikolay Samusik
 */
public class NoiseSuppressingEuclideanDistance extends DistanceMeasure {

    private static final long serialVersionUID = -1693234021614694368L;
    public static int THS = -1;

    @Override
    public NoiseSuppressingEuclideanDistance clone() {
        return new NoiseSuppressingEuclideanDistance();
    }

    public NoiseSuppressingEuclideanDistance() {
        if (THS == -1) {
            String res = JOptionPane.showInputDialog(null, "Number of most different features:", "3");
            THS = Integer.parseInt(res);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public double[] getPrototype(double[][] vectors, double[] weights) {
        if (vectors.length == 0) {
            return null;
        }
        if (vectors.length != weights.length) {
            throw new IllegalArgumentException("The weights array and vectors array have different lengths");
        }

        double[] avg = new double[vectors[0].length];
        double weightSum = 0;
        for (int i = 0; i < vectors.length; i++) {
            for (int j = 0; j < avg.length; j++) {
                avg[j] += vectors[i][j] * weights[i];
            }
            weightSum += weights[i];
        }
        for (int i = 0; i < avg.length; i++) {
            avg[i] /= weightSum;
        }
        return avg;
    }

    @Override
    public double[] getPrototype(double[][] vectors) {
        if (vectors.length == 0) {
            return null;
        }

        double[] avg = new double[vectors[0].length];
        double weightSum = 0;
        for (int i = 0; i < vectors.length; i++) {
            for (int j = 0; j < avg.length; j++) {
                avg[j] += vectors[i][j];
            }
            weightSum++;
        }
        for (int i = 0; i < avg.length; i++) {
            avg[i] /= weightSum;
        }
        return avg;
    }

    @Override
    public boolean supportsPrototyping() {
        return true;
    }

    @Override
    public double getDistance(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Supplied vectors are of a different size");
        }

        double dist = 0;

        double[] diff = new double[THS];
        double d;

        for (int i = 0; i < THS; i++) {
            diff[i] = -Math.abs(vector1[i] - vector2[i]);
        }
        Arrays.sort(diff);
        for (int i = THS; i < vector1.length; i++) {
            d = -Math.abs(vector1[i] - vector2[i]);
            if (d < diff[THS - 1]) {
                diff[THS - 1] = d;
                Arrays.sort(diff);
            }
        }
        /*
        ArrayList<Double> al = new ArrayList<>();
        for (int i = 0; i < vector1.length; i++) {
            al.add(-Math.abs(vector1[i] - vector2[i]));
        }
        Collections.sort(al);*/

        for (double e : diff) {
            dist += e * e;
        }

        return Math.sqrt(dist);
    }

    @Override
    public double getDistance(double[] vec1, double[] vec2, String name1, String name2) {
        return getDistance(vec1, vec2);
    }

    @Override
    public void init(Dataset aov) {
        return;
    }

    @Override
    public double distanceToSimilarity(double distance) {
        return 1.0 / (1.0 + distance);
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Distance between two vectors is a square root of sum of squares of differences between components, similarity is a reciprocal of the distance.";
    }

    @Override
    public String getName() {
        return "Noise Suppressing Euclidean Distance";
    }

    @Override
    public double getDistance(DenseDoubleMatrix1D vector1, DenseDoubleMatrix1D vector2) {
        return getDistance(vector1.toArray(), vector2.toArray());
    }

    @Override
    public double getSimilarity(DenseDoubleMatrix1D vec1, DenseDoubleMatrix1D vec2) {
        return distanceToSimilarity(getDistance(vec1, vec2));
    }

    @Override
    public double similarityToDistance(double similarity) {
        return (1.0 / (similarity)) - 1;
    }

    @Override
    public double getSimilarity(double[] vec1, double[] vec2) {
        return distanceToSimilarity(getDistance(vec1, vec2));
    }

    public double getLength(DenseDoubleMatrix1D vector) {
        double dist = 0;
        final int dim = vector.size();
        for (int i = 0; i < dim; i++) {
            dist += Math.pow(vector.getQuick(i), 2);
        }
        return Math.sqrt(dist);
    }

    @Override
    public double[] getDistanceBounds() {
        return new double[]{MIN_DISTANCE, MAX_DISTANCE};
    }

    @Override
    public double[] getSimilarityBounds() {
        return new double[]{MIN_SIMILARITY, MAX_SIMILARITY};
    }
    private static final double MIN_DISTANCE = 0;
    private static final double MAX_DISTANCE = Double.MAX_VALUE;
    private static final double MIN_SIMILARITY = 0;
    private static final double MAX_SIMILARITY = 1.0;

    public synchronized double getLength(double[] vector) {
        double dist = 0;
        final int dim = vector.length;
        for (int i = 0; i < dim; i++) {
            dist += Math.pow(vector[i], 2);
        }
        return Math.sqrt(dist);
    }
}
