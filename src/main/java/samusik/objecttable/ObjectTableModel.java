/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package samusik.objecttable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;


/**
 *
 * @param <T> class that the object is going to deal with
 * @author Nikolay
 */
public class ObjectTableModel<T extends RowRepresentable> extends AbstractTableModel implements PropertyChangeListener {

    ArrayList<T> data;
    HashMap<T, Integer> dataIndexHash;
    ArrayList<TableModelListener> listners;
    ArrayList<Object[]> rows;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("Row")) {
            int idx = dataIndexHash.get((T) evt.getSource());
            rows.set(idx, (Object[]) evt.getNewValue());
            fireTableListeners(new TableModelEvent(this, idx, idx, TableModelEvent.ALL_COLUMNS));
        }
        if (evt.getPropertyName().equals("Header")) {
            ((T) evt.getSource()).getHeaderRow();
            fireTableListeners(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        }
    }

    public ObjectTableModel(T[] dataArr) {
        w.lock();
        try {
            this.data = new ArrayList<T>(dataArr.length);
            this.data.addAll(Arrays.asList(dataArr));
            listners = new ArrayList<TableModelListener>();
            rows = new ArrayList<Object[]>(data.size());

            for (int i = 0; i < data.size(); i++) {
                if (!data.get(0).getClass().isInstance(data.get(0))) {
                    throw new IllegalArgumentException("Data array is non-homogenous");
                }
                rows.add(new Object[0]);
                data.get(i).addPropertyChangeListener(this);
            }
            rebuild();
        } finally {
            w.unlock();
        }
    }

    private void fireTableListeners(TableModelEvent e) {
        w.lock();
        for (int i = 0; i < listners.size(); i++) {
            listners.get(i).tableChanged(e);
        }
        
        try {
            if (this.getRowCount() == 0) {
                e = new TableModelEvent(this, TableModelEvent.HEADER_ROW);
            } else {
                int firstRow = e.getFirstRow();
                int lastRow = e.getLastRow();
                lastRow = Math.min(lastRow, this.getRowCount() - 1);
                e = new TableModelEvent(this, firstRow, lastRow);
            }

        } finally {
            w.unlock();
        }
    }

    public void swapRowsTo(int[] rowsToSwap, int newRowIndex) {
        w.lock();
        try {
            if (newRowIndex > data.size()) {
                throw new IllegalArgumentException("newRowIndex: " + newRowIndex + " is out of bounds");
            }
            ArrayList<T> alData = new ArrayList<T>();
            ArrayList<Object[]> alRows = new ArrayList<Object[]>();
            Arrays.sort(rowsToSwap);
            int rowCntAboveNew = 0;
            for (int f : rowsToSwap) {
                if (f < newRowIndex) {
                    rowCntAboveNew++;
                }
                alData.add(data.get(f));
                alRows.add(rows.get(f));
            }
            data.removeAll(alData);
            rows.removeAll(alRows);

            data.addAll(newRowIndex - rowCntAboveNew, alData);
            rows.addAll(newRowIndex - rowCntAboveNew, alRows);
            dataVector = null;
            fireTableListeners(new TableModelEvent(this, Math.min(newRowIndex, rowsToSwap[0]), data.size() - 1));
        } finally {
            w.unlock();
        }
    }

    public int getRowOfObject(T obj) {
        r.lock();
        int idx = -1;
        try {
            if (dataIndexHash.get(obj) != null) {
                idx = dataIndexHash.get(obj);
            }
        } finally {
            r.unlock();
        }
        return idx;
    }

    public T[] getObjects(T[] dataarray) {
        r.lock();
        try {
            T[] obj = data.toArray(dataarray);
            return obj;
        } finally {
            r.unlock();
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        w.lock();
        try {
            listners.add(l);
        } finally {
            w.unlock();
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        r.lock();
        try {
            Class c = Object.class;
            if (rows != null) {
                if (rows.size() > 0) {
                    if (columnIndex >= 0 && columnIndex < rows.get(0).length) {
                        if (rows.get(0)[columnIndex] != null) {
                            c = rows.get(0)[columnIndex].getClass();
                        }
                    }
                }
            }
            return c;
        } finally {
            r.unlock();
        }
    }

    @Override
    public int getColumnCount() {
        r.lock();
        int size = 0;
        try {
            if (data != null) {
                if (data.size() > 0) {
                    if (rows.get(0).length == 0) {
                        rows.set(0, data.get(0).toRow());
                    }
                    size = rows.get(0).length;
                }
            }

        } finally {
            r.unlock();
        }
        return size;
    }

    @Override
    public String getColumnName(int columnIndex) {
        r.lock();
        String cn = "";
        try {
            if (data != null) {
                if (columnIndex < data.get(0).getHeaderRow().length) {
                    cn = data.get(0).getHeaderRow()[columnIndex];
                }
            }
        } finally {
            r.unlock();
        }
        return cn;
    }

    @Override
    public int getRowCount() {
        r.lock();
        int cnt = 0;
        try {
            if (data != null) {
                cnt = data.size();
            }
        } finally {
            r.unlock();
        }
        return cnt;
    }

    public ArrayList<ArrayList<Object>> getDataVector() {
        r.lock();
        try {
            if (dataVector == null) {
                dataVector = new ArrayList<ArrayList<Object>>(data.size());
                for (Object[] f : rows) {
                    ArrayList<Object> vec = new ArrayList<Object>(f.length);
                    vec.addAll(Arrays.asList(f));
                    dataVector.add(vec);
                }
            }
        } finally {
            r.unlock();
        }
        return dataVector;
    }
    ArrayList<ArrayList<Object>> dataVector = null;

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
       
        Object obj = null;
            if (rowIndex < rows.size()) {

                if (rows.get(rowIndex).length == 0) {
                    try {
                        w.lock();
                        rows.set(rowIndex, data.get(rowIndex).toRow());
                    } finally {
                        w.unlock();
                    }
                }
                if (rowIndex < rows.size() && columnIndex < getColumnCount()) {
                   try{
                    obj = rows.get(rowIndex)[columnIndex];
                   }catch(IndexOutOfBoundsException e){
                       return "void";
                   }
                } else {
                    obj = "void";
                }
            }
        
        return obj;


    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        r.lock();
        boolean b = false;
        try {
            if (rowIndex < data.size()) {
                if (columnIndex < data.get(rowIndex).getEditibilityMask().length) {
                    b = data.get(rowIndex).getEditibilityMask()[columnIndex];
                }
            }
        } finally {
            r.unlock();
        }
        return b;
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        w.lock();
        try {
            listners.remove(l);
        } finally {
            w.unlock();
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        w.lock();
        try {
            if (isCellEditable(rowIndex, columnIndex)) {
                data.get(rowIndex).updateValue(columnIndex, aValue);
            }
        } finally {
            w.unlock();
        }
    }

    public void addRow(T rowObject) {
        w.lock();
        try {
            data.add(rowObject);
            rows.add(rowObject.toRow());
            rebuild();
        } finally {
            w.unlock();
        }
        fireTableListeners(new TableModelEvent(this, this.getRowCount() - 1, this.getRowCount(), TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    private void rebuild() {
        dataIndexHash = new HashMap<T, Integer>();
        for (int i = 0; i < data.size(); i++) {
            dataIndexHash.put(data.get(i), i);
        }
    }

    public void removeRow(T rowObject) {
        if (dataIndexHash == null) {
            rebuild();
        }
        int i = getRowOfObject(rowObject);
        removeRow(i);
    }

    public void removeRow(int row) {
        if (row < 0 || row > data.size()) {
            return;
        }
        w.lock();
        boolean rowRemoved = false;
        try {
            data.get(row).removePropertyChangeListener(this);
            data.remove(row);
            rows.remove(row);
            rowRemoved = true;
            rebuild();
        } finally {
            w.unlock();
        }
        
        if (rowRemoved && row < getRowCount() && row >=0) {
            fireTableListeners(new TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
        }
    }
}
