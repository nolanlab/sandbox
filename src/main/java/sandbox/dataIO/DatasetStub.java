/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbox.dataIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import net.sf.flowcyt.gp.modules.fcs2csv.FCSInputDataset;
import util.IO;
import util.logger;
import org.flowcyt.cfcs.CFCSDataSet;

/**
 *
 * @author Nikolay
 */
public abstract class DatasetStub {

    public abstract String[] getLongColumnNames();

    public abstract String[] getShortColumnNames();

    public abstract String getRowName(int i);

    public abstract double[] getRow(int i);

    public abstract long getRowCount();
    //public abstract String [] getSkippedColumns();

    public abstract String[] getSkippedRows();

    public abstract String getFileName();

    public abstract void close() throws IOException;

    public static DatasetStub createFromFCS(final File f) {

        try{
        FCSInputDataset fcs = new FCSInputDataset(f);
        
        final String formatString = "%07d";
        /*((int)(Math.ceil(Math.log10(fcs.getEventCount()))+2))*/

        int tmpTimeColIdx = -1;
        for (int i = 0; i < fcs.getParameterNames().length; i++) {
            if (fcs.getParameterNames()[i].trim().equalsIgnoreCase("Time")) {
                tmpTimeColIdx = i;
                break;
            }
        }

        return new DatasetStub() {

            String[] colNames;
            String[] shortColNames;
            long rowCnt = -1;

            @Override
            public void close() throws IOException {
                fcs.close();
            }

            @Override
            public String getFileName() {
                return f.getName().endsWith(".fcs")?f.getName().substring(0, f.getName().length() - 4):f.getName();
            }

            @Override
            public String[] getShortColumnNames() {
                if (shortColNames == null) {
                    shortColNames = fcs.getParameterNames();
                }
                return shortColNames;
            }

            @Override
            public String[] getSkippedRows() {
                return new String[0];
            }

            @Override
            public String[] getLongColumnNames() {
                if (colNames == null) {
                    colNames = fcs.getParameterLongNames();
                }
                return colNames;
            }

            @Override
            public String getRowName(int i) {
                if (i >= fcs.getEventCount()) {
                    throw new ArrayIndexOutOfBoundsException("out of bounds" + i);
                }
                return null;
            }

            @Override
            public double[] getRow(int i) {
                    return fcs.getEventAsDoubles(i, Boolean.FALSE);
            }

            @Override
            public long getRowCount() {
                if (rowCnt == -1) {
                    rowCnt = fcs.getEventCount();
                }
                return rowCnt;
            }
        };
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static DatasetStub createFromTXT(final File f) {

        return new DatasetStub() {
            ArrayList<String> stringList;
            final String del = "[\t,]+";
            //String [] firstStringDelimited;
            String[] headStringDelimited;
            ArrayList<String> skippedRows = new ArrayList<>();

            @Override
            public String getFileName() {
                return (f.getName().endsWith(".csv")||f.getName().endsWith(".txt"))?f.getName().substring(0, f.getName().length() - 4):f.getName();
            }

            @Override
            public String[] getShortColumnNames() {
                if (headStringDelimited == null) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(f));
                        headStringDelimited = br.readLine().split(del);
                    } catch (IOException e) {
                        logger.showException(e);
                        return null;
                    }
                }
                String[] names = Arrays.copyOfRange(headStringDelimited, 1, headStringDelimited.length);
                for (int i = 0; i < names.length; i++) {
                    names[i] = (names[i].contains(":")? names[i].split(":")[0]:names[i]).trim();
                }
                return names;
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public String[] getSkippedRows() {
                return skippedRows.toArray(new String[skippedRows.size()]);
            }

            @Override
            public String[] getLongColumnNames() {
                if (headStringDelimited == null) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(f));
                        headStringDelimited = br.readLine().split(del);
                    } catch (IOException e) {
                        logger.showException(e);
                        return null;
                    }
                }
                String[] names = Arrays.copyOfRange(headStringDelimited, 1, headStringDelimited.length);
                for (int i = 0; i < names.length; i++) {
                    names[i] = (names[i].contains(":")? names[i].split(":")[1]:names[i]).trim();
                }
                return names;
            }

            @Override
            public String getRowName(int i) {
                try {
                    if (stringList == null) {
                        stringList = IO.getListOfStringsFromStream(new FileInputStream(f));
                    }
                    try {
                        return stringList.get(i + 1).split("[\t,]")[0];
                    } catch (StringIndexOutOfBoundsException e) {
                        return null;
                    }
                } catch (IOException e) {
                    logger.showException(e);
                    return null;
                }
            }

            @Override
            public double[] getRow(int i) {
                try {
                    if (stringList == null) {
                        stringList = IO.getListOfStringsFromStream(new FileInputStream(f));
                    }
                    String[] s = stringList.get(i + 1).split(del);
                    if (s.length != getLongColumnNames().length + 1) {
                        skippedRows.add(i + ", reason: invalid number of columns (" + stringList.get(i) + " instead of " + stringList.get(i));
                        return null;
                    }
                    double[] vec = new double[s.length - 1];
                    for (int j = 0; j < vec.length; j++) {
                        try {
                            vec[j] = Double.parseDouble(s[j + 1].replace('"', ' ').trim());
                        } catch (NumberFormatException e) {
                            skippedRows.add(i + ", reason: invalid numerical format (" + s[j] + ")");
                            return null;
                        }

                    }
                    return vec;
                } catch (IOException e) {
                    logger.showException(e);
                    return null;
                }
            }

            @Override
            public long getRowCount() {
                try {
                    if (stringList == null) {
                        stringList = IO.getListOfStringsFromStream(new FileInputStream(f));
                    }
                } catch (IOException e) {
                    logger.showException(e);
                    return 0;
                }
                return stringList.size() - 1;
            }
        };

    }
}
