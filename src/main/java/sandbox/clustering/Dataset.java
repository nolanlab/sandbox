/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import sandbox.annotations.Annotation;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import samusik.objecttable.RowRepresentable;
import util.MatrixOp;

/**
 *
 * @author Nikolay
 */
public class Dataset implements RowRepresentable, Serializable {

    private static final long serialVersionUID = 1L;
    int ID;
    protected Datapoint[] datapoints;
    protected String name;
    private String[] paramNames;
    protected Date dateAccessed;
    private Date dateCreated;
    private HashMap<String, Datapoint> hmDPNames;
    private int size;

    protected ArrayList<Annotation> annotations = null;
    private ArrayList<PropertyChangeListener> al = null;
    private String[] sideParamNames;
    private String[] featureNamesCombined;
    private double[][] dataBounds;
    private double[][] sideDataBounds;
    private double[] scalingFactors;
    

    public FeaturePanel getPanel() {
        return new FeaturePanel(getFeatureNames(), getSideVarNames());
    }

    public static class FeaturePanel implements Transferable {

        public static DataFlavor panelFlavor = new DataFlavor(FeaturePanel.class, "Feature Panel");
        public String[] featurePanel;
        public String[] functionalPanel;

        public FeaturePanel(String[] featurePanel, String[] functionalPanel) {
            this.featurePanel = featurePanel;
            this.functionalPanel = functionalPanel;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(panelFlavor)) {
                return this;
            } else {
                return null;
            }
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(panelFlavor);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{panelFlavor};
        }
    }

    @Override
    public void updateValue(int col, Object value) {
    }

    public Datapoint getDPByID(int id) {
        return getDatapoints()[id];
    }

    @Override
    public String toString() {
        return getName();
    }

    public double[][] getDataBounds() {
        if (dataBounds == null) {
            dataBounds = new double[2][getNumDimensions()];
            for (int i = 0; i < getNumDimensions(); i++) {
                for (Datapoint dp : getDatapoints()) {
                    dataBounds[0][i] = Math.min(dataBounds[0][i],dp.getVector()[i]);
                    dataBounds[1][i] = Math.max(dataBounds[1][i],dp.getVector()[i]);
                }
            }
        }
        return dataBounds;
    }

    public double[][] getSideDataBounds() {
        if (sideDataBounds == null) {
            sideDataBounds = new double[2][getSideVarNames().length];
            for (int i = 0; i < getSideVarNames().length; i++) {
                for (Datapoint dp : getDatapoints()) {
                    sideDataBounds[0][i] = Math.min(sideDataBounds[0][i],dp.getSideVector()[i]);
                    sideDataBounds[1][i] = Math.max(sideDataBounds[1][i],dp.getSideVector()[i]);
                }
            }
        }
        return sideDataBounds;
    }

    public String[] getFeatureNamesCombined() {
        if (featureNamesCombined == null) {
            if (sideParamNames == null) {
                return paramNames;
            }
            if (sideParamNames.length == 0) {
                return paramNames;
            }
            featureNamesCombined = Arrays.copyOf(paramNames, paramNames.length + sideParamNames.length);
            for (int i = 0; i < sideParamNames.length; i++) {
                featureNamesCombined[i + getNumDimensions()] = sideParamNames[i];
            }
        }
        return featureNamesCombined;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener p) {
        if (al == null) {
            al = new ArrayList<>();
        }
        al.add(p);
    }

    public String[] getSideVarNames() {
        if(sideParamNames==null){
            sideParamNames = new String[0];
        }
        return sideParamNames;
    }

    public void writeToFile(File f, boolean includeProfileIDs, boolean includeHeader) throws IOException {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(f))) {
            StringBuilder sb = new StringBuilder();
            if (includeHeader) {
                if (includeProfileIDs) {
                    sb.append("ProfileID,");
                }
                sb.append(Arrays.toString(this.getFeatureNamesCombined()).replace("[", "").replace("]", ""));
                sb.append("\n");
            }
            for (Datapoint d : getDatapoints()) {
                if (includeProfileIDs) {
                    sb.append(d.getFullName());
                    sb.append(",");
                }
                sb.append(Arrays.toString(MatrixOp.concat(d.getVector(), d.getSideVector())).replace("[", "").replace("]", ""));
                sb.append("\n");
                if (d.getID() % 1000 == 0) {
                    br.write(sb.toString());
                    sb = new StringBuilder();
                }
            }
            br.write(sb.toString());
            br.flush();
        }
    }

    public Datapoint[] getQuantileDiscretized(int DiscretizationBins) {
        Datapoint[] dp = ((Dataset) this.deepCopy()).getDatapoints();
        for (int i = 0; i < paramNames.length; i++) {
            final int idx = i;
            Arrays.sort(dp, new Comparator<Datapoint>() {
                @Override
                public int compare(Datapoint o1, Datapoint o2) {
                    return (int) Math.signum(o1.getVector()[idx] - o2.getVector()[idx]);
                }
            });

            for (int j = 0; j < dp.length; j++) {
                dp[j].getVector()[idx] = (int) Math.floor((j / (double) dp.length) * DiscretizationBins);
            }
        }
        return dp;
    }

    public Datapoint[] getEqualBinDiscritized(int DiscretizationBins) {
        Datapoint[] dp = ((Dataset) this.deepCopy()).getDatapoints();
        for (int i = 0; i < paramNames.length; i++) {
            final int idx = i;
            Arrays.sort(dp, new Comparator<Datapoint>() {
                @Override
                public int compare(Datapoint o1, Datapoint o2) {
                    return (int) Math.signum(o1.getVector()[idx] - o2.getVector()[idx]);
                }
            });

            double minValue = 0;//dp[(int)Math.floor(dp.length*0.05)].getVector()[idx]; 
            double maxValue = dp[dp.length - 1].getVector()[idx];//dp[(int)Math.floor(dp.length*0.95)].getVector()[idx];

            double binSize = maxValue / DiscretizationBins;

            for (int j = 0; j < dp.length; j++) {
                double val = dp[j].getVector()[idx]; //Math.max(0,Math.min(dp[j].getVector()[idx]-minValue, maxValue));
                dp[j].getVector()[idx] = val < binSize ? 0 : 1;//Math.min((int) Math.floor(val / binSize), DiscretizationBins-1);
            }
        }
        return dp;
    }

    public void ExportQuantileDiscretizedForCAMML(File f, int QuantileDiscretizationBins) throws IOException {

        Datapoint[] dp = ((Dataset) this.deepCopy()).getDatapoints();

        for (int i = 0; i < paramNames.length; i++) {
            final int idx = i;
            Arrays.sort(dp, new Comparator<Datapoint>() {
                @Override
                public int compare(Datapoint o1, Datapoint o2) {
                    return (int) Math.signum(o1.getVector()[idx] - o2.getVector()[idx]);
                }
            });

            for (int j = 0; j < dp.length; j++) {
                dp[j].getVector()[idx] = (int) Math.floor((j / (double) dp.length) * QuantileDiscretizationBins);
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            StringBuilder sb = new StringBuilder();
            int NUM_PARAM = 20;
            sb.append(NUM_PARAM);
            sb.append((char) 0x000A);
            sb.append(dp.length);
            sb.append((char) 0x000A);
            for (int i = 0; i < NUM_PARAM; i++) {
                sb.append("param_" + i);

                sb.append((char) 0x0009);
            }
            sb.append((char) 0x000A);

            for (int i = 0; i < NUM_PARAM; i++) {
                sb.append(QuantileDiscretizationBins);

                sb.append((char) 0x0009);
            }
            sb.append((char) 0x000A);

            for (int i = 0; i < dp.length; i++) {
                for (int j = 0; j < NUM_PARAM; j++) {
                    sb.append(new Integer(((int) dp[i].getVector()[j])).toString());
                    sb.append((char) 0x0009);
                }
                sb.append((char) 0x000A);
            }
            bw.write(sb.toString());
            bw.flush();
        }

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener p) {
        if (al != null) {
            al.remove(p);
        }
    }

    @Override
    public boolean[] getEditibilityMask() {
        return new boolean[]{false, false};
    }
    private double[][] vec = null;

    public double[][] getVectors() {
        if (vec == null) {
            vec = new double[getDatapoints().length][];
            for (int i = 0; i < vec.length; i++) {
                vec[i] = getDatapoints()[i].getVector();
            }
        }
        return vec;
    }

    @Override
    public Object[] toRow() {
        return new Object[]{name, size, getFeatureNames().length};
    }

    @Override
    public String[] getHeaderRow() {
        return new String[]{"DatasetID", "Size", "Clustering dimensions"};
    }

    public synchronized void addAnnotation(Annotation ann) {
        if(annotations==null) getAnnotations();
        annotations.add(ann);
    }

    public synchronized void removeAnnotation(Annotation ann) {
        if(annotations==null) getAnnotations();
        annotations.remove(ann);
    }

    @Override
    public void invalidate() {
    }

    public Annotation[] getAnnotations() {
        if(annotations==null){
            annotations = new ArrayList<>();
        }
        return annotations.toArray(new Annotation[annotations.size()]);
    }

    public int getID() {
        return ID;
    }

    public int size() {
        return size;
    }

    public void setDateAccessed(Date dateAccessed) {
        this.dateAccessed = dateAccessed;
    }

    public Date getDateAccessed() {
        return dateAccessed;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public String getName() {
        return name;
    }

    /**
     * this constructor should only be called by StorageEngine
     */
    public Dataset(int id, String name, Date dateCreated, Date dateAccessed, int numPoints, String[] paramNames, String[] sideParamNames) {
        this.ID = id;
        this.dateCreated = dateCreated;
        this.dateAccessed = dateAccessed;
        this.name = name;
        this.paramNames = paramNames;
        this.size = numPoints;
        this.hmDPNames = new HashMap<>();
        this.sideParamNames = sideParamNames;
    }

    public Dataset(String name, Datapoint[] dp, String[] paramNames, String[] sideParamNames, double[] scalingVector) {
        this(name, dp, paramNames, sideParamNames);
        this.scalingFactors = scalingVector;
    }

    /**
     * this constructor is general-purpose
     */
    public Dataset(String name, Datapoint[] dp, String[] paramNames, String[] sideParamNames) {
        this.name = name;
        this.paramNames = paramNames;
        this.datapoints = dp;
        this.sideParamNames = sideParamNames;
        Arrays.sort(dp, new Comparator<Datapoint>() {
            @Override
            public int compare(Datapoint o1, Datapoint o2) {
                return o1.getID() - o2.getID();
            }
        });

        if (dp[0].getID() != 0) {
            throw new IllegalArgumentException("The list of datapoints doesn't start with zero ID");
        }
        for (int i = 1; i < dp.length; i++) {
            if (dp[i].getID() - dp[i - 1].getID() != 1) {
                throw new IllegalArgumentException("The datapoint IDs don't increment by one: " + dp[i].getID() + ", " + dp[i - 1].getID());
            }
            if (dp[i].getVector().length != paramNames.length) {

                throw new IllegalArgumentException("This datapoint has a vector of a different lenght \n" + paramNames.length + "\n" + dp[i].getID() + "\n" + dp[i].getVector().length
                        + "\n" + dp[i + 1].getID() + "\n" + dp[i + 1].getVector().length
                );
            }
            if (dp[i].getSideVector() != null) {
                if (dp[i].getSideVector().length != getSideVarNames().length) {
                    throw new IllegalArgumentException("This datapoint has a vector of a different lenght " + dp[i].getID());
                }
            }
        }

        this.size = dp.length;
        this.dateCreated = new Date(Calendar.getInstance().getTimeInMillis());
        hmDPNames = new HashMap<>();
    }

    public Dataset(String name, Datapoint[] dp, String[] paramNames) {
        this(name, dp, paramNames, (String[]) null);
    }

    

    public void setID(int ID) {
        this.ID = ID;
    }

    public Dataset deepCopy() {
        Datapoint[] dp = getDatapoints();
        Datapoint[] sd = new Datapoint[dp.length];
        for (int i = 0; i < sd.length; i++) {
            sd[i] = dp[i].clone();
        }
        return new Dataset(name, sd, Arrays.copyOf(paramNames, paramNames.length), sideParamNames == null ? null : Arrays.copyOf(sideParamNames, sideParamNames.length));
    }

    public int getNumDimensions() {
        return paramNames.length;
    }

    public Datapoint[] getDatapoints() {
        return datapoints;
    }

    public String[] getFeatureNames() {
        return Arrays.copyOf(paramNames, paramNames.length);
    }
}
