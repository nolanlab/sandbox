/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import sandbox.annotations.EnrichmentInfo;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import java.awt.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import samusik.objecttable.AbstractRowRepresentable;
import sandbox.annotations.Annotation;
import sandbox.annotations.SimpleEnrichment;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import util.DefaultEntry;
import util.MatrixOp;
import util.logger;

/**
 * @author Nikolay
 */
public class Cluster extends AbstractRowRepresentable implements ColorCoded, Serializable, Comparable {

    @Override
    public int compareTo(Object o) {
        return hashCode() - o.hashCode();
    }

    private String[] headerRow;
    private boolean[] editibilityMask;
    private int clusterID;
    //items [] is bulky so it is deserialized in a lazy way at the first call. Until then, 
    protected ClusterMember[] items = null;
    protected int[] memberDPIDs = null;
    protected int size = 0;
    protected Mode mode;
    private int enumIndex = 0;
    private ClusterSet cs;
    protected HashMap<Integer, ClusterMember> hmDpID;
    //protected ArrayList<ComparableEntry<String, ClusterMember>> datapointNameList;
    private Color colorCode;
    private String comment;
    private String caption;
    private boolean selected;
    private ArrayList<Score.ScoringMethod> computedScores;
    private boolean normalizeVectorsToLen = true;
    public static final long serialVersionUID = 256L;
    private double[] scaledAverage;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        fireSelectionChanged();
        fireRowChanged();
    }

    private transient ArrayList<ListSelectionListener> selectionListeners = new ArrayList<>();

    public void fireSelectionChanged() {
        for (ListSelectionListener l : selectionListeners) {
            l.valueChanged(new ListSelectionEvent(this, 0, 0, false));
        }
    }

    public void addSelectionListener(ListSelectionListener l) {
        if (!selectionListeners.contains(l)) {
            selectionListeners.add(l);
        }
    }

    public int size() {
        return items != null ? items.length : size;
    }

    public String[] getProfileIDs() throws SQLException {
        String[] c1PID = new String[getClusterMembers().length];
        for (int i = 0; i < c1PID.length; i++) {
            c1PID[i] = getClusterMembers()[i].getDatapointName();
        }
        return c1PID;
    }

    //to be overriden at deserialization
    protected ClusterMember[] deserializeItems() throws SQLException {
        return null;
    }
    private double[] avgFeatureVec = null;

    //Currently returns medians!
    public double[] getMedianFeatureVec() throws SQLException {
        if (avgFeatureVec != null) {
            return avgFeatureVec;
        }
        double[] vec = new double[getClusterMembers()[0].getDatapoint().getVector().length];
        for (int i = 0; i < vec.length; i++) {
            double[] med = new double[getClusterMembers().length];
            int idx = 0;
            for (ClusterMember c : getClusterMembers()) {
                double[] vec2 = c.getDatapoint().getVector();
                med[idx++] = vec2[i];
            }
            Arrays.sort(med);
            vec[i] = med[med.length / 2];
        }

        avgFeatureVec = vec;
        return avgFeatureVec;
    }

    public double[] getMedian(double[][] inVec) {
        double[] vec = new double[inVec[0].length];
        for (int i = 0; i < vec.length; i++) {
            double[] med = new double[inVec.length];
            int idx = 0;
            for (double[] v : inVec) {
                med[idx++] = v[i];
            }
            Arrays.sort(med);
            vec[i] = med[med.length / 2];
        }
        return vec;
    }

    //Currently returns medians!
    public double[][] getMedianVecByAnnotation(Annotation ann) {

        double[][] vec = new double[ann.getTerms().length][getClusterMembers()[0].getDatapoint().getVector().length + getClusterMembers()[0].getDatapoint().getSideVector().length + 1];
        double[] annCounts = new double[ann.getTerms().length];
        double[][] medians = new double[ann.getTerms().length][];
        for (int i = 0; i < annCounts.length; i++) {
            String term = ann.getTerms()[i];
            double[][] vecForMedian = new double[ann.getDpIDsForTerm(term).length][];
            int idx = 0;
            for (ClusterMember c : getClusterMembers()) {
                if (ann.containsAnnotationPair(term, c.getDatapoint().getID())) {
                    double[] vec2 = MatrixOp.concat(c.getDatapoint().getVector(), c.getDatapoint().getSideVector());
                    vecForMedian[idx++] = vec2;
                    for (int j = 0; j < vec2.length; j++) {
                        vec[i][j + 1] += vec2[j];
                    }
                    annCounts[i]++;
                }
            }
            if ((int) annCounts[i] > 0) {
                medians[i] = getMedian(Arrays.copyOf(vecForMedian, (int) annCounts[i]));
            } else {
                medians[i] = null;
            }
        }
        for (int i = 0; i < annCounts.length; i++) {
            /*if (annCounts[i] > 0) {
                 MatrixOp.mult(vec[i], 1.0 / annCounts[i]);
                 }*/
            if (medians[i] != null) {
                for (int j = 0; j < medians[i].length; j++) {
                    vec[i][j + 1] = medians[i][j];
                }
            }
            vec[i][0] = annCounts[i];
        }
        return vec;

    }

    private double[] avgSideVec = null;

    private double[] getAverageSideVec() throws SQLException {
        if (getClusterMembers()[0].getDatapoint().getSideVector() == null) {
            return new double[0];
        }
        if (avgSideVec == null) {
            double[] vec = new double[getClusterMembers()[0].getDatapoint().getSideVector().length];

            for (ClusterMember c : getClusterMembers()) {
                double[] vec2 = c.getDatapoint().getSideVector();
                for (int i = 0; i < vec.length; i++) {
                    vec[i] += vec2[i];
                }
            }
            MatrixOp.mult(vec, 1.0 / size());

            avgSideVec = vec;
        }

        return avgSideVec;

    }

    public double[] getUnityScaledAverage(Integer[] dpIDs) {

        ArrayList<ClusterMember> alcm = new ArrayList<>();

        if (dpIDs != null) {
            int[] dpID2 = new int[dpIDs.length];
            for (int i = 0; i < dpID2.length; i++) {
                dpID2[i] = dpIDs[i];
            }
            Arrays.sort(dpID2);

            for (ClusterMember c : getClusterMembers()) {
                if (Arrays.binarySearch(dpID2, c.getDatapoint().getID()) >= 0) {
                    alcm.add(c);
                }
            }

            if (alcm.isEmpty()) {
                double[] vec = new double[getClusterSet().getDataset().getFeatureNamesCombined().length];
                Arrays.fill(vec, Double.NaN);
                scaledAverage = vec;
            }
        } else {

            alcm.addAll(Arrays.asList(getClusterMembers()));

        }

        //ProfileAverager pa = new ProfileAverager();
        double[] avgMainVec = new double[getClusterSet().getDataset().getFeatureNames().length];
        for (ClusterMember c : alcm) {
            double[] vec = c.getDatapoint().getVector();
            for (int i = 0; i < vec.length; i++) {
                avgMainVec[i] += vec[i];
            }
        }
        for (int i = 0; i < avgMainVec.length; i++) {
            avgMainVec[i] /= alcm.size();
        }

        double[] avgSideVec = new double[getClusterSet().getDataset().getSideVarNames().length];
        for (ClusterMember c : alcm) {
            double[] vec = c.getDatapoint().getSideVector();
            for (int i = 0; i < vec.length; i++) {
                avgSideVec[i] += vec[i];
            }
        }
        for (int i = 0; i < avgSideVec.length; i++) {
            avgSideVec[i] /= alcm.size();
        }

        double[][] mainBounds = getClusterSet().getDataset().getDataBounds();

        for (int i = 0; i < avgMainVec.length; i++) {
            avgMainVec[i] /= mainBounds[i][1];
            avgMainVec[i] = Math.max(0, avgMainVec[i]);
        }

        double[][] sideBounds = getClusterSet().getDataset().getSideDataBounds();

        for (int i = 0; i < avgSideVec.length; i++) {
            avgSideVec[i] /= sideBounds[i][1];
            avgSideVec[i] = Math.max(0, avgSideVec[i]);
        }

        this.scaledAverage = MatrixOp.concat(avgMainVec, avgSideVec);

        return scaledAverage;
    }

    @Override
    protected void clearHeaderAndData() {
        headerRow = null;
        row = null;
    }

    @Override
    public void updateValue(int col, Object value) {
        if (col == 0) {
            this.setColorCode((Color) value);
        }
        if (col == 4) {
            this.setComment((String) value);
        }
        fireRowChanged();
    }

    @Override
    public int hashCode() {
        return clusterID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Cluster other = (Cluster) obj;
        if (this.clusterID != other.clusterID) {
            return false;
        }
        return true;
    }

    @Override
    public String[] getHeaderRow() {
        if (headerRow == null) {
            String[] params = cs.getDataset().getFeatureNamesCombined();
            headerRow = new String[params.length + 5];
            int l = 0;
            headerRow[l++] = "Color";
            headerRow[l++] = "ClusterID";
            headerRow[l++] = "Size";
            headerRow[l++] = "Barcode";
            headerRow[l++] = "Comment";
            for (int i = 0; i < params.length; i++) {
                headerRow[l] = params[i];
                l++;
            }
        }
        return headerRow;
    }

    @Override
    public String toString() {
        return ((getComment().length() > 1) ? getComment() : ("id" + this.getID()));
    }

    @Override
    public boolean[] getEditibilityMask() {
        if (editibilityMask == null) {
            editibilityMask = new boolean[getHeaderRow().length];
            Arrays.fill(editibilityMask, false);
            //editibilityMask[0] = true;
            editibilityMask[0] = true;
            editibilityMask[4] = true;
        }
        return editibilityMask.clone();
    }

    @Override
    public Object[] toRow() {
        if (row == null) {
            double[] vec = MatrixOp.concat(this.getMode().getVector(), this.getMode().getSideVector());//  MatrixOp.concat(getAverageFeatureVec(), getAverageSideVec());//
            row = new Object[vec.length + 5];
            int i = 0;
            row[i++] = this.getColorCode();
            row[i++] = this.getID();
            row[i++] = size();
            row[i++] = new BarCode() {

                @Override
                public double[] getRawValues() {
                    return MatrixOp.concat(getMode().getVector(), getMode().getSideVector());
                }

                @Override
                public String[] getParameterNames() {
                    return getClusterSet().getDataset().getFeatureNamesCombined();
                }

                @Override
                public double[] getProfile() {
                    return MatrixOp.concat(MatrixOp.toUnityLen(getMode().getVector()), MatrixOp.toUnityLen(getMode().getSideVector()));
                }

                @Override
                public int getSideVectorBeginIdx() {
                    return getClusterSet().getDataset().getNumDimensions();
                }

            };
            row[i++] = (this.getComment() == null) ? "" : this.getComment();
            for (int l = 0; l < vec.length; l++) {
                row[i++] = vec[l];
            }
        }
        return row;
    }

    public boolean isNormalizeVectorsToLen() {
        return normalizeVectorsToLen;
    }

    public void setNormalizeVectorsToLen(boolean normalizeVectorsToLen) {
        this.normalizeVectorsToLen = normalizeVectorsToLen;
    }

    public String getComment() {
        if (comment == null) {
            comment = "";
        }
        return comment.trim();
    }

    @Override
    public Color getColorCode() {

        return new Color(gc().getRGB()) {
            @Override
            public Color brighter() {
                return gc().brighter(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public float[] getRGBComponents(float[] compArray) {
                return gc().getRGBComponents(compArray); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public synchronized PaintContext createContext(ColorModel cm, Rectangle r, Rectangle2D r2d, AffineTransform xform, RenderingHints hints) {
                return gc().createContext(cm, r, r2d, xform, hints); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Color darker() {
                return gc().darker(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int getAlpha() {
                return gc().getAlpha(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public float[] getColorComponents(float[] compArray) {
                return gc().getColorComponents(compArray); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public float[] getColorComponents(ColorSpace cspace, float[] compArray) {
                return gc().getColorComponents(cspace, compArray); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public ColorSpace getColorSpace() {
                return gc().getColorSpace(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public float[] getComponents(float[] compArray) {
                return gc().getComponents(compArray); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public float[] getComponents(ColorSpace cspace, float[] compArray) {
                return gc().getComponents(cspace, compArray); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int getGreen() {
                return gc().getGreen(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public float[] getRGBColorComponents(float[] compArray) {
                return gc().getRGBColorComponents(compArray); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int getRGB() {
                return gc().getRGB(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean equals(Object obj) {
                return gc().equals(obj); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int hashCode() {
                return gc().hashCode(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String toString() {
                return gc().toString(); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public int getTransparency() {
                return gc().getTransparency(); //To change body of generated methods, choose Tools | Templates.
            }

        };
    }

    private Color gc() {
        if (this.colorCode == null) {
            this.colorCode = Color.WHITE;
        }
        return this.colorCode;
    }

    public void setColorCode(Color colorCode) {
        if (!colorCode.equals(gc())) {
            this.colorCode = colorCode;

            if (row != null) {
                row[0] = colorCode;
                fireRowChanged();
            }
        }
    }

    public void setComment(String comment) {
        this.comment = comment;
        if (row != null) {
            row[4] = comment;
            fireRowChanged();
        }
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean containsDpID(int dpID) {
        return Arrays.binarySearch(getMemberDPIDs(), dpID) >= 0;
    }

    protected class ComparableEntry<K extends Comparable, V> extends DefaultEntry<K, V> implements Comparable<DefaultEntry<K, V>> {

        public ComparableEntry(K key, V value) {
            super(key, value);
        }

        @Override
        public int compareTo(DefaultEntry<K, V> o) {
            return this.getKey().compareTo(o.getKey());
        }
    }

    public ClusterMember getClusterMemberByDatapointID(int datapointID) {
        if (hmDpID == null) {
            hmDpID = new HashMap<>(this.size());
            for (ClusterMember cm : this.getClusterMembers()) {
                hmDpID.put(cm.getDatapoint().getID(), cm);
            }
        }
        return hmDpID.get(datapointID);
    }

    public ClusterSet getClusterSet() {
        return cs;
    }

    public ArrayList<Score.ScoringMethod> getComputedScores() {
        return new ArrayList();
    }

    public void addComputedScore(Score.ScoringMethod score) {
        computedScores.add(score);
    }

    public final ClusterMember[] getClusterMembers() {
        if (items == null) {
            try {
                items = deserializeItems();
            } catch (SQLException e) {
                logger.showException(e);
            }
        }
        return items;
    }

    public int getID() {
        return clusterID;
    }

    public void setID(int id) {
        this.clusterID = id;
    }

    Mode weightedMode = null;

    public Mode getMode() {
        /*try {
            if (false) {
                
            
                 double[] a1 = new double[mode.getVector().length];
                 double[] a2 = new double[mode.getSideVector().length];
                 for (ClusterMember a : this.getClusterMembers()) {
                 a1= MatrixOp.sum(a1, a.getDatapoint().getVector());
                 a2= MatrixOp.sum(a2, a.getDatapoint().getSideVector());
                 }
            
                 MatrixOp.mult(a1, 1.0/this.getClusterMembers().length);
                 MatrixOp.mult(a2, 1.0/this.getClusterMembers().length);
            
                 weightedMode = new Mode(this, a1,a2);
                 
                //weightedMode = new Mode(this, getMedianFeatureVec(), getAverageSideVec());

                Annotation ann = null;
                for (Annotation a : this.getClusterSet().getDataset().getAnnotations()) {
                    if (a.getAnnotationName().toLowerCase().contains("auto")) {
                        ann = a;
                    }
                }
                double[][] medianVec = getMedianVecByAnnotation(ann);
                /*ProfileAverager pa = new ProfileAverager();
                for (double[] a : medianVec) {
                    if (a[0] > 0) {
                    pa.addProfile(a);
                    }
                }
                double[] avgvec = pa.getAverage();
                ArrayList<double[]> arr = new ArrayList<>();
                for (int i = 0; i< medianVec.length; i++) {
                    double[] a = medianVec[i];
                    if(a!=null){
                        if (a[0] > 0.0) {
                            arr.add(a);
                        }
                    }
                }
                
                double avgvec[] = getMedian(arr.toArray(new double[arr.size()][]));

                double[] main = Arrays.copyOfRange(avgvec, 1, mode.getVector().length + 1);
                double[] side = Arrays.copyOfRange(avgvec, mode.getVector().length+1, avgvec.length);

                weightedMode = new Mode(this, main, side);

            }
        } catch (Exception e) {
            logger.print("Coudn't compute weighted avg, reason:\n");
            e.printStackTrace();
        }*/
        return mode;

    }

    Enumeration<ClusterMember> enumerateMembers() {

        return new Enumeration<ClusterMember>() {
            int enumIndex = 0;

            @Override
            public ClusterMember nextElement() {
                return Cluster.this.getClusterMembers()[enumIndex++];
            }

            @Override
            public boolean hasMoreElements() {
                if (Cluster.this.enumIndex < Cluster.this.size()) {
                    return true;
                } else {
                    Cluster.this.enumIndex = 0;
                    return false;
                }
            }
        };
    }

    public Cluster(Datapoint[] items, double[] modeVector, double[] modeSideVector, String caption, DistanceMeasure dm) {
        Arrays.sort(items);
        for (int i = 0; i < items.length - 1; i++) {
            if (items[i].equals(items[i + 1])) {
                throw new IllegalArgumentException("Provided datapoint list contains a duplicate:" + items[i]);
            }
        }
        DefaultEntry<Datapoint, Double>[] it2 = new DefaultEntry[items.length];
        for (int i = 0; i < it2.length; i++) {
            it2[i] = new DefaultEntry<>(items[i], 1.0);
        }

        init(it2, modeVector, modeSideVector, caption);
    }

    public Cluster(Datapoint[] items, double[] modeVector, double[] modeSideVector, String caption) {
        Arrays.sort(items);
        for (int i = 0; i < items.length - 1; i++) {
            if (items[i].equals(items[i + 1])) {
                throw new IllegalArgumentException("Provided datapoint list contains a duplicate at position " + i + " and +1:" + items[i].getID() + " and " + items[i + 1].getID());
            }
        }

        init(items, modeVector, modeSideVector, caption);
    }

    public void removeClusterMember(ClusterMember cm) {
        if (cm.getCluster().equals(this)) {
            ArrayList<ClusterMember> al = new ArrayList<>(Arrays.asList(items));
            al.remove(cm);
            this.items = al.toArray(new ClusterMember[al.size()]);
        }
    }

    private void setMode(double[] modeVector, double[] sideVector) {
        this.mode = new Mode(this, modeVector, sideVector);
    }

    public Cluster(Entry<Datapoint, Double>[] dpMembership, double[] modeVector, double[] modeSideVector, String caption) {
        init(dpMembership, modeVector, modeSideVector, caption);
    }

    public Cluster(Entry<Datapoint, Double>[] dpMembership, double[] modeVector, double[] modeSideVector, String caption, DistanceMeasure dm) {
        init(dpMembership, modeVector, modeSideVector, caption);
    }

    private void init(Entry<Datapoint, Double>[] dpMembership, double[] modeVector, double[] modeSideVector, String caption) {
        this.items = new ClusterMember[dpMembership.length];
        for (int i = 0; i < items.length; i++) {
            this.items[i] = new ClusterMember(dpMembership[i].getKey(), this, new Score[]{new Score(Score.ScoringMethod.Membership, dpMembership[i].getValue())}, "");
        }
        if (this.computedScores == null) {
            computedScores = new ArrayList<>();
        }
        computedScores.add(Score.ScoringMethod.Membership);
        this.size = items.length;

        this.caption = caption;
        setMode(modeVector, modeSideVector);
    }

    private void init(Datapoint[] dpMembership, double[] modeVector, double[] modeSideVector, String caption) {
        this.items = new ClusterMember[dpMembership.length];
        for (int i = 0; i < items.length; i++) {
            this.items[i] = new ClusterMember(dpMembership[i], this, null, null);
        }
        if (this.computedScores == null) {
            computedScores = new ArrayList<>();
        }
        computedScores.add(Score.ScoringMethod.Membership);
        this.size = items.length;

        this.caption = caption;
        setMode(modeVector, modeSideVector);
    }

    public void setClusterSet(ClusterSet cs) {
        this.cs = cs;
    }

    public void setMode(Mode mode) {
        if (this.mode != null) {
            throw new IllegalAccessError("Mode in Cluster is immutable and can be only set once");
        }
        this.mode = mode;
    }

    //to be used for deserialization
    public Cluster(int clusterID, ClusterSet cs, int size, String caption, String comment, Color colorCode, double[] modeBaseVector, double[] modeSideVector, ArrayList<Score.ScoringMethod> scoresComputed) {
        this.clusterID = clusterID;
        this.cs = cs;
        this.size = size;
        this.caption = caption == null ? "" : caption;
        this.colorCode = (colorCode == null) ? Color.white : colorCode;
        this.comment = comment == null ? "" : comment;
        this.mode = new Mode(this, modeBaseVector, modeSideVector);
        this.computedScores = scoresComputed;
    }

    double avgsize = -1;

    public double getAvgSize() {
        /*try {
            if (Config.showWeightedAvgClusterProfiles() && avgsize < 0) {
                Annotation ann = null;
                for (Annotation a : this.getClusterSet().getDataset().getAnnotations()) {
                    if (a.getAnnotationName().toLowerCase().contains("auto")) {
                        ann = a;
                    }
                }
                double[] sizes = new double[ann.getTerms().length];
                for (int i = 0; i < sizes.length; i++) {
                    String term = ann.getTerms()[i];
                    double countTerm = 0;
                    for (ClusterMember c : getClusterMembers()) {
                        if (c.getDatapoint().getName().startsWith(term)) {
                            countTerm++;
                        }
                    }
                    sizes[i] = countTerm / ann.getProfileIDsForTerm(term).length;
                }
                double avg = new NormalStatistics(sizes).getMean();
                return avg * this.getClusterSet().getDataset().size();
            }
        } catch (Exception e) {
            logger.print("Coudn't compute weighter avg, reason:\n" + e);
            return size();
        }*/

        return size();
    }

    public int getOverlap(Cluster c) {
        int mutualShare = 0;
        for (int i : getMemberDPIDs()) {
            if (c.containsDpID(i)) {
                mutualShare++;
            }
        }
        //logger.print(mutualShare);
        return mutualShare;// / (c.getSize() + size);
    }

    public int getOverlap(Integer[] dpIDs) {
        int mutualShare = 0;
        for (int i : dpIDs) {
            if (this.containsDpID(i)) {
                mutualShare++;
            }
        }
        return mutualShare;// / (c.getSize() + size);
    }

    protected int[] getMemberDPIDs() {
        if (memberDPIDs == null) {

            memberDPIDs = new int[this.size()];
            for (int i = 0; i < this.size(); i++) {
                memberDPIDs[i] = getClusterMembers()[i].getDatapoint().getID();
            }
        }
        return memberDPIDs;
    }

    public EnrichmentInfo getEnrichmentIn(Cluster c) throws SQLException {
        Datapoint[] dp = this.getClusterSet().getDataset().getDatapoints();
        int[] totalSet = new int[dp.length];
        for (int i = 0; i < totalSet.length; i++) {
            totalSet[i] = dp[i].getID();
        }
        int[] sample = new int[this.size()];
        for (int i = 0; i < sample.length; i++) {
            sample[i] = this.getMemberDPIDs()[i];
        }
        int[] ann = new int[c.size()];
        for (int i = 0; i < ann.length; i++) {
            ann[i] = c.getMemberDPIDs()[i];
        }
        return SimpleEnrichment.getEnrichment(totalSet, sample, ann, this.toString() + " in " + c.toString());
    }

    class BinninedCorrelationResult {

        private DenseDoubleMatrix2D[] corrMtxs;
        private int[] binSizes;

        public BinninedCorrelationResult(DenseDoubleMatrix2D[] corrMtxs, int[] binSizes) {
            this.corrMtxs = corrMtxs;
            this.binSizes = binSizes;
        }

        public int[] getBinSizes() {
            return binSizes;
        }

        public DenseDoubleMatrix2D[] getCorrMtxs() {
            return corrMtxs;
        }
    }
}
