/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import sandbox.clustering.Datapoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 *
 * @author Nikolay
 */
public class util {

    public static double sum(double... arg) {
        double res = 0;
        for (double d : arg) {
            res += d;
        }
        return res;
    }

    public static int[] toArray(ArrayList<Integer> al) {
        int[] res = new int[al.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = al.get(i);
        }
        return res;
    }

    public static String[] getDatapointPIDs(Datapoint[] dp) {
        String[] res = new String[dp.length];
        for (int i = 0; i < dp.length; i++) {
            res[i] = dp[i].getFullName();
        }
        return res;
    }

    public static <T extends Object> List<T> removeDuplicates(List<T> list) {
        Collections.sort(list, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.hashCode() - o2.hashCode();
            }
        });
        ArrayList<T> out = new ArrayList<>();
        if (list.size() > 1) {
            for (int i = 0; i < list.size() - 1; i++) {
                if (!list.get(i).equals(list.get(i + 1))) {
                    out.add(list.get(i));
                }
            }
            out.add(list.get(list.size() - 1));
        } else {
            return list;
        }
        return out;
    }
}
