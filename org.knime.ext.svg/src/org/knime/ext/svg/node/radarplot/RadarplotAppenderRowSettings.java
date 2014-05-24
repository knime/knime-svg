/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * ------------------------------------------------------------------------
 */
package org.knime.ext.svg.node.radarplot;

import java.awt.Color;

/**
 * 
 * @author Andreas Burger
 * 
 * This class manages all the settings of one Row in the radarplot configurations dialog.
 *
 */

public class RadarplotAppenderRowSettings {
	
	private double m_minValue;
	private double m_maxValue;
	private double m_lowerValue;
	private double m_upperValue;
	private Color m_barColor;
	private boolean m_enabled;
	private String m_name;
	private boolean m_isDouble;
	private boolean m_set = false;
	
	public boolean isDouble() {
		return m_isDouble;
	}
	public void setDouble(boolean isDouble) {
		this.m_isDouble = isDouble;
	}
	public double getMinValue() {
		return m_minValue;
	}
	public void setMinValue(double minValue) {
		this.m_minValue = minValue;
	}
	public double getMaxValue() {
		return m_maxValue;
	}
	public void setMaxValue(double maxValue) {
		this.m_maxValue = maxValue;
	}
	public double getLowerValue() {
		return m_lowerValue;
	}
	public void setLowerValue(double lowerValue) {
		this.m_lowerValue = lowerValue;
	}
	public double getUpperValue() {
		return m_upperValue;
	}
	public void setUpperValue(double upperValue) {
		this.m_upperValue = upperValue;
	}
	public boolean isEnabled() {
		return m_enabled;
	}
	public void setEnabled(boolean enabled) {
		this.m_enabled = enabled;
	}
	public String getName() {
		return m_name;
	}
	public void setName(String name) {
		this.m_name = name;
	}
	public Color getBarColor() {
		return m_barColor;
	}
	public void setBarColor(Color barColor) {
		this.m_barColor = barColor;
	}
	
	public boolean isSet() {
		return m_set;
	}
	/** This method allows setting all the values at once
	 * @param min Valid minimum
	 * @param low Lower value
	 * @param high Higher value
	 * @param max Valid maximum
	 * @param enabled Is this row visible in the output?
	 * @param name The name of this row
	 * @param isDouble The row contains only doubles?
	 */
	public void set(double min, double low, double high, double max, boolean enabled, String name, boolean isDouble){
		m_minValue = min;
		m_maxValue = max;
		m_lowerValue = low;
		m_upperValue = high;
		m_enabled = enabled;
		m_name = name;
		m_isDouble = isDouble;
		m_set = true;
	}

}
