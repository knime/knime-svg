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
	
	private double minValue;
	private double maxValue;
	private double lowerValue;
	private double upperValue;
	private Color barColor;
	private boolean enabled;
	private String name;
	private boolean isDouble;
	
	public boolean isDouble() {
		return isDouble;
	}
	public void setDouble(boolean isDouble) {
		this.isDouble = isDouble;
	}
	public double getMinValue() {
		return minValue;
	}
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}
	public double getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}
	public double getLowerValue() {
		return lowerValue;
	}
	public void setLowerValue(double lowerValue) {
		this.lowerValue = lowerValue;
	}
	public double getUpperValue() {
		return upperValue;
	}
	public void setUpperValue(double upperValue) {
		this.upperValue = upperValue;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Color getBarColor() {
		return barColor;
	}
	public void setBarColor(Color barColor) {
		this.barColor = barColor;
	}
	
	public void set(double min, double low, double high, double max, boolean enabled, String name, boolean isDouble){
		minValue = min;
		maxValue = max;
		lowerValue = low;
		upperValue = high;
		this.enabled = enabled;
		this.name = name;
		this.isDouble = isDouble;
	}

}
