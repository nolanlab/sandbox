
package samusik.glasscmp;

import java.beans.*;
import java.io.Serializable;
import java.awt.*;
import java.awt.geom.*;
/**
 * @author VeRaptor
 */
public class GlassBorder extends javax.swing.border.AbstractBorder implements Serializable {
    
    private int RoSize;
    private GeneralPath border;
     private float borderWidth = 1.0f;
     int width; int height;
    public GlassBorder() {
        RoSize = 5;
    }
   
    public Shape getShape(){
     rebuildBorder();
     return border;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) 
    {
        this.height= height;
        this.width = width;
        rebuildBorder();
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(new Color(150,150,150, 100));
        g2.translate(x, y);
        g2.draw(border);
        g2.translate(-x, -y);
    }
     private void rebuildBorder()
    {

    int w = width-1;
    int h = height -1;
    int inset = (int)Math.round(borderWidth/2)+1;
    border = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 8);

    border.moveTo(inset,RoSize+inset);

    border.quadTo(inset,inset,RoSize+inset,inset);

    border.lineTo(w-(RoSize+inset),inset);
    border.quadTo(w-inset,inset,w-inset,(RoSize+inset));

    border.lineTo(w-inset,(h-inset)-RoSize);
    border.quadTo((w-inset)+1,(h-inset)+1,(w-inset)-RoSize,h-inset);

    border.lineTo(RoSize+inset,h-inset);
    border.quadTo(inset,h-inset,inset,(h-inset)-RoSize);

    border.closePath();
    //this.getGraphics().setClip(border);
    }
    
    
}
