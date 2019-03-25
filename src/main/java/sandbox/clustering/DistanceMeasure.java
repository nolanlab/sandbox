/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import java.io.Serializable;
import sandbox.annotations.AnnotationDistanceMeasure;
import util.logger;

/**
 *
 * @author Nikolay
 */
public abstract class DistanceMeasure implements Serializable {

    private static final long serialVersionUID = -6423042508107317575L;

    public abstract double[] getDistanceBounds();

    public abstract boolean isPublic();

    public abstract double[] getSimilarityBounds();

    public abstract double similarityToDistance(double similarity);

    public abstract double distanceToSimilarity(double distance);

    public abstract String getName();

    public abstract String getDescription();

    public abstract double getDistance(DenseDoubleMatrix1D vec1, DenseDoubleMatrix1D vec2);

    public abstract double getDistance(double[] vec1, double[] vec2);

    public abstract double getDistance(double[] vec1, double[] vec2, String name1, String name2);

    public abstract double getSimilarity(DenseDoubleMatrix1D vec1, DenseDoubleMatrix1D vec2);

    public abstract double getSimilarity(double[] vec1, double[] vec2);

    public abstract double[] getPrototype(double[][] vectors);

    public abstract double[] getPrototype(double[][] vectors, double[] weights);

    public abstract boolean supportsPrototyping();

    @Override
    public abstract DistanceMeasure clone();

    public abstract void init(Dataset ds);

    public static Class<DistanceMeasure>[] getAvailableSubclasses() {
        return new Class[]{AngularDistance.class, EuclideanDistance.class};
    }

    public static DistanceMeasure getInitializedDistanceMeasure(Class<? extends DistanceMeasure> c, Dataset ds) {

        DistanceMeasure dm = null;
        try {
            dm = (DistanceMeasure) (c.getConstructor().newInstance());
            if (c.equals(AnnotationDistanceMeasure.class)) {
            } else {
                dm.init(ds);
            }
        } catch (Exception e) {
            logger.showException(e);
        }
        return dm;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getClass().equals(obj.getClass());
    }
}
