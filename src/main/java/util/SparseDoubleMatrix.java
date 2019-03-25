/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Nikolay
 */
public class SparseDoubleMatrix {

    private final int rows;
    private final int columns;
    //private final int factor = ;
    private ConcurrentHashMap<Integer, Double> hm;
    private ConcurrentLinkedQueue<Integer>[] columnsForRow;
    private ConcurrentLinkedQueue<Integer>[] rowsForColumn;

    public SparseDoubleMatrix(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        hm = new ConcurrentHashMap<>();
        columnsForRow = new ConcurrentLinkedQueue[rows];
        rowsForColumn = new ConcurrentLinkedQueue[columns];
    }

    public Queue<Integer> getNonemptyRowsForColumn(int i) {
        Queue<Integer> ret = rowsForColumn[i];
        return (ret == null) ? new ConcurrentLinkedQueue<Integer>() : ret;
    }

    public Queue<Integer> getNonemptyColumnsForRow(int i) {
        //return columnsForRow[i];
        Queue<Integer> ret = columnsForRow[i];
        return (ret == null) ? new ConcurrentLinkedQueue<Integer>() : ret;
    }

    public double get(int x, int y) {
        /*if (x > rows && y > columns) {
         throw new IllegalArgumentException("Out of range! x=" + x + " y=" + y + " rows=" + rows + " columns=" + columns);
         }*/
        Integer idx = x * columns + y;
        Double res = hm.get(idx);
        return (res == null) ? 0 : res;
    }

    public int rows() {
        return rows;
    }

    public int columns() {
        return columns;
    }

    public void set(int x, int y, double val) {
        /*if (x > rows && y > columns) {
         throw new IllegalArgumentException("Out of range! x=" + x + " y=" + y + " rows=" + rows + " columns=" + columns);
         }*/

        Integer idx = x * columns + y;
        if (hm.get(idx) == null) {
            if (columnsForRow[x] == null) {
                columnsForRow[x] = new ConcurrentLinkedQueue<>();
            }
            columnsForRow[x].add(y);
            if (rowsForColumn[y] == null) {
                rowsForColumn[y] = new ConcurrentLinkedQueue<>();
            }
            rowsForColumn[y].add(x);
        }
        hm.put(idx, val);
    }
}
