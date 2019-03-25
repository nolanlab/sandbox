/*
 * Copyright (C) 2019 Nikolay Samusik and Stanford University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import sandbox.clustering.Dataset;
import java.util.Arrays;

/**
 *
 * @author Nikolay
 */
public class ColorScale {

    private static final int ox = 100;
    private static final int oy = 15;

    private static final double num_ticks = 5.0;

    private int height;
    private Dataset ds;

    private QuantileMap qm;
    private QuantileMap sqm;
    private double[] minValues;
    private double[] maxValues;

    public enum ScalingMode {
        LINEAR, QUANTILE
    };

    private ScalingMode mode;

    public void setMode(ScalingMode mode) {
        this.mode = mode;
    }

    public double getScaledValue(int paramIDX, double[] vec) {
        return getScaledValue(paramIDX, vec[paramIDX]);
    }
    
    public double getScaledValue(int paramIDX, double val) {

        double scaledVal = 0;

        switch (mode) {
            case LINEAR:
                scaledVal = (val - minValues[paramIDX]) / (maxValues[paramIDX] - minValues[paramIDX]);
                break;
            case QUANTILE:
                if (qm == null) {
                    qm = QuantileMap.getQuantileMap(ds);
                    sqm = QuantileMap.getQuantileMapForSideParam(ds);
                }
                if (paramIDX < ds.getFeatureNamesCombined().length) {
                    double noiseQuantile = (paramIDX >= ds.getNumDimensions()) ? sqm.getQuantileForValue(paramIDX - ds.getNumDimensions(), minValues[paramIDX]) : qm.getQuantileForValue(paramIDX, minValues[paramIDX]);
                    scaledVal = (paramIDX >= ds.getNumDimensions()) ? (float) sqm.getQuantileForValue(paramIDX - ds.getNumDimensions(), val) : (float) qm.getQuantileForValue(paramIDX, val);//(float) ((vec[paramIDX] - scalingBounds[paramIDX][0]) / (scalingBounds[paramIDX][1] - scalingBounds[paramIDX][0]));
                    scaledVal = (scaledVal - noiseQuantile) / (1.0 - noiseQuantile);
                }
                break;
            default:
                throw new IllegalStateException("Illegal mode:" + mode);
        }

        return Math.min(1.0, Math.max(0, scaledVal));
    }

    public ScalingMode getMode() {
        return mode;
    }

    public Color getColor(int paramIDX, double[] vec) {
        return getRainbowColorForValue(getScaledValue(paramIDX, vec));
    }
    
    public Color getColor(int paramIDX, double val) {
        return getRainbowColorForValue(getScaledValue(paramIDX, val));
    }

    public ColorScale(int height, Dataset ds, double defaultMinVal, QuantileMap qm, QuantileMap sqm, ScalingMode mode) {
        this.height = height;
        this.ds = ds;
        this.minValues = new double[ds.getFeatureNamesCombined().length];
        this.maxValues = new double[ds.getFeatureNamesCombined().length];
        Arrays.fill(minValues, defaultMinVal);
        int k = 0;
        for (int i = 0; i < qm.getDimensionality(); i++) {
            maxValues[k++] = qm.getSourceDatasetQuantile(i, 0.99);
        }
        for (int i = 0; i < sqm.getDimensionality(); i++) {
            maxValues[k++] = sqm.getSourceDatasetQuantile(i, 0.99);
        }
        this.qm = qm;
        this.sqm = sqm;
        this.mode = mode;
    }

    public void setMinValue(int paramIdx, double minVal) {
        this.minValues[paramIdx] = minVal;
    }

    public BufferedImage generateScaleImage(int paramIdx) {
        switch (mode) {
            case LINEAR:
                return generateScaleImageLinear(height, minValues[paramIdx], maxValues[paramIdx]);
            case QUANTILE:
                return generateQuantileScaledImage(paramIdx);
            default:
                throw new IllegalStateException("Invalid color scaling mode:" + mode);
        }
    }

    private BufferedImage generateQuantileScaledImage(int paramIDX) {
        int w = (height) / 3;
        BufferedImage bi = new BufferedImage(w + ox, height + oy, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setPaint(Color.WHITE);
        g.translate(-20, 7);
        //g.fillRect(0, -20, w+100, height+50);
        for (int i = 0; i < height; i++) {
            double val = (i / (double) height);
            g.setPaint(getRainbowColorForValue(val));
            g.drawLine(0, height - i, w, height - i);
        }
        //logger.print("end of color fill");
        g.setFont(g.getFont().deriveFont(14f));

        double noiseQuantile = (paramIDX >= ds.getNumDimensions()) ? sqm.getQuantileForValue(paramIDX - ds.getNumDimensions(), minValues[paramIDX]) : qm.getQuantileForValue(paramIDX, minValues[paramIDX]);

        double qIncr = (0.99 - noiseQuantile) / 5.0;

        for (double i = 0; i <= 5.0; i++) {
            int y = (int) (i * (height / 5.0));
            double qval = noiseQuantile + (i * qIncr);
            double val = (paramIDX >= ds.getNumDimensions()) ? sqm.getSourceDatasetQuantile(paramIDX - ds.getNumDimensions(), qval) : qm.getSourceDatasetQuantile(paramIDX, qval);
            g.setPaint(fwdCol);
            g.drawLine(w, height - y, w + 10, height - y);
            try {
                g.drawString(String.valueOf(val).substring(0, 4), w + 12, (height - y) + 5);
            } catch (StringIndexOutOfBoundsException e) {
                g.drawString(String.valueOf(val), w + 12, (height - y) + 5);
            }
            //String.valueOf((int) (Math.sinh(val) * 5)), w + 12, (height - y) + 5);
        }

        return bi;
    }

    public void setForegroundColor(Color fwdCol) {
        this.fwdCol = fwdCol;
    }
    
    public BufferedImage generateScaleImageLinear(int height, double minVal, double maxVal) {
        int w = height / 3;
        BufferedImage bi = new BufferedImage(w + ox, height + oy, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setPaint(Color.WHITE);
        g.translate(-20, 7);
        //g.fillRect(0, -20, w+100, height+50);
        for (int i = 0; i < height; i++) {
            double val = (i / (double) height);
            g.setPaint(getRainbowColorForValue(val));
            g.drawLine(0, height - i, w, height - i);
        }
        //logger.print("end of color fill");
        g.setFont(g.getFont().deriveFont(14f));

        for (double i = minVal; i <= maxVal; i += ((maxVal - minVal) / 4)) {
            int y = (int) (((i - minVal) / (maxVal - minVal)) * height);
            double val = i;
            g.setPaint(fwdCol);
            g.drawLine(w, height - y, w + 10, height - y);
            try {
                g.drawString(String.valueOf(val).substring(0, 4), w + 12, (height - y) + 5);
            } catch (StringIndexOutOfBoundsException e) {
                g.drawString(String.valueOf(val), w + 12, (height - y) + 5);
            }
            //String.valueOf((int) (Math.sinh(val) * 5)), w + 12, (height - y) + 5);
        }

        return bi;
    }
    
    private Color fwdCol = Color.BLACK;
    

    public static Color getRainbowColorForValue(double val) {
        if (Double.isNaN(val)) {
            return Color.GRAY;
        }

        return new Color(Color.HSBtoRGB(0.60f - (float) val * 0.60f, 1, 1f));
    }

    private Color getColorForValue(double val, int alpha) {
        Color col = getRainbowColorForValue(val);
        return new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha);
    }
}
