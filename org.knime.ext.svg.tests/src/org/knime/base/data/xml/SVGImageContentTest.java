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
 *   12.03.2013 (meinl): created
 */
package org.knime.base.data.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.batik.transcoder.TranscoderException;
import org.junit.Test;
import org.knime.core.node.NodeLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Testcases for {@link SvgImageContent}
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class SVGImageContentTest {
    // contains a non-ASCII character (é)
    private static final String SVG_NON_ASCII = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
        + "<svg xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\""
        + "     width=\"210mm\" height=\"297mm\" id=\"svg2\">\n"
        + "    <text x=\"322.85715\" y=\"286.64789\" id=\"text2987\" xml:space=\"preserve\">\n"
        + "      <tspan x=\"322.85715\" y=\"286.64789\" id=\"tspan2989\">é</tspan>\n" + "    </text>\n" + "</svg>\n";

    private static final String SVG_ALL_WITHIN_BMP = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
        + "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.0\" width=\"300mm\" height=\"200mm\">"
        + "    <text x=\"150\" y=\"115\" font-size=\"40\" text-anchor=\"middle\">SVG \u2764</text>" + "</svg>";

    private static final String SVG_WITH_EMOJI = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
        + "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.0\" width=\"300mm\" height=\"200mm\">"
        + "    <text x=\"150\" y=\"115\" font-size=\"40\" text-anchor=\"middle\">SVG \u1f633</text>" + "</svg>";

    /**
     * Test if de-/serialization of non-ASCII characters in SVG works properly
     *
     * @throws IOException if an exception occurs
     */
    @Test
    public void testSerialization() throws IOException {
        NodeLogger.getLogger(getClass())
            .debug("Context classloader: " + Thread.currentThread().getContextClassLoader());
        byte[] input = SVG_NON_ASCII.getBytes(Charset.forName("UTF-8"));

        ByteArrayInputStream is = new ByteArrayInputStream(input);
        SvgImageContent imageContent = new SvgImageContent(is);
        ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
        // save must create valid UTF-8
        imageContent.save(os);
        os.close();

        input = os.toByteArray();
        is = new ByteArrayInputStream(input);

        // this call should work with an exception, see bug #4108
        imageContent = new SvgImageContent(is);
    }

    /**
     * Test whether the serialisation preserves compatible characters
     *
     * @throws IOException
     * @throws TranscoderException
     */
    @Test
    public void testSerializePreservesCharacters() throws IOException, TranscoderException {
        try (InputStream is = new ByteArrayInputStream(SVG_ALL_WITHIN_BMP.getBytes())) {
            SvgImageContent image = new SvgImageContent(is);

            String serialised = SvgImageContent.serialize(image.getSvgDocument());

            assertTrue("The heart character should survive the serialisation.", serialised.contains("\u2764"));
            assertTrue("There's no surrogate pairs introduced",
                serialised.chars().allMatch(c -> c < 0xD800 || 0xDFFF < c));
        }
    }

    /**
     * Test whether unsupported characters are filtered out and no exception arises
     *
     * @throws IOException
     * @throws TranscoderException
     */
    @Test
    public void testSerializeFiltersUnsupportedChars() throws IOException, TranscoderException {
        try (InputStream is = new ByteArrayInputStream(SVG_WITH_EMOJI.getBytes())) {
            SvgImageContent image = new SvgImageContent(is);

            Document doc = image.getSvgDocument();

            // add another emoji to the document
            Node n = doc.getChildNodes().item(0).getChildNodes().item(0);
            n.setNodeValue(new StringBuilder().appendCodePoint(0x1f171).toString());

            String serialised = SvgImageContent.serialize(image.getSvgDocument()); // Before AP-18895, this failed.

            assertFalse("There's no character > 0xFFFF left.", serialised.codePoints().anyMatch(c -> c > 0xFFFF));
            assertTrue("The emoji is replaced by U+FFFD.", serialised.codePoints().anyMatch(c -> c == 0xFFFD));
        }
    }
}
