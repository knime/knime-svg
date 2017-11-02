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
 * --------------------------------------------------------------------
 *
 * History
 *   03.07.2007 (cebron): created
 */
package org.knime.base.node.preproc.colconvert.stringtosvg;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;

/**
 * Model for the String to Svg node.
 *
 * @author Marcel Hanser
 */
final class StringToSvgNodeModel extends NodeModel {

    /** Node Logger of this class. */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(StringToSvgNodeModel.class);

    /** The included columns. */
    private StringToSvgConfig m_colConfig = new StringToSvgConfig();

    /**
     * Constructor with one in and one out.
     */
    StringToSvgNodeModel() {
        super(1, 1);
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        ColumnRearranger createColRearranger = createColRearranger(inSpecs[0], new AtomicInteger());
        return new DataTableSpec[]{createColRearranger.createSpec()};
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        AtomicInteger failCount = new AtomicInteger();
        ColumnRearranger colRe = createColRearranger(inData[0].getDataTableSpec(), failCount);
        BufferedDataTable resultTable = exec.createColumnRearrangeTable(inData[0], colRe, exec);

        int rowCount = resultTable.getRowCount();
        int fail = failCount.get();

        if (fail > 0) {
            setWarningMessage("Failed to read " + fail + " of " + rowCount + " files");
        }

        return new BufferedDataTable[]{resultTable};
    }

    private ColumnRearranger createColRearranger(final DataTableSpec spec, final AtomicInteger failCounter)
        throws InvalidSettingsException {

        StringBuilder warnings = new StringBuilder();

        if (m_colConfig.getImageColName() == null) {
            // throws ISE
            m_colConfig.guessDefaults(spec);
        }

        String imageColName = m_colConfig.getImageColName();
        String newColName = m_colConfig.getNewColumnName();

        final int colIndex = spec.findColumnIndex(m_colConfig.getImageColName());
        CheckUtils.checkSetting(colIndex >= 0, "Column: '%s' does not exist anymore.", imageColName);
        final DataColumnSpec colSpec = spec.getColumnSpec(colIndex);
        CheckUtils.checkSetting(colSpec.getType().isCompatible(StringValue.class),
            "Selected column '%s' is not string/xml-compatible", imageColName);

        DataColumnSpecCreator colSpecCreator;

        if (newColName != null) {
            String newName = DataTableSpec.getUniqueColumnName(spec, newColName);
            colSpecCreator = new DataColumnSpecCreator(newName, SvgCell.TYPE);
        } else {
            colSpecCreator = new DataColumnSpecCreator(colSpec);
            colSpecCreator.setType(SvgCell.TYPE);
            colSpecCreator.removeAllHandlers();
            colSpecCreator.setDomain(null);
        }

        DataColumnSpec outColumnSpec = colSpecCreator.createSpec();
        ColumnRearranger rearranger = new ColumnRearranger(spec);
        CellFactory fac = new SingleCellFactory(outColumnSpec) {
            @Override
            public DataCell getCell(final DataRow row) {
                DataCell cell = row.getCell(colIndex);
                if (cell.isMissing()) {
                    return DataType.getMissingCell();
                } else {
                    String image = ((StringValue)cell).getStringValue();
                    try {
                        SvgImageContent svgImageContent =
                            new SvgImageContent(new ByteArrayInputStream(image.getBytes("UTF-8")));
                        return svgImageContent.toImageCell();
                    } catch (Exception e) {
                        if (m_colConfig.isFailOnInvalid()) {
                            if (e instanceof RuntimeException) {
                                throw (RuntimeException)e;
                            } else {
                                throw new RuntimeException(e.getMessage(), e);
                            }
                        } else {
                            String message =
                                "Failed to read image content from " + "\"" + image + "\": " + e.getMessage();
                            LOGGER.warn(message, e);
                            failCounter.incrementAndGet();
                            return DataType.getMissingCell();
                        }
                    }
                }
            }
        };
        if (newColName == null) {
            rearranger.replace(fac, colIndex);
        } else {
            rearranger.append(fac);
        }
        if (warnings.length() > 0) {
            setWarningMessage(warnings.toString());
        }

        return rearranger;
    }

    @Override
    protected void reset() {
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colConfig = new StringToSvgConfig();
        m_colConfig.loadConfigurationInModel(settings);
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_colConfig != null) {
            m_colConfig.saveConfiguration(settings);
        }
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colConfig = new StringToSvgConfig();
        m_colConfig.loadConfigurationInModel(settings);
    }

    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
    }

}
