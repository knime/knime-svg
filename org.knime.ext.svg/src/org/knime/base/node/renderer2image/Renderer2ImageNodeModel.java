/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 *   18.12.2010 (meinl): created
 */
package org.knime.base.node.renderer2image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.knime.base.data.xml.SvgCellFactory;
import org.knime.base.data.xml.SvgProvider;
import org.knime.base.data.xml.SvgValueRenderer;
import org.knime.base.node.renderer2image.Renderer2ImageSettings.ImageType;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.DataValueRendererFamily;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 * This is the model for the renderer-to-svg node.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class Renderer2ImageNodeModel extends NodeModel {
    private final Renderer2ImageSettings m_settings = new Renderer2ImageSettings();

    /**
     * Creates a new node model with one input and one output port.
     */
    public Renderer2ImageNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        Renderer2ImageSettings s = new Renderer2ImageSettings();
        s.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        DataTableSpec inSpec = inData[0].getDataTableSpec();
        DataColumnSpec cs = inSpec.getColumnSpec(m_settings.columnName());

        ColumnRearranger crea = createRearranger(inSpec, cs);
        BufferedDataTable outTable = exec.createColumnRearrangeTable(inData[0], crea, exec);
        return new BufferedDataTable[]{outTable};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        if (m_settings.columnName() == null) {
            throw new InvalidSettingsException("No column selected");
        }

        DataColumnSpec cs = inSpecs[0].getColumnSpec(m_settings.columnName());
        if (cs == null) {
            throw new InvalidSettingsException("Column '" + m_settings.columnName() + "' does not exist");
        }

        if (m_settings.rendererDescription() == null) {
            throw new InvalidSettingsException("No renderer selected");
        }

        return new DataTableSpec[]{createRearranger(inSpecs[0], cs).createSpec()};
    }

    private ColumnRearranger createRearranger(final DataTableSpec inSpec, final DataColumnSpec colSpec)
        throws InvalidSettingsException {

        final DataValueRendererFamily renderer = colSpec.getType().getRenderer(colSpec);
        boolean found = false;
        for (String desc : renderer.getRendererDescriptions()) {
            if (desc.equals(m_settings.rendererDescription())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new InvalidSettingsException("Renderer '" + m_settings.rendererDescription()
                + "' does not exist for column '" + m_settings.columnName() + "'");
        }
        renderer.setActiveRenderer(m_settings.rendererDescription());

        String colName = m_settings.columnName() + " rendered with " + m_settings.rendererDescription();
        colName = DataTableSpec.getUniqueColumnName(inSpec, colName);

        final int colIndex = inSpec.findColumnIndex(m_settings.columnName());
        SingleCellFactory cf;
        if (ImageType.Svg.equals(m_settings.imageType())) {
            DataColumnSpecCreator append = new DataColumnSpecCreator(colName, SvgCellFactory.TYPE);
            Dimension prefSize = renderer.getPreferredSize();

            HashMap<String, String> newProps = new HashMap<String, String>();
            newProps.put(SvgValueRenderer.OPTION_KEEP_ASPECT_RATIO, "true");
            newProps.put(SvgValueRenderer.OPTION_PREFERRED_HEIGHT, Integer.toString(prefSize.height));
            newProps.put(SvgValueRenderer.OPTION_PREFERRED_WIDTH, Integer.toString(prefSize.width));
            DataColumnProperties props = new DataColumnProperties(newProps);
            append.setProperties(props);
            cf = new SingleCellFactory(append.createSpec()) {
                @Override
                public DataCell getCell(final DataRow row) {
                    return createSvgCell(row.getCell(colIndex), renderer);
                }
            };
        } else if (ImageType.Png.equals(m_settings.imageType())) {
            DataColumnSpecCreator append = new DataColumnSpecCreator(colName, PNGImageContent.TYPE);
            cf = new SingleCellFactory(append.createSpec()) {
                @Override
                public DataCell getCell(final DataRow row) {
                    try {
                        return createPngCell(row.getCell(colIndex), renderer);
                    } catch (IOException ex) {
                        getLogger().error("Could not create PNG image: " + ex.getMessage(), ex);
                        return DataType.getMissingCell();
                    }
                }
            };
        } else {
            throw new InvalidSettingsException("Unsupported image type: " + m_settings.imageType());
        }
        ColumnRearranger crea = new ColumnRearranger(inSpec);
        crea.append(cf);
        return crea;
    }

    DataCell createSvgCell(final DataCell cell, final DataValueRenderer renderer) {
        Component comp = renderer.getRendererComponent(cell);
        if (comp instanceof SvgProvider) {
            SVGDocument doc = ((SvgProvider)comp).getSvg();
            return SvgCellFactory.create(doc);
        }

        Dimension size = comp.getPreferredSize();
        if ((size.width <= 0) || (size.width <= 0)) {
            size = new Dimension(100, 100);
        }
        comp.setSize(size);

        DOMImplementation domImpl = new SVGDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        Document myFactory = domImpl.createDocument(svgNS, "svg", null);
        SVGGraphics2D g = new SVGGraphics2D(myFactory);
        g.setColor(Color.GREEN);
        g.setSVGCanvasSize(size);

        comp.update(g);

        myFactory.replaceChild(g.getRoot(), myFactory.getDocumentElement());
        return SvgCellFactory.create((SVGDocument)myFactory);
    }

    DataCell createPngCell(final DataCell cell, final DataValueRenderer renderer) throws IOException {
        Component comp = renderer.getRendererComponent(cell);
        if (comp instanceof SvgProvider) {
            SVGDocument doc = ((SvgProvider)comp).getSvg();
            return SvgCellFactory.create(doc);
        }

        Dimension size = comp.getPreferredSize();
        if ((size.width <= 0) || (size.width <= 0)) {
            size = new Dimension(100, 100);
        }
        comp.setSize(size);

        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        // create graphics object to paint in
        Graphics2D graphics = image.createGraphics();
        comp.paint(graphics);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        ImageIO.write(image, "png", bos);
        bos.close();

        return new PNGImageContent(bos.toByteArray()).toImageCell();
    }

}
