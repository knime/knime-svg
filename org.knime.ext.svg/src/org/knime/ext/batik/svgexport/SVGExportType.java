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
 *   Aug 29, 2007 (wiswedel): created
 */
package org.knime.ext.batik.svgexport;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.JComponent;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.knime.core.node.NodeViewExport.ExportType;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * SVG export type for {@link org.knime.core.node.NodeView} objects. It will be
 * added to the list of available export types in
 * {@link org.knime.core.node.NodeViewExport} on plugin startup.
 *
 * @author Bernd Wiswedel, University of Konstanz
 */
public class SVGExportType implements ExportType {

    /** {@inheritDoc} */
    @Override
    public void export(final File destination, final Component cont,
            final int width, final int height) throws IOException {
        // Get a DOMImplementation.
        DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document myFactory = domImpl.createDocument(svgNS, "svg", null);

        // need to switch off double buffering, otherwise the svg file only
        // contains an image
        boolean isDoubleBuffered = false;
        if (cont instanceof JComponent) {
            isDoubleBuffered = ((JComponent)cont).isDoubleBuffered();
            if (isDoubleBuffered) {
                setDoubleBufferedRecursively((JComponent)cont, false);
            }
        }
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(myFactory);
        ctx.setComment("Generated by KNIME with Batik SVG Generator");
        ctx.setEmbeddedFontsOn(true);
        SVGGraphics2D g2d = new SVGGraphics2D(ctx, false);
        g2d.setSVGCanvasSize(cont.getSize());
        cont.update(g2d);
        if (isDoubleBuffered) {
            setDoubleBufferedRecursively((JComponent)cont, true);
        }

        boolean useCSS = true; // we want to use CSS style attributes
        FileOutputStream fileOut = new FileOutputStream(destination);
        Writer out = new OutputStreamWriter(fileOut, "UTF-8");
        g2d.stream(out, useCSS);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "SVG - Scalable Vector Graphics";
    }

    /** {@inheritDoc} */
    @Override
    public String getFileSuffix() {
        return "svg";
    }

    private void setDoubleBufferedRecursively(final JComponent c,
            final boolean isDoubleBuffered) {
        for (Component child : c.getComponents()) {
            if (child instanceof JComponent) {
                setDoubleBufferedRecursively((JComponent)child,
                        isDoubleBuffered);
            }
        }
        c.setDoubleBuffered(isDoubleBuffered);
    }
}
