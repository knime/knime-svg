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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.container.BlobDataCell;
import org.knime.core.node.NodeLogger;
import org.w3c.dom.svg.SVGDocument;

/**
 * Cell factory that creates SVG cells. Depending on the size either normal
 * {@link DataCell}s or {@link BlobDataCell}s are created.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class SvgCellFactory {
    /**
     * Minimum size for blobs in bytes. That is, if a given string is at least
     * as large as this value, it will be represented by a blob cell
     */
    public static final int DEF_MIN_BLOB_SIZE_IN_BYTES = 8 * 1024;

    private static final int MIN_BLOB_SIZE_IN_BYTES;

    static {
        int size = DEF_MIN_BLOB_SIZE_IN_BYTES;
        String envVar = "org.knime.xmlminblobsize";
        String property = System.getProperty(envVar);
        if (property != null) {
            String s = property.trim();
            int multiplier = 1;
            if (s.endsWith("m") || s.endsWith("M")) {
                s = s.substring(0, s.length() - 1);
                multiplier = 1024 * 1024;
            } else if (s.endsWith("k") || s.endsWith("K")) {
                s = s.substring(0, s.length() - 1);
                multiplier = 1024;
            }
            try {
                int newSize = Integer.parseInt(s);
                if (newSize < 0) {
                    throw new NumberFormatException("Size < 0" + newSize);
                }
                size = newSize * multiplier;
                NodeLogger.getLogger(SvgCellFactory.class).debug(
                        "Setting min blob size for XML cells to " + size
                                + " bytes");
            } catch (NumberFormatException e) {
                NodeLogger.getLogger(SvgCellFactory.class).warn(
                        "Unable to parse property " + envVar
                                + ", using default", e);
            }
        }
        MIN_BLOB_SIZE_IN_BYTES = size;
    }

    /** Type for CML cells. */
    public static final DataType TYPE = DataType.getType(SvgCell.class);

    /** Don't instantiate this class. */
    private SvgCellFactory() {

    }

    /**
     * Factory method to create {@link DataCell} representing SVG documents.
     * The returned cell is either of type {@link SvgCell} (for small strings)
     * or {@link SvgBlobCell} (otherwise, default threshold is
     * {@value #DEF_MIN_BLOB_SIZE_IN_BYTES} bytes or larger).
     *
     * @param string String representing of the SVG document
     * @return DataCell representing the SVG document
     * @throws IOException if an error occurs while reading the XML string
     * @throws NullPointerException if argument is null
     */
    public static DataCell create(final String string) throws IOException {
        if (string == null) {
            throw new NullPointerException("SVG must not be null");
        }
        if (string.length() >= MIN_BLOB_SIZE_IN_BYTES) {
            return new SvgBlobCell(string);
        } else {
            return new SvgCell(string);
        }
    }

    /**
     * Factory method to create {@link DataCell} representing SVG documents.
     * The returned cell is either of type {@link SvgCell} (for small strings)
     * or {@link SvgBlobCell} (otherwise, default threshold is
     * {@value #DEF_MIN_BLOB_SIZE_IN_BYTES} bytes or larger).
     *
     * @param doc the SVG document
     * @return DataCell representing the SVG document
     * @throws NullPointerException if argument is null
     */
    public static DataCell create(final SVGDocument doc) {
        if (doc == null) {
            throw new NullPointerException("SVG must not be null");
        }
        // TODO add a reasonable size check
        return new SvgBlobCell(doc);
    }
}
