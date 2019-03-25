
package samusik.glasscmp;
import java.awt.*;
import java.awt.geom.*;
import java.beans.*;
import java.io.Serializable;

/**
 * @author VeRaptor
 */
public class GlassPanel extends javax.swing.JPanel implements Serializable {
    
    private GeneralPath border;
    public static final Color BG_COLOR = new Color(238,237,235);
    private float borderWidth = 1.0f;
    private Color col1  = new Color(150,150,150, 50);
    private Color col2 = new Color(150,150,150, 20);
    private BasicStroke stroke1 = new BasicStroke(1);
    private BasicStroke stroke2 = new BasicStroke(this.borderWidth,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
    private GradientPaint gp1 = new GradientPaint(0,0,new Color(255,255,255),0,30,BG_COLOR);
    private Color col3 = new Color(210,210,210);
    private int RoSize;

    @Override
    public Color getBackground() {
        return BG_COLOR;
    }

    @Override
    public void setBackground(Color bg) {
        return;
    }

    public GlassPanel() {
        this.RoSize= 10;
        this.border = rebuildBorder(getWidth()-1, getHeight()-1);
        super.setOpaque(false);
    }

    public Shape getShape(int w, int h){
        return (Shape)rebuildBorder(w, h);
    }

    @Override
    public void setOpaque(boolean isOpaque) {
        super.setOpaque(true);
    }
    
    private GeneralPath rebuildBorder(int w, int h){
    int inset = Math.round(borderWidth/2)+1;
    GeneralPath newBorder = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 8);
    
    newBorder.moveTo(inset,RoSize+inset);
    
    newBorder.quadTo(inset,inset,RoSize+inset,inset);

    newBorder.lineTo(w-(RoSize+inset),inset);
    newBorder.quadTo(w-inset,inset,w-inset,(RoSize+inset));
    
    newBorder.lineTo(w-inset,(h-inset)-RoSize);
    newBorder.quadTo((w-inset)+1,(h-inset)+1,(w-inset)-RoSize,h-inset);
    
    newBorder.lineTo(RoSize+inset,h-inset);
    newBorder.quadTo(inset,h-inset,inset,(h-inset)-RoSize);
    newBorder.closePath();
    //this.getGraphics().setClip(border);
    return newBorder;
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
    border = rebuildBorder(getWidth()-1, getHeight()-1);    
    Graphics2D g2 = (Graphics2D)g;
    
    g2.setPaint(Color.WHITE);
    
    g2.fill(border);
    
    RenderingHints hints = new RenderingHints (null);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.addRenderingHints(hints);     
        g2.setStroke(stroke2);
    g2.setPaint(col1);
    g2.translate(1,1);
    g2.draw(border);
    g2.setPaint(col2);
    g2.translate(1,1);
    g2.draw(border);
    g2.translate(-2,-2);
    g2.setStroke(stroke1);
    g2.setPaint(gp1);
    g2.fill(border);
    g2.setPaint(col3);
    g2.draw(border);
    }
}
