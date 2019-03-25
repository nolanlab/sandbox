/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JOptionPane;
import util.logger;

/**
 *
 * @author Nikolay
 */
public class DistanceMatrix {

    DistanceMeasure dm;
    double[][] matrix;
    Dataset baseDataset;
    Dataset derivedDataset;

    public DistanceMeasure getDistanceMeasure() {
        return dm;
    }

    public void writeToFile(File f) throws IOException {
        if (f == null) {
            return;
        }
        String separator = JOptionPane.showInputDialog("Please specify the column separator, or leave blank to use tabs");
        if (separator.length() == 0) {
            separator = "\t";
        }
        double ths = Double.NEGATIVE_INFINITY;
        try {
            Double.parseDouble(JOptionPane.showInputDialog("Please specify the cutoff for minimal similarity, or leave blank for no cutoff"));
        } catch (NumberFormatException e) {
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(f));

        for (Datapoint d1 : baseDataset.getDatapoints()) {
            for (Datapoint d2 : baseDataset.getDatapoints()) {
                if (!d2.equals(d1)) {
                    double val = Math.max(dm.distanceToSimilarity(getValue(d1, d2)), ths);
                    out.append(d1.getFullName() + separator + d2.getFullName() + separator + val + "\n");
                }
            }
        }
        out.flush();
        out.close();
        JOptionPane.showMessageDialog(null, "Export Done!");
    }

    public Dataset getBaseDataset() {
        return baseDataset;
    }

    protected DistanceMatrix(Dataset nd) {
        this.baseDataset = nd;
    }

    public DistanceMatrix(Dataset nd, final DistanceMeasure dm) {
        this.baseDataset = nd;

        Datapoint[] dp = nd.getDatapoints();
        final int len = dp.length;
        this.dm = dm;
        if (len < 2) {
            throw new IllegalArgumentException("There must be at least two datapoints");
        }

        final double[][] vec = nd.getVectors();

        logger.print("Computing distance matrix for " + len + " datapoints");
        final AtomicInteger xGlobal = new AtomicInteger(-1);

        ThreadGroup tg = new ThreadGroup("ConvolvingThreads");

        Thread[] t = new Thread[Runtime.getRuntime().availableProcessors()];

        matrix = new double[len][];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = new double[len - i];
        }
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(tg, new Runnable() {

                @Override
                public void run() {
                    do {
                        int i = xGlobal.addAndGet(1);
                        if (i >= len) {
                            return;
                        }

                        if (i % 100 == 0) {
                            System.err.println("Distance mtx: " + i);
                        }
                        for (int j = i; j < len; j++) {
                            double dist = dm.getDistance(vec[i], vec[j]);
                            setValue(i, j, dist);
                        }
                    } while (true);

                }
            });
            t[i].start();
        }
        do {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        } while (tg.activeCount() > 0);
    }

    public double getValue(Datapoint dp1, Datapoint dp2) {
        int idx1 = dp1.getID();
        int idx2 = dp2.getID();
        return (idx2 > idx1) ? matrix[idx1][idx2 - idx1] : matrix[idx2][idx1 - idx2];
    }

    private void setValue(int idx1, int idx2, double val) {
        if (idx2 > idx1) {
            matrix[idx1][idx2 - idx1] = val;
        } else {
            matrix[idx2][idx1 - idx2] = val;
        }
    }
}
