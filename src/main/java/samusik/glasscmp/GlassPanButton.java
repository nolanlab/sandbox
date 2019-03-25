
package samusik.glasscmp;

import java.beans.*;
import java.io.Serializable;
import java.awt.*;
import java.awt.geom.*;
/**
 * @author VeRaptor
 */
public class GlassPanButton extends javax.swing.JButton implements Serializable {
    
        
    private javax.swing.ImageIcon tmpIco;
    
    private int iconCaptionDelimiter = 10;
    private GeneralPath border;
    private GeneralPath glare;
    private float borderWidth = 3.0f;
    private int inset;
    private boolean rollover = false;
    
    private boolean mousedown = false;
     
    private int RoSize;
    
    private void rebuildBorder()
    {
    inset = (int)Math.round(borderWidth/2)+1;
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
    
           
    glare = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 8);
        
    glare.moveTo(inset,RoSize+inset);
    
    glare.quadTo(inset,inset,RoSize+inset,inset);

    glare.lineTo(w-(RoSize+inset),inset);
    glare.quadTo(w-inset,inset,w-inset,(RoSize+inset));
    
    glare.lineTo(w-inset,h/5);
    glare.curveTo(inset+w/4,h/4,inset+w/4,h/4,inset,h/2);
    
    glare.closePath();
    
    
    }
    
 

    
    public GlassPanButton() {
       this.RoSize= 2;
       
         this.setRolloverEnabled(true);
         this.setBorderPainted(false);
         this.setContentAreaFilled(false);
         this.setOpaque(false);
       
       this.rebuildBorder();
       
       this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                GlassPanButton.this.rollover = true;
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
             GlassPanButton.this.rollover = false;
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                GlassPanButton.this.mousedown = true;
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                GlassPanButton.this.mousedown = false;
            }
        });
       
       
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
    
     
     
    public void paintComponent(Graphics g)
    {
    
    
    Graphics2D g2 = (Graphics2D)g;
    
    
    RenderingHints hints = new RenderingHints (null);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.addRenderingHints(hints);    
        
    Rectangle2D strBounds = g.getFontMetrics().getStringBounds(this.getText(),g);
    
    
    // set fill colour gradient
    
    
    
    if (this.mousedown){
    //g2.setPaint(new GradientPaint(new Point2D.Float(0,0),new Color(5,56,113),new Point2D.Float((float)this.getWidth(),0), new Color(25,100,177)));
    g2.setPaint(new Color (0,0,0,20));
    g2.fill(border);  
    }else{
    //g2.setPaint(new GradientPaint(new Point2D.Float(0,0),new Color(25,76,133),new Point2D.Float((float)this.getWidth(),0), new Color(45,120,197)));      
    }
   
    
    //g2.fill(new Rectangle(0,0,this.getWidth(),this.getHeight()));
    
       
    
    // set border colour gradient
    
    float w = (float)this.getWidth();
    float h = (float)this.getHeight();
    
    float arg1 = (float)(Math.pow(w,2)+Math.pow(h,2));
    
    float x1 = (float)(2*Math.pow(h,2)*w)/arg1;
    float y1 = (float)(2*Math.pow(w,2)*h)/arg1;
    
    g2.setStroke(new BasicStroke(this.borderWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
    
    if (this.mousedown){
      
    g2.setPaint(new GradientPaint(0f,0f,new Color(3,72,137),x1,y1, new Color(146,197,244)));
    
    g2.draw(border);
    }else{
    if (this.rollover){
    g2.setPaint(new GradientPaint(0f,0f,new Color(146,197,244),x1,y1, new Color(3,72,137)));
    g2.draw(border);
    }}  
    
     
    
    
    
    tmpIco = (javax.swing.ImageIcon)this.getIcon();
            
    if (tmpIco != null) tmpIco.setImage(TransparencyMaker.makeColorTransparent(tmpIco.getImage(), new Color(255,0,0,0)));
     
    if (tmpIco!=null) tmpIco.paintIcon(this, g, (int)Math.round((this.getWidth()-(strBounds.getWidth()+ this.getIconTextGap()*2 + ((tmpIco == null)?0:tmpIco.getIconWidth())))/2) ,(int)(this.getHeight()/2 - tmpIco.getIconHeight()/2));
    
    g.drawString(this.getText(),(int)((this.getWidth()-(strBounds.getWidth()- ((tmpIco == null)?0:tmpIco.getIconWidth())))/2)+1,(int)(this.getHeight()/2 + strBounds.getHeight()/2)-1);
    g.setColor(new Color(255,255,255,255));
    g.drawString(this.getText(),(int)Math.round((this.getWidth()-(strBounds.getWidth()-((tmpIco == null)?0:tmpIco.getIconWidth())))/2),(int)(this.getHeight()/2 + strBounds.getHeight()/2)-2);
    
    //Set middle highlight gradient
    if (this.rollover|| this.mousedown){
    g2.setPaint(new RadialGradientPaint(new Point2D.Float(this.getWidth()*0.5f,this.getHeight()*0.5f),this.getHeight()*0.5f,((this.getWidth()*2)/this.getHeight()),new Color(0,245,245,40), new Color(0,245,245,0)));
    g2.fill(border);
    }
    //Set glare gradient
    if (this.rollover || this.mousedown){
    g2.setPaint(new RadialGradientPaint(new Point2D.Float(0f,0f),this.getHeight()*0.5f,((this.getWidth()*2)/this.getHeight()),new Color(245,245,245,130), new Color(245,245,245,20)));
    g2.fill(glare);
    }
      
    
    
    }
    
   
    

    
}
