/*
 * MahalonobisSpace.java
 *
 * Created on January 23, 2008, 6:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sandbox.clustering;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.CholeskyDecomposition;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import util.Correlation;
import util.DefaultEntry;
import util.MatrixOp;
import util.logger;



/**
 *
 * @author Nikolay
 */
public class MahalonobisSpace implements Serializable {

    private Algebra algebra = new Algebra();
    public static final long serialVersionUID = 206L;
    private DenseDoubleMatrix2D correlMatrix;
    private DenseDoubleMatrix2D invCorrelMatrix;
    private DenseDoubleMatrix2D covarianceMatrix;
    private DenseDoubleMatrix2D invertedCovMtx;
    private DenseDoubleMatrix2D invertedStDevMtx;
    private DenseDoubleMatrix2D stDevMtx;
    private CholeskyDecomposition cd;
    private DenseDoubleMatrix2D transL;

    /**
     * Creates a new instance of MahalonobisSpace
     */
    public DenseDoubleMatrix2D getInverseCovMtx() {
        return invertedCovMtx;
    }

    public DenseDoubleMatrix2D getCovarianceMtx() {
        return covarianceMatrix;
    }

    public DenseDoubleMatrix2D getCorrelMatrix() {
        return correlMatrix;
    }

    public static DenseDoubleMatrix2D computeCorrelMtxForDP(Datapoint[] dp) {
        double[][] ds = new double[dp.length][];
        int cnt=0;
        for (Datapoint d : dp) {
            ds[cnt++] = d.getVector();
        }
        return new  DenseDoubleMatrix2D(Correlation.estimateParameterCorrelationMatrix(ds));
    }

    public static DenseDoubleMatrix2D computeCovarMtxForDP(Datapoint[] dp) {
        double[][] ds = new double[dp.length][];
        int cnt=0;
        for (Datapoint d : dp) {
            ds[cnt++] = d.getVector();
        }
        return new  DenseDoubleMatrix2D(Correlation.estimateParameterCovarianceMatrix(ds));
    }

    public DenseDoubleMatrix2D getInvStDevMtx() {
        return invertedStDevMtx;
    }

    public DenseDoubleMatrix2D getStDevMtx() {
        return stDevMtx;
    }

    public double getEuclideanCosine(DenseDoubleMatrix1D vector1, DenseDoubleMatrix1D vector2) throws java.lang.IllegalArgumentException {
        if (vector1.size() != vector2.size()) {
            throw new java.lang.IllegalArgumentException("Size of vector1 (" + String.valueOf(vector1.size()) + ") doesn't match size of vector2 (" + String.valueOf(vector2.size()) + ")");
        }
        double numerator = algebra.mult(vector1, vector2);
        double denom1 = getEuclideanLength(vector1);
        double denom2 = getEuclideanLength(vector2);
        double denominator = denom1 * denom2;
        return Math.max(-1.0, Math.min(numerator / denominator, 1.0));
    }

    public DenseDoubleMatrix2D getTransL() {
        return transL;
    }

    public MahalonobisSpace(DenseDoubleMatrix2D covMatrix) {
        covarianceMatrix = covMatrix;
        stDevMtx = (DenseDoubleMatrix2D) covMatrix.copy();

        for (int x = 0; x < stDevMtx.columns(); x++) {
            for (int y = 0; y < stDevMtx.rows(); y++) {
                if (y != x) {
                    stDevMtx.set(x, y, 0.0);
                }
            }
        }
        invertedStDevMtx = (DenseDoubleMatrix2D) algebra.inverse(stDevMtx);
        invertedCovMtx = (DenseDoubleMatrix2D) algebra.inverse(covarianceMatrix);
        //invCorrelMatrix = (DenseDoubleMatrix2D)algebra.inverse(correlMatrix);
        cd = new CholeskyDecomposition(invertedCovMtx);
        transL = (DenseDoubleMatrix2D) algebra.transpose(cd.getL());
    }

