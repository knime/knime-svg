/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 * 
 * History
 *   18.06.2012 (kilian): created
 */
package org.knime.ext.svg.node.radarplot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.SwingUtilities;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.knime.base.data.xml.SvgCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.SingleCellFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 * Factory to create cells containing the svg radar plots.
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 2.6
 */
class RadarPlotCellFactory extends SingleCellFactory {

    private ColumnSettingsTable m_originalSettings;
    
    private int m_dim = 0;
    private int[] m_indices = new int[m_dim];
    private double[] m_min = new double[m_dim];
    private double[] m_max = new double[m_dim];
    private double[] m_validMin = new double[m_dim];
    private double[] m_validMax = new double[m_dim];
    private String[] m_labels = new String[m_dim];
    
    /**
     * Constructor of <code>RadarPlotCellFactory</code> with given spec of 
     * column to store the svg plots in, the original input spec and the 
     * settings specified by the user.
     * @param colSpec The spec of the solumn to store the svg plots in.
     * @param spec The spec of the original input data table.
     * @param origSettings The settings specified by the user.
     */
    RadarPlotCellFactory(final DataColumnSpec colSpec, final DataTableSpec spec,
            final ColumnSettingsTable origSettings) {
        super(colSpec);
        
        m_originalSettings = origSettings;
        
        // read the possibly new input spec, match the given original settings
        // against it and take over as many of the original settings as possible
        ColumnSettingsTable testSettings = new ColumnSettingsTable(
                RadarplotAppenderNodeModel.COLUMNRANGEPREFIX);
        testSettings.setNewSpecAndTakeoverSettings(spec, origSettings);
        
        m_dim = testSettings.getNrSelected();
        m_indices = new int[m_dim];
        m_min = new double[m_dim];
        m_max = new double[m_dim];
        m_validMin = new double[m_dim];
        m_validMax = new double[m_dim];
        m_labels = new String[m_dim];
        
        int nrSelCol = 0; // keep track of selected (and double) columns

        for (int i = 0; i < spec.getNumColumns(); i++) {
            String colName = spec.getColumnSpec(i).getName();
            int j = m_originalSettings.getIndexOfAttr(colName);
            int k = testSettings.getIndexOfAttr(colName);
            if (j > -1 && m_originalSettings.isSelected(j)
                    && testSettings.isSelected(k)) {
                // retrieve min/max values. We know that they exist
                // because only columns with valid domains are allowed to be 
                // selected.
                m_validMin[nrSelCol] = Double.NaN;
                m_validMax[nrSelCol] = Double.NaN;
                try {
                    if (spec.getColumnSpec(i).getDomain().hasLowerBound()) {
                        m_validMin[nrSelCol] =
                                ((DoubleValue)spec.getColumnSpec(i)
                                        .getDomain().getLowerBound())
                                        .getDoubleValue();
                    }
                    if (spec.getColumnSpec(i).getDomain().hasUpperBound()) {
                        m_validMax[nrSelCol] =
                                ((DoubleValue)spec.getColumnSpec(i)
                                        .getDomain().getUpperBound())
                                        .getDoubleValue();
                    }
                } catch (Exception e) {
                    assert false;
                }
                assert !Double.isNaN(m_validMin[nrSelCol]);
                assert !Double.isNaN(m_validMax[nrSelCol]);
                // we know that min/max are set properly so we can !-!
                // force valid ranges into this range as well:
                m_min[nrSelCol] = m_originalSettings.getMin(j);
                m_max[nrSelCol] = m_originalSettings.getMax(j);
                if (m_validMin[nrSelCol] > m_min[nrSelCol]) {
                    m_min[nrSelCol] = m_validMin[nrSelCol];
                    // m_adjustedValidRanges = true;
                }
                if (m_validMax[nrSelCol] < m_max[nrSelCol]) {
                    m_max[nrSelCol] = m_validMax[nrSelCol];
                    // m_adjustedValidRanges = true;
                }
                m_labels[nrSelCol] = spec.getColumnSpec(i).getName();
                m_indices[nrSelCol] = i;
                nrSelCol++;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell getCell(DataRow row) {
        double[] values = new double[m_dim];
        for (int j = 0; j < m_dim; j++) {
            if (!row.getCell(m_indices[j]).isMissing()) {
                // value is double and not missing
                values[j] =
                        ((DoubleValue)row.getCell(m_indices[j]))
                                .getDoubleValue();
            } else {
                // represent missing value with NotANumber
                values[j] = Double.NaN;
            }
        }

        Color[] colors = new Color[4];
        colors[0] = m_originalSettings.getBackgroundColor();
        colors[1] = m_originalSettings.getIntervalColor();
        colors[2] = m_originalSettings.getBendColor();
        colors[3] = m_originalSettings.getOutlyingBendColor();

        RadarPlotPanel panel =
                new RadarPlotPanel(values, m_min, m_max, m_validMin,
                        m_validMax, m_labels, colors);

        int maxWidth = 0;
        int maxHeight = 0;

        for (int i = 0; i < m_labels.length; i++) {
            Font font = panel.getFont();
            maxWidth = Math.max(SwingUtilities.computeStringWidth(
                    panel.getFontMetrics(font), m_labels[i]), maxWidth);
            maxHeight = Math.max(panel.getFontMetrics(font).getHeight(),
                    maxHeight);
        }

        // setup box and paint border
        final int xWidth = 200 + 2 * maxWidth + 10;
        final int yHeight = 175 + 2 * maxHeight + 5;
        DOMImplementation domImpl = new SVGDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document myFactory =
                domImpl.createDocument(svgNS, "svg", null);
        SVGGraphics2D g = new SVGGraphics2D(myFactory);
        g.setSVGCanvasSize(new Dimension(xWidth, yHeight));
        g.translate(maxWidth, maxHeight);
        g.setColor(Color.BLUE);

        g.setStroke(new BasicStroke(1));
        panel.paintComponentSVG(g);

        myFactory.replaceChild(g.getRoot(),
                myFactory.getDocumentElement());
        DataCell dc = new SvgCell((SVGDocument)myFactory);
        return dc;
    }
}
