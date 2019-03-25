/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.dataIO;

import sandbox.clustering.Datapoint;
import sandbox.clustering.Dataset;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import util.MatrixOp;
import util.NormalStatistics;
import util.Optimization;
import util.logger;

/**
 *
 * @author Nikolay
 */
public class DatasetImporter {

    private static final boolean output = true;

    /**
     * @return a number of rows imported
     */
    public static Dataset importDataset(DatasetStub[] stubs, ImportConfigObject config) {

        double sigma = config.euclidean_len_ths > 0 ? (Math.sqrt(config.euclidean_len_ths / (config.featureColumnNames.length * 0.33))) / 100 : 0.001;

        NormalGen ng = new NormalGen(new MRG32k3a(), 0, sigma);

        if (output) {
            System.err.println("Dataset Import Sigma = " + sigma);
        }

        LinkedList<Datapoint> datapoints = new LinkedList<>();
        int dpID = 0;

        int dim = config.featureColumnNames.length;

        int totalCount = 0;
        for (DatasetStub s : stubs) {
            totalCount += s.getRowCount();
        }

        if (totalCount < 3) {
            throw new IllegalStateException("The data files contain " + totalCount + " measurements in total, while the minimum is 3. Aborting import.");
        }

        DatasetStub first = stubs[0];

        String[] shortColNamesF = first.getShortColumnNames();
        String[] longColNamesF = first.getLongColumnNames();

        String[] featureColNamesDS = new String[config.featureColumnNames.length];
        String[] sideColNamesDS = new String[config.sideColumnNames.length];

        for (int i = 0; i < featureColNamesDS.length; i++) {
            String colName = config.featureColumnNames[i];
            int currColIDX = -1;
            for (int j = 0; j < shortColNamesF.length; j++) {
                if (colName.equalsIgnoreCase(shortColNamesF[j])) {
                    currColIDX = j;
                }
            }
            if (currColIDX == -1) {
                throw new IllegalArgumentException("Invalid channel name: " + colName + "\n Accepted channel names:" + Arrays.toString(shortColNamesF));
            }
            featureColNamesDS[i] = longColNamesF[currColIDX] + ":" + shortColNamesF[currColIDX];
        }

        for (int i = 0; i < sideColNamesDS.length; i++) {
            String colName = config.sideColumnNames[i];
            int currColIDX = -1;
            for (int j = 0; j < shortColNamesF.length; j++) {
                if (colName.equalsIgnoreCase(shortColNamesF[j])) {
                    currColIDX = j;
                }
            }
            if (currColIDX == -1) {
                throw new IllegalArgumentException("Invalid channel name: " + colName + "\n Accepted channel names:" + Arrays.toString(shortColNamesF));
            }
            sideColNamesDS[i] = longColNamesF[currColIDX] + ":" + shortColNamesF[currColIDX];
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Following rows were skipped:");

        int cntSkip = 0;
        for (DatasetStub s : stubs) {
            if (output) {
                logger.print("Importing " + s.getFileName());
            }
            //LinkedList<String> fileDatapointNames = new LinkedList<>();
            int[] featureColIdx = new int[config.featureColumnNames.length];
            String[] colNames = s.getShortColumnNames();
            String[] longColNames = s.getLongColumnNames();

            for (int i = 0; i < colNames.length; i++) {
                if (Optimization.indexOf(colNames, colNames[i]) != i) {
                    throw new IllegalArgumentException("Duplicate short column name#" + i + " " + colNames[i] + " \n" + Arrays.toString(colNames));
                };
            }

            for (int i = 0; i < featureColIdx.length; i++) {
                String colName = config.featureColumnNames[i];
                featureColIdx[i] = -1;
                for (int j = 0; j < colNames.length; j++) {
                    if (colName.equalsIgnoreCase(colNames[j])) {
                        featureColIdx[i] = j;
                    }
                }
                if (featureColIdx[i] == -1) {
                    throw new IllegalArgumentException("Invalid channel name: " + colName + "\n Accepted channel names:" + Arrays.toString(colNames));
                }
            }

            int[] sideColIdx = new int[config.sideColumnNames.length];
            for (int i = 0; i < sideColIdx.length; i++) {
                String colName = config.sideColumnNames[i];
                sideColIdx[i] = -1;
                for (int j = 0; j < colNames.length; j++) {
                    if (colName.equalsIgnoreCase(colNames[j])) {
                        sideColIdx[i] = j;
                    }
                }
                if (sideColIdx[i] == -1) {
                    throw new IllegalArgumentException("Invalid channel name: " + colName + "\n Accepted channel names:" + Arrays.toString(colNames) + "\nFull channel name: " + config.sideColumnNames[i]);
                }
            }

            int cnt = 0;
            th:
            for (int i = 0; i < s.getRowCount(); i++) {

                double[] row = s.getRow(i);

                if (row == null) {
                    continue;
                }

                double[] dataVec = MatrixOp.subset(s.getRow(i), featureColIdx);
                double[] sideVec = MatrixOp.subset(s.getRow(i), sideColIdx);
                if (i % 1000 == 0) {
                    if (output) {
                        logger.print(i);
                    }
                }
                for (double[] vec : new double[][]{dataVec, sideVec}) {

                    if (config.noise_threshold > 0) {

                        for (int j = 0; j < vec.length; j++) {
                            double val = vec[j];
                            double sign = Math.signum(val);
                            val -= config.noise_threshold;
                            vec[j] = sign * Math.max(val, 0);
                        }
                    }
                    if (config.transf == ImportConfigObject.TransformationType.ASINH) {
                        for (int j = 0; j < vec.length; j++) {
                            double val = vec[j];
                            val /= config.scaling_factor;
                            val = Math.log(val + Math.sqrt(val * val + 1));
                            vec[j] = val;
                        }

                    }
                    if (config.transf == ImportConfigObject.TransformationType.DOUBLE_ASINH) {
                        for (int j = 0; j < vec.length; j++) {
                            double val = vec[j];
                            val /= config.scaling_factor;
                            val = Math.log(val + Math.sqrt(val * val + 1));
                            val = Math.log(val + Math.sqrt(val * val + 1));
                            vec[j] = val;
                        }
                    }

                }
                if (config.noise_threshold > 0) {
                    for (int j = 0; j < dataVec.length; j++) {
                        if (dataVec[j] == 0) {
                            dataVec[j] = ng.nextDouble();//(Math.random() - 0.5) * sigma;
                        }
                    }
                }
                if (MatrixOp.lenght(dataVec) > config.euclidean_len_ths) {
                    //System.err.println("importing:"+s.getRowName(i) );
                    Datapoint d = new Datapoint(s.getRowName(i), dataVec, sideVec, dpID++, s.getFileName(), i);

                    //System.err.println("importing:"+s.getRowName(i)+"|"+d.getName());
                    datapoints.add(d);
                    cnt++;
                    if (cnt >= config.limitRowsPerFile && config.limitRowsPerFile > 0) {
                        break th;
                    }
                }
            }

            if (output) {
                logger.print("done reading file");
            }
            try {
                s.close();
            } catch (IOException ex) {
                logger.showException(ex);
            }

            for (String skp : s.getSkippedRows()) {
                sb.append("\n").append(s.getFileName()).append(", row#").append(skp);
            }

            cntSkip += s.getSkippedRows().length;
        }

        if (cntSkip > 0) {
            logger.print(sb.toString());
            JScrollPane sp = new JScrollPane(new JTextArea(sb.toString()));
            sp.setPreferredSize(new Dimension(500, 500));
            sp.setMinimumSize(new Dimension(500, 500));
            sp.setMaximumSize(new Dimension(500, 500));
            JOptionPane.showMessageDialog(null, sp, cntSkip + " events could not be imported", JOptionPane.INFORMATION_MESSAGE);
        }

        double[] scalingVec = null;
        Datapoint[] dp = datapoints.toArray(new Datapoint[datapoints.size()]);
        datapoints = null;
        switch (config.rescale) {
            case QUANTILE:
                scalingVec = new double[dim];
                double q = config.quantile;
                int idx1 = (int) Math.floor(q * dp.length);

                double[] val = new double[dp.length];

                for (int i = 0; i < dim; i++) {
                    for (int j = 0; j < dp.length; j++) {
                        val[j] = dp[j].getVector()[i];
                    }
                    Arrays.sort(val);
                    scalingVec[i] = val[idx1];
                }
                break;
            case SD:
                scalingVec = new double[dim];
                val = new double[dp.length];
                for (int i = 0; i < dim; i++) {
                    for (int j = 0; j < dp.length; j++) {
                        val[j] = dp[j].getVector()[i];
                    }
                    NormalStatistics ns = new NormalStatistics(val);
                    scalingVec[i] = ns.getSD();
                }
                break;
            case NONE:
                break;
        }

        if (scalingVec != null) {
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dp.length; j++) {
                    dp[j].getVector()[i] /= scalingVec[i];
                }
            }
        }

        if (dp.length == 0) {
            logger.showException(new IllegalStateException("No datapoints were imported!"));
            return null;
        }

        Dataset ds = new Dataset(config.datasetName, dp, featureColNamesDS, sideColNamesDS);

        return ds;
    }
}
