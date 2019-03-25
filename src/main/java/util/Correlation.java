/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import sandbox.clustering.Datapoint;
import sandbox.clustering.Dataset;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author Nikolay
 */
public class Correlation {

    public static double getCenteredCorrelation(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("The lists differ in size");
        }
        double avg1 = 0;
        double avg2 = 0;
        double prod = 0;
        double len1 = 0;
        double len2 = 0;
        for (int i = 0; i < vec1.size(); i++) {
            avg1 += vec1.get(i);
            avg2 += vec2.get(i);
        }
        avg1 /= (double) vec1.size();
        avg2 /= (double) vec1.size();

        for (int i = 0; i < vec1.size(); i++) {
            prod += (vec1.get(i) - avg1) * (vec2.get(i) - avg2);
            len1 += Math.pow(vec1.get(i) - avg1, 2);
            len2 += Math.pow(vec2.get(i) - avg2, 2);
        }
        len1 = Math.sqrt(len1);
        len2 = Math.sqrt(len2);
        return prod / (len1 * len2);
    }

    public static class BinomialContigencyMatrix {

        public double[][] likelihoodRatioMatrix;
        public double[][] pValueMatrix;
        private String[] parameterNames;

        public String[] getParameterNames() {
            return Arrays.copyOf(parameterNames, parameterNames.length);
        }

        public BinomialContigencyMatrix(double[][] likelihoodRatioMatrix, double[][] pValueMatrix, String[] parameterNames) {
            this.likelihoodRatioMatrix = likelihoodRatioMatrix;
            this.pValueMatrix = pValueMatrix;
            this.parameterNames = parameterNames;
        }
    }

    public static double getCenteredCorrelation(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("The lists differ in size");
        }
        double avg1 = 0;
        double avg2 = 0;
        double prod = 0;
        double len1 = 0;
        double len2 = 0;
        for (int i = 0; i < vec1.length; i++) {
            avg1 += vec1[i];
            avg2 += vec2[i];
        }
        avg1 /= (double) vec1.length;
        avg2 /= (double) vec1.length;

        for (int i = 0; i < vec1.length; i++) {
            prod += (vec1[i] - avg1) * (vec2[i] - avg2);
            len1 += Math.pow(vec1[i] - avg1, 2);
            len2 += Math.pow(vec2[i] - avg2, 2);
        }
        len1 = Math.sqrt(len1);
        len2 = Math.sqrt(len2);
        return prod / (len1 * len2);
    }

    public static double getCenteredCovariance(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("The lists differ in size");
        }
        double avg1 = 0;
        double avg2 = 0;
        double prod = 0;
        for (int i = 0; i < vec1.length; i++) {
            avg1 += vec1[i];
            avg2 += vec2[i];
        }
        avg1 /= (double) vec1.length;
        avg2 /= (double) vec2.length;

        for (int i = 0; i < vec1.length; i++) {
            prod += (vec1[i] - avg1) * (vec2[i] - avg2);
        }
        return prod / vec1.length;
    }

    public static double getUncenteredCorrelation(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("The lists differ in size");
        }
        double prod = 0;
        double len1 = 0;
        double len2 = 0;

        for (int i = 0; i < vec1.size(); i++) {
            prod += (vec1.get(i)) * (vec2.get(i));
            len1 += Math.pow(vec1.get(i), 2);
            len2 += Math.pow(vec2.get(i), 2);
        }
        len1 = Math.sqrt(len1);
        len2 = Math.sqrt(len2);
        return prod / (len1 * len2);
    }

    public static double getUncenteredCorrelation(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("The lists differ in size");
        }
        double prod = 0;
        double len1 = 0;
        double len2 = 0;

        for (int i = 0; i < vec1.length; i++) {
            prod += (vec1[i]) * (vec2[i]);
            len1 += Math.pow(vec1[i], 2);
            len2 += Math.pow(vec2[i], 2);
        }

        len1 = Math.sqrt(len1);
        len2 = Math.sqrt(len2);
        return prod / (len1 * len2);
    }

    public static double getUncenteredCorrelation(Double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("The lists differ in size");
        }
        double prod = 0;
        double len1 = 0;
        double len2 = 0;

        for (int i = 0; i < vec1.length; i++) {
            prod += (vec1[i]) * (vec2[i]);
            len1 += Math.pow(vec1[i], 2);
            len2 += Math.pow(vec2[i], 2);
        }

        len1 = Math.sqrt(len1);
        len2 = Math.sqrt(len2);
        return prod / (len1 * len2);
    }

    public static double getUncenteredCorrelation(double[] vec1, Double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("The lists differ in size");
        }
        double prod = 0;
        double len1 = 0;
        double len2 = 0;

        for (int i = 0; i < vec1.length; i++) {
            prod += (vec1[i]) * (vec2[i]);
            len1 += Math.pow(vec1[i], 2);
            len2 += Math.pow(vec2[i], 2);
        }

        len1 = Math.sqrt(len1);
        len2 = Math.sqrt(len2);
        return prod / (len1 * len2);
    }

    public static double getUncenteredCorrelation(Double[] vec1, Double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("The lists differ in size");
        }
        double prod = 0;
        double len1 = 0;
        double len2 = 0;

        for (int i = 0; i < vec1.length; i++) {
            prod += (vec1[i]) * (vec2[i]);
            len1 += Math.pow(vec1[i], 2);
            len2 += Math.pow(vec2[i], 2);
        }

        len1 = Math.sqrt(len1);
        len2 = Math.sqrt(len2);
        return prod / (len1 * len2);
    }

    public static double[][] estimateParameterCorrelationMatrix(double[][] profiles) {
        DoubleMatrix2D cov = new DenseDoubleMatrix2D(estimateParameterCovarianceMatrix(profiles));
       
        DoubleMatrix2D corr = new DenseDoubleMatrix2D(cov.rows(), cov.columns());
        
        for (int i = 0; i < cov.rows(); i++) {
            for (int j = 0; j < cov.columns(); j++) {
                corr.setQuick(i, i, cov.getQuick(i, j)/Math.sqrt(cov.getQuick(i, i)*cov.getQuick(j, j)));
            }
        }
        return corr.toArray();
    }
    
     public static double[][] estimateParameterCovarianceMatrix(double[][] profiles) {
         DoubleMatrix2D mtx = Algebra.DEFAULT.transpose(new DenseDoubleMatrix2D(profiles));
        return Algebra.DEFAULT.mult(mtx, mtx).toArray();
    }
}
