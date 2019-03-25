/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import util.DefaultEntry;
import util.LinePlusExponent;
import util.MatrixOp;
import util.ProfileAverager;
import util.Shuffle;
import util.logger;


/**
 *
 * @author Nikolay
 */
public class DensityDependentDownsampling {
    
    private int iter = 0;
    private final boolean angular;
    private final double pVal = 0.01;
    public DensityDependentDownsampling(DistanceMeasure dm) {
        logger.print("DensityDependentDownsampling init");
        this.angular = false;
    }
    
    
    private double acos(double x) {
        //Using fast acos approximation
        //http://stackoverflow.com/questions/3380628/fast-arc-cos-algorithm/

        double a = Math.sqrt(2 + 2 * x);
        double b = Math.sqrt(2 - 2 * x);
        double c = Math.sqrt(2 - a);
        return (8 * c - b) / 3;
    }
    
    double [] K= null;
    Integer nSize = -1;

    
    public Datapoint[] doDensityDependendDownsampling(final Dataset ds, final int K, double targetDensityRatio, boolean useExactDensity) {
        
        // final Tesselation tess = new Tesselation(cells);

        final int[][] sortedNeighborLists = new int[ds.size()][];

        //final double pval = 10E-2;
        logger.print("iter:"+ (iter++));
        logger.print("ds size: " + ds.getDatapoints().length);
        logger.print("targetDensityRatio: " + targetDensityRatio);
        logger.print("K: " + K);
        logger.print("useExactDensity:" + useExactDensity);
        

        final ConcurrentLinkedQueue<Datapoint> q = new ConcurrentLinkedQueue<>();
        final Datapoint[] dataset = ds.getDatapoints();
        q.addAll(Arrays.asList(dataset));
        
        final AtomicInteger numDone = new AtomicInteger(0);

        int cpu = Runtime.getRuntime().availableProcessors();
        Thread[] t = new Thread[cpu];
        ThreadGroup tg = new ThreadGroup("KNNthreads");
        final double[] density = new double[dataset.length];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(tg, new Runnable() {
                @Override
                public void run() {
                    Datapoint d;
                    while (true) {
                        synchronized (q) {
                            d = q.poll();
                        }
                        if (d != null) {
                                    sortedNeighborLists[d.getID()] = getKNNExhaustive(d.getVector(), dataset, K);   
                                    density[d.getID()] = Math.exp(getDensityWithKNN(dataset, sortedNeighborLists[d.getID()], d.getVector(), K).doubleValue()) ;
                                int i = numDone.addAndGet(1);
                                if(i%10000==0)logger.print("dp done:" + i);
                            
                        } else {
                            break;
                        }
                    }
                }
            });
            t[i].start();
        }
        do {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.print(e);
            }
        } while (tg.activeCount() > 0);
        System.gc();
        
        double [] copyDensity = Arrays.copyOf(density, density.length);
               
        logger.print("dp array: " + dataset.length);
        logger.print("density array: " + density.length);
        logger.print("copyDensity array: " + copyDensity.length);
        
        
        Arrays.sort(copyDensity);
        double targetDens = 0;
        
        if(useExactDensity){
            targetDens = targetDensityRatio;
            if(copyDensity[copyDensity.length-1]<targetDens){
                return dataset;
            }
        }else{
            targetDens = copyDensity[(int)((copyDensity.length-1)*targetDensityRatio)];
        }
        
        ArrayList<Datapoint> sample = new ArrayList<>();
        
        for (int i = 0; i < density.length; i++) {
            double prob = targetDens/density[i];
            if(Math.random()<prob){
                sample.add(dataset[i]);
            }           
        }
        
        if(iter>20){
            return sample.toArray(new Datapoint[sample.size()]);
        }
        
        Datapoint [] newDP = new Datapoint[sample.size()];
        int [] idMap = new int [sample.size()];
        
        for (int i = 0; i < newDP.length; i++) {
            Datapoint dp = sample.get(i);
            newDP[i] = new Datapoint(dp.getFullName(), dp.getVector(), i);
            idMap[i] = dp.getID();
        }
        Dataset newDS = new Dataset("temp" +((int)(Math.random()*1000000)), newDP, ds.getFeatureNames());
        
        Datapoint [] nextSample = doDensityDependendDownsampling(newDS, K, targetDens, true);
        
        Datapoint [] ret = new Datapoint[nextSample.length];
        for (int i = 0; i < nextSample.length; i++) {
           ret[i] = dataset[idMap[nextSample[i].getID()]];
            logger.print("correl:"+ MatrixOp.getEuclideanCosine(ret[i].getVector(), nextSample[i].getVector()));
        }
        
        return ret;
        
    }

    

    private double dist(double[] v1, double[] v2) {
        if (angular) {
            double d = MatrixOp.getEuclideanCosine(v1, v2);
            if(Double.isNaN(d)||Double.isInfinite(d)) d = 0;
            return acos(d);
        } else {
            double[] minusVec = MatrixOp.diff(v1, v2);
            return MatrixOp.lenght(minusVec);
        }
    }

    private Double sim(double[] v1, double[] v2) {
        //BigDecimal res = 
        //logger.print(res);
        if(angular){
            double d  = MatrixOp.getEuclideanCosine(v1, v2);
            if(Double.isInfinite(d)||Double.isNaN(d)){
                d = 0;
            }
            return d;
        }
        return 1 / (dist(v1, v2) + 1);
    }

    

    private BigDecimal getDensityWithKNN(Datapoint[] dp, int[] sortedN, double[] center, int knn) {
        BigDecimal dens = new BigDecimal(0, MathContext.DECIMAL128);
        //double minSim = 1;
        for (int i = 0; i < knn; i++) {
            dens = dens.add(new BigDecimal(-dist(center, dp[sortedN[i]].getVector()), MathContext.DECIMAL128));
            //minSim = Math.min(sim(center, dp[sortedN[i]].getVector()), minSim);
        }
        //logger.print(dens);

        //dens.divide(new BigDecimal(knn, MathContext.DECIMAL128));
        //logger.print(dens);
        return   dens;
    }


    private int[] getKNNExhaustive(double[] cent, Datapoint[] dataset, int knn) {
        DefaultEntry<Datapoint, Double>[] lst = new DefaultEntry[knn];

        for (int k = 0; k < knn; k++) {
            lst[k] = new DefaultEntry<>(dataset[k], dist(cent, dataset[k].getVector()));
        }
        Arrays.sort(lst, new Comparator<Entry<Datapoint, Double>>() {
            @Override
            public int compare(Entry<Datapoint, Double> o1, Entry<Datapoint, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        LinkedList<DefaultEntry<Datapoint, Double>> ln = new LinkedList<>();
        for (int k = 0; k < knn; k++) {
            ln.add(lst[k]);
        }

        lst = null;

        for (int k = knn; k < dataset.length; k++) {
            Double maxDist = ln.getLast().getValue();
            Double d = dist(cent, dataset[k].getVector());
            if (d.compareTo(maxDist) < 0) {
                for (int j = 0; j < knn; j++) {
                    if (ln.get(j).getValue().compareTo(d) > 0) {
                        ln.add(j, new DefaultEntry<>(dataset[k], d));
                        ln.removeLast();
                        break;
                    }
                }
            }
        }

        int[] res = new int[ln.size()];
        for (int p = 0; p < res.length; p++) {
            res[p] = ln.get(p).getKey().getID();
        }
        ln = null;
        //logger.print("getKNNExhaustive K"+knn + " took " + (Calendar.getInstance().getTimeInMillis()-time));
        return res;
    }

    private class DatapointDist implements Comparable {

        Datapoint dp;
        double dist;

        @Override
        public String toString() {
            return dp.getID() + ", " + dist;
        }

        public DatapointDist(Datapoint dp, double dist) {
            this.dp = dp;
            this.dist = dist;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof DatapointDist) {
                return (int) Math.signum(this.dist - ((DatapointDist) o).dist);
            } else {
                return 0;
            }
        }
    }
}
