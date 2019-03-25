/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import samusik.objecttable.AbstractRowRepresentable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import util.MatrixOp;

/**
 * @author Nikolay
 */
public class ClusterMember extends AbstractRowRepresentable implements Serializable {

    public static final long serialVersionUID = 257L;
    private int index;
    private double simToMode;
    private String comment;
    private final Cluster cluster;
    private Datapoint datapoint;
    private static boolean[] editibilityMask = null;
    private final HashMap<Score.ScoringMethod, Score> scores;
    private String[] headerRow = null;

    public BarCode getBarCode() {
        return new BarCode() {
            @Override
            public double[] getProfile() {
                return MatrixOp.concat(MatrixOp.toUnityLen(getDatapoint().getVector()), MatrixOp.toUnityLen(getDatapoint().getSideVector()));
            }

            @Override
            public double[] getRawValues() {
                return MatrixOp.concat(getDatapoint().getVector(), getDatapoint().getSideVector());
            }

            @Override
            public int getSideVectorBeginIdx() {
                return getDatapoint().getVector().length;
            }

            @Override
            public String[] getParameterNames() {
                return getCluster().getClusterSet().getDataset().getFeatureNamesCombined();
            }
        };
    }

    public double[] getScaledVectorCombined() {
        double[] vec = datapoint.getVector();
        double[] d = new double[getCluster().getClusterSet().getDataset().getFeatureNamesCombined().length];

        if (datapoint.getSideVector() != null) {
            for (int i = vec.length; i < d.length; i++) {
                d[i] = datapoint.getSideVector()[i - vec.length];
            }
        }
        return d;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public HashMap<Score.ScoringMethod, Score> getScores() {
        return scores;
    }

    @Override
    public boolean[] getEditibilityMask() {
        if (editibilityMask == null) {
            getHeaderRow();
        }
        return editibilityMask;
    }

    public String getDatapointName() {
        return getDatapoint().getFullName();
    }

    public void setComment(String comment) {
        this.comment = comment;
        fireRowChanged();
    }

    public String getComment() {
        return comment;
    }

    public double getSimilarityToMode() {
        if (Double.isNaN(simToMode) || simToMode == 0) {
            this.simToMode = this.getCluster().getClusterSet().getDistanceMeasure().getSimilarity(datapoint.getVector(), cluster.getMode().getVector());
        }
        return simToMode;
    }

    public void setSimilarityToMode(double distanceToMode) {
        this.simToMode = distanceToMode;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    protected void clearHeaderAndData() {
        headerRow = null;
        editibilityMask = null;
        row = null;
    }

    @Override
    public String[] getHeaderRow() {
        if (headerRow == null) {
            String[] params = this.getCluster().getClusterSet().getDataset().getFeatureNames();
            String[] sideParams = this.getCluster().getClusterSet().getDataset().getSideVarNames();
            if (sideParams == null) {
                sideParams = new String[0];
            }
            headerRow = new String[params.length + sideParams.length + 6 + this.getCluster().getComputedScores().size()];
            editibilityMask = new boolean[headerRow.length];
            Arrays.fill(editibilityMask, false);
            int i = 0;
            editibilityMask[i] = true;
            headerRow[i++] = "EventID";
            headerRow[i++] = "File Name";
            headerRow[i++] = "Index in File";
            headerRow[i++] = "Event Name";
            for (Score.ScoringMethod s : cluster.getComputedScores()) {
                headerRow[i++] = scores.get(s).scoringMethod.toString().replaceAll("_", " ");
            }
            headerRow[i++] = "Barcode";
            editibilityMask[i] = true;
            headerRow[i++] = "Comment";
            int z;
            for (z = 0; z < params.length; z++) {
                headerRow[z + i] = params[z];
            }
            for (int k = 0; k < sideParams.length; k++) {
                headerRow[z + k + i] = sideParams[k];
            }

        }
        return headerRow;
    }

    @Override
    public String toString() {
        return getDatapoint().getFullName();
    }

    public ClusterMember(Datapoint dp, Cluster cluster, Score[] scores, String comment) {
        this.comment = comment;
        this.cluster = cluster;
        this.scores = new HashMap<>();
        if (scores != null) {
            for (Score score : scores) {
                this.scores.put(score.scoringMethod, score);
            }
        }
        this.datapoint = dp;
    }
    
     public ClusterMember(Datapoint dp, Cluster cluster) {
        this.cluster = cluster;
        this.scores = null;
        this.datapoint = dp;
    }

    public Datapoint getDatapoint() {
        return datapoint;
    }

    @Override
    public void updateValue(int col, Object value) {
        if (getHeaderRow()[col].equals("Comment")) {
            this.row[col] = (String) value;
            this.setComment((String) value);
        }
        fireRowChanged();
    }

    @Override
    public Object[] toRow() {
        if (row == null) {
            Object[] tmpRow = new Object[getHeaderRow().length];
            int i = 0;
            tmpRow[i++] = datapoint.getID();
            tmpRow[i++] = datapoint.getFilename();
            tmpRow[i++] = getDatapoint().getIndexInFile();
            tmpRow[i++] = getDatapoint().getName();
            for (Score.ScoringMethod s : cluster.getComputedScores()) {
                tmpRow[i++] = scores.get(s).score;
            }
            tmpRow[i++] = getBarCode();
            tmpRow[i++] = getComment();
            double[] vec = MatrixOp.concat(this.datapoint.getVector(), this.datapoint.getSideVector());
            for (int j = 0; j < vec.length; j++) {
                tmpRow[i++] = vec[j];
            }
            row = tmpRow;
        }
        return row;
    }
}
