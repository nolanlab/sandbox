/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import sandbox.clustering.Cluster;
import sandbox.annotations.Annotation;


/**
 *
 * @author Nikolay
 */
public class ClusterValidation {

    public static class ValidationInfo {

        public final String className;
        public final Cluster bestMatchCluster;
        public final double precision;
        public final double recall;
        public final double FMeasure;

        public ValidationInfo(String className, Cluster bestMatchCluster, double precision, double recall, double FMeasure) {
            this.className = className;
            this.bestMatchCluster = bestMatchCluster;
            this.precision = (Double.isNaN(precision) || Double.isInfinite(precision)) ? 0.0 : precision;
            this.recall = (Double.isNaN(recall) || Double.isInfinite(recall)) ? 0.0 : recall;;
            this.FMeasure = (Double.isNaN(FMeasure) || Double.isInfinite(FMeasure)) ? 0.0 : FMeasure;
        }
    }

    

    public static class AnnotationCorrelationInfo {

        public final double correl;
        public final double pValue;
        public final Annotation annotation;
        public final Cluster cluster1;
        public final Cluster cluster2;

        public AnnotationCorrelationInfo(double correl, double pValue, Annotation annotation, Cluster cluster1, Cluster cluster2) {
            this.correl = correl;
            this.pValue = pValue;
            this.annotation = annotation;
            this.cluster1 = cluster1;
            this.cluster2 = cluster2;
        }
    }

    

    
}
