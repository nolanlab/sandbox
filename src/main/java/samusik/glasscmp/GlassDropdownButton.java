

package samusik.glasscmp;

/**
 *
 * @author VeRaptor
 */
import java.beans.*;
import java.io.Serializable;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
/**
 * @author VeRaptor
 */
public class GlassDropdownButton extends javax.swing.JButton implements Serializable {
    
        
    private javax.swing.ImageIcon tmpIco;
    private boolean popupEnabled = false;
    private Color dropdownTriangleColor = new Color(255,255,255);
    private GeneralPath border;
    private GeneralPath glare;
    private float borderWidth = 3.0f;
    javax.swing.JPopupMenu popup;
    private boolean rollover = false;
    private int inset;
    public JPopupMenu getPopup() {
        return popup;
    }

    public void setDropdownTriangleColor(Color dropdownTriangleColor) {
        this.dropdownTriangleColor = dropdownTriangleColor;
    }

    public Color getDropdownTriangleColor() {
        return dropdownTriangleColor;
    }

    
    
    @Override
    protected void fireActionPerformed(ActionEvent arg0) {
        if (!popupEnabled){
            super.fireActionPerformed(arg0);
        }else{popup.show(this, 0, getHeight()-inset);}
    }
    
    
    
    public void setPopup(JPopupMenu popup) {
        this.popup = popup;
    }
    
    
    
    private boolean mousedown = false;
     
    private int RoSize;
    private GeneralPath dropdownButtonArea;
    private float dropdownButtonWidth = 20.0f;
    private void rebuildBorder()
    {
    
        int w = this.getWidth()-1;
        int h = this.getHeight()-1;
        inset = (int)Math.round(borderWidth/2)+1;
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
    
    dropdownButtonArea = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 8);
    
    dropdownButtonArea.moveTo(w - Math.max(dropdownButtonWidth, RoSize + inset),inset);
        
    dropdownButtonArea.lineTo(w-(RoSize+inset),inset);
    dropdownButtonArea.quadTo(w-inset,inset,w-inset,(RoSize+inset));
    
    dropdownButtonArea.lineTo(w-inset,(h-inset)-RoSize);
    dropdownButtonArea.quadTo((w-inset)+1,(h-inset)+1,(w-inset)-RoSize,h-inset);
    
