/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.SoftReference;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.util.XMLResourceDescriptor;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataTypeRegistry;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.BlobDataCell;
import org.knime.core.data.image.ImageContent;
import org.knime.core.data.xml.util.XmlDomComparer;
import org.w3c.dom.svg.SVGDocument;

/**
 * {@link BlobDataCell} implementation that encapsulated SVG documents.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
@SuppressWarnings("serial")
public class SvgBlobCell extends BlobDataCell implements SvgValue, StringValue {
    /**
     * Serializer for {@link SvgBlobCell}s.
     *
     * @noreference This class is not intended to be referenced by clients.
     */
    public static final class SvgSerializer implements DataCellSerializer<SvgBlobCell> {
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

    private SoftReference<String> m_xmlString;

    private final SvgImageContent m_content;

    private boolean m_isNormalized;

    /**
     * Returns the serializer for SVG cells.
     *
     * @return a serializer
     * @deprecated use {@link DataTypeRegistry#getSerializer(Class)} instead
     */
    @Deprecated
    public static DataCellSerializer<SvgBlobCell> getCellSerializer() {
        return new SvgSerializer();
    }

    /**
     * Creates a new SVGCell by parsing the passed string. It must contain a
     * valid SVG document, including all XML headers.
     *
     * Please consider using {@link SvgCellFactory#create(String)} instead of
     * this constructor as the latter dynamically decides if a in-table cell or
     * a blob cell is created (depending on the size).
     *
     * @param xmlString an SVG document
     * @throws IOException if an error occurs while reading the XML string.
     * @deprecated use {@link SvgCellFactory#create(String)} instead
     */
    @Deprecated
    public SvgBlobCell(final String xmlString) throws IOException {
        m_xmlString = new SoftReference<String>(xmlString);
        String parserClass = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parserClass);

        m_content =
                new SvgImageContent(f.createSVGDocument(null, new StringReader(
                        xmlString)), false);
    }

    /**
     * Creates a new SVGCell by using the passed SVG document.
     *
     * Please consider using {@link SvgCellFactory#create(SVGDocument)} instead
     * of this constructor as the latter dynamically decides if a in-table cell
     * or a blob cell is created (depending on the size).
     *
     * @param doc an SVG document
     * @deprecated use {@link SvgCellFactory#create(SVGDocument)} instead
     */
    @Deprecated
    public SvgBlobCell(final SVGDocument doc) {
        m_content = new SvgImageContent(doc, true);
    }


    SvgBlobCell(final InputStream is) throws IOException {
        String parserClass = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parserClass);

        m_content = new SvgImageContent(f.createSVGDocument(null, is), false);
    }


    SvgBlobCell(final Reader reader) throws IOException {
        String parserClass = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parserClass);

        m_content = new SvgImageContent(f.createSVGDocument(null, reader), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SVGDocument getDocument() {
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
            final String thisString = this.m_xmlString.get();
            if (this.m_isNormalized && (thisString != null)) {
                s1 = thisString;
            } else {
                s1 = SvgImageContent.serialize(getDocument());
            }

            final String cellString = cell.m_xmlString.get();
            if (cell.m_isNormalized && (cellString != null)) {
                s2 = cellString;
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
    protected boolean equalContent(final DataValue otherValue) {
        return SvgValue.equalContent(this, (SvgValue)otherValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return XmlDomComparer.hashCode(getDocument(), SvgCell.SVG_XML_CUSTOMIZER);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImageExtension() {

        return "svg";
    }
}
