/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map.Entry;
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
        in = Arrays.copyOf(in, in.length);
        Random rnd = new Random();
        Entry<T, Double>[] en = new Entry[in.length];
        for (int i = 0; i < en.length; i++) {
            en[i] = new DefaultEntry<>(in[i], rnd.nextDouble());
        }
        Arrays.sort(en, new Comparator<Entry<T, Double>>() {
            @Override
            public int compare(Entry<T, Double> o1, Entry<T, Double> o2) {
                return (int) Math.signum(o1.getValue() - o2.getValue());
            }
        });
        for (int i = 0; i < en.length; i++) {
            in[i] = en[i].getKey();
        }
        return in;
    }
    
    public static double[] shuffleCopyArray(double[] in) {
        in = Arrays.copyOf(in, in.length);
        Random rnd = new Random();
        Entry<Double, Double>[] en = new Entry[in.length];
        for (int i = 0; i < en.length; i++) {
            en[i] = new DefaultEntry<>(in[i], rnd.nextDouble());
        }
        Arrays.sort(en, (Entry<Double, Double> o1, Entry<Double, Double> o2) -> (int) Math.signum(o1.getValue() - o2.getValue()));
        for (int i = 0; i < en.length; i++) {
            in[i] = en[i].getKey();
        }
        return in;
    }
    
    public static void shuffleArray(double[] in) {
        Random rnd = new Random();
        Entry<Double, Double>[] en = new Entry[in.length];
        for (int i = 0; i < en.length; i++) {
            en[i] = new DefaultEntry<>(in[i], rnd.nextDouble());
        }
        Arrays.sort(en, (Entry<Double, Double> o1, Entry<Double, Double> o2) -> (int) Math.signum(o1.getValue() - o2.getValue()));
        for (int i = 0; i < en.length; i++) {
            in[i] = en[i].getKey();
        }
    }

    public void shuffleArray(T[] in) {

        in = Arrays.copyOf(in, in.length);

        Random rnd = new Random();
        Entry<T, Double>[] en = new Entry[in.length];
        for (int i = 0; i < en.length; i++) {
            en[i] = new DefaultEntry<>(in[i], rnd.nextDouble());
        }
        Arrays.sort(en, new Comparator<Entry<T, Double>>() {
            @Override
            public int compare(Entry<T, Double> o1, Entry<T, Double> o2) {
                return (int) Math.signum(o1.getValue() - o2.getValue());
            }
        });
        for (int i = 0; i < en.length; i++) {
            in[i] = en[i].getKey();
        }
    }
}
