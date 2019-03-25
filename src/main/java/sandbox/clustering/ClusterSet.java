/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import java.awt.Color;
import java.io.Serializable;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventListener;
import java.util.EventObject;
import samusik.objecttable.AbstractRowRepresentable;
import util.logger;

/**
 *
 * @author Nikolay
 */
public class ClusterSet extends AbstractRowRepresentable implements Serializable{

    public static final long serialVersionUID = 252L;
    private int clusterSetID;
    private Date dateCreated;
    protected Dataset nDataset;
    protected Cluster[] clusters;
    private String comment;
    private Color colorCode;
    private String clusteringAlgorithm;
    private String clusteringParameters = "";
    private double mainClusteringParameterValue;
    private int batchID;
    private int numberOfClusters;
    private static String[] hRow = null;
    private static boolean[] editibilityMask = null;
    private Scorer scorer;
    private DistanceMeasure dist;
    private ArrayList<ClusterSetDeletionListener> deletionListeners = new ArrayList<>();

    public Color getColorCode() {
        return colorCode;
    }

    public void setColorCode(Color colorCode) {
        this.colorCode = colorCode;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

   

    public DistanceMeasure getDistanceMeasure() {
        if (dist == null) {
            dist = new AngularDistance();
        }
        return dist;
    }

    public String getClusteringParameterString() {
        return clusteringParameters;
    }

    public static synchronized double getAdjustedRandIndex(ClusterSet cs1, ClusterSet cs2) {

        if (!cs1.getDataset().equals(cs2.getDataset())) {
            throw new IllegalStateException("The cluster sets are based on different datasets!");
        }
        double n = cs1.getDataset().size();
        // Form contingency matrix
        int[][] cont = new int[cs1.getClusters().length][cs2.getClusters().length];

        for (int i = 0; i < cs1.getClusters().length; i++) {
            for (int j = 0; j < cs2.getClusters().length; j++) {
                cont[i][j] = (int) cs1.getClusters()[i].getOverlap(cs2.getClusters()[j]);
            }
        }

        // sum over rows & columnns of nij^2
        double t2 = 0;

        // sum of squares of sums of rows
        double[] ni = new double[cont.length];
        try {
            for (int i = 0; i < cont.length; i++) {
                for (int j = 0; j < cont[i].length; j++) {
                    ni[i] += cont[i][j];
                }
            }
        } catch (Exception e) {
            logger.showException(e);
        }

        double nis = 0;
        for (int k = 0; k < ni.length; k++) {
            nis += ni[k] * ni[k];
        }

        // sum of squares of sums of columns
        double[] nj = new double[cont[0].length];
        try {
            for (int j = 0; j < cont[0].length; j++) {
                for (int i = 0; i < cont.length; i++) {
                    nj[j] += cont[i][j];
                    t2 += cont[i][j] * cont[i][j];
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        double njs = 0;
        for (int k = 0; k < nj.length; k++) {
            njs += nj[k] * nj[k];
        }

        // total number of pairs of entities
        double t1 = n * (n - 1) / 2;

        double t3 = 0.5 * (nis + njs);

        // expected index (for adjustment)
        double nc = (n * (n * n + 1) - (n + 1) * nis - (n + 1) * njs + 2 * (nis * njs) / n) / (2 * (n - 1));

        double agreements = t1 + t2 - t3;	// number of agreements

        double adjustedRandIndex;
        if (t1 == nc) {
            adjustedRandIndex = 0;	// avoid division by zero; if k=1, define Rand = 0
        } else {
            adjustedRandIndex = (agreements - nc) / (t1 - nc);	// adjusted Rand - Hubert & Arabie 1985
        }
        return adjustedRandIndex;

    }
    
    public int [] getAssignmentIndexString(){
        int [] idx = new int[getDataset().getDatapoints().length];
        
            int x = 1;
            for (Cluster c : getClusters()) {
            for (ClusterMember cm : c.getClusterMembers()) {
               idx[cm.getDatapoint().getID()]=c.getID()>0?c.getID():x++;
            }
        }
        
        return idx;
    }

    public double getMainClusteringParameterValue() {
        return mainClusteringParameterValue;
    }

    public void setID(int csID) {
        if (this.clusterSetID != 0) {
            throw new IllegalStateException("Attempt to change ClusterSetID, which is immutable");
        }
        this.clusterSetID = csID;
    }

    public Scorer getScorer() {
        if (scorer == null) {
            scorer = new Scorer(this.getDataset());
        }
        return scorer;
    }

    @Override
    protected void clearHeaderAndData() {
        row = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ClusterSet) {
            return ((ClusterSet) o).clusterSetID == clusterSetID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (this.clusterSetID ^ (this.clusterSetID >>> 32));
        return hash;
    }

    private void fireClusterSetDeleted() {
        for (ClusterSetDeletionListener l : deletionListeners) {
            l.clusterSetDeleted(new ClusterSetDeletionEvent(this));
        }
    }

    public void delete() throws SQLException {
        fireClusterSetDeleted();
    }

    public interface ClusterSetDeletionListener extends EventListener {

        public void clusterSetDeleted(ClusterSetDeletionEvent e);
    }

    public class ClusterSetDeletionEvent extends EventObject {

        private ClusterSet sourceCS;

        public ClusterSetDeletionEvent(ClusterSet source) {
            super(source);
            this.sourceCS = source;
        }

        @Override
        public ClusterSet getSource() {
            return sourceCS;
        }
    }

    @Override
    public Object[] toRow() {
        //loadFromDB();
        if (row == null) {
            row = new Object[]{clusterSetID, clusteringAlgorithm, dist, clusteringParameters, mainClusteringParameterValue, numberOfClusters};
        }
        return row;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public void updateValue(int col, Object value) {
    }

    @Override
    public String[] getHeaderRow() {
        if (hRow == null) {
            hRow = new String[]{"ClusterSetID", "Method", "Distance Measure", "Parameters", "Free Parameter Value", "Num. Clusters"};
        }
        return hRow;
    }

    public int getBatchID() {
        return batchID;
    }

    @Override
    public boolean[] getEditibilityMask() {
        if (editibilityMask == null) {
            editibilityMask = new boolean[]{false, false, false, false, false, false};
        }
        return editibilityMask;
    }

    public String getComment() {
        return comment;
    }

    public ClusterSet(int batchID, Dataset nd, Cluster[] clusters, DistanceMeasure dm, String clusteringAlgorithm, String parameters, double mainParameterValue, String comment) {
        this.dist = dm;
        this.nDataset = nd;
        this.comment = comment;
        this.batchID = batchID;
        this.dateCreated = new Date(Calendar.getInstance().getTimeInMillis());
        this.clusteringAlgorithm = clusteringAlgorithm;
        this.colorCode = new Color(255, 255, 255);
        this.clusters = clusters;
        this.mainClusteringParameterValue = mainParameterValue;
        this.clusteringParameters = parameters;
        this.numberOfClusters = clusters.length;
        for (Cluster c : clusters) {
            c.setClusterSet(this);
        }
    }

    public Cluster[] getClusters() {
        return clusters;
    }

    public void close() {
        clusters = null;
    }

    //used at deserialization
    public ClusterSet(int clusterSetID, Dataset nDataset, Color colorCode, String clusteringAlgorithm, String parameters, double mainParameterVal, int clusterNumber, int batchID, Date date, String comment, DistanceMeasure dist) {
        this.clusterSetID = clusterSetID;
        this.batchID = batchID;
        this.dateCreated = date;
        this.nDataset = nDataset;
        this.comment = comment;
        this.colorCode = colorCode;
        this.mainClusteringParameterValue = mainParameterVal;
        this.clusteringAlgorithm = clusteringAlgorithm;
        this.batchID = batchID;
        this.numberOfClusters = clusterNumber;
        this.clusteringParameters = parameters;
        this.dist = dist;
    }

    @Override
    public String toString() {
        try {
            return String.valueOf((this.clusteringAlgorithm.length() > 0) ? this.clusteringAlgorithm + ", " : "") + getClusteringParameterString() + ", " + getNumberOfClusters() + " clusters" + ((this.comment != null) ? ((this.comment.length() > 0) ? ", " + this.comment : "") : "");
        } catch (NullPointerException e) {
            logger.print(this.getID() + ", " + nDataset + ", " + clusteringAlgorithm + ", " + comment + ", " + batchID);
            logger.showException(e);
            return null;
        }
    }

    public String getClusteringAlgorithm() {
        return clusteringAlgorithm;
    }

    public int getNumberOfClusters() {
        return numberOfClusters;
    }

    public Dataset getDataset() {
        return nDataset;
    }

    public String getDateTime() {
        return dateCreated.toString();
    }

    public int getID() {
        return clusterSetID;
    }
}
