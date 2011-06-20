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
 * --------------------------------------------------------------------- *
 *
 * History
 *   21.01.2007 (berthold): created
 */
package org.knime.ext.svg.node.radarplot;

import java.awt.Color;

import javax.swing.table.AbstractTableModel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;


public class ColumnSettingsTable extends AbstractTableModel {

    private int m_nrAttr = 0;
    private boolean[] m_isSelected;
    private String[] m_attrName;
    private double[] m_min;
    private double[] m_max;
    private double[] m_validMin;
    private double[] m_validMax;
    private String[] m_backgroundColor;
    private String[] m_intervalColor;
    private String[] m_bendColor;
    private String[] m_outlyingBendColor;
    private boolean[] m_isDouble;

    private final String m_confName;

    public ColumnSettingsTable(final String confName) {
        m_confName = confName;
        m_nrAttr = 0;
    }

    public void setNewSpec(final DataTableSpec spec)
    throws NotConfigurableException {
        initMembers(spec.getNumColumns());
        for (int i = 0; i < m_nrAttr; i++) {
            DataColumnSpec thisSpec = spec.getColumnSpec(i);
            m_attrName[i] = thisSpec.getName();
            if (thisSpec.getType().isCompatible(DoubleValue.class)) {
                m_isSelected[i] = true;
               if ((thisSpec.getDomain().getLowerBound()) != null){
            	   m_min[i] = ((DoubleValue)(thisSpec.getDomain().getLowerBound())).getDoubleValue();
            	   m_max[i] = ((DoubleValue)(thisSpec.getDomain().getUpperBound())).getDoubleValue();
               }
               else {
            	   m_min[i] = -1;
            	   m_max[i] = -1;
               }
                m_validMin[i] = m_min[i];
                m_validMax[i] = m_max[i];
                m_isDouble[i] = true;
            } else {
                m_isSelected[i] = false;
                m_isDouble[i] = false;
            }
        }
        fireTableDataChanged();
    }

    private void initMembers(final int nrAttr) {
        m_nrAttr = nrAttr;
        m_isSelected = new boolean[m_nrAttr];
        m_attrName = new String[m_nrAttr];
        m_min = new double[m_nrAttr];
        m_max = new double[m_nrAttr];
        m_validMin = new double[m_nrAttr];
        m_validMax = new double[m_nrAttr];
        m_backgroundColor = new String[m_nrAttr];
        m_intervalColor = new String[m_nrAttr];
        m_bendColor = new String[m_nrAttr];
        m_outlyingBendColor = new String[m_nrAttr];
        m_isDouble = new boolean[m_nrAttr];
    }

    final static String[] COLNAMES = {"E", "Values"};

    @Override
    public int getColumnCount() {
        return COLNAMES.length;
    }

    @Override
    public String getColumnName(final int i) {
        return COLNAMES[i];
    }

    public Color getBackgroundColor(){
    	if (m_backgroundColor[0] != null){
	    	String[] result = m_backgroundColor[0].split("\\.");
	    	int red = Integer.parseInt(result[0]);
	    	int green = Integer.parseInt(result[1]);
	    	int blue = Integer.parseInt(result[2]);
	    	return new Color(red, green, blue);
    	} else {
            return new Color(175, 220, 240);
        }
    }

    public Color getIntervalColor(){
    	if (m_intervalColor[0] != null){
	    	String[] result = m_intervalColor[0].split("\\.");
	    	int red = Integer.parseInt(result[0]);
	    	int green = Integer.parseInt(result[1]);
	    	int blue = Integer.parseInt(result[2]);
	    	return new Color(red, green, blue);
    	} else {
            return new Color(105, 150, 170);
        }
    }

    public Color getBendColor(){
    	if (m_bendColor[0] != null){
	    	String[] result = m_bendColor[0].split("\\.");
	    	int red = Integer.parseInt(result[0]);
	    	int green = Integer.parseInt(result[1]);
	    	int blue = Integer.parseInt(result[2]);
	    	return new Color(red, green, blue);
    	} else {
            return new Color(0, 255, 0);
        }
    }

    public boolean isSelected(final int i) {
        return m_isSelected[i];
    }

    public boolean isDouble(final int i) {
        return m_isDouble[i];
    }

    public double getMin(final int i) {
        return m_min[i];
    }

    public double getValidMin(final int i) {
        return m_validMin[i];
    }

    public double getValidMax(final int i) {
        return m_validMax[i];
    }

    public double getMax(final int i) {
        return m_max[i];
    }

    public int getNrSelected() {
        int s = 0;
        for (int i = 0; i < m_nrAttr; i++) {
            if (m_isSelected[i]) {
                s++;
            }
        }
        return s;
    }

    @Override
    public Class<?> getColumnClass(final int col) {
        switch (col) {
        case 0: return Boolean.class;
        case 1: return String.class;
        case 2:
        case 3:
        case 4:
        case 5: return Double.class;
        }
        assert false;
        return String.class;
    }

