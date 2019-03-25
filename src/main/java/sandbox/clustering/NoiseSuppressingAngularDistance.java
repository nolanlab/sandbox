/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JOptionPane;
import util.MatrixOp;
import static util.MatrixOp.diff;

/**
 *
 * @author Nikolay Samusik
 */
public class NoiseSuppressingAngularDistance extends DistanceMeasure  {

    private static final long serialVersionUID = -169321614694368L;
    public static int THS = -1;
    private final transient Sorter com = new Sorter();

    @Override
    public NoiseSuppressingAngularDistance clone() {
        return new NoiseSuppressingAngularDistance();
    }

    public NoiseSuppressingAngularDistance() {
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
        return MatrixOp.toUnityLen(avg);
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
        return MatrixOp.toUnityLen(avg);
    }

    @Override
    public boolean supportsPrototyping() {
        return true;
    }

    private static class Sorter implements Comparator<double[]>{
        @Override
        public int compare(double[] o1, double[] o2) {
            return (int) Math.signum(o2[2] - o1[2]);
        }
    };

    @Override
    public double getDistance(double[] vector1, double[] vector2) {
        return similarityToDistance(getSimilarity(vector1, vector2));
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
        return Math.cos(distance);
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
        return "Noise Supressing Angular Distance";
    }

    @Override
    public double getDistance(DenseDoubleMatrix1D vector1, DenseDoubleMatrix1D vector2) {
        return getDistance(vector1.toArray(), vector2.toArray());
    }

    @Override
    public double getSimilarity(DenseDoubleMatrix1D vec1, DenseDoubleMatrix1D vec2) {
        return getSimilarity(vec1.toArray(), vec2.toArray());
    }

    @Override
    public double similarityToDistance(double similarity) {
        return Math.acos(similarity);
    }

    @Override
    public double getSimilarity(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Supplied vectors are of a different size");
        }

        double vec1Len = MatrixOp.lenght(vector1);
        double vec2Len = MatrixOp.lenght(vector2);

        double[][] arr = new double[vector1.length][3];

        for (int i = 0; i < vector1.length; i++) {
            arr[i][0] = vector1[i] / vec1Len;
            arr[i][1] = vector2[i] / vec2Len;
            arr[i][2] = Math.abs(arr[i][0] - arr[i][1]);
        }

        Arrays.sort(arr, com);
        /*
        double maxDrop = 0;
        double drop = 0;
        int
        for (int i = 1; i < arr.length; i++) {
            drop = arr[i-1][2] - arr[i][2];
            if(drop>maxDrop){
                maxDrop = drop;
                THS = i;
            }
        }*/
        
        //double d = MatrixOp.getEuclideanCosine(vector1, vector2);
        double prod = 0;
        double sumsq1 = 0;
        double sumsq2 = 0;
        for (int i = 0; i < THS; i++) {
            prod += arr[i][0] * arr[i][1];
            sumsq1 += arr[i][0] * arr[i][0];
            sumsq2 += arr[i][1] * arr[i][1];
        }

        return prod / Math.sqrt(sumsq1 * sumsq2);
    }

    public double getLength(DenseDoubleMatrix1D vector) {
        return getLength(vector.toArray());
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
    private static final double MAX_DISTANCE = Math.PI/2;
    private static final double MIN_SIMILARITY = -1;
    private static final double MAX_SIMILARITY = 1.0;

    public synchronized double getLength(double[] vector) {
        return MatrixOp.lenght(vector);
    }
}
