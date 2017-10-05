/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * ------------------------------------------------------------------------
 */
package org.knime.ext.svg.node.sparklines;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgValueRenderer;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

/**
 * Append a column holding SvgDataCells holding SparkLines for the selected
 * numerical columns.
 *
 * @author M. Berthold, University of Konstanz
 */
public class SparkLineNodeModel extends NodeModel {

    /** Config identifier: Included columns. */
    static final String CFG_COLUMNS = "columns";

    /** Config identifier: Name of new column. */
    static final String CFG_NEW_COLUMN_NAME = "new_column_name";

    private SettingsModelFilterString m_columns = new SettingsModelFilterString(CFG_COLUMNS);

    private SettingsModelString m_newColName = new SettingsModelString(
            CFG_NEW_COLUMN_NAME, "Spark Lines");

    /**
     * Constructor for the node model.
     */
    protected SparkLineNodeModel() {
        super(1, 1);
    }

    /** {@inheritDoc} */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        ColumnRearranger arranger =
                createColumnRearranger(inData[0].getDataTableSpec());
        BufferedDataTable out =
                exec.createColumnRearrangeTable(inData[0], arranger, exec);
        return new BufferedDataTable[]{out};
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
    }

    /** {@inheritDoc} */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpec inSpec = inSpecs[0];
        if (m_columns.getIncludeList().size() == 0
                && m_columns.getExcludeList().size() == 0) {
            if (inSpecs[0] == null) {
                throw new InvalidSettingsException("No Settings available!");
            }
            List<String> includedColumns = new ArrayList<String>();
            List<String> excludedColumns = new ArrayList<String>();
            for (int i = 0; i < inSpec.getNumColumns(); i++) {
                DataColumnSpec colSpec = inSpec.getColumnSpec(i);
                if (colSpec.getType().isCompatible(DoubleValue.class)) {
                    includedColumns.add(colSpec.getName());
                } else {
                    excludedColumns.add(colSpec.getName());
                }
            }
            // set all double compatible columns as include list
            m_columns.setIncludeList(includedColumns);
            m_columns.setExcludeList(excludedColumns);
        }
        // check if some columns are included
        if (m_columns.getIncludeList().size() <= 0) {
            setWarningMessage("No column in include list!");
        }
        for (String s : m_columns.getIncludeList()) {
            if (!inSpec.containsName(s)) {
                throw new InvalidSettingsException("No such column: " + s);
            }
        }
        if (inSpec.containsName(m_newColName.getStringValue())) {
            throw new InvalidSettingsException("Column already exits: "
                    + m_newColName);
        }
        ColumnRearranger arranger = createColumnRearranger(inSpec);
        return new DataTableSpec[]{ arranger.createSpec() };
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec spec)
    throws InvalidSettingsException {
        ColumnRearranger result = new ColumnRearranger(spec);
        // add new column
        DataColumnSpecCreator append =
                new DataColumnSpecCreator(m_newColName.getStringValue(),
                        SvgCell.TYPE);
        // and set properties to adjust renderer appropriately
        HashMap<String,String> newProps = new HashMap<String,String>();
        newProps.put(SvgValueRenderer.OPTION_KEEP_ASPECT_RATIO, "false");
        newProps.put(SvgValueRenderer.OPTION_PREFERRED_HEIGHT, "10");
        newProps.put(SvgValueRenderer.OPTION_PREFERRED_WIDTH, "50");
        DataColumnProperties props = new DataColumnProperties(newProps);
        append.setProperties(props);
        // create list of cell indices to include in SparkLine.
        List<String> colNames = m_columns.getIncludeList();
        final int[] indices = new int[colNames.size()];
        int j = 0;
        for (int k = 0; k < spec.getNumColumns(); k++) {
            DataColumnSpec cs = spec.getColumnSpec(k);
            if (colNames.contains(cs.getName())) {
                indices[j] = k;
                j++;
            }
        }
        // store ranges for affected columns
        final double[] m_rangeMin = new double[colNames.size()];
        final double[] m_rangeMax = new double[colNames.size()];
        for (int i = 0; i < indices.length; i++) {
            DataCell lowerBound =
                    spec.getColumnSpec(indices[i]).getDomain().getLowerBound();
            m_rangeMin[i] =
                    lowerBound instanceof DoubleValue ? ((DoubleValue)lowerBound)
                            .getDoubleValue() : Double.NaN;
            DataCell upperBound =
                    spec.getColumnSpec(indices[i]).getDomain().getUpperBound();
            m_rangeMax[i] =
                    upperBound instanceof DoubleValue ? ((DoubleValue)upperBound)
                            .getDoubleValue() : Double.NaN;
            if (Double.isNaN(m_rangeMax[i]) || Double.isNaN(m_rangeMax[i])) {
                throw new InvalidSettingsException("Missing Domain "
                        + "information! Preceed with Domain Calculator.");
            }
        }

        result.append(new SingleCellFactory(append.createSpec()) {
            @Override
            public DataCell getCell(final DataRow row) {
                // setup box and paint border
                final int xWidth = 500;
                final int xOffset = 1;
                final int yHeight = 100;
                final int yOffset = 1;
                DOMImplementation domImpl = new SVGDOMImplementation();
                String svgNS = "http://www.w3.org/2000/svg";
                Document myFactory = domImpl.createDocument(svgNS, "svg", null);
                SVGGraphics2D g = new SVGGraphics2D(myFactory);
                g.setColor(Color.GREEN);
                g.setSVGCanvasSize(new Dimension(xWidth+2*xOffset, yHeight+2*yOffset));
                g.setBackground(Color.WHITE);
                g.setColor(Color.BLACK);
                g.drawRect(0, 0, xWidth + 2*xOffset - 1, yHeight + 2*yOffset - 1);
                // draw spark line (skip missing values!)
                g.setColor(Color.BLUE);
                g.setStroke(new BasicStroke(3));
                if (indices.length >= 2) {
                    DataCell c = row.getCell(indices[0]);
                    double d = c instanceof DoubleValue ? ((DoubleValue)c)
                                    .getDoubleValue() : Double.NaN;
                    int y0 = yHeight+yOffset - (int)(yHeight * (d - m_rangeMin[0]) / (m_rangeMax[0] - m_rangeMin[0]));
                    int x0 = xOffset;
                    for (int i = 1; i < indices.length; i++) {
                        c = row.getCell(indices[i]);
                        d = c instanceof DoubleValue ? ((DoubleValue)c)
                                        .getDoubleValue() : Double.NaN;
                        int y1 = yHeight+yOffset - (int)(yHeight * (d - m_rangeMin[i]) / (m_rangeMax[i] - m_rangeMin[i]));
                        int x1 = x0 + xWidth/(indices.length - 1);
                        if (!Double.isNaN(y0) && !Double.isNaN(y1)) {
                            g.drawLine(x0, y0, x1, y1);
                        }
                        x0 = x1;
                        y0 = y1;
                    }
                }

                myFactory.replaceChild(g.getRoot(),
                        myFactory.getDocumentElement());
                DataCell dc = new SvgCell((SVGDocument)myFactory);
                return dc;
            }
        });
        return result;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_columns.saveSettingsTo(settings);
        m_newColName.saveSettingsTo(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_columns.loadSettingsFrom(settings);
        m_newColName.loadSettingsFrom(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_columns.validateSettings(settings);
        m_newColName.validateSettings(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

}
