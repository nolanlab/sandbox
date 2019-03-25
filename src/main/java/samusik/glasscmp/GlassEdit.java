
package samusik.glasscmp;

import java.beans.*;
import java.io.Serializable;
import java.awt.*;
import java.awt.geom.*;
/**
 * @author VeRaptor
 */
public class GlassEdit extends javax.swing.JTextField implements Serializable {
    
    private GeneralPath border;
    private float inset = 1.5f;
    private float borderWidth = 2.0f;
    private boolean rollover = false;
    private int RoSize = 4;
    
    private void rebuildBorder()
    {
    int w = this.getWidth()-1;
    int h = this.getHeight()-1;
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
    }
    
    
     public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x,y,width,height);
        this.rebuildBorder();
    }
    
     public void setBounds(Rectangle r)
    {
        super.setBounds(r);
        this.rebuildBorder();
    }
     
     public void setSize(int width, int height)
     {
         super.setSize(width, height);
         this.rebuildBorder();
         
     }
    
    public GlassEdit() {
    super();    
    
    this.setBorder(javax.swing.BorderFactory.createEmptyBorder((int)borderWidth + 2,(int)borderWidth+2,(int)borderWidth+2,(int)borderWidth+2));
    this.setBackground(new Color(0,0,0,0));
    this.setOpaque(false);
    }
    
    public void paintComponent(Graphics g)
    {
     Graphics2D g2 = (Graphics2D)g;
        
    RenderingHints hints = new RenderingHints (null);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.addRenderingHints(hints);  
      g2.setPaint(new Color(255,255,255));
    g2.fill(border);  
        
        super.paintComponent(g);
        
    
        
        
        if (this.rollover){
    g2.setPaint(new GradientPaint(0f,0f,new Color(23,92,157),(float)this.getWidth(),(float)this.getHeight(), new Color(146,197,244)));
    }else{
    g2.setPaint(new GradientPaint(0f,0f,new Color(146,197,244),(float)this.getWidth(),(float)this.getHeight(), new Color(23,92,157)));
    }    
    g2.setStroke(new BasicStroke(this.borderWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
    g2.draw(border);
    
    
    }
   
    
}
