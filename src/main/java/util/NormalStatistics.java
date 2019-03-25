/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;


/**
 *
 * @author Nikolay
 */
public class NormalStatistics {

    double[] sample;
    double mean;
    double SD;
    int sampleSize;

    public NormalStatistics(double[] sample) {
        if (sample.length < 2) {
            throw new IllegalArgumentException("Sample must contain at least two measurements");
        }
        this.sample = sample;
        init();
    }

    private void init() {
        mean = 0;
        SD = 0;
        sampleSize = sample.length;
        for (double d : sample) {
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                sampleSize--;
                continue;
            }
            mean += d;
        }
        mean /= sampleSize;
        for (double d : sample) {
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                continue;
            }
            SD += (d - mean) * (d - mean);
        }
        SD /= sampleSize - 1;
        SD = Math.sqrt(SD);
    }

    public NormalStatistics(ArrayList<Double> sample) {
        if (sample.size() < 2) {
            throw new IllegalArgumentException("Sample must contain at least two measurements");
        }

        double[] samp = new double[sample.size()];
        for (int i = 0; i < samp.length; i++) {
            samp[i] = sample.get(i);
        }
        this.sample = samp;
        init();
    }

    public double getSD() {
        return SD;
    }

    public double getMean() {
        return mean;
    }

    public double getSampleSize() {
        return sampleSize;
    }

    
}
