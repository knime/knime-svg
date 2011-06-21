/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Feb 20, 2009 (bw&mb): created
 */
package org.knime.ext.svg.node.radarplot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Stroke;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Panel showing a Radarplot and allowing editing the same.
 *
 * @author Bernd Wiswedel, University of Konstanz
 * @author Michael Berthold, University of Konstanz
 * @author Andreas Burger, University of Konstanz
 */
class RadarPlotPanel extends JPanel {
//    private final static Color VALID_BG_COLOR = new Color(175, 220, 240);
//    private final static Color AREA_COLOR = new Color(105, 150, 170);
//    private final static Color BEND_COLOR = Color.RED.darker();
    private final static Color FONT_VIOLATE_COLOR = Color.RED.darker();
    private final static Color FONT_OK_COLOR = Color.BLACK;
    private final static Color COORDINATE_COLOR = Color.BLACK;
    private final static Stroke LINE_STROKE = new BasicStroke(1);

    private Font m_boldFont;
    private Font m_normalFont;
    private boolean m_isDrawLabels = true;

    // we reuse arrays here as all elements in a column potentially have
    // the same dimensionality.
    private int[] m_xMaxs = new int[0];
    private int[] m_yMaxs = new int[0];
    private int[] m_xMins = new int[0];
    private int[] m_yMins = new int[0];
    private int[] m_xVals = new int[0];
    private int[] m_yVals = new int[0];
    private int[] m_xLabelPos = new int[0];
    private int[] m_yLabelPos = new int[0];
    private final int[] m_xBends = new int[4];
    private final int[] m_yBends = new int[4];
    private Color[] m_colors;
    private double[] m_values;
    private double[] m_mins;
    private double[] m_maxs;
    private double[] m_validMins;
    private double[] m_validMaxs;
    private String[] m_labels;


    public RadarPlotPanel(final double[] values, final double[] _min, final double[] _max,
			final double[] _validMin, final double[] _validMax, final String[] _labels,
			final Color[] colors) {
    	m_values = values;
    	m_mins = _min;
    	m_maxs = _max;
    	m_validMins = _validMin;
    	m_validMaxs = _validMax;
    	m_labels = _labels;
    	m_colors = colors;
        setBackground(Color.WHITE);
	}

    /** Returns a tiny String describing this Panel
     * @return Descriptive String
     */
    public String getDescription() {
        return "Labeled Radar Plot";
    }

    /** {@inheritDoc} */
    @Override
    public void setFont(final Font font) {
        Font oldFont = getFont();
        super.setFont(font);
        if (oldFont == null || m_boldFont == null
                || !oldFont.equals(getFont())) {
            float size = getFont().getSize();
            m_boldFont = getFont().deriveFont(Font.BOLD, size);
            m_normalFont = getFont().deriveFont(size);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 175);
    }

