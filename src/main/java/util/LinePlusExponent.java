/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Nikolay
 */
public class LinePlusExponent {

    public static double findElbowPointLinePlusExp(double[] y, double[] x) {
        if (y.length != x.length) {
            throw new IllegalStateException("Arrays are of a different size");
        }
        if (y.length < 5) {
            throw new IllegalStateException("At leaset 5 measurements are required");
        }

        double[][] table = new double[x.length][2];

        for (int i = 0; i < table.length; i++) {
            table[i] = new double[]{x[i], y[i]};
        }

        Arrays.sort(table, new Comparator<double[]>() {
            @Override
            public int compare(double[] o1, double[] o2) {
                return Double.compare(o1[0], o2[0]);
            }
        });

        for (int i = 0; i < table.length; i++) {
            x[i] = table[i][0];
            y[i] = table[i][1];
            logger.print(x[i],y[i]);
        }

        double minSumSq = Double.MAX_VALUE;
        int bestI = 0;
        double bestB1 = 0, bestB2 = 0, bestA1 = 0, bestA2 = 0;
        for (int i = 2; i < x.length - 2; i++) {
            double[] x1 = Arrays.copyOfRange(x, 0, i + 1);
            double[] y1 = Arrays.copyOfRange(y, 0, i + 1);
            double[] x2 = Arrays.copyOfRange(x, i, x.length);
            double[] y2 = Arrays.copyOfRange(y, i, y.length);

            double b2 = getRegressionSlope(y2, x2);

            double a2 = getRegressionOffset(y2, x2, b2);

            for (int j = 0; j < y1.length; j++) {
                y1[j] = Math.log(Math.max(1, y1[j] - (b2 * x1[j] + a2)));
            }
            double b1 = getRegressionSlope(y1, x1);
            double a1 = getRegressionOffset(y1, x1, b1);

            double currSumSq = getSumSquaresLineExp(y, x, b1, a1, b2, a2);//getSumSquaresExp(y1, x1, b1, a1) + getSumSquares(y2, x2, b2, a2);
            
            for (int j = 0; j < 20; j++) {
                double newB1 = b1;
                for (double bexp = Math.min(b1 * 0.5,b1*2); bexp < Math.max(b1 * 0.5,b1*2); bexp += 0.001) {
                    //logger.print(bexp, b1*1.2);
                    if (getSumSquaresLineExp(y, x, bexp, a1, b2, a2) < currSumSq) {
                        newB1 = bexp;
                        currSumSq = getSumSquaresLineExp(y, x, bexp, a1, b2, a2);
                    }
                }
                b1 = newB1;
                
                double newA1 = a1;
                for (double aexp = Math.min(a1 * 0.5,a1*2); aexp < Math.max(a1 / 0.5,a1*2); aexp += 0.001) {
                    if (getSumSquaresLineExp(y, x, b1, aexp, b2, a2) < currSumSq) {
                        newA1 = aexp;
                        currSumSq = getSumSquaresLineExp(y, x, b1, aexp, b2, a2);
                    }
                }
                a1 = newA1;
            }
            
            
            if (currSumSq < minSumSq) {
                minSumSq = currSumSq;
                bestI = i;
                bestB1 = b1;
                bestB2 = b2;
                bestA1 = a1;
                bestA2 = a2;
            }
        }
        logger.print("bestI = " + bestI + ", x = " + x[bestI], "b1 = " + bestB1, "b2 = " + bestB2, "a1 = " + bestA1, "a2 = " + bestA2);
        return ( x[bestI]);
    }
    

    public static void main(String[] args) {
        double[] x = new double[]{200, 190, 180, 170, 160, 150, 140, 130, 120, 110, 100, 95, 90, 85, 80, 75, 70, 65, 60, 55, 50, 45, 40, 35, 30, 5, 20, 15, 10, 25};
        double[] y = new double[]{18, 19, 19, 20, 20, 20, 21, 21, 22, 22, 23, 23, 23, 23, 24, 25, 25, 26, 26, 27, 28, 29, 31, 33, 34, 149, 47, 59, 89, 40};
        logger.print(findElbowPointLinePlusExp(y, x));
    }

    public static double getRegressionSlope(double[] y, double[] x) {
        double slope = Correlation.getCenteredCovariance(y, x) / Correlation.getCenteredCovariance(x, x);
        return slope;
    }

    public static double avg(double[] vec) {
        double res = 0;
        for (double d : vec) {
            res += d;
        }
        return res / vec.length;
    }

    public static double getRegressionOffset(double[] y, double[] x, double slope) {
        return avg(y) - (avg(x) * slope);
    }

    public static double getSumSquares(double[] y, double[] x, double slope, double offset) {
        double sumSq = 0;
        for (int i = 0; i < x.length; i++) {
            sumSq += Math.pow(y[i] - ((x[i] * slope) + offset), 2);
        }
        return sumSq;
    }

    public static double getSumSquaresExp(double[] y, double[] x, double slope, double offset) {
        double sumSq = 0;
        for (int i = 0; i < x.length; i++) {
            sumSq += Math.pow(y[i] - Math.exp(((x[i] * slope) + offset)), 2);
        }
        return sumSq;
    }

    public static double getSumSquaresLineExp(double[] y, double[] x, double slopeExp, double offsetExp, double slope, double offset) {
        double sumSq = 0;
        for (int i = 0; i < x.length; i++) {
            sumSq += Math.pow(y[i] - (Math.exp(((x[i] * slopeExp) + offsetExp)) + ((x[i] * slope) + offset)), 2);
        }
        return sumSq;
    }
}
