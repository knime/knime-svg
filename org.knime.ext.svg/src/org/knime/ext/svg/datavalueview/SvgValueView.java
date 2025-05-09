/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *   Oct 29, 2024 (Paul Bärnreuther): created
 */
package org.knime.ext.svg.datavalueview;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.knime.base.data.xml.SvgValue;
import org.knime.core.webui.node.view.table.datavalue.DataValueView;
import org.knime.core.webui.node.view.table.datavalue.views.image.ImageInitialData;
import org.knime.core.webui.node.view.table.datavalue.views.image.ImageValueView;
import org.w3c.dom.Document;

/**
 * A {@link DataValueView} associated to an {@link SvgValue}
 *
 * @author Paul Bärnreuther
 */
public final class SvgValueView implements ImageValueView {
    private final SvgValue m_value;

    @SuppressWarnings("javadoc")
    public SvgValueView(final SvgValue value) {
        m_value = value;
    }

    @Override
    public ImageInitialData getInitialData() {
        try (var svgSupplier = m_value.getDocumentSupplier()) {
            return new ImageInitialData(getDataFromDocument(svgSupplier.get()), "image/svg+xml");
        }
    }

    private static byte[] getDataFromDocument(final Document doc) {
        DOMSource domSource = new DOMSource(doc);
        TransformerFactory tf = TransformerFactory.newInstance();
        try (StringWriter writer = new StringWriter()) {
            StreamResult result = new StreamResult(writer);
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString().getBytes();
        } catch (TransformerException | IOException ex) {
            throw new IllegalArgumentException("Could not serialize SVG document", ex);
        }
    }
}
