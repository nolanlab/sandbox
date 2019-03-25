/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package samusik.objecttable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

/**
 *
 * @author Nikolay
 */
public class TableModelReorderHandler extends TableTransferHandler {

    public static DataFlavor DTable_Flavor = new DataFlavor(DTableData.class, "DTableData");
    private static DataFlavor[] supportedFlavors = {DTable_Flavor};

    @Override
    @SuppressWarnings({"unchecked", "unchecked"})
    public boolean importData(TransferSupport support) {
        final JTable target = (JTable) support.getComponent();
        if(target==null)return false;
        if(target.getDropLocation()==null) return false;
        
        final int dropIndex = target.getDropLocation().getRow();
        int insertionAdjustment = 0;
        try {
            final JTable dragTable = ((DTableData) support.getTransferable().getTransferData(DTable_Flavor)).parentTable;
            int[] draggedRows = ((DTableData) support.getTransferable().getTransferData(DTable_Flavor)).rowsToReorder;

            TableModel dragModel = ((DTableData) support.getTransferable().getTransferData(DTable_Flavor)).parent;
            TableModel dropModel = ((DTableData) support.getTransferable().getTransferData(DTable_Flavor)).parent;

            Arrays.sort(draggedRows);

            final int dataLength = draggedRows.length;
            if (dataLength == 0) {
                return false;
            }

            int rowsAbove2 = 0;

            for (int i : draggedRows) {
                if (i < dropIndex) {
                    rowsAbove2++;
                }
                if (dropIndex == i) {
                    return false;
                }
            }
            final int rowsAbove = rowsAbove2;

            if (ObjectTableModel.class.isAssignableFrom(dropModel.getClass())) {
                ((ObjectTableModel) dropModel).swapRowsTo(draggedRows, dropIndex);
            } else {
                swapRowsTo(dropModel, draggedRows, dropIndex);
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    target.clearSelection();
                    target.getSelectionModel().setSelectionInterval(dropIndex - rowsAbove, dropIndex + dataLength - (1 + rowsAbove));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void swapRowsTo(TableModel dropModel, int[] draggedRows, int dropIndex) {
        if (draggedRows.length == 0) {
            return;
        }
        draggedRows = Arrays.copyOf(draggedRows, draggedRows.length);
        Arrays.sort(draggedRows);

        int lastDraggedIdx = Math.max(dropIndex, draggedRows[draggedRows.length - 1]);
        int firstDraggedIdx = Math.min(dropIndex, draggedRows[0]);

        Object[][] rows1 = new Object[draggedRows.length][dropModel.getColumnCount()];
        //System.err.println(firstDraggedIdx+" "+lastDraggedIdx + " " + draggedRows.length);
        Object[][] rows2 = new Object[1 + (lastDraggedIdx - firstDraggedIdx) - draggedRows.length][dropModel.getColumnCount()];
        int cnt1 = 0;
        int cnt2 = 0;
        int offset = 0;
        for (int i = firstDraggedIdx; i < dropModel.getRowCount(); i++) {
            if (Arrays.binarySearch(draggedRows, i) >= 0) {
                for (int j = 0; j < dropModel.getColumnCount(); j++) {
                    rows1[cnt1][j] = dropModel.getValueAt(i, j);
                }
                cnt1++;
                if(i < dropIndex) offset++;
            } else {
                if (cnt2 < rows2.length) {
                    for (int j = 0; j < dropModel.getColumnCount(); j++) {
                        rows2[cnt2][j] = dropModel.getValueAt(i, j);
                    }
                    cnt2++;
                }
            }
        }
        
        cnt1 = 0;
        cnt2 = 0;

        for (int i = firstDraggedIdx; i < dropModel.getRowCount(); i++) {
            if (i >= dropIndex-offset && cnt1 < rows1.length) {
                for (int j = 0; j < dropModel.getColumnCount(); j++) {
                    dropModel.setValueAt(rows1[cnt1][j], i, j);
                }
                cnt1++;
            } else {
                if(cnt2<rows2.length){
                for (int j = 0; j < dropModel.getColumnCount(); j++) {
                    dropModel.setValueAt(rows2[cnt2][j], i, j);
                }
                cnt2++;
                }
            }
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }
    TableModel tmSrc = null;

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (JTable.class.isAssignableFrom(c.getClass())) {
            if (((JTable) c).getModel() instanceof TableModel) {
                tmSrc = (TableModel) ((JTable) c).getModel();
                int[] rows = ((JTable) c).getSelectedRows();

                return new DTableData(tmSrc, rows, (JTable) c);
            }
        }
        return new DTableData(null, new int[0], null);
    }

    @Override
    public boolean canImport(TransferSupport support) {
        try {
            if (!JTable.class.isAssignableFrom(support.getComponent().getClass()) || !support.isDrop() || !support.isDataFlavorSupported(DTable_Flavor) || !((DTableData) support.getTransferable().getTransferData(DTable_Flavor)).parentTable.equals(support.getComponent())) {
                return false;
            }
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public Icon getVisualRepresentation(Transferable t) {
        return super.getVisualRepresentation(t);
    }

    private class DTableData implements Transferable {

        public TableModel parent;
        public JTable parentTable;
        int[] rowsToReorder;

        protected DTableData(TableModel p, int[] rowsToReorder, JTable parentTable) {
            parent = p;
            this.parentTable = parentTable;
            this.rowsToReorder = rowsToReorder;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(DTable_Flavor)) {
                return DTableData.this;
            } else {
                return null;
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            return supportedFlavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return true;
        }
    }
}
