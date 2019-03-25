/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samusik.objecttable;

/**
 *
 * @author Nikolay
 */
import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class DraggableTable extends JTable {

   
    public DraggableTable() {
        super();
        setTransferHandler(new TableModelReorderHandler());
        setDragEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.setDropMode(DropMode.ON_OR_INSERT_ROWS);
    }

    public DraggableTable(DefaultTableModel m) {
        this();
        setModel(m);
    }

    
}

