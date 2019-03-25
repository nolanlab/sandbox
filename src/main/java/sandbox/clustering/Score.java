/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.clustering;

import java.io.Serializable;

/**
 *
 * @author Nikolay
 */
public class Score implements Serializable {

    public static final long serialVersionUID = 1L;
    public final ScoringMethod scoringMethod;
    public double score;

    public static enum ScoringMethod {

        Euclidean_Length, Chi2, PSS, Membership, Similarity_To_Mode, LDA
    };

    public Score(ScoringMethod scoringMethod, double score) {
        this.scoringMethod = scoringMethod;
        this.score = score;
    }

    @Override
    public String toString() {
        return String.valueOf(score);
    }
}
