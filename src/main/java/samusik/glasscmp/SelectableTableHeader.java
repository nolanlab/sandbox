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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Nikolay
 */
public class SelectableTableHeader extends DefaultTableCellRenderer {

    private Color col1 = new Color(93, 161, 224);
    private Color col12 = new Color(0, 215, 235, 200);
    private Color col13 = new Color(0, 215, 235, 0);
    private Color col14 = new Color(20, 245, 255, 200);
    private Color col15 = new Color(30, 245, 255, 0);
    private Color col18 = new Color(255, 255, 255, 120);
    private Color col19 = new Color(255, 255, 255, 30);
    private Color col2 = new Color(139, 197, 235);
    private Color col20 = new Color(255, 255, 255, 170);
    private Color col21 = new Color(255, 255, 255, 70);
    private Color col3 = new Color(23, 92, 157);
    private Color col4 = new Color(146, 197, 244);
    private Color col5 = new Color(146, 197, 244);
    private Color col6 = new Color(23, 92, 157);
    private GradientPaint gp1, gp2, gp3, gp4, gp5;
    float W = this.getWidth();
    float H = this.getHeight();
    private RadialGradientPaint rgp5, rgp6;
    private static int rolloverColNum = -1;
    private static boolean globalRollover = false;
    private boolean mousedown = false;
    int column;
    private JTableHeader header;

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

    public SelectableTableHeader(final JTableHeader header) {
        setOpaque(true);
        setName("Table.cellRenderer");

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
            public void mouseClicked(MouseEvent e) {
                JTable table = ((JTableHeader) e.getSource()).getTable();
                TableColumnModel columnModel = table.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                SelectableTableHeader.this.column = table.convertColumnIndexToModel(viewColumn);
                
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
                    SelectableTableHeader.this.column = table.convertColumnIndexToModel(viewColumn);                    
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

            gp1 = new GradientPaint(0f, 0f, col1, 0f, H, col2);
            gp2 = new GradientPaint(0f, 0f, col3, 0f, H, col4);
            gp3 = new GradientPaint(0f, 0f, col5, 0f, H, col6);

            Point2D p3 = new Point2D.Float(W * 0.5F, H);
            rgp5 = new RadialGradientPaint(p3, H * 0.7f, (W / H) * 2.0f, col12, col13);
            rgp6 = new RadialGradientPaint(p3, H * 0.7f, (W / H) * 2.0f, col14, col15);
            gp4 = new GradientPaint(0f, 0f, col18, 0f, H / 4.0f, col19);
            gp5 = new GradientPaint(0f, 0f, col20, 0f, H / 4.0f, col21);
        }

        Rectangle2D strBounds = g.getFontMetrics().getStringBounds(this.getText(), g);

        if (this.mousedown) {
            g2.setPaint(gp1);
        } else {
            if (rolloverColNum == column && globalRollover) {
                g2.setPaint(gp3);
            } else {
                g2.setPaint(gp3);
            }
        }
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (this.mousedown) {
            g2.setPaint(rgp5);
        } else {
            if (rolloverColNum == column && globalRollover) {
                g2.setPaint(rgp6);
            } else {
                g2.setPaint(rgp5);
            }
        }
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());

        //g.drawString(this.getText(),(int)((this.getWidth()-strBounds.getWidth())/2)+1,(int)(this.getHeight()/2 + strBounds.getHeight()/2)-1);

        g2.drawLine(0, this.getHeight() - 1, this.getWidth(), this.getHeight() - 1);

        if (rolloverColNum == column && globalRollover) {
            g2.setPaint(gp5);
        } else {
            g2.setPaint(gp4);
        }
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());

        g2.setPaint(new Color(70, 110, 155));
        g2.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight());
        g2.drawLine(0, this.getHeight() - 1, this.getWidth(), this.getHeight() - 1);

        g2.setClip(0, 0, (int) Math.min(g2.getClip().getBounds2D().getWidth(), this.getWidth() - 3), this.getHeight());
        g2.setPaint(gp3);
        g.drawString(this.getText(), 2, (int) (this.getHeight() / 2.0 + strBounds.getHeight() / 2.0) - 2);
        g2.setPaint(new Color(255, 255, 255));
        g2.drawString(this.getText(), 2, (int) (this.getHeight() / 2.0 + strBounds.getHeight() / 2.0) - 3);
        g2.setClip(0, 0, this.getWidth(), this.getHeight());
    }
}
