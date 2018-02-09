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
import java.util.Collection;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.knime.base.data.xml.SvgBlobCell;
import org.knime.base.data.xml.SvgCell;
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
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.image.png.PNGImageBlobCell;
import org.knime.core.data.image.png.PNGImageCell;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.DataValueRendererFactory;
import org.knime.core.data.util.AutocloseableSupplier;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.Pointer;
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

        MyColumnRearranger crea = createRearranger(inSpec, cs);
        BufferedDataTable outTable = exec.createColumnRearrangeTable(inData[0], crea, exec);
        outTable = exec.createSpecReplacerTable(outTable, fixPropertiesInSpec(outTable.getSpec(), crea));
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

        if (!m_settings.replaceColumn()) {
            if (inSpecs[0].getColumnSpec(m_settings.newColumnName()) != null) {
                throw new InvalidSettingsException("Output column '" + m_settings.newColumnName()
                    + "' already exists in input table.");
            } else if ((m_settings.newColumnName() == null) || m_settings.newColumnName().isEmpty()) {
                // this is mainly for backwards compatibility because the configurable output column name was
                // added in 2.9
                String colName = m_settings.columnName() + " rendered with " + m_settings.rendererDescription();
                m_settings.newColumnName(colName);
            }
        }

        return new DataTableSpec[]{createRearranger(inSpecs[0], cs).createSpec()};
    }

    private MyColumnRearranger createRearranger(final DataTableSpec inSpec, final DataColumnSpec colSpec)
        throws InvalidSettingsException {

        Collection<DataValueRendererFactory> rendererFactories = colSpec.getType().getRendererFactories();

        final Pointer<DataValueRendererFactory> activeRendererPointer = new Pointer<>();
        for (DataValueRendererFactory f: rendererFactories) {
            if (f.getDescription().equals(m_settings.rendererDescription())) {
                activeRendererPointer.set(f);
                break;
            }
        }
        if (activeRendererPointer.get() == null) {
            throw new InvalidSettingsException("Renderer '" + m_settings.rendererDescription()
                + "' does not exist for column '" + m_settings.columnName() + "'");
        }


        String colName = m_settings.replaceColumn() ? m_settings.columnName()
            : DataTableSpec.getUniqueColumnName(inSpec, m_settings.newColumnName());

        final LazyInitializer<DataValueRenderer> rendererInitializer = new LazyInitializer<DataValueRenderer>() {
            @Override
            protected DataValueRenderer initialize() {
                return activeRendererPointer.get().createRenderer(colSpec);
            }
        };
        final int colIndex = inSpec.findColumnIndex(m_settings.columnName());
        SingleCellFactory cf;
        if (ImageType.Svg.equals(m_settings.imageType())) {
            DataColumnSpecCreator append = new DataColumnSpecCreator(colName, SvgCellFactory.TYPE);
            cf = new SingleCellFactory(append.createSpec()) {
                @Override
                public DataCell getCell(final DataRow row) {
                    try {
                        return createSvgCell(row.getCell(colIndex), rendererInitializer.get());
                    } catch (ConcurrentException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } else if (ImageType.Png.equals(m_settings.imageType())) {
            DataColumnSpecCreator append = new DataColumnSpecCreator(colName, PNGImageContent.TYPE);
            cf = new SingleCellFactory(append.createSpec()) {
                @Override
                public DataCell getCell(final DataRow row) {
                    try {
                        return createPngCell(row.getCell(colIndex), rendererInitializer.get());
                    } catch (ConcurrentException e) {
                        throw new RuntimeException(e);
                    } catch (IOException ex) {
                        getLogger().error("Could not create PNG image: " + ex.getMessage(), ex);
                        return DataType.getMissingCell();
                    }
                }
            };
        } else {
            throw new InvalidSettingsException("Unsupported image type: " + m_settings.imageType());
        }
        MyColumnRearranger crea = new MyColumnRearranger(inSpec, colName, rendererInitializer);
        if (m_settings.replaceColumn()) {
            crea.replace(cf, m_settings.columnName());
        } else {
            crea.append(cf);
        }

        return crea;
    }

    private DataTableSpec fixPropertiesInSpec(final DataTableSpec spec, final MyColumnRearranger rearranger)
            throws ConcurrentException {
        if (!ImageType.Svg.equals(m_settings.imageType())) {
            return spec;
        }
        DataTableSpecCreator tableSpecCreator = new DataTableSpecCreator(spec);

        int columnIndex = spec.findColumnIndex(rearranger.getColName());
        assert columnIndex >= 0;
        DataColumnSpec oldColSpec = spec.getColumnSpec(columnIndex);

        DataColumnSpecCreator newColSpecCreator = new DataColumnSpecCreator(oldColSpec);
        Dimension prefSize = rearranger.getRenderer().getPreferredSize();

        HashMap<String, String> newProps = new HashMap<String, String>();
        newProps.put(SvgValueRenderer.OPTION_KEEP_ASPECT_RATIO, "true");
        newProps.put(SvgValueRenderer.OPTION_PREFERRED_HEIGHT, Integer.toString(prefSize.height));
        newProps.put(SvgValueRenderer.OPTION_PREFERRED_WIDTH, Integer.toString(prefSize.width));
        DataColumnProperties props = new DataColumnProperties(newProps);
        newColSpecCreator.setProperties(props);
        tableSpecCreator.replaceColumn(columnIndex, newColSpecCreator.createSpec());
        return tableSpecCreator.createSpec();
    }

    /**
     * Creates a new SVG cell using the given renderer and the given data cell.
     *
     * @param cell a data cell
     * @param renderer a renderer
     * @return a new {@link SvgCell} or {@link SvgBlobCell}
     */
    DataCell createSvgCell(final DataCell cell, final DataValueRenderer renderer) {
        if (cell.isMissing()) {
            return cell;
        }
        Component comp = renderer.getRendererComponent(cell);
        if (comp instanceof SvgProvider) {

            try (AutocloseableSupplier<SVGDocument> supplier = ((SvgProvider)comp).getSvgSupplier()) {
                SVGDocument doc = supplier.get();
                return SvgCellFactory.create(doc);
            }
        }

        Dimension size = comp.getPreferredSize();
        if ((size.width <= 0) || (size.height <= 0)) {
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

    /**
     * Creates a new PNG cell using the given renderer and the given data cell.
     *
     * @param cell a data cell
     * @param renderer a renderer
     * @return a new {@link PNGImageCell} or {@link PNGImageBlobCell}
     * @throws IOException if an I/O error occurs while creating the image
     */
    DataCell createPngCell(final DataCell cell, final DataValueRenderer renderer) throws IOException {
        if (cell.isMissing()) {
            return cell;
        }
        Component comp = renderer.getRendererComponent(cell);

        Dimension size = m_settings.pngSize();
        if ((size.width <= 0) || (size.height <= 0)) {
            size = new Dimension(100, 100);
        }
        comp.setSize(size);

        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        // create graphics object to paint in
        Graphics2D graphics = image.createGraphics();
        comp.paint(graphics);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
        ImageIO.write(image, "png", bos);
        bos.close();

        return new PNGImageContent(bos.toByteArray()).toImageCell();
    }

    private static final class MyColumnRearranger extends ColumnRearranger {

        private String m_colName;
        private LazyInitializer<DataValueRenderer> m_rendererInitializer;

        /** @param original forwarded to super.
         * @param rendererInitializer
         * @param colName */
        MyColumnRearranger(final DataTableSpec original, final String colName,
            final LazyInitializer<DataValueRenderer> rendererInitializer) {
            super(original);
            m_colName = colName;
            m_rendererInitializer = rendererInitializer;
        }

        /** @return the colName of the modified/new column. */
        String getColName() {
            return m_colName;
        }

        /** @return the renderer used (lazy initialized - don't call on configure()).
         * @throws ConcurrentException as per commons.lang API - not actually thrown. */
        DataValueRenderer getRenderer() throws ConcurrentException {
            return m_rendererInitializer.get();
        }

    }

}