    /** Paints the radarplot into a given Graphics-object
     * @param g The graphics object in which to paint
     */
    protected void paintComponentSVG(final Graphics g) {

    	// This method paints twice. First the basic Radarplot, then the highlighted bend.
    	//1
        super.paintComponent(g);
        if (m_values == null) {
            return;
        }
        Insets insets = getInsets();
        Dimension size = new Dimension(175, 150);
        int originOffset = size.width / 30;
        originOffset = Math.min(size.height / 30, originOffset);
        originOffset = Math.min(5, originOffset);
        double[] min = m_mins;
        double[] max = m_maxs;
        double[] values = m_values;
        double[] validMin = m_validMins;
        double[] validMax = m_validMaxs;
        final int length = min.length;
        if (length != m_xMaxs.length) {
            m_xMaxs = new int[length];
            m_yMaxs = new int[length];
            m_xMins = new int[length];
            m_yMins = new int[length];
            m_xVals = new int[length];
            m_yVals = new int[length];
            m_xLabelPos = new int[length];
            m_yLabelPos = new int[length];
        }
        int xOrigin = size.width - insets.left - insets.right;
        int yOrigin = size.height - insets.top - insets.bottom;
        g.setColor(getBackground());
        g.fillRect(insets.left, insets.bottom, xOrigin, yOrigin);
        xOrigin /= 2;
        yOrigin /= 2;
        int xRadarSize = 7 * xOrigin / 8;
        int yRadarSize = 7 * yOrigin / 8;
        int xRadarZero = Math.max(Math.min(1 * xOrigin / 10, 20), 5);
        int yRadarZero = Math.max(Math.min(1 * yOrigin / 10, 20), 5);
        int[] xMaxs = m_xMaxs;
        int[] yMaxs = m_yMaxs;
        int[] xMins = m_xMins;
        int[] yMins = m_yMins;
        int[] xVals = m_xVals;
        int[] yVals = m_yVals;
        int[] xBends = m_xBends;
        int[] yBends = m_yBends;
        int xBendMinFirst = -1;
        int xBendMaxFirst = -1;
        int yBendMinFirst = -1;
        int yBendMaxFirst = -1;
        double angle = 0.0;
        double angleOffset = 2 * Math.PI / length;



        for (int i = 0; i < length; i++) {
            angle = i * angleOffset;
            double sinus = Math.sin(angle);
            double cosinus = Math.cos(angle);
            int maxXPoint = (int)(sinus * xRadarSize);
            int maxYPoint = (int)(cosinus * yRadarSize);
            int minXPoint = (int)(sinus * xRadarZero);
            int minYPoint = (int)(cosinus * yRadarZero);


            double scaleVal = (values[i] - validMin[i]) / (validMax[i] - validMin[i]);
            int xValPoint = (int)(scaleVal * (maxXPoint - minXPoint));
            int yValPoint = (int)(scaleVal * (maxYPoint - minYPoint));
            xMaxs[i] = xOrigin + maxXPoint;
            yMaxs[i] = yOrigin + maxYPoint;
            xMins[i] = (xOrigin + minXPoint);
            yMins[i] = (yOrigin + minYPoint);
            xVals[i] = xMins[i] + xValPoint;
            yVals[i] = yMins[i] + yValPoint;

            double scaleMinBend = 0 / (validMax[i] - validMin[i]);
            int xMinBend = (int)(scaleMinBend * (maxXPoint - minXPoint));
            int yMinBend = (int)(scaleMinBend * (maxYPoint - minYPoint));
            double scaleMaxBend = (validMax[i] - validMin[i]) / (validMax[i] - validMin[i]);
            int xMaxBend = (int)(scaleMaxBend * (maxXPoint - minXPoint));
            int yMaxBend = (int)(scaleMaxBend * (maxYPoint - minYPoint));
            xBends[0] = xBends[1];
            xBends[3] = xBends[2];
            xBends[1] = xMins[i] + xMaxBend;
            xBends[2] = xMins[i] + xMinBend;
            yBends[0] = yBends[1];
            yBends[3] = yBends[2];
            yBends[1] = yMins[i] + yMaxBend;
            yBends[2] = yMins[i] + yMinBend;
            if (i == 0) {
                xBendMaxFirst = xBends[1];
                xBendMinFirst = xBends[2];
                yBendMaxFirst = yBends[1];
                yBendMinFirst = yBends[2];
            } else {
                Polygon validPol = new Polygon(xBends, yBends, xBends.length);
                g.setColor(m_colors[0]);
                g.fillPolygon(validPol);
            }
            if (i == length - 1) {
                xBends[0] = xBends[1];
                xBends[3] = xBends[2];
                xBends[1] = xBendMaxFirst;
                xBends[2] = xBendMinFirst;
                yBends[0] = yBends[1];
                yBends[3] = yBends[2];
                yBends[1] = yBendMaxFirst;
                yBends[2] = yBendMinFirst;
                Polygon validPol = new Polygon(xBends, yBends, xBends.length);
                g.setColor(m_colors[0]);
                g.fillPolygon(validPol);
            }




//            if (m_isDrawLabels) {
//                Font font;
//                if (values[i] < validMin[i] || values[i] > validMax[i]) {
//                    g.setColor(FONT_VIOLATE_COLOR);
//                    font = m_boldFont;
//                } else {
//                    g.setColor(FONT_OK_COLOR);
//                    font = m_normalFont;
//                }
//                int stringWidth = SwingUtilities.computeStringWidth(
//                        getFontMetrics(font), m_value.getLabels()[i]);
//                int stringHeight = getFontMetrics(font).getHeight();
//                int rightBorder = size.width - insets.right;
//                int x = Math.min(xMaxs[i] + 2, rightBorder) - stringWidth;
//                x = Math.max(insets.left, x);
//                // labels for coordinates in quadrant 1 and 4 are
//                // shifted upwards
//                int yLabelOffSet = cosinus - 1E-6 < 0.0 ? -2 : 2 + stringHeight;
//                int y = Math.max(
//                        insets.top + stringHeight, yMaxs[i] + yLabelOffSet);
//                y = Math.min(size.height - insets.bottom, y);
//                m_xLabelPos[i] = x;
//                m_yLabelPos[i] = y;
//            }
        }

        //2

        for (int i = 0; i < length; i++) {
            angle = i * angleOffset;
            double sinus = Math.sin(angle);
            double cosinus = Math.cos(angle);
            int maxXPoint = (int)(sinus * xRadarSize);
            int maxYPoint = (int)(cosinus * yRadarSize);
            int minXPoint = (int)(sinus * xRadarZero);
            int minYPoint = (int)(cosinus * yRadarZero);


          double scaleVal = (values[i] - validMin[i]) / (validMax[i] - validMin[i]);
          int xValPoint = (int)(scaleVal * (maxXPoint - minXPoint));
          int yValPoint = (int)(scaleVal * (maxYPoint - minYPoint));
          xMaxs[i] = xOrigin + maxXPoint;
          yMaxs[i] = yOrigin + maxYPoint;
          xMins[i] = xOrigin + minXPoint;
          yMins[i] = yOrigin + minYPoint;
          xVals[i] = xMins[i] + xValPoint;
          yVals[i] = yMins[i] + yValPoint;

          double scaleMinBend = (min[i] - validMin[i]) / (validMax[i] - validMin[i]);
          int xMinBend = (int)(scaleMinBend * (maxXPoint - minXPoint));
          int yMinBend = (int)(scaleMinBend * (maxYPoint - minYPoint));
          double scaleMaxBend = (max[i] - validMin[i]) / (validMax[i] - validMin[i]);
          int xMaxBend = (int)(scaleMaxBend * (maxXPoint - minXPoint));
          int yMaxBend = (int)(scaleMaxBend * (maxYPoint - minYPoint));
          xBends[0] = xBends[1];
          xBends[3] = xBends[2];
          xBends[1] = xMins[i] + xMaxBend;
          xBends[2] = xMins[i] + xMinBend;
          yBends[0] = yBends[1];
          yBends[3] = yBends[2];
          yBends[1] = yMins[i] + yMaxBend;
          yBends[2] = yMins[i] + yMinBend;
          if (i == 0) {
              xBendMaxFirst = xBends[1];
              xBendMinFirst = xBends[2];
              yBendMaxFirst = yBends[1];
              yBendMinFirst = yBends[2];
          } else {
              Polygon validPol = new Polygon(xBends, yBends, xBends.length);
              g.setColor(m_colors[1]);
              g.fillPolygon(validPol);
          }
          if (i == length - 1) {
              xBends[0] = xBends[1];
              xBends[3] = xBends[2];
              xBends[1] = xBendMaxFirst;
              xBends[2] = xBendMinFirst;
              yBends[0] = yBends[1];
              yBends[3] = yBends[2];
              yBends[1] = yBendMaxFirst;
              yBends[2] = yBendMinFirst;
              Polygon validPol = new Polygon(xBends, yBends, xBends.length);
              g.setColor(m_colors[1]);
              g.fillPolygon(validPol);
          }




            if (m_isDrawLabels) {
                Font font;
                if (values[i] < validMin[i] || values[i] > validMax[i]) {
                    g.setColor(FONT_VIOLATE_COLOR);
                    font = m_boldFont;
                } else {
                    g.setColor(FONT_OK_COLOR);
                    font = m_normalFont;
                }
                int stringWidth = SwingUtilities.computeStringWidth(
                        getFontMetrics(font), m_labels[i]);
                int stringHeight = getFontMetrics(font).getHeight();
                int rightBorder = size.width - insets.right;
                int x = Math.min(xMaxs[i] + 2, rightBorder) - stringWidth;
                if(sinus >=0) {
                    x = x+stringWidth;
                }
                //x = Math.max(insets.left, x);
                // labels for coordinates in quadrant 1 and 4 are
                // shifted upwards
                int yLabelOffSet = cosinus - 1E-6 < 0.0 ? -2 : 2 + stringHeight;
                int y = yMaxs[i] + yLabelOffSet;
                y = Math.min(size.height - insets.bottom, y);

                m_xLabelPos[i] = x;
                m_yLabelPos[i] = y;
            }
        }
        g.setColor(COORDINATE_COLOR);
        g.drawPolygon(xMins, yMins, length);
        for (int i = 0; i < length; i++) {
            g.setColor(COORDINATE_COLOR);
            g.drawLine(xMins[i], yMins[i], xMaxs[i], yMaxs[i]);
            if (m_isDrawLabels) {
                Font font;
                if (values[i] < validMin[i] || values[i] > validMax[i]) {
                    g.setColor(FONT_VIOLATE_COLOR);
                    font = m_boldFont;
                } else {
                    g.setColor(FONT_OK_COLOR);
                    font = m_normalFont;
                }
                g.setFont(font);
                g.drawString(m_labels[i],
                        m_xLabelPos[i], m_yLabelPos[i]);
            }
        }
        Polygon valuePolygon = new Polygon(xVals, yVals, length);
        g.setColor(m_colors[2]);
        for(int i = 0; i < values.length; i++) {
            if ((values[i] < min[i])|(values[i] > max[i])) {
                g.setColor(m_colors[3]);
            }
        }
        ((Graphics2D)g).setStroke(LINE_STROKE);
        g.drawPolygon(valuePolygon);
    }

}
