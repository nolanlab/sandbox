
package samusik.glasscmp;

/**
 *
 * @author VeRaptor
 */
import java.awt.*;

public class TransparencyMaker {
    
  public static Image makeColorTransparent
    (java.awt.Image im, final java.awt.Color color) {
    java.awt.image.ImageFilter filter = new java.awt.image.RGBImageFilter() {
      // the color we are looking for... Alpha bits are set to opaque
      public int markerRGB = color.getRGB() | 0xFF000000;

      public final int filterRGB(int x, int y, int rgb) {
        if ( ( rgb | 0xFF000000 ) == markerRGB ) {
          // Mark the alpha bits as zero - transparent
          return 0x00FFFFFF & rgb;
          }
        else {
          // nothing to do
          return rgb;
          }
        }
      }; 
     
    java.awt.image.ImageProducer ip = new java.awt.image.FilteredImageSource(im.getSource(), filter);
  
    return Toolkit.getDefaultToolkit().createImage(ip);
    }
}
