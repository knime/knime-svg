/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 * -------------------------------------------------------------------
 *
 * History
 *   26.08.2006 (mb): created
 */
package org.knime.ext.svg.node.radarplot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.knime.base.data.xml.SvgCell;
import org.knime.core.data.DataCell;
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
import org.knime.core.node.NotConfigurableException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

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
        m_rangeModels.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO
        // m_rangeModels[i].validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_rangeModels = new ColumnSettingsTable(COLUMNRANGEPREFIX);
        m_rangeModels.loadSettings(settings);
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
            if (spec.getColumnSpec(colIndex).getType()
                    .isCompatible(DoubleValue.class)) {
                if (!spec.getColumnSpec(colIndex).getDomain().hasLowerBound()
                        || !spec.getColumnSpec(colIndex).getDomain()
                                .hasUpperBound()) {
                    needDomainComputing = true;
                }
                if (!spec.getColumnSpec(colIndex).getDomain().getLowerBound()
                        .getType().isCompatible(DoubleValue.class)) {
                    throw new Exception("Domain Minimum is not a double!");
                }
                if (!spec.getColumnSpec(colIndex).getDomain().getUpperBound()
                        .getType().isCompatible(DoubleValue.class)) {
                    throw new Exception("Domain Maximum is not a double!");
                }
            }
        }
        if (needDomainComputing) {
            mon = exec.createSubProgress(0.5);
            final int colCount = spec.getNumColumns();
            DataCell[] mins = new DataCell[colCount];
            DataCell[] maxs = new DataCell[colCount];
            DataValueComparator[] comparators =
                    new DataValueComparator[colCount];
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
        if (m_rangeModels == null) {

            m_rangeModels = new ColumnSettingsTable(COLUMNRANGEPREFIX);
            try {
                m_rangeModels.setNewSpec(inSpecs[0]);
            } catch (NotConfigurableException nce) {
                throw new InvalidSettingsException("Could not load"
                        + "configuration!");
            }
            this.setWarningMessage("Guessing that you want to use all Doubles");
        } else {
            ColumnSettingsTable testTable =
                    new ColumnSettingsTable(COLUMNRANGEPREFIX);
            try {
                int isProper = 0;
                testTable.setNewSpec(inSpecs[0]);
                boolean subset = false, tesbus = false, equal = false;
                isProper = testTable.isProper();
                if ((isProper >= 0)) {
                    if (isProper == 0) {
                        this.setWarningMessage("Some columns disabled because "
                                + "they contain only one value");
                    }
                    if (m_rangeModels.isSimilarTo(testTable, null)) {
                        this.setWarningMessage("Additional columns detected");
                        subset = true;
                    }
                    if (testTable.isSimilarTo(m_rangeModels, null)) {
                        this.setWarningMessage("Some columns are missing");
                        tesbus = true;
                    }
                    if (m_rangeModels.equals(testTable)) {
                        equal = true;
                    }
                    if (!subset && !tesbus && !equal) {
                        throw new InvalidSettingsException(
                                "New input Table found");

                    }
                } else {
                    throw new InvalidSettingsException("Some columns have no "
                            + "valid domain, maybe the previous node is not "
                            + "executed.");
                }
            } catch (NotConfigurableException nce) {
                throw new InvalidSettingsException("Could not load"
                        + "configuration");
            }
        }
        if (m_rangeModels.getNrSelected() < 3) {
            throw new InvalidSettingsException("At least 3 double columns are "
                    + "required");
        }
        ColumnRearranger result = createColumnRearranger(inSpecs[0]);
        return new DataTableSpec[]{result.createSpec()};
    }

    private boolean m_adjustedValidRanges = false;

    private ColumnRearranger createColumnRearranger(final DataTableSpec spec) {
        m_adjustedValidRanges = false;
        final DataColumnSpec newColSpec =
                new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(
                        spec, "Radar Plot"), SvgCell.TYPE).createSpec();
        final int _dim = m_rangeModels.getNrSelected();
        final int[] _indices = new int[_dim];
        final double[] _min = new double[_dim];
        final double[] _max = new double[_dim];
        final double[] _validMin = new double[_dim];
        final double[] _validMax = new double[_dim];
        final String[] _labels = new String[_dim];
        int nrSelCol = 0; // keep track of selected (and double) columns
        for (int i = 0; i < Math.min(spec.getNumColumns(),
                m_rangeModels.getColumnCount()); i++) {
            if (m_rangeModels.isSelected(i)) {
                // retrieve min/max values. We know that they exist
                // because we precomputed them in the execute if
                // necessary!
                _validMin[nrSelCol] = Double.NaN;
                _validMax[nrSelCol] = Double.NaN;
                try {
                    if (spec.getColumnSpec(i).getDomain().hasLowerBound()) {
                        _validMin[nrSelCol] =
                                ((DoubleValue)spec.getColumnSpec(i).getDomain()
                                        .getLowerBound()).getDoubleValue();
                    }
                    if (spec.getColumnSpec(i).getDomain().hasUpperBound()) {
                        _validMax[nrSelCol] =
                                ((DoubleValue)spec.getColumnSpec(i).getDomain()
                                        .getUpperBound()).getDoubleValue();
                    }
                } catch (Exception e) {
                    assert false;
                }
                assert !Double.isNaN(_validMin[nrSelCol]);
                assert !Double.isNaN(_validMax[nrSelCol]);
                // we know that min/max are set properly so we can !-!
                // force valid ranges into this range as well:
                _min[nrSelCol] = m_rangeModels.getMin(i);
                _max[nrSelCol] = m_rangeModels.getMax(i);
                if (_validMin[nrSelCol] > _min[nrSelCol]) {
                    _min[nrSelCol] = _validMin[nrSelCol];
                    // m_adjustedValidRanges = true;
                }
                if (_validMax[nrSelCol] < _max[nrSelCol]) {
                    _max[nrSelCol] = _validMax[nrSelCol];
                    // m_adjustedValidRanges = true;
                }
                _labels[nrSelCol] = spec.getColumnSpec(i).getName();
                _indices[nrSelCol] = i;
                nrSelCol++;
            }
        }

        CellFactory cc = new SingleCellFactory(newColSpec) {
            @Override
            public DataCell getCell(final DataRow row) {
                double[] values = new double[_dim];
                for (int j = 0; j < _dim; j++) {
                    if (!row.getCell(_indices[j]).isMissing()) {
                        // value is double and not missing
                        values[j] =
                                ((DoubleValue)row.getCell(_indices[j]))
                                        .getDoubleValue();
                    } else {
                        // represent missing value with NotANumber
                        values[j] = Double.NaN;
                    }
                }

                Color[] colors = new Color[4];
                colors[0] = m_rangeModels.getBackgroundColor();
                colors[1] = m_rangeModels.getIntervalColor();
                colors[2] = m_rangeModels.getBendColor();
                colors[3] = m_rangeModels.getOutlyingBendColor();

                RadarPlotPanel panel =
                        new RadarPlotPanel(values, _min, _max, _validMin,
                                _validMax, _labels, colors);

                int maxWidth = 0;
                int maxHeight = 0;

                for (int i = 0; i < _labels.length; i++) {
                    Font font = panel.getFont();
                    maxWidth =
                            Math.max(SwingUtilities.computeStringWidth(
                                    panel.getFontMetrics(font), _labels[i]),
                                    maxWidth);
                    maxHeight =
                            Math.max(panel.getFontMetrics(font).getHeight(),
                                    maxHeight);
                }

                // setup box and paint border
                final int xWidth = 200 + 2 * maxWidth + 10;
                final int yHeight = 175 + 2 * maxHeight + 5;
                DOMImplementation domImpl = new SVGDOMImplementation();
                String svgNS = "http://www.w3.org/2000/svg";
                Document myFactory = domImpl.createDocument(svgNS, "svg", null);
                SVGGraphics2D g = new SVGGraphics2D(myFactory);
                // g.setColor(Color.GREEN);
                g.setSVGCanvasSize(new Dimension(xWidth, yHeight));
                g.translate(maxWidth, maxHeight);
                // g.setBackground(Color.WHITE);
                // g.setColor(Color.BLACK);
                g.setColor(Color.BLUE);

                g.setStroke(new BasicStroke(1));
                panel.paintComponentSVG(g);

                myFactory.replaceChild(g.getRoot(),
                        myFactory.getDocumentElement());
                DataCell dc = new SvgCell((SVGDocument)myFactory);
                return dc;

            }
        };
        ColumnRearranger result = new ColumnRearranger(spec);
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
