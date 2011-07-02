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
