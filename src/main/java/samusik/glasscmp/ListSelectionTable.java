/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package samusik.glasscmp;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import samusik.objecttable.DraggableTable;
import samusik.objecttable.TableModelReorderHandler;

/**
 *
 * @author Nikolay
 */
public class ListSelectionTable<T extends Object> extends DraggableTable {

    DefaultTableModel dtm;
    @Deprecated
    private ListSelectionTable(DefaultTableModel m) {
        super(m);
        this.setTransferHandler(new TableModelReorderHandler());
        
    }

    @Override
    public ListSelectionModel getSelectionModel() {
        return super.getSelectionModel(); //To change body of generated methods, choose Tools | Templates.
    }
    ArrayList<ListSelectionListener> lst = new ArrayList<>();

    public void setSelected(T[] options) {

        for (int j = 0; j < dtm.getRowCount(); j++) {
            dtm.setValueAt(false, j, 1);
        }
        for (int i = 0; i < options.length; i++) {
            for (int j = 0; j < dtm.getRowCount(); j++) {
                if (dtm.getValueAt(j, 2).equals(options[i])) {
                    dtm.setValueAt(true, j, 1);
                    break;
                }
            }
        }
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        lst.add(listener);
    }

    public void removeListSelectionListener(ListSelectionListener listener) {
        lst.remove(listener);
    }

    @Override
    public String getToolTipText() {
        return "Left-click on checkboxes to choose multiple options. Right-click to choose one option. Select and drag rows to reorder.";
    }
    

    public ListSelectionTable(T[] options, String optionName) {
        if (options.length == 0) {
            throw new IllegalArgumentException("Empty list provided");
        }
        dtm = new DefaultTableModel(new String[]{"#", "Selected", optionName}, 0) {
            @Override
            public Object getValueAt(int row, int column) {
                if (column == 0) {
                    return row;
                } else {
                    return super.getValueAt(row, column);
                }
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return Boolean.class;
                    default:
                        return Object.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        dtm.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 1) {
                    for (ListSelectionListener l : lst) {
                        l.valueChanged(new ListSelectionEvent(this, e.getFirstRow(), e.getLastRow(), false));
                    }
                }
            }
        });

        for (int i = 0; i < options.length; i++) {
            Object[] row = new Object[]{
                i,
                false,
                options[i]
            };
            dtm.addRow(row);
        }
        this.setModel(dtm);
        this.getColumnModel().getColumn(0).setPreferredWidth(20);
        this.getColumnModel().getColumn(1).setPreferredWidth(20);
        this.getColumnModel().getColumn(2).setPreferredWidth(1000);
        this.setTransferHandler(new TableModelReorderHandler());
        
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                 if(e.getButton()==MouseEvent.BUTTON3&&ListSelectionTable.this.columnAtPoint(e.getPoint())==1){
                    int x = ListSelectionTable.this.rowAtPoint(e.getPoint());
                    for (int i = 0; i < dtm.getRowCount(); i++) {
                        dtm.setValueAt(i==x, i, 1);
                    }
                }
            }
        });
        
    }
    
    
    
    public void addValue(T value, int idx) {
        for (int i = 0; i < dtm.getRowCount(); i++) {
            if(dtm.getValueAt(i, 2).equals(value)){
                return;
            }
        }
        //removeValue(value);
        idx = Math.min(dtm.getRowCount(), Math.max(0, idx));
        dtm.addRow(new Object[]{idx, false, value});
        ((TableModelReorderHandler) this.getTransferHandler()).swapRowsTo(dtm, new int[]{dtm.getRowCount() - 1}, idx);
        for (ListSelectionListener l : lst) {
            l.valueChanged(new ListSelectionEvent(this, idx, idx, false));
        }
    }
    
    public void setSelected(T value, boolean v){
        for (int i = 0; i < dtm.getRowCount(); i++) {
            if (dtm.getValueAt(i, 2).equals(value)) {
                dtm.setValueAt(v, i,1);
                for (ListSelectionListener l : lst) {
                    l.valueChanged(new ListSelectionEvent(this, i, i, false));
                }
            }
        }
    }
    
    public void removeValue(T value) {
        for (int i = 0; i < dtm.getRowCount(); i++) {
            if (dtm.getValueAt(i, 2).equals(value)) {
                dtm.removeRow(i);
                for (ListSelectionListener l : lst) {
                    l.valueChanged(new ListSelectionEvent(this, i, i, false));
                }
            }

        }

    }

    public T[] getSelectedOptions() {

        ArrayList<T> al = new ArrayList<>();
        for (int i = 0; i < dtm.getRowCount(); i++) {
            if ((boolean) dtm.getValueAt(i, 1)) {
                al.add((T) dtm.getValueAt(i, 2));
            }
        }

        return al.toArray((T[]) Array.newInstance(dtm.getValueAt(0, 2).getClass(), al.size()));
    }
}
