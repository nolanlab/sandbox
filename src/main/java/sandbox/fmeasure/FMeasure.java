/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.fmeasure;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

/**
 *
 * @author Nikolay
 */
public class FMeasure {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        /*
         for (int i = 1; i <= 30; i++) {
         if(i==2||i==22){
         continue;
         }
         test(i);
         }
*/
         if (args.length != 2) {
         System.err.println("Usage java -jar FMeasure.jar [clusters] [labels]\nBoth cluster and labels are single-column txt files of zero-based indices.\nRows labelled with the same number belong to the same cluster or class. Zero means unassigned");
         System.exit(0);
         }
        int[] clusters = IO.getListOfIntegersFromStream(new FileInputStream(new File(args[0])), -1);
        int[] labels = IO.getListOfIntegersFromStream(new FileInputStream(new File(args[1])), -1);
        getFMeasures(clusters, labels);
    }

    private static void test(int num) throws Exception {
        String s = String.format("%03d", num);
        int[] clustersADI = IO.getListOfIntegersFromStream(new FileInputStream(new File("C:\\Users\\Nikolay\\Local Working Folder\\FlowCapI\\data\\Submissions\\CH1\\ADICyt\\NDD\\" + s + ".csv")), -1);
        int[] clustersFlowMeans = IO.getListOfIntegersFromStream(new FileInputStream(new File("C:\\Users\\Nikolay\\Local Working Folder\\FlowCapI\\data\\Submissions\\CH1\\flowMeans\\NDD\\" + s + ".csv")), -1);
        int[] clustersXshift = IO.getListOfIntegersFromStream(new FileInputStream(new File("C:\\Users\\Nikolay\\Local Working Folder\\FlowCapI\\X-shift NDD\\" + s + ".fcs_assgnments.txt")), -1);
        int[] labels = IO.getListOfIntegersFromStream(new FileInputStream(new File("C:\\Users\\Nikolay\\Local Working Folder\\FlowCapI\\data\\Labels\\NDD\\" + s + ".csv")), -1);

        //System.err.println("\nADI:");
        getFMeasures(clustersADI, labels);
        //System.err.println("\nflowMeans:");
        getFMeasures(clustersFlowMeans, labels);
        //System.err.println("\nX-shift:");
        getFMeasures(clustersXshift, labels);

    }

    public static double[] getFMeasures(int[] clusters, int[] labels) {
        if (clusters.length != labels.length) {
            throw new IllegalArgumentException("clusters.length != labels.length");
        }

        int maxClusterID = 0;
        int maxLabel = 0;

        int len = labels.length;

        for (int i = 0; i < len; i++) {
            maxLabel = Math.max(labels[i], maxLabel);
            maxClusterID = Math.max(clusters[i], maxClusterID);
        }

        double[][] adjMatrix = new double[maxLabel][maxClusterID];

        for (int i = 0; i < len; i++) {
            if (labels[i] * clusters[i] > 0) {
                adjMatrix[labels[i] - 1][clusters[i] - 1]++;
            }
        }

        double precMatrix[][] = new double[adjMatrix.length][adjMatrix[0].length];
        for (int i = 0; i < adjMatrix[0].length; i++) {
            double sumCol = 0;
            for (int j = 0; j < adjMatrix.length; j++) {
                sumCol += adjMatrix[j][i];
            }
            for (int j = 0; j < adjMatrix.length; j++) {
                precMatrix[j][i] = adjMatrix[j][i] / sumCol;
            }
        }
        double recallMtx[][] = new double[adjMatrix.length][adjMatrix[0].length];
        for (int i = 0; i < adjMatrix.length; i++) {
            double sumRow = 0;
            for (int j = 0; j < adjMatrix[0].length; j++) {
                sumRow += adjMatrix[i][j];
            }
            for (int j = 0; j < adjMatrix[0].length; j++) {
                recallMtx[i][j] = adjMatrix[i][j] / sumRow;
            }
        }

        double fMatrix[][] = new double[adjMatrix.length][adjMatrix[0].length];
        for (int i = 0; i < adjMatrix.length; i++) {
            for (int j = 0; j < adjMatrix[0].length; j++) {
                fMatrix[i][j] = /*recallMtx[i][j];*/ (2.0 * precMatrix[i][j] * recallMtx[i][j]) / (precMatrix[i][j] + recallMtx[i][j]);
                if (Double.isInfinite(fMatrix[i][j]) || Double.isNaN(fMatrix[i][j])) {
                    fMatrix[i][j] = 0;
                }
            }
        }

        fMatrix = hungarianMax_extern(fMatrix);

        double[] out = new double[maxLabel];
        for (int i = 0; i < maxLabel; i++) {
            double max = 0;
            int maxIDX = 0;
            for (int j = 0; j < maxClusterID; j++) {
                if (fMatrix[i][j] > max) {
                    max = fMatrix[i][j];
                    maxIDX = j;
                }
            }
            System.err.print(fMatrix[i][maxIDX] + "\t");
            out[i] = fMatrix[i][maxIDX];
        }
        System.err.println("");

        return out;
    }

    private static double[][] hungarianMax_extern(double[][] fMatrix) {
        double[][] out = new double[fMatrix.length][fMatrix[0].length];

        double[][] inter = new double[fMatrix.length][fMatrix[0].length];
        for (int i = 0; i < inter.length; i++) {
            Arrays.fill(inter[i], 1.0);
        }
        for (int i = 0; i < fMatrix.length; i++) {
            for (int j = 0; j < fMatrix[0].length; j++) {
                inter[i][j] = 1.0 - fMatrix[i][j];
            }
        }
        int[] assignments = (new HungarianAlgorithm(inter)).execute();
        for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] != -1) {
                out[i][assignments[i]] = fMatrix[i][assignments[i]];
            }
        }
        return out;
    }

}
