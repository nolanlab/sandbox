/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author Nikolay
 */
public abstract class Optimization< T extends Object> {

    public static <T> int indexOf(T[] objects, T object) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i].equals(object)) {
                return i;
            }
        }
        return -1;
    }

    public Entry<T, Double> getArgMax(List<T> argumentList) {
        if (argumentList.isEmpty()) {
            throw new IllegalArgumentException("Empty list provided");
        }

        T argMax = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (T arg : argumentList) {
            double currScore = scoringFunction(arg);
            if (currScore > bestScore) {
                argMax = arg;
                bestScore = currScore;
            }
        }
        return new DefaultEntry<T, Double>(argMax, bestScore);
    }

    public Entry<T, Double> getArgMin(List<T> argumentList) {
        if (argumentList.isEmpty()) {
            throw new IllegalArgumentException("Empty list provided");
        }

        T argMin = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (T arg : argumentList) {
            double currScore = scoringFunction(arg);
            if (currScore < bestScore) {
                argMin = arg;
                bestScore = currScore;
            }
        }
        return new DefaultEntry<T, Double>(argMin, bestScore);
    }

    public Entry<T, Double> getArgMax(T[] argumentList) {
        return getArgMax(java.util.Arrays.asList(argumentList));
    }

    public Entry<T, Double> getArgMin(T[] argumentList) {
        return getArgMin(java.util.Arrays.asList(argumentList));
    }

    public abstract double scoringFunction(T arg);
}
