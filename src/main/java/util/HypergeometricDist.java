/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import cern.jet.random.Binomial;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import java.math.BigDecimal;
import java.math.MathContext;


/**
 *
 * @author Nikolay
 */
public class HypergeometricDist {

    private static Binomial bn1, bn2, bn3;
    private static RandomEngine re;

    /**
     * This method calculates p-value of hypergeometric distribution through
     * logbinomal, it works on large numbers.
     *
     * @param m the size of the population
     * @param mt size of success items in population
     * @param n size of sample drawn from distribution
     * @param nt number of success from the sample
     * @return the probability of getting as many or more success out of a
     * random sample.
     */
    public static double pValHyperGeom(int m, int mt, int n, int nt) {
        try {
            re = new MersenneTwister(new java.util.Date());

            double pValue = 0;
            double p = n / (double) m; //it works for any p, but they recommend this one.

            if (mt == 0) //this shouldn't happen, but might as well check.
            {
                return 0;
            }
            for (int i = nt; i <= Math.min(mt, n); i++) {

                bn1 = new Binomial(mt, p, re);
                bn2 = new Binomial(m - mt, p, re);
                bn3 = new Binomial(m, p, re);
                pValue += bn1.pdf(i) * bn2.pdf(n - i) / bn3.pdf(n);
            }
            return pValue;
        } catch (IllegalArgumentException e) {
            return Double.NaN;
        }
    }

    public static BigDecimal logPValHyperGeomBigDecimal(int N, int K, int n, int k1) {
        BigDecimal pval = new BigDecimal(0, MathContext.DECIMAL128);
        for (int k = k1; k < Math.min(n, K); k++) {
            BigDecimal logpdf = new BigDecimal(logPDF(N, K, n, k), MathContext.DECIMAL128);
            pval.add(BigFunctions.ln((BigFunctions.exp(logpdf.subtract(pval), 1).add(BigDecimal.ONE)), 1));
        }
        return pval;
    }

    public static double logPValHyperGeom(int N, int K, int n, int k1) {
        /* double pval = 0;     
         for (int k = k1; k < Math.min(n, K); k++) {
         pval += Math.exp(logPDF(N, K, n, k)+300);
         if(Double.isNaN(pval)){
         logger.print("NAN", N, K, n, k1);               
         }
         }
         pval = Math.log(pval) - 300;*/
        return logPValHyperGeomBigDecimal(N, K, n, k1).doubleValue();
    }

    public static double logPDF(int N, int K, int n, int k) {
        double ret = logBinomialCoeff(K, k) + logBinomialCoeff(N - K, n - k) - logBinomialCoeff(N, n);

        return ret;
    }

    private static double logBinomialCoeff(int n, int k) {
        return logGamma(n) - (logGamma(k) + logGamma(n - k));
    }

    private static double logGamma(double z) {
        return (z - 0.5) * Math.log(z) - z + 0.5 * Math.log(2 * Math.PI);
    }
}
