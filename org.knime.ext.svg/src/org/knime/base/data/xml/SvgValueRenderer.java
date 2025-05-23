/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.renderer.StaticRenderer;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.renderer.AbstractDataValueRendererFactory;
import org.knime.core.data.renderer.AbstractPainterDataValueRenderer;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.util.LockedSupplier;
import org.w3c.dom.svg.SVGDocument;

/**
 * Renderer for SVG documents. Apache Batik is used to render the images.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
@SuppressWarnings("serial")
public class SvgValueRenderer extends AbstractPainterDataValueRenderer
        implements SvgProvider {
    /**
     * Factory for SVG Value renderers.
     *
     * @since 2.10
     */
    public static final class Factory extends AbstractDataValueRendererFactory {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return DESCRIPTION;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataValueRenderer createRenderer(final DataColumnSpec colSpec) {
            return new SvgValueRenderer(colSpec);
        }
    }

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

    static final String DESCRIPTION = "SVG renderer";

    private SVGDocument m_doc;

    private static final Font NO_SVG_FONT = new Font(Font.SANS_SERIF,
            Font.ITALIC, 12);

    private static final UserAgent UA = new UserAgentAdapter();

    private static final RenderingHints R_HINTS = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    private final boolean m_keepAspectRatio;

    private static final int DEFAULT__PREFERRED_WIDTH = 90;

    private static final int DEFAULT_PREFERRED_HEIGHT = 90;

    private final Dimension m_preferredSize;

    private final ReentrantLock m_lock;

    private SvgValue m_currentValue;

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
        m_preferredSize = getPreferredSizeFromProps(props).orElse(null);
        m_lock = new ReentrantLock();
    }

    private static Optional<Dimension> getPreferredSizeFromProps(final DataColumnProperties props) {

        String widthProp = props.getProperty(OPTION_PREFERRED_WIDTH);
        String heightProp = props.getProperty(OPTION_PREFERRED_HEIGHT);
        if (widthProp == null && heightProp == null) {
            return Optional.empty();
        }
        int width = widthProp == null ? DEFAULT__PREFERRED_WIDTH : Integer.parseInt(widthProp);
        int height = heightProp == null ? DEFAULT_PREFERRED_HEIGHT : Integer.parseInt(heightProp);
        return Optional.of(new Dimension(width, height));
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
        if (!(value instanceof SvgValue)) {
            m_doc = null;
            return;
        }

        m_currentValue = (SvgValue)value;
        try (LockedSupplier<SVGDocument> supplier = m_currentValue.getDocumentSupplier()) {
            m_doc = supplier.get();
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

        paint(m_doc, (Graphics2D)g, getBounds(), m_keepAspectRatio);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * @return the preferred size set via spec properties or the preferred size of the current image value or
     *         Dimension(90, 90).
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        if (m_preferredSize != null) {
            return m_preferredSize;
        }
        if (m_currentValue != null) {
            return m_currentValue.getImageContent().getPreferredSize();
        }
        return new Dimension(DEFAULT__PREFERRED_WIDTH, DEFAULT_PREFERRED_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize(final Dimension viewPortDimension) {
        if (m_keepAspectRatio == false || m_currentValue == null) {
            return viewPortDimension;
        }
        var preferredSize = m_currentValue.getImageContent().getPreferredSize();
        double aspectRatio = (double)preferredSize.width / preferredSize.height;
        double width = viewPortDimension.height * aspectRatio;
        if (width <= viewPortDimension.width) {
            return new Dimension((int)width, viewPortDimension.height);
        } else {
            double height = viewPortDimension.width / aspectRatio;
            return new Dimension(viewPortDimension.width, (int)height);
        }
    }

    /**
     * Renders an SVG document on a graphics object. The image is scaled to fit
     * in the specified bounds.
     *
     * @param doc an SVG document
     * @param g the graphics object
     * @param componentBounds the bound in which the image should be drawn
     * @param keepAspectRatio <code>true</code> if the aspect ratio should be
     *            kept, <code>false</code> if the image should be scaled in both
     *            direction to the maximum
     */
    public static void paint(final SVGDocument doc, final Graphics2D g,
            final Rectangle componentBounds, final boolean keepAspectRatio) {
        if ((componentBounds.getHeight() < 1)
                || (componentBounds.getWidth() < 1)) {
            return;
        }

        GVTBuilder gvtBuilder = new GVTBuilder();
        BridgeContext bridgeContext = new BridgeContext(UA);
        GraphicsNode gvtRoot = gvtBuilder.build(bridgeContext, doc);

        Rectangle2D svgBounds = gvtRoot.getBounds();
        if (svgBounds == null) {
            g.setFont(NO_SVG_FONT);
            g.drawString("Invalid SVG", 2, 14);
            return;
        }

        double scaleX = (componentBounds.getWidth() - 2) / svgBounds.getWidth();
        double scaleY =
                (componentBounds.getHeight() - 2) / svgBounds.getHeight();
        if (keepAspectRatio) {
            scaleX = Math.min(scaleX, scaleY);
            scaleY = Math.min(scaleX, scaleY);
        }

        AffineTransform transform = new AffineTransform();
        transform.scale(scaleX, scaleY);
        transform.translate(-svgBounds.getX(), -svgBounds.getY());

        StaticRenderer renderer = new StaticRenderer(R_HINTS, transform);
        renderer.setTree(gvtRoot);
        renderer.updateOffScreen((int)componentBounds.getWidth(),
                (int)componentBounds.getHeight());
        renderer.clearOffScreen();
        renderer.repaint(componentBounds);
        final BufferedImage image = renderer.getOffScreen();

        double heightDiff =
                componentBounds.getHeight() - scaleY * svgBounds.getHeight();

        double widthDiff =
                componentBounds.getWidth() - scaleX * svgBounds.getWidth();

        g.drawImage(image, (int)(widthDiff / 2), (int)(heightDiff / 2), null);
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    @Deprecated
    @Override
    public SVGDocument getSvg() {
        return m_doc;
    }

    /**
     * {@inheritDoc}
     * @since 3.6
     */
    @Override
    public LockedSupplier<SVGDocument> getSvgSupplier() {
        return new LockedSupplier<SVGDocument>(m_doc, m_lock);
    }
}
