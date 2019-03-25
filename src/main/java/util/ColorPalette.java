/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Nikolay
 */
public class ColorPalette {

    private ArrayList<Color> colors;
    private static final Random rnd = new Random(1L);
    public static final ColorPalette NEUTRAL_PALETTE = new ColorPalette(new Color[]{
        new Color(79, 129, 189),
        new Color(192, 80, 88),
        new Color(155, 187, 89),
        new Color(105, 38, 157),
        new Color(247, 150, 70),
        new Color(29, 78, 49),
        new Color(244, 11, 244),
        new Color(0, 255, 255),
        new Color(0, 128, 152),
        new Color(0xFF4500),//orangeRed
        new Color(255, 0, 0),
        new Color(0, 255, 0),
        new Color(0, 0, 255),
        new Color(0, 0, 0),
        new Color(0xCDBE70),//lightgoldenrod    
        new Color(0x00BFFF),//deepskyblue

        new Color(0x8B008B), //darkMagenta
        new Color(0xADFF2F),//greenyellow
        new Color(0xEE7600),//darkOrange
        new Color(0x71C671),//SGI chartreuse
    });
    public static final ColorPalette EMT_PALETTE = new ColorPalette(new Color[]{
        new Color(0, 0, 255),
        //new Color(100,100,100),
        new Color(0, 255, 0),
        new Color(204, 0, 0),
        new Color(104, 3, 104)
    });
    public static final ColorPalette BRIGHT_PALETTE = new ColorPalette(new Color[]{
        new Color(255, 0, 0),
        new Color(0, 255, 0),
        new Color(0, 0, 255),
        new Color(255, 0, 255),
        new Color(0, 255, 255),
        new Color(255, 255, 0),
        //new Color(100,100,100),
        new Color(0, 162, 232),
        new Color(70, 220, 0),
        new Color(0, 0, 0)
    });

    public ColorPalette(Color[] color) {
        colors = new ArrayList<>();
        colors.addAll(Arrays.asList(color));
    }
    
    Color gray = new Color(200,200,200);

    public Color getColor(int i) {
        if (i < 0) {
            return gray;
        }
        int diff = i - (colors.size() - 1);

        for (int j = 0; j < diff; j++) {
            colors.add(new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
        }

        return colors.get(i);

    }

}
