
package samusik.glasscmp;

/**
 *
 * @author VeRaptor
 */
import java.awt.geom.Point2D.Float;
import java.beans.*;
import java.io.Serializable;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
/**
 * @author VeRaptor
 */
public class GlassButton extends javax.swing.JButton implements Serializable {
    private Color col1 = new Color(93, 161, 224);
    private Color col10 = new Color(149, 207, 245);
    private Color col11 = new Color(73, 141, 204);
    private Color col12 = new Color(0, 215, 235, 150);
    private Color col13 = new Color(0, 215, 235, 0);
    private Color col14 = new Color(20, 245, 255, 150);
    private Color col15 = new Color(30, 245, 255, 0);
    private Color col16 = new Color(0, 235, 255, 150);
    private Color col17 = new Color(0, 235, 255, 0);
    private Color col18 = new Color(255, 255, 255, 190);
    private Color col19 = new Color(255, 255, 255, 70);
    private Color col2 = new Color(139,197,235);
    private Color col20 = new Color(255, 255, 255, 170);
    private Color col21 = new Color(255, 255, 255, 50);
    private Color col3 = new Color(23,92,157);
    private Color col4 = new Color(146,197,244);
    private Color col5 = new Color(146,197,244);
    private Color col6 = new Color(23,92,157);
    private Color col7 = new Color(129,187,235);
    private Color col8 =  new Color(53,121,184);
    private Color col9 = new Color(169, 227, 235);
    
    private GeneralPath border;
    private GeneralPath glare;
    private float borderWidth = 3.0f;
    private Float p1;
    private Float p2;
    private Float p3;
    private Float p4;
    private boolean rollover = false;
    private boolean mousedown = false;
    private int RoSize;
    private GradientPaint gp1, gp2, gp3;
    private RadialGradientPaint rgp1, rgp2, rgp3, rgp4, rgp5, rgp6, rgp7,rgp8,rgp9;

    private void rebuildBorder()
    {
        if(this.getHeight() == 0 || this.getWidth() == 0) return;
    int w = this.getWidth();
    int h = this.getHeight();

    float W = this.getWidth();
    float H = this.getHeight();

    gp1 = new GradientPaint(((float)this.getWidth())/10,0f,col1,((float)this.getWidth())*0.9f,(float)this.getHeight(), col2);
    gp2 = new GradientPaint(0f,0f,col3,W,H, col4);
    gp3 = new GradientPaint(0f,0f,col5,W,H, col6);
    p1 = new Point2D.Float(this.getWidth(), (float) this.getHeight());
    rgp1 = new RadialGradientPaint(p1,H*0.66f,(W/H),col7, col8);
    rgp2 = new RadialGradientPaint(p1,H*0.66f,(W/H),col9, col1);
    rgp3 = new RadialGradientPaint(p1,H*0.66f,(W/H), col10,col11);
        p2 = new Point2D.Float(W * 0.5F, H * 0.5F);
    rgp4 = new RadialGradientPaint(p2,H*0.5f,2,Color.BLACK, Color.BLACK);
        p3 = new Point2D.Float(W * 0.5F, H);
    rgp5 = new RadialGradientPaint(p3,H*0.7f,(W/H)*1.2f, col12,col13);
    rgp6 = new RadialGradientPaint(p3,H*0.7f,(W/H)*2.0f, col14,col15);
    rgp7 = new RadialGradientPaint(p3,H*0.7f,(W/H)*1.2f, col16,col17);
        p4 = new Point2D.Float(0.0F, 0.0F);
    rgp8 = new RadialGradientPaint(p4,H*0.7f,(W/H), col18,col19);
    rgp9 = new RadialGradientPaint(p4,H*0.7f,(W/H), col20,col21);

    int inset = (int)Math.round(borderWidth/2);
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
    
    glare.lineTo(w-inset,h/2.6f);
    glare.curveTo(inset+w/3,h/3,inset,(h-inset)-RoSize,inset,(h-inset)-RoSize);
    
    glare.closePath();        
    }

    public GlassButton() {
       this.RoSize= 5;
       
         this.setRolloverEnabled(true);
         this.setBorderPainted(false);
         this.setContentAreaFilled(false);
         this.setOpaque(false);
       
       this.rebuildBorder();
       
       this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                GlassButton.this.rollover = true;
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
             GlassButton.this.rollover = false;
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                GlassButton.this.mousedown = true;
                
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                GlassButton.this.mousedown = false;
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

     BufferedImage [] bi = new BufferedImage[3]; //normal, rollover, mousedown




     
    public void paintComponent(Graphics g){

    //BufferedImage img = new BufferedImage(this.getWidth(), this.getWidth(), BufferedImage.TYPE_INT_ARGB_PRE);

    Graphics2D g2 = (Graphics2D)g;
    //g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
    Rectangle2D strBounds = g.getFontMetrics().getStringBounds(this.getText(),g);
    
    g2.translate(-1, -1);
    // set fill colour gradient
    g2.setPaint(gp1);
    if (this.mousedown){
    g2.setPaint(rgp1);
    }else{
    if (this.rollover){
    g2.setPaint(rgp2);
    }else{
    g2.setPaint(rgp3);
    }}
   g2.fill(border);
    
    //set middle highlight gradient
    g2.setPaint(rgp4);
    if (this.mousedown){
     g2.setPaint(rgp5);
    
    }else{
    if (this.rollover){
    g2.setPaint(rgp6);
    }else{
    g2.setPaint(rgp7);
    }}
    g2.fill(border);

    // set border colour gradient
    if (this.mousedown){
    g2.setPaint(gp2);
    }else{
    g2.setPaint(gp3);
    }    
    g2.setStroke(new BasicStroke(this.borderWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
    g2.draw(border); 
    Icon icon = this.getIcon();

    if (this.getIcon() != null) icon.paintIcon(this, g, (int)Math.round((this.getWidth()-(strBounds.getWidth()+ this.getIconTextGap()*2 + (icon.getIconWidth())))/2) ,(int)(this.getHeight()/2 - icon.getIconHeight()/2));
    
    g.drawString(this.getText(),(int)((this.getWidth()-(strBounds.getWidth()- ((icon == null)?0:icon.getIconWidth())))/2.0)+1,(int)(this.getHeight()/2.0 + strBounds.getHeight()/2.0)-1);
    g.setColor(new Color(255,255,255,255));
    g.drawString(this.getText(),(int)Math.round((this.getWidth()-(strBounds.getWidth()-((icon == null)?0:icon.getIconWidth())))/2.0),(int)(this.getHeight()/2.0 + strBounds.getHeight()/2.0)-2);
    
    //Set glare gradient
    if (this.rollover){
    g2.setPaint(rgp8);
    }else{
    g2.setPaint(rgp9);
    }
    g2.fill(glare);
    
    }
    
   
    

    
}