    public MahalonobisSpace(Dataset nd) {
        double pct = 0.25;

        ArrayList<Entry<Datapoint, Double>> filtered = new ArrayList<>();

        for (Datapoint d : nd.getDatapoints()) {
            if (true || d.getFullName().startsWith("MOCK")) {
                filtered.add(new DefaultEntry<>(d, MatrixOp.lenght(d.getVector())));
            }
        }
        Collections.sort(filtered, new Comparator<Entry<Datapoint, Double>>() {
            @Override
            public int compare(Entry<Datapoint, Double> o1, Entry<Datapoint, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        logger.print("1st dp in the MahalonobisSpace list" + filtered.get(0).getKey() + ", " + filtered.get(0).getValue());
        double[][] vec = new double[(int) ((double) filtered.size() * pct)][];
        for (int i = 0; i < vec.length; i++) {
            vec[i] = filtered.get(i).getKey().getVector();
        }
        logger.print("Init MahalonobisSpace on " + vec.length + " vectors");
        init(vec);
    }
    private static final double DAMPEN_COVARIANCE_COEFF = 1;

    private void init(double[][] vec) {
        int dim = vec[0].length;
        //umontreal.iro.lecuyer.stat.list.ListOfTallies dataset = new umontreal.iro.lecuyer.stat.list.ListOfTallies();
        //dataset = dataset.createWithTallyStore(vec[0].length);
        ///for(double[] v: vec){
        //     dataset.add(v);
        // }
        DoubleMatrix2D vecMtx = Algebra.DEFAULT.transpose(new DenseDoubleMatrix2D(vec));

        double[][] transposed = vecMtx.toArray();

        double[][] covMtx = new double[dim][dim];

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                covMtx[i][j] = Correlation.getCenteredCovariance(transposed[i], transposed[j]);
            }
        }

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                if (i != j) {
                    covMtx[i][j] *= DAMPEN_COVARIANCE_COEFF;
                }
            }

        }



