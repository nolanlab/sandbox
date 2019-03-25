/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package samusik.objecttable;

import java.awt.Cursor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

/**
 *
 * @author Nikolay
 */
public class TableTransferHandler extends TransferHandler {

    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
        super.exportToClipboard(comp, clip, action);
        if (action == COPY && JTable.class.isAssignableFrom(comp.getClass())) {
            try {
                JTable tab = (JTable) comp;
                String s2 = "";

                ArrayList<Integer> columns = new ArrayList<>();

                boolean colSel = tab.getColumnSelectionAllowed();
                if (colSel) {
                    for (int i : tab.getSelectedColumns()) {
                        if (true || Number.class.isAssignableFrom(tab.getColumnClass(i)) || String.class.equals(tab.getColumnClass(i))) {
                            columns.add(i);
                            s2 += tab.getColumnName(i) + "\t";
                        }
                    }
                } else {
                    for (int i = 0; i < tab.getColumnCount(); i++) {
                        if (true || Number.class.isAssignableFrom(tab.getColumnClass(i)) || String.class.equals(tab.getColumnClass(i))) {
                            columns.add(i);
                            s2 += tab.getColumnName(i) + "\t";
                        }
                    }
                }
                if (s2.length() > 0) {
                    s2 = s2.substring(0, s2.length() - 1);
                }
                s2 += "\n";
                ListSelectionModel lsm = tab.getSelectionModel();

                if (lsm.getMinSelectionIndex() == -1) {
                    return;
                }
                StringBuilder strBuf = new StringBuilder(s2);
                try {
                    tab.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    for (int x = lsm.getMinSelectionIndex(); x <= Math.min(lsm.getMaxSelectionIndex(), tab.getRowCount() - 1); x++) {
                        if (lsm.isSelectedIndex(x)) {

                            for (int i : columns) {
                                strBuf.append(tab.getValueAt(x, i)).append("\t");
                            }
                            strBuf.replace(strBuf.length() - 1, strBuf.length(), "\n");
                        }
                    }
                } finally {
                    tab.setCursor(Cursor.getDefaultCursor());
                }
                final String s3 = strBuf.toString();
                Transferable tr2 = new Transferable() {
                    private String str = s3;

                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[]{DataFlavor.stringFlavor};
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor.equals(DataFlavor.stringFlavor);
                    }

                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        if (!flavor.equals(DataFlavor.stringFlavor)) {
                            throw new UnsupportedFlavorException(flavor);
                        }
                        return str;
                    }
                };
                clip.setContents(tr2, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