    dropdownButtonArea.lineTo(w - Math.max(dropdownButtonWidth, RoSize + inset),h-inset);
    dropdownButtonArea.closePath();
           
   
    
    }
  
    public GlassDropdownButton() {
       this.RoSize= 5;
       
         this.setRolloverEnabled(true);
         this.setBorderPainted(false);
         this.setContentAreaFilled(false);
         this.setOpaque(false);
       
       this.rebuildBorder();
              
       
       this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                GlassDropdownButton.this.rollover = true;
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
             GlassDropdownButton.this.rollover = false;
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                GlassDropdownButton.this.mousedown = true;
                if (GlassDropdownButton.this.dropdownButtonArea.contains(evt.getPoint())) {
                GlassDropdownButton.this.popupEnabled = true;
                }else{
                    GlassDropdownButton.this.popupEnabled = false;
                }
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                GlassDropdownButton.this.mousedown = false;
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
    //g2.setPaint(new GradientPaint(((float)this.getWidth())/10,0f,new Color(93,161,224),((float)this.getWidth())*0.9f,(float)this.getHeight(), new Color(139,197,235)));
    if (this.mousedown){
    g2.setPaint(new RadialGradientPaint(new Point2D.Float(this.getWidth(),(float)this.getHeight()),this.getHeight()*0.66f,(this.getWidth()/this.getHeight()),new Color(129,187,235), new Color(53,121,184)));   
    }else{
    if (this.rollover){
    g2.setPaint(new RadialGradientPaint(new Point2D.Float(this.getWidth(),(float)this.getHeight()),this.getHeight()*0.66f,(this.getWidth()/this.getHeight()),new Color(169,227,235), new Color(93,161,224)));
    }else{
    g2.setPaint(new RadialGradientPaint(new Point2D.Float(this.getWidth(),(float)this.getHeight()),this.getHeight()*0.66f,(this.getWidth()/this.getHeight()),new Color(149,207,245), new Color(73,141,204)));
    }}
    
    g2.fill(border);
    
    //set middle highlight gradient
    //g2.setPaint(new RadialGradientPaint(new Point2D.Float(this.getWidth()*0.5f,(float)this.getHeight()*0.5f),this.getHeight()*0.5f,2,new Color(0,0,0), new Color(0,0,0)));
    if (this.mousedown){
     g2.setPaint(new RadialGradientPaint(new Point2D.Float(this.getWidth()*0.5f,(float)this.getHeight()),this.getHeight()*0.7f,(this.getWidth()/this.getHeight())*1.2f,new Color(0,215,235,150), new Color(0,215,235,0)));
    
    }else{
    if (this.rollover){
    g2.setPaint(new RadialGradientPaint(new Point2D.Float(this.getWidth()*0.5f,(float)this.getHeight()),this.getHeight()*0.7f,(this.getWidth()/this.getHeight())*2.0f,new Color(20,245,255,150), new Color(30,245,255,0)));
    
    }else{
    g2.setPaint(new RadialGradientPaint(new Point2D.Float(this.getWidth()*0.5f,(float)this.getHeight()),this.getHeight()*0.7f,(this.getWidth()/this.getHeight())*1.2f,new Color(0,235,255,150), new Color(0,235,255,0)));
    }}
    g2.fill(border);
    
    //dropdown area fill
    
    g2.setPaint(new GradientPaint(0f,0f,new Color(0,0,255,200),0f,(float)this.getHeight()/2.0f, new Color(0,0,255,200),true));
    g2.setStroke(new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_BEVEL));
    
    g2.draw(dropdownButtonArea);
    
    g2.setPaint(new GradientPaint(0f,0f,new Color(255,255,255,30),0f,(float)this.getHeight()/2.0f, new Color(255,255,255,120),true));

    
    //g2.setPaint(new RadialGradientPaint(new Point2D.Float(this.getWidth() - (dropdownButtonWidth*0.5f),(float)this.getHeight()/2.0f),this.getHeight()*0.5f,(this.getWidth()/this.getHeight())*1.2f,new Color(255,255,255,150), new Color(255,255,255,0)));

    g2.fill(dropdownButtonArea);
    
    // set border colour gradient
    if (this.mousedown){
    g2.setPaint(new GradientPaint(0f,0f,new Color(23,92,157),(float)this.getWidth(),(float)this.getHeight(), new Color(146,197,244)));
    }else{
    g2.setPaint(new GradientPaint(0f,0f,new Color(146,197,244),(float)this.getWidth(),(float)this.getHeight(), new Color(23,92,157)));
    }    
    g2.setStroke(new BasicStroke(this.borderWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_BEVEL));
    g2.draw(border); 

    tmpIco = (javax.swing.ImageIcon)this.getIcon();
            
    if (tmpIco != null) tmpIco.setImage(TransparencyMaker.makeColorTransparent(tmpIco.getImage(), new Color(255,0,0,0)));
     
    if (tmpIco!=null) tmpIco.paintIcon(this, g, (int)Math.round((this.getWidth()-(strBounds.getWidth()+ this.getIconTextGap()*2 + ((tmpIco == null)?0:tmpIco.getIconWidth())))/2) ,(int)(this.getHeight()/2 - tmpIco.getIconHeight()/2));
    
    g.drawString(this.getText(),(int)(((this.getWidth()-dropdownButtonWidth)-(strBounds.getWidth()- ((tmpIco == null)?0:tmpIco.getIconWidth())))/2)+1,(int)(this.getHeight()/2 + strBounds.getHeight()/2)-1);
    g.setColor(new Color(255,255,255,255));
    g.drawString(this.getText(),(int)Math.round(((this.getWidth()-dropdownButtonWidth)-(strBounds.getWidth()-((tmpIco == null)?0:tmpIco.getIconWidth())))/2),(int)(this.getHeight()/2 + strBounds.getHeight()/2)-2);
    
    //paint dropdown triangle
    g2.setPaint(dropdownTriangleColor);
    g2.fillPolygon(new int[]{this.getWidth()-(int)(this.dropdownButtonWidth/2 + 3 + inset),this.getWidth()-(int)(this.dropdownButtonWidth/2 - 3 + inset),this.getWidth()-(int)(this.dropdownButtonWidth/2 + inset)}, new int[]{this.getHeight()/2,this.getHeight()/2,this.getHeight()/2 + 3}, 3);
    
    
    //Set glare gradient
    if (this.rollover){
    g2.setPaint(new RadialGradientPaint(new Point2D.Float(0f,0f),this.getHeight()*0.7f,(this.getWidth()/this.getHeight()),new Color(255,255,255,190), new Color(255,255,255,70)));
     
    }else{
    g2.setPaint(new RadialGradientPaint(new Point2D.Float(0f,0f),this.getHeight()*0.7f,(this.getWidth()/this.getHeight()),new Color(255,255,255,170), new Color(255,255,255,50)));
    
    }
    
    g2.fill(glare);
    
    }
    
   
    

    
}