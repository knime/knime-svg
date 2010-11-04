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
 *   03.11.2010 (meinl): created
 */
package org.knime.base.data.xml;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.knime.core.data.DataCell;
import org.knime.core.data.image.ImageContent;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 * {@link ImageContent} implementation for SVG images.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class SvgImageContent implements ImageContent {
    private static final SVGTranscoder TRANSCODER = new SVGTranscoder();

    private SVGDocument m_doc;

    private Dimension m_preferredSize;

    private static final UserAgent UA = new UserAgentAdapter();

    /**
     * Creates a new SVG image content containing the passed SVG document.
     *
     * @param doc an SVG document
     */
    public SvgImageContent(final SVGDocument doc) {
        m_doc = doc;
    }

    /**
     * Creates a new SVG image content by reading the XML from the passed
     * input stream.
     *
     * @param in an input stream
     * @throws IOException if an I/O error occurs
     */
    public SvgImageContent(final InputStream in) throws IOException {
        String parserClass = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parserClass);
        m_doc = f.createSVGDocument(null, in);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(final Graphics2D g, final int width, final int height) {
        Rectangle componentBounds = new Rectangle(new Dimension(width, height));
        SvgValueRenderer.paint(m_doc, g, componentBounds, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        if (m_preferredSize == null) {
            GVTBuilder gvtBuilder = new GVTBuilder();
            BridgeContext bridgeContext = new BridgeContext(UA);
            GraphicsNode gvtRoot = gvtBuilder.build(bridgeContext, m_doc);

            m_preferredSize =
                    new Dimension((int)gvtRoot.getBounds().getWidth(),
                            (int)gvtRoot.getBounds().getHeight());
        }
        return m_preferredSize;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell toImageCell() {
        return new SvgCell(m_doc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final OutputStream out) throws IOException {
        TranscoderOutput tout = new TranscoderOutput(out);
        TranscoderInput in = new TranscoderInput(m_doc);
        try {
            TRANSCODER.transcode(in, tout);
        } catch (TranscoderException ex) {
            throw new IOException(ex);
        }
    }

    SVGDocument getSvgDocument() {
        return m_doc;
    }

    static String serialize(final Document doc) throws TranscoderException {
        StringWriter buffer = new StringWriter(1024);
        TranscoderOutput out = new TranscoderOutput(buffer);

        TranscoderInput in = new TranscoderInput(doc);
        TRANSCODER.transcode(in, out);
        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        Dimension dim = getPreferredSize();
        return "SVG " + dim.width + " x " + dim.height;
    }
}
