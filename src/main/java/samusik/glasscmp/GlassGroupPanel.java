
package samusik.glasscmp;

import java.awt.*;
import java.awt.geom.*;
import java.beans.*;
import java.io.Serializable;
/**
 * @author VeRaptor
 */
public class GlassGroupPanel extends javax.swing.JPanel implements Serializable {
   private String title = "";
   private float RoSize = 10.0f;
   private GeneralPath border;
   private float borderWidth = 1.0f;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
  
    public GlassGroupPanel() {
        this.RoSize= 10;
       this.rebuildBorder();
        
         this.setOpaque(false);
    }
    
      private void rebuildBorder()
    {
    
    int w = this.getWidth()-3;
    int h = this.getHeight()-3;
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
    
    public void paintComponent(Graphics g)
    {
     this.rebuildBorder();    
     Graphics2D g2 = (Graphics2D)g;
    g2.setPaint(new Color(150,150,150));
    g2.draw(border);
    g2.setPaint(new Color(0,0,0));
    g2.drawString(this.getTitle(), 10.0f, 5.0f);
    }
    
}
