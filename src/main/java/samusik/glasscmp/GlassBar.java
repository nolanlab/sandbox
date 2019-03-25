
package samusik.glasscmp;

import java.beans.*;
import java.io.Serializable;
import java.awt.*;
import java.awt.geom.*;
/**
 * @author VeRaptor
 */
public class GlassBar extends javax.swing.JPanel implements Serializable {
    java.awt.image.BufferedImage Img;
    
    private String text = "";
    
    public void setText (String t) { this.text = t;}
    public String getText () {return this.text;}
    
    
    public GlassBar() {
     this.setMinimumSize(new Dimension(10,10));
        this.setSize(10,10);
     
        this.add(javax.swing.Box.createVerticalStrut(33));
        
     Img = new java.awt.image.BufferedImage(this.getWidth(),this.getHeight(),java.awt.image.BufferedImage.TYPE_INT_ARGB);
     
     this.rebuildImg();
               
    }
    
     public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x,y,width,height);
        this.rebuildImg();
    }
    
     public void setBounds(Rectangle r)
    {
        super.setBounds(r);
        this.rebuildImg();
    }
     
     public void setSize(int width, int height)
     {
         super.setSize(width, height);
         this.rebuildImg();
         
     }
    
    private void rebuildImg()
    {
        Img = new java.awt.image.BufferedImage(Math.max(this.getWidth(),10),Math.max(this.getHeight(),10),java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = Img.createGraphics();
     
        RenderingHints hints = new RenderingHints (null);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.addRenderingHints(hints);  
    int w = this.getWidth();
    int h = this.getHeight();
    
    Color c1 = new Color(5,56,113);
    Color c2 = new Color (65,140,217);
    
    float step_R = ((float)c2.getRed() - (float)c1.getRed())/w;
    float step_G = ((float)c2.getGreen() - (float)c1.getGreen())/w;
    float step_B = ((float)c2.getBlue() - (float)c1.getBlue())/w;
            
    float curr_R = (float)c1.getRed();
    float curr_G = (float)c1.getGreen();
    float curr_B = (float)c1.getBlue();
    
    GeneralPath line = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 8);
    
    line.moveTo(w/2,0);
    
    line.curveTo(-w/4f,h*0.25f,(float)w*1.25f,h*0.75f,w/2f,(float)h);
    
    g2.translate(-w*0.85f,0);
    g2.setStroke(new BasicStroke(2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
    for (int i = 0;  i< w*2;i++)
    {  
    
    
    g2.setPaint(new Color((int)Math.round(curr_R),(int)Math.round(curr_G),(int)Math.round(curr_B)));
    
    
    g2.draw(line);
    g2.translate(1,0);
   
    if(curr_R < 255 -step_R && curr_R > 0 -step_R) curr_R += step_R;
    if(curr_G < 255 -step_R && curr_G > 0 -step_G) curr_G += step_G;
    if(curr_B < 255 -step_B && curr_B > 0 -step_B) curr_B += step_B;
    }
    
    
    g2.translate (-w*1.15f,0);    
    g2.dispose();
    }  
    
     
     
     public void paintComponent(Graphics g)
    {
    
       
         
    Rectangle2D strBounds = g.getFontMetrics().getStringBounds(this.getText(),g);
    
    Graphics2D g2 = (Graphics2D)g;
    
      RenderingHints hints = new RenderingHints (null);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.addRenderingHints(hints);  
    
    g2.drawImage(Img,0,0,null);
    
    g2.setPaint(new java.awt.GradientPaint((int)(this.getWidth()*0.1),0, new Color(230,230,210,0),(int)(this.getWidth()*0.5),0, new Color(230,230,210,255),true));
    g2.fillRect((int)(this.getWidth()*0.1), 29,(int)(this.getWidth()*0.8), 3);
    
            g2.setPaint(new Color(0,0,0,255));
    g2.drawString(this.getText(),(int)((this.getWidth()-strBounds.getWidth())/2)+1,(int)(15 + strBounds.getHeight()/2)+1);
    g2.setColor(new Color(255,255,255,255));
    g2.drawString(this.getText(),(int)((this.getWidth()-strBounds.getWidth())/2),(int)(15 + strBounds.getHeight()/2));
    
    
    
    
    }
}