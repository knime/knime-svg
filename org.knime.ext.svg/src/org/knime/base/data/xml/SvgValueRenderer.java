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
 *   11.08.2010 (meinl): created
 */
package org.knime.base.data.xml;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.renderer.StaticRenderer;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.renderer.AbstractPainterDataValueRenderer;
import org.w3c.dom.svg.SVGDocument;

/**
 * Renderer for SVG documents. Apache Batik is used to render the images.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class SvgValueRenderer extends AbstractPainterDataValueRenderer {
    /**
     * Property key that can be used in {@link DataColumnProperties} to specify
     * whether the aspect ration of the SVG should be kept (<code>true</code>)
     * or if the images should be scaled to the maximum on both directions (
     * <code>false</code>).
     */
    public static final String OPTION_KEEP_ASPECT_RATIO =
            SvgValueRenderer.class + ".keepAspectRatio";

    /**
     * Property key that can be used in {@link DataColumnProperties} to specify
     * the preferred height of the rendered image. The value must be a positive
     * integer.
     */
    public static final String OPTION_PREFERRED_HEIGHT = SvgValueRenderer.class
            + ".preferredHeight";

    /**
     * Property key that can be used in {@link DataColumnProperties} to specify
     * the preferred width of the rendered image. The value must be a positive
     * integer.
     */
    public static final String OPTION_PREFERRED_WIDTH = SvgValueRenderer.class
            + ".preferredWidth";

    private SVGDocument m_doc;

    private static final Font NO_SVG_FONT = new Font(Font.SANS_SERIF,
            Font.ITALIC, 12);

    private static final UserAgent UA = new UserAgentAdapter();

    private static final RenderingHints R_HINTS = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    private final boolean m_keepAspectRatio;

    private final Dimension m_preferredSize;

    /**
     * Creates a new renderer for SVG values. The passed spec may contain
     * properties that can be used to fine-tune rendering.
     *
     * @param spec a data column spec with properties
     */
    public SvgValueRenderer(final DataColumnSpec spec) {
        DataColumnProperties props = spec.getProperties();
        m_keepAspectRatio =
                Boolean.parseBoolean(props.getProperty(
                        OPTION_KEEP_ASPECT_RATIO, "true"));

        int width =
                Integer.parseInt(props
                        .getProperty(OPTION_PREFERRED_WIDTH, "90"));
        int height =
                Integer.parseInt(props.getProperty(OPTION_PREFERRED_HEIGHT,
                        "90"));
        m_preferredSize = new Dimension(width, height);
    }

    /**
     * Sets the string object for the cell being rendered.
     *
     * @param value the string value for this cell; if value is
     *            <code>null</code> it sets the text value to an empty string
     * @see javax.swing.JLabel#setText
     *
     */
    @Override
    protected void setValue(final Object value) {
        try {
            m_doc = ((SvgValue)value).getDocument();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to render SVG", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (m_doc == null) {
            g.setFont(NO_SVG_FONT);
            g.drawString("?", 2, 14);
            return;
        }

        Rectangle clipBounds = g.getClipBounds();
        if ((clipBounds.getHeight() < 1) || (clipBounds.getWidth() < 1)) {
            return;
        }

        GVTBuilder gvtBuilder = new GVTBuilder();
        BridgeContext bridgeContext = new BridgeContext(UA);
        GraphicsNode gvtRoot = gvtBuilder.build(bridgeContext, m_doc);

        Rectangle2D svgBounds = gvtRoot.getBounds();
        if (svgBounds == null) {
            g.setFont(NO_SVG_FONT);
            g.drawString("Invalid SVG", 2, 14);
            return;
        }

        double scaleX = (clipBounds.getWidth() - 10) / svgBounds.getWidth();
        double scaleY = (clipBounds.getHeight() - 10) / svgBounds.getHeight();
        if (m_keepAspectRatio) {
            scaleX = Math.min(scaleX, scaleY);
            scaleY = Math.min(scaleX, scaleY);
        }

        AffineTransform transform = new AffineTransform();
        transform.scale(scaleX, scaleY);
        transform.translate(-svgBounds.getX(), -svgBounds.getY());

        StaticRenderer renderer = new StaticRenderer(R_HINTS, transform);
        renderer.setTree(gvtRoot);
        renderer.updateOffScreen((int)clipBounds.getWidth(),
                (int)clipBounds.getHeight());
        renderer.clearOffScreen();
        renderer.repaint(clipBounds);
        final BufferedImage image = renderer.getOffScreen();

        double heightDiff =
                clipBounds.getHeight() - scaleX * svgBounds.getHeight();

        double widthDiff =
                clipBounds.getWidth() - scaleY * svgBounds.getWidth();

        g.drawImage(image, (int)(widthDiff / 2), (int)(heightDiff / 2), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "SVG Renderer";
    }

    /**
     * @return new Dimension(80, 80);
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return m_preferredSize;
    }
}
