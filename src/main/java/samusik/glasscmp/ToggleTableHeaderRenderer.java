/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package samusik.glasscmp;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JTable;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Nikolay
 */
public class ToggleTableHeaderRenderer extends DefaultTableCellRenderer {

    private Color shadowCol = new Color(100, 100, 100, 70);
    private Color shadowTranslucentCol = new Color(100, 100, 100, 0);
    private Color highlightCol = new Color(255, 255, 255, 50);
    private GradientPaint gp1;
    
    private List<Entry<String, Color>> availableStates;
    private int[] statesOfColumns;
    private boolean[] mutablilityMask;
    //, gp4, gp5;
    
    float W = this.getWidth();
    float H = this.getHeight();
    private static int rolloverColNum = -1;
    private static boolean globalRollover = false;
    private int column;

    public int[] getStatesOfColumns() {
        return statesOfColumns;
    }

    public List<Entry<String, Color>> getAvailableStates() {
        return availableStates;
    }
    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (column >= table.getColumnCount()) {
            return this;
        }
        try {
            this.setToolTipText(table.getColumnName(column));
        } catch (Exception e) {
            return this;
        }
        this.column = column;
        return this;
    }

    public ToggleTableHeaderRenderer(final JTableHeader header, final ArrayList<Entry<String, Color>> availableStates, final int[] initStatesOfColumns, final boolean[] mutablilityMask) {
        setOpaque(true);
        setName("Table.ToggleCellRenderer");

        this.availableStates = (ArrayList<Entry<String, Color>>) availableStates.clone();
        statesOfColumns = Arrays.copyOf(initStatesOfColumns, initStatesOfColumns.length);
        this.mutablilityMask = mutablilityMask;

        assert (mutablilityMask.length == initStatesOfColumns.length);

        for (int i = 0; i < mutablilityMask.length; i++) {
            assert (initStatesOfColumns[i] < availableStates.size());
        }

        header.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                globalRollover = true;
                ((JTableHeader) evt.getSource()).repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                globalRollover = false;
                ((JTableHeader) evt.getSource()).repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JTable table = ((JTableHeader) e.getSource()).getTable();
                TableColumnModel columnModel = table.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int col = table.convertColumnIndexToModel(viewColumn);
                if (col <   ToggleTableHeaderRenderer.this.mutablilityMask.length) {
                    if (  ToggleTableHeaderRenderer.this.mutablilityMask[col]) {
                        if(e.getButton()==MouseEvent.BUTTON1){
                        statesOfColumns[col]++;
                        if (statesOfColumns[col] == availableStates.size()) {
                            statesOfColumns[col] = 0;
                        }
                        }
                        if(e.getButton()==MouseEvent.BUTTON3){
                        statesOfColumns[col]--;
                        if (statesOfColumns[col] == -1) {
                            statesOfColumns[col] = availableStates.size()-1;
                        }
                        }
                    }
                }
                ((JTableHeader) e.getSource()).repaint();
            }
        });

        header.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (header.getColumnModel().getColumnIndexAtX(e.getX()) != rolloverColNum) {
                    rolloverColNum = header.getColumnModel().getColumnIndexAtX(e.getX());
                    JTable table = ((JTableHeader) e.getSource()).getTable();
                    TableColumnModel columnModel = table.getColumnModel();
                    int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                    ToggleTableHeaderRenderer.this.column = table.convertColumnIndexToModel(viewColumn);
                    ((JTableHeader) e.getSource()).repaint();
                    //table2.getTableHeader().repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (this.getHeight() != W && this.getWidth() != H) {
            W = this.getWidth();
            H = this.getHeight();
            gp1 = new GradientPaint(0f, 0f, shadowCol, 0f, H / 2, shadowTranslucentCol, true);
        }

        Rectangle2D strBounds = g.getFontMetrics().getStringBounds(this.getText(), g);

        g2.setPaint(availableStates.get(statesOfColumns[column]).getValue());
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.setPaint(gp1);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (rolloverColNum == column && globalRollover) {
            g2.setPaint(highlightCol);
            g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        }

        //g.drawString(this.getText(),(int)((this.getWidth()-strBounds.getWidth())/2)+1,(int)(this.getHeight()/2 + strBounds.getHeight()/2)-1);

        g2.drawLine(0, this.getHeight() - 1, this.getWidth(), this.getHeight() - 1);

        g2.setPaint(new Color(70, 110, 155));
        g2.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight());
        g2.drawLine(0, this.getHeight() - 1, this.getWidth(), this.getHeight() - 1);

        //g2.setClip(0,0,(int)Math.min(g2.getClip().getBounds2D().getWidth(), this.getWidth()), this.getHeight());
        g2.setPaint(Color.WHITE);
        g.drawString(this.getText(), 2, (int) (this.getHeight() / 2.0 + strBounds.getHeight() / 2.0) - 2);
        g2.setPaint(Color.BLACK);
        g2.drawString(this.getText(), 2, (int) (this.getHeight() / 2.0 + strBounds.getHeight() / 2.0) - 3);
        g2.setClip(0, 0, this.getWidth(), this.getHeight());
    }
}
