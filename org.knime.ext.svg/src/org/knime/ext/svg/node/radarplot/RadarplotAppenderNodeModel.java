/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
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
 * -------------------------------------------------------------------
 *
 * History
 *   26.08.2006 (mb): created
 */
package org.knime.ext.svg.node.radarplot;

import java.io.File;
import java.io.IOException;

import org.knime.base.data.xml.SvgCell;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValueComparator;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowIterator;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Model that attaches a new column holding individual radar plots to each row.
 *
 * @author M. Berthold, University of Konstanz
 * @author Andreas Burger
 */
public class RadarplotAppenderNodeModel extends NodeModel {

    /** Logger to print debug info to. */
    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(RadarplotAppenderNodeModel.class);

    /**
     * The following strings are used by the dialog and the model as key to
     * store settings in the settings object.
     */
    static final String COLUMNRANGEPREFIX = "COL_";

    /*
     * these are the members storing user settings. Use the same settings model
     * (but a new instance) as in the node dialog for that value.
     */
    private ColumnSettingsTable m_rangeModels = null;

    /** Inits a new node model, it will have 1 data input and 1 model output. */
    public RadarplotAppenderNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_rangeModels != null) {
            m_rangeModels.saveSettings(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // validate settings, check for at least 3 attributes
        ColumnSettingsTable cst = new ColumnSettingsTable(COLUMNRANGEPREFIX);
        cst.loadSettingsModel(settings);
        if (cst.getNrSelected() < 3) {
            throw new InvalidSettingsException("At least 3 double "
                    + "columns need to be selected!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_rangeModels = new ColumnSettingsTable(COLUMNRANGEPREFIX);
        m_rangeModels.loadSettingsModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws CanceledExecutionException,
            Exception {
        ExecutionMonitor mon = exec;
        LOGGER.debug("starting execution.");
        boolean needDomainComputing = false;
        DataTableSpec spec = inData[0].getDataTableSpec();
        for (int colIndex = 0; colIndex < spec.getNumColumns(); colIndex++) {
            final DataColumnSpec columnSpec = spec.getColumnSpec(colIndex);
            if (columnSpec.getType().isCompatible(DoubleValue.class)) {
                final DataColumnDomain domain = columnSpec.getDomain();
                if (!domain.hasLowerBound() || !domain.hasUpperBound()) {
                    needDomainComputing = true;
                }
            }
        }
        if (needDomainComputing) {
            mon = exec.createSubProgress(0.5);
            final int colCount = spec.getNumColumns();
            DataCell[] mins = new DataCell[colCount];
            DataCell[] maxs = new DataCell[colCount];
            DataValueComparator[] comparators = new DataValueComparator[colCount];
            for (int i = 0; i < colCount; i++) {
                DataColumnSpec col = spec.getColumnSpec(i);
                if (col.getType().isCompatible(DoubleValue.class)) {
                    mins[i] = DataType.getMissingCell();
                    maxs[i] = DataType.getMissingCell();
                    comparators[i] = col.getType().getComparator();
                } else {
                    mins[i] = null;
                    maxs[i] = null;
                    comparators[i] = null;
                }
            }

            int row = 0;
            final double rowCount = inData[0].getRowCount();
            for (RowIterator it = inData[0].iterator(); it.hasNext(); row++) {
                DataRow r = it.next();
                for (int i = 0; i < colCount; i++) {
                    DataCell c = r.getCell(i);
                    if (!c.isMissing() && mins[i] != null) {
                        if (mins[i].isMissing()) {
                            mins[i] = c;
                            maxs[i] = c;
                            continue; // it was the first row with a valid value
                        }
                        if (comparators[i].compare(c, mins[i]) < 0) {
                            mins[i] = c;
                        }
                        if (comparators[i].compare(c, maxs[i]) > 0) {
                            maxs[i] = c;
                        }
                    }
                }
                mon.checkCanceled();
                mon.setProgress(row / rowCount, "Determining domain, row #"
                        + (row + 1) + " (\"" + r.getKey() + "\")");
            }
            DataColumnSpec[] colSpec = new DataColumnSpec[colCount];
            for (int i = 0; i < colSpec.length; i++) {
                DataColumnSpec original = spec.getColumnSpec(i);
                DataCell min =
                        mins[i] != null && !mins[i].isMissing() ? mins[i]
                                : null;
                DataCell max =
                        maxs[i] != null && !maxs[i].isMissing() ? maxs[i]
                                : null;
                DataColumnDomainCreator domainCreator =
                        new DataColumnDomainCreator(min, max);
                DataColumnSpecCreator specCreator =
                        new DataColumnSpecCreator(original);
                specCreator.setDomain(domainCreator.createDomain());
                colSpec[i] = specCreator.createSpec();
            }
            spec = new DataTableSpec(spec.getName(), colSpec);
            mon = exec.createSubProgress(0.5);
        }
        ColumnRearranger c = createColumnRearranger(spec);
        BufferedDataTable o =
                exec.createColumnRearrangeTable(inData[0], c, mon);
        if (m_adjustedValidRanges) {
            this.setWarningMessage("Entries for 'Valid Range' outside of"
                    + " domain were automatically adjusted.");
        }
        return new BufferedDataTable[]{o};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // this.m_oldSpec = null;
        // this.m_rangeModels = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        // if settings no settings object exist, meaning node has not been
        // configured yet
        if (m_rangeModels == null) {
            m_rangeModels = new ColumnSettingsTable(COLUMNRANGEPREFIX);
            m_rangeModels.setNewSpec(inSpecs[0]);

            this.setWarningMessage("Guessing that you want to use all Doubles");

            // check for valid domains and fail if domains are not provided
            if (m_rangeModels.isProper() <= 0) {
                throw new InvalidSettingsException("Some columns have no "
                        + "valid domain, maybe the previous node is not "
                        + "executed.");
            }

        // else, the settings object exists, meaning the node has been
        // configured some time. The configuration need to be matched against
        // the input data table spec.
        } else {
            ColumnSettingsTable testSettings =
                new ColumnSettingsTable(COLUMNRANGEPREFIX);
            // take over as many settings specified by the user via
            // dialog as possible, ignore additional columns
            testSettings.setNewSpecAndTakeoverSettings(inSpecs[0],
                    m_rangeModels);

            // check for valid and existing domains
            int isProper = 0;
            isProper = testSettings.isProper(true);
            if ((isProper > 0)) {
                // check for missing columns and fail if specified
                if (!testSettings.allColumnAvailable(m_rangeModels)
                        && m_rangeModels.getFailOnMissingCols()) {
                    throw new InvalidSettingsException(
                            "Specified column is missing");
                }

                // need to check if still at least three columns
                // (attributes) are selected and available since input
                // data may have changed
                if (testSettings.getNrSelected() < 3) {
                    throw new InvalidSettingsException("At least 3 double "
                            + "columns need to be selected!");
                }
            } else {
                throw new InvalidSettingsException("Some columns have no "
                        + "valid domain, maybe the previous node is not "
                        + "executed.");
            }
        }
        ColumnRearranger result = createColumnRearranger(inSpecs[0]);
        return new DataTableSpec[]{result.createSpec()};
    }

    private boolean m_adjustedValidRanges = false;

    /**
     * Creates and returns the column rearranger used to create spec and output
     * data table.
     * @param spec The original input spec.
     * @return The column rearranger used to create spec and output data table.
     * @since 2.6
     */
    private ColumnRearranger createColumnRearranger(final DataTableSpec spec) {
        ColumnRearranger result = new ColumnRearranger(spec);
        m_adjustedValidRanges = false;
        final DataColumnSpec newColSpec =
                new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(
                        spec, "Radar Plot"), SvgCell.TYPE).createSpec();

        CellFactory cc = new RadarPlotCellFactory(newColSpec, spec,
                m_rangeModels);
        result.append(cc);
        return result;
    }

    /**
     * Load internals.
     *
     * @param nodeInternDir The intern node directory to load tree from.
     * @param exec Used to report progress or cancel saving.
     * @throws IOException Always, since this method has not been implemented
     *             yet.
     * @see org.knime.core.node.NodeModel
     *      #loadInternals(java.io.File,ExecutionMonitor)
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException {
        // nothing done on purpose.
    }

    /**
     * Save internals.
     *
     * @param nodeInternDir The intern node directory to save table to.
     * @param exec Used to report progress or cancel saving.
     * @throws IOException Always, since this method has not been implemented
     *             yet.
     * @see org.knime.core.node.NodeModel
     *      #saveInternals(java.io.File,ExecutionMonitor)
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException {
        // nothing done on purpose.
    }

}
