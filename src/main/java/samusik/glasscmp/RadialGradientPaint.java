package samusik.glasscmp;

/**
 *
 * @author VeRaptor
 */

import java.awt.*;
import java.awt.geom.*;

import java.awt.image.*;
 
public class RadialGradientPaint
        
        // takes radius-vector and stretches the circle towards X axis at the given ratio
    implements Paint {
  protected Point2D p1;
  
  protected double radius;
  protected Color c1, c2;
  protected double stretch_ratio;
  
  public RadialGradientPaint(Point2D center, double radius, double stretch_ratio, java.awt.Color color1, java.awt.Color color2) {
    if (radius <= 0)
    throw new IllegalArgumentException("Radius must be greater than 0.");
    p1 = center;
    this.radius = radius;
    this.stretch_ratio=stretch_ratio;
    c1 = color1;
    c2 = color2;
    
  }
  
    @Override
  public PaintContext createContext(ColorModel cm,
      Rectangle deviceBounds, Rectangle2D userBounds,
      AffineTransform xform, RenderingHints hints) {
    Point2D transformedPoint1 = xform.transform(p1, null);
        
        
    return new RoundGradientContext(transformedPoint1,
        radius,this.stretch_ratio,c1, c2);
  }
  
    @Override
  public int getTransparency() {
    int a1 = c1.getAlpha();
    int a2 = c2.getAlpha();
    return (((a1 & a2) == 0xff) ? OPAQUE : TRANSLUCENT);
  }
}


class RoundGradientContext
    implements PaintContext {
  protected Point2D p1;
  
  protected double radius;
  protected Color c1, c2;
  protected double stretch_ratio;
  public RoundGradientContext(Point2D center, double radius, double stretch_ratio, java.awt.Color color1, java.awt.Color color2) {
    
 
    p1 = center;
    this.radius = radius;
    this.stretch_ratio = stretch_ratio;
    c1 = color1;
    c2 = color2;
  }
  
    @Override
  public void dispose() {}
  
    @Override
  public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }
  
    @Override
  public Raster getRaster(int x, int y, int w, int h) {
    WritableRaster raster =
        getColorModel().createCompatibleWritableRaster(w, h);
    
        
    int[] data = new int[w * h * 4];
    for (int j = 0; j < h; j++) {
      for (int i = 0; i < w; i++) {
        
         double d1 = (p1.getX()-(x+i))/this.stretch_ratio;
         double d2 = p1.getY()-(y+j);
        
        double ratio = Math.sqrt(d1*d1+d2*d2) / radius;
        if (ratio > 1.0) ratio = 1.0;
      
        int base = (j * w + i) * 4;
        data[base + 0] = (int)(c1.getRed() + ratio *
            (c2.getRed() - c1.getRed()));
        data[base + 1] = (int)(c1.getGreen() + ratio *
            (c2.getGreen() - c1.getGreen()));
        data[base + 2] = (int)(c1.getBlue() + ratio *
            (c2.getBlue() - c1.getBlue()));
        data[base + 3] = (int)(c1.getAlpha() + ratio *
            (c2.getAlpha() - c1.getAlpha()));
      }
    }
    raster.setPixels(0, 0, w, h, data);
    
    return raster;
  }
}

