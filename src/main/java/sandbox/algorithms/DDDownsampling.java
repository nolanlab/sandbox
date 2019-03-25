package sandbox.algorithms;

import sandbox.clustering.AngularDistance;
import sandbox.clustering.Datapoint;
import sandbox.clustering.Dataset;
import sandbox.clustering.DensityDependentDownsampling;
import sandbox.dataIO.DatasetStub;
import sandbox.dataIO.ImportConfigObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import util.IO;
import util.logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Nikolay
 */
public class DDDownsampling {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        int K = -1;
        double targetDensityRate = -1;
        logger.print("args: " + Arrays.toString(args));
        try {
            K = Integer.parseInt(args[0].trim());
            targetDensityRate = Double.parseDouble(args[1].trim());
        } catch (Exception e) {
            logger.print("Error parsing arguments. \nUsage: java -jar X-shift.jar NUM_NEAREST_NEIGHBORS TargetDensityRate");
            System.exit(0);
        }

        logger.print("Target density " + targetDensityRate);
        logger.print("NUM_NEAREST_NEIGHBORS " + targetDensityRate);
        /*String CONFIG_FILE_PATH = "C:\\Users\\Nikolay\\YandexDisk\\Working folder\\Cytobank\\X-shift impl for Cytobank\\config.txt";
         String FCS_FILE_LIST_PATH = "C:\\Users\\Nikolay\\YandexDisk\\Working folder\\Cytobank\\X-shift impl for Cytobank\\fcsFileList.txt";*/
        String CONFIG_FILE_PATH = "config.txt";
        String FCS_FILE_LIST_PATH = "fcsFileList.txt";
        String OUTPUT_PATH = "C:\\Users\\Nikolay\\Local Working Folder\\Kara\\ALL5v2 Downsampling\\out\\";

        //1. Read config
        ImportConfigObject config = null;

        try {
            config = ImportConfigObject.readFromFile(new File(CONFIG_FILE_PATH));
            logger.print("datasetName", config.datasetName);
            logger.print("euclidean_len_ths", config.euclidean_len_ths);
            logger.print("euclidean_len_ths", Arrays.toString(config.featureColumnNames));
            logger.print("limitRowsPerFile", config.limitRowsPerFile);
            logger.print("noise_threshold", config.noise_threshold);
            logger.print("quantile", config.quantile);
            logger.print("rescale", config.rescale);
            logger.print("rescaleSeparately", config.rescaleSeparately);
            logger.print("scaling_factor", config.scaling_factor);
            logger.print("transf", config.transf);
        } catch (IOException e) {
            logger.print("Error reading config file. Exiting");
            logger.print("Reason: " + e.toString() + ", " + e.getMessage());
            for (StackTraceElement st : e.getStackTrace()) {
                logger.print(st.toString());
            }
            System.exit(1);
        }

        //2. Read FCS File List
        List<String> fcsFilePaths = null;
        try {
            fcsFilePaths = IO.getListOfStringsFromStream(new FileInputStream(FCS_FILE_LIST_PATH));

        } catch (IOException e) {
            logger.print("Error reading the list of FCS files. Exiting");
            logger.print("Reason: " + e.toString() + ", " + e.getMessage());
            for (StackTraceElement st : e.getStackTrace()) {
                logger.print(st.toString());
            }
            System.exit(2);
        }
        for (String fcsFilePath : fcsFilePaths) {

            DatasetStub stub = null;
            try {
                stub = DatasetStub.createFromFCS(new File(fcsFilePath));
            } catch (Exception e) {
                logger.print("Error reading one of the FCS files. Exiting");
                logger.print("Reason: " + e.toString() + ", " + e.getMessage());
                for (StackTraceElement st : e.getStackTrace()) {
                    logger.print(st.toString());
                }
                System.exit(3);
            }

            Dataset nd = null;
            try {
                nd = sandbox.dataIO.DatasetImporter.importDataset(new DatasetStub[]{stub}, config);
            } catch (Exception e) {
                logger.print("Error importing the dataset. Exiting");
                logger.print("Reason: " + e.toString() + ", " + e.getMessage());
                for (StackTraceElement st : e.getStackTrace()) {
                    logger.print(st.toString());
                }
                System.exit(4);
            }

            try {
                Datapoint[] sample = null;
                try {
                    DensityDependentDownsampling dd = new DensityDependentDownsampling(new AngularDistance());
                    sample = dd.doDensityDependendDownsampling(nd, K, targetDensityRate, false);

                } catch (Exception e) {
                    logger.print("Error in downsampling. Exiting");
                    logger.print("Reason: " + e.toString() + ", " + e.getMessage());
                    for (StackTraceElement st : e.getStackTrace()) {
                        logger.print(st.toString());
                    }
                    System.exit(5);
                }

                try {
                    Dataset ds = new Dataset("downsampled", sample, nd.getFeatureNames(), nd.getSideVarNames());
                    logger.print("Exporting FCS files");
                    sandbox.dataIO.ClusterSetToFCSExporter.exportDatapoints(nd, null, "..", ds.getAnnotations()[0], config.transf.toString());
                } catch (Exception e) {
                    logger.print("Error exporting the data. Exiting");
                    logger.print("Reason: " + e.toString() + ", " + e.getMessage());
                    for (StackTraceElement st : e.getStackTrace()) {
                        logger.print(st.toString());
                    }
                    System.exit(6);
                }

            } catch (Exception e) {
                logger.print("Error in downsampling. Exiting");
                logger.print("Reason: " + e.toString() + ", " + e.getMessage());
                for (StackTraceElement st : e.getStackTrace()) {
                    logger.print(st.toString());
                }
                System.exit(5);
            }
        }
    }
}
