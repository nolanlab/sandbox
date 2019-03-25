/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import sandbox.clustering.ClusterSet;
import sandbox.dataIO.DatasetStub;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


/**
 *
 * @author Nikolay
 */
public class IO {

    
    public static TableModel readTableModelFromCSV(File f) throws IOException{
        ArrayList<String> lines  = getListOfStringsFromStream(new FileInputStream(f));
        String del = "[,\t]+";
        String [] header = lines.get(0).split(del);
        String[][] data = new String[lines.size()-1][];
        for (int i = 0; i < data.length; i++) {
            data[i] = lines.get(i+1).split(del);
            assert(data[i].length==header.length);
        }
        return new DefaultTableModel(data, header);
    }
    
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
                br.close();
            }
        } catch (IOException e) {
            logger.showException(e);
            return null;
        }
        return al;
    }

    public static ArrayList<Integer> getListOfIntegersFromStream(InputStream in, int default_value) {

        ArrayList<Integer> al = new ArrayList<>();
        try {
            String s;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((s = br.readLine()) != null) {
                if (s.trim().length() > 0) {
                    try{
                        al.add(Integer.parseInt(s));
                    }catch(NumberFormatException e){
                        al.add(default_value);
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            logger.showException(e);
            return null;
        }
        return al;
    }

    public static void exportCellClusteringToFCSFile(ClusterSet cs) {
    }

    public static File chooseFileWithDialog(String FileChooserID, final String FileFilterDescription, final String[] extensions, boolean save) {
        
        Preferences pref = Preferences.userRoot().node("sandbox/prefs").node("fileChooserPaths");
        File f = new File(pref.get(FileChooserID, ""));
        JFileChooser jfc = new JFileChooser(f);
        jfc.setSelectedFile(f);
        jfc.setFileSelectionMode(extensions == null ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (extensions != null) {
                    for (String s : extensions) {
                        if (f.isFile() && f.getName().endsWith("." + s)) {
                            return true;
                        }
                    }
                }
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return FileFilterDescription;
            }
        });
        f = null;
        if (save) {
            if (jfc.showSaveDialog(jfc) == JFileChooser.APPROVE_OPTION) {
                f = jfc.getSelectedFile();
            }
            if (f != null) {
                if (f.exists() && extensions != null && !f.isDirectory()) {
                    if (JOptionPane.showConfirmDialog(null, "File '" + f.getName() + "' exists. Overwrite?", "Warning", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                        f = null;
                    }
                }
            }
        } else {
            if (jfc.showOpenDialog(jfc) == JFileChooser.APPROVE_OPTION) {
                f = jfc.getSelectedFile();
            }
        }
        if (f != null) {
            pref.put(FileChooserID, f.getPath());
        }
        return f;
    }
}
