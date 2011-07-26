package org.knime.ext.svg.node.radarplot;

import javax.swing.DefaultBoundedRangeModel;

/**
 * Internal model for the RadarSlider class.
 * @author Andreas Burger
 *
 */
@SuppressWarnings("serial")
public class RadarSliderModel extends DefaultBoundedRangeModel {

	private double m_value1, m_value2;
	private int m_extent;
	private double m_minVal, m_maxVal;
	private boolean m_enabled = true;

	/** Returns the value of the selected Thumb. NOTE: Thumbs are numbered 1 and 2!
	 * @param n The desired thumb
	 * @return the Value of the thumb or -1 if invalid thumb is selected
	 */
	public double getValue(final int n){
		if (n == 1) {
            return m_value1;
        }
		if (n == 2) {
            return m_value2;
        } else {
            return -1;
        }
	}

	@Override
    public int getValue(){
		return (int)getValue(1);
	}

	@Override
    public void setValue(final int n) {
		m_value1 = n;
		fireStateChanged();
	}

/**
 *
 * @param n The desired value of the thumb
 * @param thumb The thumb whose value should be changed. (1 or 2!)
 */
	public void setValue(final double n, final int thumb) {
		if (m_enabled){
			if (thumb == 1) {
	            m_value1 = n;
	        }
			if (thumb == 2) {
	            m_value2 = n;
	        }
			fireStateChanged();
		}
	}

	@Override
	public int getExtent() {
		return this.m_extent;
	}

	@Override
    public int getMaximum() {
		return (int) this.m_maxVal;
	}

	@Override
	public int getMinimum() {
		return (int) this.m_minVal;
	}

	public double doubleGetMaximum() {
		return  this.m_maxVal;
	}

	public double doubleGetMinimum() {
		return this.m_minVal;
	}

	@Override
	public void setExtent(final int n) {
//		if((value2+extent <= maxVal)&&(n >-1))
		this.m_extent = n;
	}

	@Override
	public void setMaximum(final int n) {
//		if(value2+extent <= n)
		m_maxVal = n;
	}

	@Override
	public void setMinimum(final int n) {
//		if (n <= value1)
		m_minVal = n;
	}

	public void setMaximum(final double n) {
//		if(value2+extent <= n)
		m_maxVal = n;
	}

	public void setMinimum(final double n) {
//		if (n <= value1)
		m_minVal = n;
	}

	@Override
	public String toString() {
		String result = "";
		result = result + "Min. Val.:" + m_minVal + " Value 1:" +m_value1+ " Value 2:"+m_value2+ "Extent:"+ m_extent +  "Max. Val.:"+m_maxVal;
		return result;
	}

	public void setEnabled(boolean enabled) {
		m_enabled = enabled;
		
	}

}
