/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.dataIO;

import sandbox.annotations.Annotation;
import sandbox.clustering.Cluster;
import sandbox.clustering.ClusterMember;
import sandbox.clustering.ClusterSet;
import sandbox.clustering.Datapoint;
import sandbox.clustering.Dataset;
import sandbox.fcs.ExportFCS;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import util.MatrixOp;

/**
 *
 * @author Nikolay
 */
public class ClusterSetToFCSExporter {

    public static void exportDatapoints(Dataset ds, ClusterSet cs, String out, Annotation ann, String back_transform) throws IOException, SQLException {

        String[] shortParamNames = new String[ds.getFeatureNamesCombined().length + ((cs == null) ? 0 : 1)];
        String[] longParamNames = new String[ds.getFeatureNamesCombined().length + ((cs == null) ? 0 : 1)];

        for (int i = 0; i < ds.getFeatureNamesCombined().length; i++) {
            String s = ds.getFeatureNamesCombined()[i];
            String[] s2 = s.split("[\\|:]");
            shortParamNames[i] = (s2.length > 1) ? s2[1] : s2[0];
            longParamNames[i] = s2[0];
        }

        int[] idx = null;
        if (cs != null) {
            idx = cs.getAssignmentIndexString();
            shortParamNames[shortParamNames.length - 1] = "clusterID";
            longParamNames[longParamNames.length - 1] = "clusterID " + cs.getClusteringAlgorithm() + ", " + cs.getClusteringParameterString();
        }

        Datapoint[] dp = ds.getDatapoints();

        if (ann != null) {
            for (String term : ann.getTerms()) {
                int[] dpid = ann.getDpIDsForTerm(term);
                float[][] evt = new float[dpid.length][];
                int x = 0;
                for (int i : dpid) {
                    double[] vecD = Arrays.copyOf(MatrixOp.concat(dp[i].getVector(), dp[i].getSideVector()), ds.getFeatureNamesCombined().length + 1);
                    float[] vec = new float[vecD.length];

                    //System.err.println("back_transform:"+back_transform);
                    switch (back_transform) {
                        case "ASINH":
                            for (int j = 0; j < vec.length; j++) {
                                vec[j] = (float) (Math.sinh(vecD[j]) * 5);
                            }
                            //System.err.println("chosen ASINH");
                            break;
                        case "LOG":
                            for (int j = 0; j < vec.length; j++) {
                                vec[j] = (float) (Math.exp(vecD[j]));
                            }
                            //System.err.println("chosen LOG");
                            break;
                        default:
                            for (int j = 0; j < vec.length; j++) {
                                vec[j] = (float) vecD[j];
                            }
                        //System.err.println("chosen DEFAULT");
                    }
                    if (cs != null) {
                        vec[ds.getFeatureNamesCombined().length] = idx[i];
                    }
                    evt[x++] = vec;
                }
                try {
                    (new ExportFCS()).writeFCSAsFloat(out + File.separator + ds.getName() + "_" + (cs != null ? (cs.getClusteringAlgorithm() + "_" + cs.getClusteringParameterString() + "_") : "") + term + ".fcs", evt, shortParamNames, longParamNames);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void exportDatapointsSeparateClusters(Dataset ds, ClusterSet cs, String out, Annotation ann, String back_transform) throws IOException, SQLException {

        if (cs == null) {
            throw new IllegalArgumentException("Cluster Set is null");
        }

        String[] shortParamNames = new String[ds.getFeatureNamesCombined().length];
        String[] longParamNames = new String[ds.getFeatureNamesCombined().length];

        for (int i = 0; i < ds.getFeatureNamesCombined().length; i++) {
            String s = ds.getFeatureNamesCombined()[i];
            String[] s2 = s.split("[\\|:]");
            shortParamNames[i] = (s2.length > 1) ? s2[1] : s2[0];
            longParamNames[i] = s2[0];
        }

        Cluster[] c = cs.getClusters();
        
        for (Cluster cluster : c) {
            ArrayList<Datapoint> al = new ArrayList<>();
            
            for (ClusterMember cm : cluster.getClusterMembers()) {
                al.add(cm.getDatapoint());
            }
            
            if(al.isEmpty()) continue;
            
            Datapoint[] dp= al.toArray(new Datapoint[al.size()]);
            
            int[] dpIDsInCluster = new int[dp.length];
            
            for (int i = 0; i < dpIDsInCluster.length; i++) {
                dpIDsInCluster[i] = dp[i].getID();
                
            }
            
            Arrays.sort(dpIDsInCluster);
            
            if (ann != null) {
            for (String term : ann.getTerms()) {
                int[] dpid = ann.getDpIDsForTerm(term);
                
                ArrayList<float[]> evt = new ArrayList<>();
                int x = 0;
                for (int i : dpid) {
                    
                    if(Arrays.binarySearch(dpIDsInCluster, i)<0){
                        continue;
                    }
                    
                    double[] vecD = Arrays.copyOf(MatrixOp.concat(ds.getDPByID(i).getVector(), ds.getDPByID(i).getSideVector()), ds.getFeatureNamesCombined().length + 1);
                    float[] vec = new float[vecD.length];

                    //System.err.println("back_transform:"+back_transform);
                    switch (back_transform) {
                        case "ASINH":
                            for (int j = 0; j < vec.length; j++) {
                                vec[j] = (float) (Math.sinh(vecD[j]) * 5);
                            }
                            //System.err.println("chosen ASINH");
                            break;
                        case "LOG":
                            for (int j = 0; j < vec.length; j++) {
                                vec[j] = (float) (Math.exp(vecD[j]));
                            }
                            //System.err.println("chosen LOG");
                            break;
                        default:
                            for (int j = 0; j < vec.length; j++) {
                                vec[j] = (float) vecD[j];
                            }
                        //System.err.println("chosen DEFAULT");
                    }
                    evt.add(vec);
                }
                if(evt.isEmpty()) continue;
                try {
                    (new ExportFCS()).writeFCSAsFloat(out + File.separator + ds.getName() + "_" +  (cs.getClusteringAlgorithm() + "_" + cs.getClusteringParameterString() + "_clusterID_"+ cluster.getID())  + term + ".fcs", evt.toArray(new  float[evt.size()][]), shortParamNames, longParamNames);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        }

        

    }

    /*
    public static void exportDatapoints(Datapoint[] dp, String fcsFilePath, File out, DatasetStub stub, ImportConfigObject ico, boolean writeOriginalValues, String postfix) throws IOException, SQLException {

        BigFCSFile fcsFile = new BigFCSFile(fcsFilePath);

        FCSWriter writer = new FCSWriter();

        FCSParameterList pl = new FCSParameterList();

        int chIDX = 1;
        for (int i = 0; i < fcsFile.getChannelCount(); i++) {
            pl.addChannel(new FCSChannel(chIDX++, fcsFile.channelShortname[i], fcsFile.channelName[i]));
        }

        double[][] events = new double[dp.length][fcsFile.getChannelCount()];

        int[] evtIndexMap = new int[dp.length];
        for (int i = 0; i < evtIndexMap.length; i++) {
            Datapoint d = dp[i];
            String name[] = d.getFullName().split("Event");
            int evtidx = Integer.parseInt(name[1].trim());
            evtIndexMap[i] = evtidx;
        }

        double[][] inEvents = fcsFile.getEventList();

        for (int i = 0; i < evtIndexMap.length; i++) {
            for (int j = 0; j < fcsFile.getChannelCount(); j++) {
                events[i][j] = writeOriginalValues ? inEvents[j][evtIndexMap[i]] : dp[i].getVector()[j];
            }
        }

        try {
            writer.writeFCSFile(events, pl, out.getPath() + File.separator + fcsFile.getName().split("\\.")[0] + postfix + ".fcs", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportDatapointsPerCluster(ClusterSet cs, File out, Annotation ann, String back_transform) throws IOException, SQLException {

        Dataset ds = cs.getDataset();

        FCSWriter writer = new FCSWriter();

        FCSParameterList pl = new FCSParameterList();

        if (!out.isDirectory()) {
            out = out.getParentFile();
        }

        int chIDX = 1;
        for (int i = 0; i < ds.getFeatureNamesCombined().length; i++) {
            pl.addChannel(new FCSChannel(chIDX++, (ds.getFeatureNamesCombined()[i].contains(":") || ds.getFeatureNamesCombined()[i].contains("|")) ? ds.getFeatureNamesCombined()[i].split("[\\|:]")[1] : ds.getFeatureNamesCombined()[i], ds.getFeatureNamesCombined()[i].split("[\\|:]")[0]));
        }

        for (Cluster c : cs.getClusters()) {

            for (String term : ann.getTerms()) {
                int[] dpid = ann.getDpIDsForTerm(term);
                Arrays.sort(dpid);
                List<double[]> evt = new LinkedList<>();
                for (ClusterMember cm : c.getClusterMembers()) {
                    Datapoint dp = cm.getDatapoint();
                    if (Arrays.binarySearch(dpid, dp.getID()) < 0) {
                        continue;
                    }
                    double[] vec = MatrixOp.concat(dp.getVector(), dp.getSideVector());

                    switch (back_transform) {
                        case "ASINH":
                            for (int j = 0; j < vec.length; j++) {
                                vec[j] = Math.sinh(vec[j]);
                            }
                        case "LOG":
                            for (int j = 0; j < vec.length; j++) {
                                vec[j] = Math.exp(vec[j]);
                            }
                    }
                    evt.add(vec);
                }
                try {
                    writer.writeFCSFile(evt.toArray(new double[evt.size()][]), pl, out.getPath() + File.separator + "Cluster_id" + c.getID() + "_" + term + ".fcs", false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void exportDatapoints(ClusterSet cs, File out, Annotation ann, String back_transform) throws IOException, SQLException {

        Dataset ds = cs.getDataset();

        int[] idx = cs.getAssignmentIndexString();

        FCSWriter writer = new FCSWriter();

        FCSParameterList pl = new FCSParameterList();

        if (!out.isDirectory()) {
            out = out.getParentFile();
        }

        int chIDX = 1;
        for (int i = 0; i < ds.getFeatureNamesCombined().length; i++) {
            pl.addChannel(new FCSChannel(chIDX++, (ds.getFeatureNamesCombined()[i].contains(":") || ds.getFeatureNamesCombined()[i].contains("|")) ? ds.getFeatureNamesCombined()[i].split("[\\|:]")[1] : ds.getFeatureNamesCombined()[i], ds.getFeatureNamesCombined()[i].split("[\\|:]")[0]));
        }

        pl.addChannel(new FCSChannel(chIDX++, "cluster " + cs.getClusteringAlgorithm() + ", " + cs.getClusteringParameterString(), "cluster"));

        Datapoint[] dp = ds.getDatapoints();

        for (String term : ann.getTerms()) {
            int[] dpid = ann.getDpIDsForTerm(term);
            double[][] evt = new double[dpid.length][];
            int x = 0;
            for (int i : dpid) {
                double[] vec = Arrays.copyOf(MatrixOp.concat(dp[i].getVector(), dp[i].getSideVector()), ds.getFeatureNamesCombined().length + 1);

                switch (back_transform) {
                    case "ASINH":
                        for (int j = 0; j < vec.length; j++) {
                            vec[j] = Math.sinh(vec[j]);
                        }
                    case "LOG":
                        for (int j = 0; j < vec.length; j++) {
                            vec[j] = Math.exp(vec[j]);
                        }
                }

                vec[ds.getFeatureNamesCombined().length] = idx[i];
                evt[x++] = vec;
            }
            try {
                writer.writeFCSFile(evt, pl, out.getPath() + File.separator + ds.getName() + "_" + cs.getClusteringAlgorithm() + "_" + cs.getClusteringParameterString() + "_" + term + ".fcs", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
     */
}
