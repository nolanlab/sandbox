/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.fmeasure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author Nikolay
 */
public class IO {

    public static ArrayList<String> getListOfStringsFromStream(InputStream in) {

        ArrayList<String> al = new ArrayList<>();
        try {
            String s;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                while ((s = br.readLine()) != null) {
                    if (s.trim().length() > 0) {
                        al.add(s);
                    }
                }
            }
        } catch (IOException e) {
          e.printStackTrace();
            return null;
        }
        return al;
    }

    public static int[] getListOfIntegersFromStream(InputStream in, int default_value) {

        ArrayList<Integer> al = new ArrayList<>();
        try {
            String s;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((s = br.readLine()) != null) {
                if (s.trim().length() > 0) {
                    try{
                        al.add(Integer.parseInt(s));
                    }catch(NumberFormatException e){
                        //System.err.println("Can't parse: " + s);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
             e.printStackTrace();
            return null;
        } 
        int[] out = new int[al.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = al.get(i);
        }
        return out;
    }

}
