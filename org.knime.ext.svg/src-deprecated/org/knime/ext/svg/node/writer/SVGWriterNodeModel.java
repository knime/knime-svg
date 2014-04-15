/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by
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
 *   29.10.2010 (meinl): created
 */
package org.knime.ext.svg.node.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.knime.base.data.xml.SvgValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.w3c.dom.Document;

/**
 * This is the model for the SVG writer node. It takes an SVG column from the
 * input table and writes each cell into a separate file in the output
 * directory.
 *
 * @author Thorsten Meinl, University of Konstanz
 * @deprecated replaced by the ImageColWriter node
 */
@Deprecated
public class SVGWriterNodeModel extends NodeModel {
    private final SettingsModelString m_svgColumn = new SettingsModelString(
            "svgColumn", null);

    private final SettingsModelString m_directory = new SettingsModelString(
            "directory", null);

    private final SettingsModelBoolean m_overwrite = new SettingsModelBoolean(
            "overwrite", false);

    /**
     * Creates a new model with one input port and not output port.
     */
    public SVGWriterNodeModel() {
        super(1, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        if (m_directory.getStringValue() == null) {
            throw new InvalidSettingsException("No output directory selected");
        }

        File dir = new File(m_directory.getStringValue());
        if (!dir.exists()) {
            throw new InvalidSettingsException("Directory '"
                    + dir.getAbsolutePath() + "' does not exist");
        }
        if (!dir.isDirectory()) {
            throw new InvalidSettingsException("'" + dir.getAbsolutePath()
                    + "' is not a directory");
        }

        if (m_svgColumn.getStringValue() == null) {
            for (DataColumnSpec cs : inSpecs[0]) {
                if (cs.getType().isCompatible(SvgValue.class)) {
                    m_svgColumn.setStringValue(cs.getName());
                    break;
                }
            }
            if (m_svgColumn.getStringValue() == null) {
                throw new InvalidSettingsException(
                        "Input table does not contain an SVG column");
            }
        }

        int colIndex = inSpecs[0].findColumnIndex(m_svgColumn.getStringValue());
        if (colIndex == -1) {
            throw new InvalidSettingsException("SVG column '"
                    + m_svgColumn.getStringValue() + "' does not exist");
        }
        if (!inSpecs[0].getColumnSpec(colIndex).getType()
                .isCompatible(SvgValue.class)) {
            throw new InvalidSettingsException("Column '"
                    + m_svgColumn.getStringValue()
                    + "' does not contain SVG images");
        }

        return new DataTableSpec[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        double max = inData[0].getRowCount();
        int count = 0;

        File dir = new File(m_directory.getStringValue());

        final int colIndex =
                inData[0].getDataTableSpec().findColumnIndex(
                        m_svgColumn.getStringValue());

        SVGTranscoder transcoder = new SVGTranscoder();

        for (DataRow row : inData[0]) {
            exec.checkCanceled();
            exec.setProgress(count++ / max, "Writing " + row.getKey() + ".svg");

            File svgFile = new File(dir, row.getKey() + ".svg");
            if (!m_overwrite.getBooleanValue() && svgFile.exists()) {
                throw new IOException("File '" + svgFile.getAbsolutePath()
                        + "' already exists");
            }

            Document doc = ((SvgValue)row.getCell(colIndex)).getDocument();

            Writer fileOut =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(svgFile), Charset.forName("UTF-8")));
            TranscoderOutput out = new TranscoderOutput(fileOut);
            TranscoderInput in = new TranscoderInput(doc);
            transcoder.transcode(in, out);
            fileOut.close();
        }

        return new BufferedDataTable[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_svgColumn.saveSettingsTo(settings);
        m_directory.saveSettingsTo(settings);
        m_overwrite.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_svgColumn.validateSettings(settings);
        m_directory.validateSettings(settings);
        m_overwrite.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_svgColumn.loadSettingsFrom(settings);
        m_directory.loadSettingsFrom(settings);
        m_overwrite.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }
}
