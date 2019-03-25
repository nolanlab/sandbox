/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samusik.hideabletree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JTree;


/**
 *
 * @author Kola
 */
public class HideableTreeCellRenderer extends javax.swing.tree.DefaultTreeCellRenderer {

    private boolean hidden = false;
    private Color mark = new Color(255,255,255,0);
  public Component getTreeCellRendererComponent(JTree   tree,
                                                Object  value,
                                                boolean sel,
                                                boolean expanded,
                                                boolean leaf,
                                                int     row,
                                                boolean hasFocus)
  {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
if (value instanceof HideableMutableTreeNode) {
    
    hidden = !((HideableMutableTreeNode)value).isVisible();
    Object o = ((HideableMutableTreeNode)value).getUserObject();
    //if(o instanceof ColorCoded){mark = ((ColorCoded)o).getColorCode();}
    
}
   
    return this;
  }

    @Override
    protected void paintComponent(Graphics arg0) {
        
        super.paintComponent(arg0);
                
        if(hidden){
        Graphics2D g2 = (Graphics2D)arg0;
        g2.setPaint(new Color(127,127,127,127));
        g2.fillRoundRect(0, 2, getWidth(), getHeight()-2,3,3);
        }
    }
  
}
    

