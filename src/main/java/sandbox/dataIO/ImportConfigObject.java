/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.dataIO;

import sandbox.clustering.DistanceMeasure;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import util.logger;

/**
 *
 * @author Nikolay
 */
public class ImportConfigObject {

    public double euclidean_len_ths;
    public String datasetName;
    public String[] featureColumnNames;
    public String[] sideColumnNames;
    public double scaling_factor;
    public double noise_threshold;
    public RescaleType rescale;
    public TransformationType transf;
    public double quantile;
    public boolean rescaleSeparately;
    public int limitRowsPerFile;

    public void writeToFile(File f) {
        Properties defaultProps = new Properties();
    }

    public static ImportConfigObject readFromFile(File f) throws IOException {
        Properties p = new Properties();
        FileInputStream in = new FileInputStream(f);
        p.load(in);

        ImportConfigObject obj = new ImportConfigObject(
                "default_ds",
                p.getProperty("clustering_columns", "").split(";"),
                p.getProperty("side_columns", "").split(";"),
                RescaleType.valueOf(p.getProperty("rescale", "NONE")),
                TransformationType.valueOf(p.getProperty("transformation", "ASINH")),
                Double.parseDouble(p.getProperty("scaling_factor", "5")),
                Double.parseDouble(p.getProperty("quantile", "0.95")),
                Boolean.parseBoolean(p.getProperty("rescale_separately", "false")),
                Integer.parseInt(p.getProperty("limit_events_per_file", "0")),
                Double.parseDouble(p.getProperty("euclidian_length_threshold", "0.0")),
                Double.parseDouble(p.getProperty("noise_threshold", "0"))
        );
        return obj;

    }

    public enum TransformationType {
        NONE, ASINH, DOUBLE_ASINH
    };

    public enum RescaleType {
        NONE, SD, QUANTILE
    };

    public ImportConfigObject(String datasetName, String[] featureColumnNames, String[] sideColumnNames, RescaleType rescale, TransformationType transf, double scaling_Factor, double quantile, boolean rescaleSeparately, int limitRowsPerFile, double eucl_len_threshold, double noise_threshold) {
        this.euclidean_len_ths = eucl_len_threshold;
        this.datasetName = datasetName;
        this.featureColumnNames = featureColumnNames;
        this.sideColumnNames = sideColumnNames;
        this.rescale = rescale;
        this.transf = transf;
        this.quantile = quantile;
        this.rescaleSeparately = rescaleSeparately;
        this.limitRowsPerFile = limitRowsPerFile;
        this.scaling_factor = scaling_Factor;
        this.noise_threshold = noise_threshold;
    }
}
