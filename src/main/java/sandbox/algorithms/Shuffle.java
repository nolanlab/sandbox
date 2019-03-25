/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Nikolay
 */
public class Shuffle<T> {

    public ArrayList<T> shuffleArrayList(ArrayList<T> in) {
        ArrayList<T> al = (ArrayList<T>) in.clone();
        ArrayList<T> out = new ArrayList<T>(al.size());
        Random rnd = new Random();
        int idx;
        for (int i = 0; i < in.size(); i++) {
            idx = rnd.nextInt(al.size());
            out.add(al.get(idx));
            al.remove(idx);
        }
        return out;
    }

    public T[] shuffleCopyArray(T[] in) {
        Random rnd = new Random();
        T[] out = Arrays.copyOf(in, in.length);
        T tmp;
        int idx;
        for (int i = out.length - 1; i > 0; i--) {
            idx = rnd.nextInt(i);
            tmp = out[i];
            out[i] = out[idx];
            out[idx] = tmp;
        }
        return out;
    }

    public void shuffleArray(T[] in) {
        Random rnd = new Random();
        T tmp;
        int idx;
        for (int i = in.length - 1; i > 0; i--) {
            idx = rnd.nextInt(i);
            tmp = in[i];
            in[i] = in[idx];
            in[idx] = tmp;
        }
    }
    
    public double[] shuffleArray(double [] in) {
        in = Arrays.copyOf(in, in.length);
        Random rnd = new Random();
        double tmp;
        int idx;
        for (int i = in.length - 1; i > 0; i--) {
            idx = rnd.nextInt(i);
            tmp = in[i];
            in[i] = in[idx];
            in[idx] = tmp;
        }
        return in;
    }
}
