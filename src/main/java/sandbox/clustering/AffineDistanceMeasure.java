/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

/**
 *
 * @author Nikolay
 */
public abstract class AffineDistanceMeasure extends DistanceMeasure {

    public abstract double getAffineDistance(DenseDoubleMatrix1D vec1, DenseDoubleMatrix1D vec2);

    public abstract double getAffineSimilarity(DenseDoubleMatrix1D vec1, DenseDoubleMatrix1D vec2);

    public abstract DenseDoubleMatrix2D getTransformationMatrix();
}
