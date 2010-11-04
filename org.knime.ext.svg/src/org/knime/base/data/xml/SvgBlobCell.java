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

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.SoftReference;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.css.engine.value.svg.SVGValue;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.util.XMLResourceDescriptor;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.BlobDataCell;
import org.knime.core.data.image.ImageContent;
import org.knime.core.data.image.ImageValue;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.SAXException;

/**
 * {@link BlobDataCell} implementation that encapsulated SVG documents.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class SvgBlobCell extends BlobDataCell implements SvgValue, StringValue,
        ImageValue {
    private static class SvgSerializer implements
            DataCellSerializer<SvgBlobCell> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final SvgBlobCell cell,
                final DataCellDataOutput output) throws IOException {
            try {
                output.writeUTF(cell.getStringValue());
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IOException("Could not serialize SVG", ex);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public SvgBlobCell deserialize(final DataCellDataInput input)
                throws IOException {
            String s = input.readUTF();
            return new SvgBlobCell(s);
        }
    }

    private final static SvgSerializer SERIALIZER = new SvgSerializer();

    private SoftReference<String> m_xmlString;

    private SvgImageContent m_content;

    private boolean m_isNormalized;

    /**
     * Returns the serializer for SVG cells.
     *
     * @return a serializer
     */
    public static DataCellSerializer<SvgBlobCell> getCellSerializer() {
        return SERIALIZER;
    }

    /**
     * Returns the preferred value class for SVG cells which is {@link SVGValue}
     * .
     *
     * @return the preferred value class
     */
    public static Class<? extends DataValue> getPreferredValueClass() {
        return SvgValue.class;
    }

    /**
     * Creates a new SVGCell by parsing the passed string. It must contain a
     * valid SVG document, including all XML headers.
     *
     * @param xmlString an SVG document
     * @throws IOException if an error occurs while reading the XML string.
     */
    public SvgBlobCell(final String xmlString) throws IOException {
        m_xmlString = new SoftReference<String>(xmlString);
        String parserClass = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parserClass);

        m_content =
                new SvgImageContent(f.createSVGDocument(null, new StringReader(
                        xmlString)));
    }

    /**
     * Creates a new SVGCell by using the passed SVG document.
     *
     * @param doc an SVG document
     */
    public SvgBlobCell(final SVGDocument doc) {
        m_content = new SvgImageContent(doc);
    }

    /**
     * {@inheritDoc}
     *
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    @Override
    public SVGDocument getDocument() throws SAXException, IOException,
            ParserConfigurationException {
        return m_content.getSvgDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getStringValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        SvgBlobCell cell = (SvgBlobCell)dc;

        try {
            String s1, s2;
            if (this.m_isNormalized && (this.m_xmlString.get() != null)) {
                s1 = this.m_xmlString.get();
            } else {
                s1 = SvgImageContent.serialize(getDocument());
            }

            if (cell.m_isNormalized && (cell.m_xmlString.get() != null)) {
                s2 = cell.m_xmlString.get();
            } else {
                s2 = SvgImageContent.serialize(cell.getDocument());
            }
            return s1.equals(s2);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Cannot create string representation of XML document", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (m_isNormalized && (m_xmlString.get() != null)) {
            return m_xmlString.get().hashCode();
        }
        try {
            return SvgImageContent.serialize(getDocument()).hashCode();
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Cannot create string representation of XML document", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue() {
        if ((m_xmlString == null) || (m_xmlString.get() == null)) {
            try {
                m_xmlString =
                        new SoftReference<String>(
                                SvgImageContent.serialize(m_content
                                        .getSvgDocument()));
                m_isNormalized = true;
            } catch (TranscoderException ex) {
                throw new RuntimeException(
                        "Cannot create string representation of XML document",
                        ex);
            }
        }
        return m_xmlString.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageContent getImageContent() {
        return m_content;
    }
}