        covarianceMatrix = new DenseDoubleMatrix2D(covMtx);
        correlMatrix = new DenseDoubleMatrix2D(dim, dim);

        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                correlMatrix.setQuick(i, j, covMtx[i][j] / Math.sqrt(covMtx[i][i] * covMtx[j][j]));
            }
        }
        stDevMtx = (DenseDoubleMatrix2D) covarianceMatrix.copy();


        for (int x = 0; x < stDevMtx.columns(); x++) {
            for (int y = 0; y < stDevMtx.rows(); y++) {
                if (y != x) {
                    stDevMtx.set(x, y, 0.0);
                }
            }
        }
        try {
            invertedStDevMtx = (DenseDoubleMatrix2D) algebra.inverse(stDevMtx);
            invertedCovMtx = (DenseDoubleMatrix2D) algebra.inverse(covarianceMatrix);
            invCorrelMatrix = (DenseDoubleMatrix2D) algebra.inverse(correlMatrix);
            cd = new CholeskyDecomposition(invertedCovMtx);
            transL = (DenseDoubleMatrix2D) algebra.transpose(cd.getL());
        } catch (IllegalArgumentException e) {
            logger.print(e.getMessage());
        }
    }
    /*
     public MahalonobisSpace(double [][] vec) {
     init(vec);
     }*/

    public double getNormEuclideanCosine(DenseDoubleMatrix1D vector1, DenseDoubleMatrix1D vector2) throws java.lang.IllegalArgumentException {
        if (vector1.size() != vector2.size()) {
            throw new java.lang.IllegalArgumentException("Size of vector1 (" + String.valueOf(vector1.size()) + ") doesn't match size of vector2 (" + String.valueOf(vector2.size()) + ")");
        }
        if (vector1.size() != invertedStDevMtx.columns()) {
            throw new java.lang.IllegalArgumentException("Size of vectors doesn't match covarience matrix size");
        }
        DenseDoubleMatrix1D num1 = (DenseDoubleMatrix1D) algebra.mult(invertedStDevMtx, vector2);
        double numerator = algebra.mult(vector1, num1);
        double denom1 = getNormEuclideanLength(vector1);
        double denom2 = getNormEuclideanLength(vector2);
        double denominator = denom1 * denom2;
        return numerator / denominator;
    }

    public double getCorrelMahCosine(DenseDoubleMatrix1D vector1, DenseDoubleMatrix1D vector2) throws java.lang.IllegalArgumentException {
        if (vector1.size() != vector2.size()) {
            throw new java.lang.IllegalArgumentException("Size of vector1 (" + String.valueOf(vector1.size()) + ") doesn't match size of vector2 (" + String.valueOf(vector2.size()) + ")");
        }
        try {
            if (vector1.size() != invCorrelMatrix.columns()) {
                throw new java.lang.IllegalArgumentException("Size of vectors doesn't match covarience matrix size");
            }
        } catch (NullPointerException e) {
            logger.print(vector1 + ", " + vector2 + ", " + invCorrelMatrix);
            e.printStackTrace();
        }
        DenseDoubleMatrix1D num1 = (DenseDoubleMatrix1D) algebra.mult(invCorrelMatrix, vector2);
        double numerator = algebra.mult(vector1, num1);
        double denom1 = getCorrelMahLength(vector1);
        double denom2 = getCorrelMahLength(vector2);
        double denominator = denom1 * denom2;
        return numerator / denominator;
    }

    public double getCorrelMahLength(DenseDoubleMatrix1D vector) {
        if (vector.size() != invCorrelMatrix.columns()) {
            throw new java.lang.IllegalArgumentException("Size of vectors doesn't match covarience matrix size");
        }
        double tmp = algebra.mult(vector, (DenseDoubleMatrix1D) algebra.mult(invCorrelMatrix, vector));
        return Math.sqrt(tmp);
    }

    public double getMahalonobisCosine(double[] vector1, double[] vector2) throws java.lang.IllegalArgumentException {
        if (vector1.length != vector2.length) {
            throw new java.lang.IllegalArgumentException("Size of vector1 (" + String.valueOf(vector1.length) + ") doesn't match size of vector2 (" + String.valueOf(vector2.length) + ")");
        }
        if (vector1.length != invertedCovMtx.columns()) {
            throw new java.lang.IllegalArgumentException("Size of vectors doesn't match covarience matrix size");
        }

        DenseDoubleMatrix1D num1 = (DenseDoubleMatrix1D) algebra.mult(invertedCovMtx, new DenseDoubleMatrix1D(vector2));
        double numerator = algebra.mult(new DenseDoubleMatrix1D(vector1), num1);
        double denom1 = getMahalonobisLength(vector1);
        double denom2 = getMahalonobisLength(vector2);
        double denominator = denom1 * denom2;
        return numerator / denominator;
    }

    public double getNormEuclideanLength(DenseDoubleMatrix1D vector) {
        if (vector.size() != invertedStDevMtx.columns()) {
            throw new java.lang.IllegalArgumentException("Size of vectors doesn't match covarance matrix size");
        }
        double tmp = algebra.mult(vector, (DenseDoubleMatrix1D) algebra.mult(invertedStDevMtx, vector));
        return Math.sqrt(tmp);
    }

    public double getEuclideanLength(DenseDoubleMatrix1D vector) throws java.lang.IllegalArgumentException {
        return Math.sqrt(algebra.mult(vector, vector));
    }

    public double getMahalonobisLength(double[] vector) throws java.lang.IllegalArgumentException {
        if (vector.length != invertedCovMtx.columns()) {
            throw new java.lang.IllegalArgumentException("Size of vectors doesn't match covarance matrix size");
        }
        DenseDoubleMatrix1D vec2 = new DenseDoubleMatrix1D(vector);
        double tmp = algebra.mult(vec2, (DenseDoubleMatrix1D) algebra.mult(invertedCovMtx, vec2));
        return Math.sqrt(tmp);
    }

    public DenseDoubleMatrix1D orthogonizeVector(DenseDoubleMatrix1D vector) {
        return (DenseDoubleMatrix1D) algebra.mult(transL, vector);
    }

    public DenseDoubleMatrix1D deorthogonizeVector(DenseDoubleMatrix1D vector) {
        return (DenseDoubleMatrix1D) algebra.mult(algebra.inverse(transL), vector);
    }
}
