/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.annotations;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import sandbox.clustering.DistanceMeasure;
import java.util.ArrayList;
import java.util.Arrays;
import sandbox.clustering.Dataset;
import util.logger;


/**
 *
 * @author Nikolay
 */
public class AnnotationDistanceMeasure extends DistanceMeasure {

    @Override
    public DistanceMeasure clone() {
        AnnotationDistanceMeasure clone = new AnnotationDistanceMeasure();
        clone.annotationDS = annotationDS;
        clone.dim = dim;
        clone.annotation = annotation;
        return clone;
    }

    @Override
    public String toString() {
        return getName() + annotation.getAnnotationName();
    }

    @Override
    public boolean supportsPrototyping() {
        return false;
    }

    @Override
    public double[] getPrototype(double[][] vectors) {
        throw new UnsupportedOperationException("This distance measure doesn't support prototyping");
    }

    @Override
    public double[] getPrototype(double[][] vectors, double[] weights) {
        throw new UnsupportedOperationException("This distance measure doesn't support prototyping");
    }
    private Dataset annotationDS;
    private int dim;
    private Annotation annotation;

    public Annotation getAnnotation() {
        return annotation;
    }

    public AnnotationDistanceMeasure() {
    }

    public AnnotationDistanceMeasure(Dataset annotationDS) {
        init(annotationDS);
    }

    @Override
    public double distanceToSimilarity(double distance) {
        return -Math.log(distance);
    }

    public Dataset getAnnotationDataset() {
        return annotationDS;
    }

    @Override
    public double similarityToDistance(double similarity) {
        return Math.exp(-similarity);
    }

    @Override
    public final void init(Dataset ds) {
        this.annotationDS = ds;
        dim = annotationDS.getNumDimensions();
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public double getDistance(double[] vec1, double[] vec2) {
        return getDistance(vec1, vec2, null, null);
    }

    @Override
    public double getDistance(double[] vec1, double[] vec2, String s1, String s2) {
        if (annotationDS == null) {
            throw new IllegalStateException("Not initialized");
        }
        if (vec1.length != dim || vec2.length != dim) {
            throw new IllegalStateException("Vectors have different lenght than the dimensionality of the bg matrix");
        }
        int countMatchingTerms = 0;
        boolean[] mask = new boolean[annotationDS.size()];
        Arrays.fill(mask, true);
        ArrayList<String> matchingParams = new ArrayList<String>();
        for (int i = 0; i < dim; i++) {
            if (vec1[i] > 0 && vec2[i] > 0) {
                matchingParams.add(annotationDS.getFeatureNames()[i]);
                countMatchingTerms++;
                for (int j = 0; j < mask.length; j++) {
                    mask[j] &= (annotationDS.getDatapoints()[j].getVector()[i] > 0);
                }
            }
        }

        double countGenesWithAllMatchingTerms = 0;
        for (boolean b : mask) {
            if (b) {
                countGenesWithAllMatchingTerms++;
            }
        }

        double prob = 1.0;
        if (countMatchingTerms > 0) {
            prob = countGenesWithAllMatchingTerms / (double) mask.length;
            if (s1 != null) {
                if (!s1.equals(s2)) {
                    logger.print(s1, s2, "Prob" + prob, "Match: ", matchingParams.size(), matchingParams.toString());
                }
            }
        }
        return prob;
    }

    @Override
    public double getSimilarity(double[] vec1, double[] vec2) {
        return distanceToSimilarity(getDistance(vec1, vec2));
    }

    @Override
    public double[] getDistanceBounds() {
        return new double[]{0, 1.0};
    }

    @Override
    public double getSimilarity(DenseDoubleMatrix1D vec1, DenseDoubleMatrix1D vec2) {
        return getSimilarity(vec1.toArray(), vec2.toArray());
    }

    @Override
    public double[] getSimilarityBounds() {
        return new double[]{0, Double.MAX_VALUE};
    }

    @Override
    public double getDistance(DenseDoubleMatrix1D vec1, DenseDoubleMatrix1D vec2) {
        return getDistance(vec1.toArray(), vec2.toArray());
    }

    @Override
    public String getName() {
        return "Annotation Distance";
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("-log probability of two entities to share the annotations that they share by chance");
    }
}
