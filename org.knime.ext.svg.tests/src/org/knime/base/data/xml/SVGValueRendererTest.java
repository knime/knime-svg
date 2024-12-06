/*
 * ------------------------------------------------------------------------
 *
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
 *   Dec 6, 2024 (Paul Bärnreuther): created
 */
package org.knime.base.data.xml;

import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.junit.Test;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.renderer.AbstractPainterDataValueRenderer;

/**
 * Test cases for the {@link SvgValueRenderer}.
 *
 * @author Paul Bärnreuther
 */
public class SVGValueRendererTest {

    private static final String SVG_WITH_DIMENSIONS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
        + "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"150\" height=\"50\" viewBox=\"0 0 150 50\">" //
        + "  <rect width=\"150\" height=\"50\" fill=\"lightblue\" />" //
        + "  <text x=\"75\" y=\"30\" font-size=\"12\" text-anchor=\"middle\" fill=\"darkblue\">150x50 SVG</text>" //
        + "</svg>";

    /**
     * Default preferred size is 90x90.
     */
    @Test
    public void testDefaultPreferredSize() {
        final AbstractPainterDataValueRenderer renderer = createRenderer(null);
        final Dimension preferredSize = renderer.getPreferredSize();
        assertTrue("width should default to 90", preferredSize.width == 90);
        assertTrue("height should default ot 90", preferredSize.height == 90);
    }

    /**
     * Preferred size can be set via spec properties.
     */
    @Test
    public void testPreferredSizeSpecProperties() {
        final AbstractPainterDataValueRenderer renderer = createRenderer(new DataColumnProperties(
            Map.of(SvgValueRenderer.OPTION_PREFERRED_WIDTH, "100", SvgValueRenderer.OPTION_PREFERRED_HEIGHT, "200")));
        final Dimension preferredSize = renderer.getPreferredSize();
        assertTrue("width should be set by prop", preferredSize.width == 100);
        assertTrue("height should be set by prop", preferredSize.height == 200);
    }

    /**
     * Takes image dimensions from currently set image if it is available.
     *
     * @throws IOException in case the above string cannot be read as svg image.
     */
    @Test
    public void testPreferredSizeFromCurrentImage() throws IOException {

        final SvgValueRenderer renderer = createRenderer(null);

        setImageWithPreferredSize(renderer);

        final Dimension preferredSize = renderer.getPreferredSize();
        assertTrue("width should be taken from image", preferredSize.width == 150);
        assertTrue("height should be taken from image", preferredSize.height == 50);

    }

    /**
     * When an image with dimensions is set as value, we still want the preferred size to be taken from the properties.
     *
     * @throws IOException
     */
    @Test
    public void testPrefersPreferredSizesFromPropsOverFromCurrentImage() throws IOException {
        final SvgValueRenderer renderer = createRenderer(new DataColumnProperties(
            Map.of(SvgValueRenderer.OPTION_PREFERRED_WIDTH, "100", SvgValueRenderer.OPTION_PREFERRED_HEIGHT, "200")));
        setImageWithPreferredSize(renderer);
        final Dimension preferredSize = renderer.getPreferredSize();
        assertTrue("width should be taken from properties", preferredSize.width == 100);
        assertTrue("height should be taken from properties", preferredSize.height == 200);
    }

    /**
     * When providing view port dimensions, the renderer should scale the preferred size accordingly.
     *
     * @throws IOException
     */
    @Test
    public void testGetPreferredSizesFromViewPortDimension() throws IOException {
        final SvgValueRenderer renderer = createRenderer(null);
        assertTrue(renderer.getPreferredSize(new Dimension(75, 75)).equals(new Dimension(75, 75)));
        setImageWithPreferredSize(renderer);
        assertTrue(renderer.getPreferredSize(new Dimension(75, 75)).equals(new Dimension(75, 25)));
    }

    private static SvgValueRenderer createRenderer(final DataColumnProperties properties) {
        final DataColumnSpecCreator creator = new DataColumnSpecCreator("svg", SvgCell.TYPE);
        if (properties != null) {
            creator.setProperties(properties);
        }
        final DataColumnSpec dummySpec = creator.createSpec();
        return new SvgValueRenderer(dummySpec);

    }

    private static void setImageWithPreferredSize(final SvgValueRenderer renderer) throws IOException {
        byte[] input = SVG_WITH_DIMENSIONS.getBytes(Charset.forName("UTF-8"));

        ByteArrayInputStream is = new ByteArrayInputStream(input);
        final SvgValue svgValue = new SvgCell(is);
        renderer.setValue(svgValue);
    }

}