    @Override
    public int getRowCount() {
        return m_nrAttr;
    }

    public String getRowName(final int i){
    	return m_attrName[i];
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
        return m_isDouble[row] && ((col == 0) || (col == 3) || (col == 4));
    }

    @Override
    public Object getValueAt(final int row, final int col) {
        switch (col) {
        case 0: return m_isSelected[row];
        case 1: return m_attrName[row];
        }
        if (!m_isDouble[row]) {
            return Double.NaN;
        }
        switch (col) {
        case 2: return m_min[row];
        case 3: return m_validMin[row];
        case 4: return m_validMax[row];
        case 5: return m_max[row];
        }
        assert false;
        return "N/A";
    }

    @Override
    public void setValueAt(final Object obj, final int row, final int col) {
        switch (col) {
        case 0: m_isSelected[row] = ((Boolean)obj).booleanValue(); return;
        case 1: assert false;
        case 2: assert false;
        case 5: assert false;
        }
        double val = ((Double)obj).doubleValue();
        if ((val < m_min[row]) || (val > m_max[row])) {
            throw new IllegalArgumentException();
        }
        switch (col) {
        case 3: m_validMin[row] = val; return;
        case 4: m_validMax[row] = val; return;
        }
        return;

    }
    void loadSettings(final NodeSettingsRO settings) {
        try {
            NodeSettingsRO mySettings = settings.getNodeSettings(m_confName);
            int nrAttr = mySettings.getInt("NRATTR");
            initMembers(nrAttr);
            for (int i = 0; i < m_nrAttr; i++) {
                NodeSettingsRO thisSettings = mySettings.getNodeSettings(
                        "Attribute_" + i);
                m_attrName[i] = thisSettings.getString("NAME");
                m_validMin[i] = thisSettings.getDouble("VALIDMIN");
                m_validMax[i] = thisSettings.getDouble("VALIDMAX");
                m_min[i] = thisSettings.getDouble("MIN");
                m_max[i] = thisSettings.getDouble("MAX");
                m_backgroundColor[i] = thisSettings.getString("BACKGROUND");
                m_intervalColor[i] = thisSettings.getString("INTERVAL");
                m_bendColor[i] = thisSettings.getString("BEND");
                m_outlyingBendColor[i] = thisSettings.getString("OUTLYINGBEND");
                m_isSelected[i] = thisSettings.getBoolean("SELECTED");
                m_isDouble[i] = thisSettings.getBoolean("ISDOUBLE");
            }
        } catch (InvalidSettingsException ise) {
            // ignore - try to set as much as possible in the dialog
        }
        fireTableDataChanged();
    }

    void saveSettings(final NodeSettingsWO settings) {
        NodeSettingsWO mySettings = settings.addNodeSettings(m_confName);
        mySettings.addInt("NRATTR", m_nrAttr);
        for (int i = 0; i < m_nrAttr; i++) {
            NodeSettingsWO thisSettings = mySettings.addNodeSettings(
                    "Attribute_" + i);
            thisSettings.addString("NAME", m_attrName[i]);
            thisSettings.addDouble("VALIDMIN", m_validMin[i]);
            thisSettings.addDouble("VALIDMAX", m_validMax[i]);
            thisSettings.addDouble("MIN", m_min[i]);
            thisSettings.addDouble("MAX", m_max[i]);
            thisSettings.addString("BACKGROUND", m_backgroundColor[i]);
            thisSettings.addString("INTERVAL", m_intervalColor[i]);
            thisSettings.addString("BEND", m_bendColor[i]);
            thisSettings.addString("OUTLYINGBEND", m_outlyingBendColor[i]);
            thisSettings.addBoolean("SELECTED", m_isSelected[i]);
            thisSettings.addBoolean("ISDOUBLE", m_isDouble[i]);
        }
    }

	public boolean equals(final ColumnSettingsTable table) {
		if (this.getRowCount() != table.getRowCount()) {
            return false;
        }
		for (int i = 0; i < this.getRowCount(); i++){
			if (!(this.m_attrName[i].equals(table.m_attrName[i]))) {
                return false;
            }
			if (this.getValidMax(i) != table.getValidMax(i)) {
                return false;
            }
			if (this.getValidMin(i) != table.getValidMin(i)) {
                return false;
            }
		}
		return true;
	}

	public boolean isProper() {
		for(int i = 0; i < this.getRowCount(); i++){
			if (this.getValidMax(i) != this.getValidMin(i)) {
                return true;
            }
		}
		return false;
	}

	public Color getOutlyingBendColor() {
		if (m_outlyingBendColor[0] != null){
	    	String[] result = m_outlyingBendColor[0].split("\\.");
	    	int red = Integer.parseInt(result[0]);
	    	int green = Integer.parseInt(result[1]);
	    	int blue = Integer.parseInt(result[2]);
	    	return new Color(red, green, blue);
    	} else {
            return new Color(255, 0, 0);
        }
	}



}
