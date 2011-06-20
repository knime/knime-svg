package org.knime.ext.svg.node.radarplot;

import javax.swing.DefaultBoundedRangeModel;

/**
 * Internal model for the RadarSlider class.
 * @author Andreas Burger
 *
 */
@SuppressWarnings("serial")
public class RadarSliderModel extends DefaultBoundedRangeModel {

	private double value1, value2;
	private int extent;
	private double minVal, maxVal;

	public double getValue(final int n){
		if (n == 1) {
            return value1;
        }
		if (n == 2) {
            return value2;
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
		value1 = n;
		fireStateChanged();
	}

/**
 *
 * @param n The desired value of the thumb
 * @param thumb The thumb whose value should be changed. (1 or 2!)
 */
	public void setValue(final double n, final int thumb) {
		if (thumb == 1) {
            value1 = n;
        }
		if (thumb == 2) {
            value2 = n;
        }
		fireStateChanged();
	}

	@Override
	public int getExtent() {
		return this.extent;
	}

	@Override
    public int getMaximum() {
		return (int) this.maxVal;
	}

	@Override
	public int getMinimum() {
		return (int) this.minVal;
	}

	public double doubleGetMaximum() {
		return  this.maxVal;
	}

	public double doubleGetMinimum() {
		return this.minVal;
	}

	@Override
	public void setExtent(final int n) {
//		if((value2+extent <= maxVal)&&(n >-1))
		this.extent = n;
	}

	@Override
	public void setMaximum(final int n) {
//		if(value2+extent <= n)
		maxVal = n;
	}

	@Override
	public void setMinimum(final int n) {
//		if (n <= value1)
		minVal = n;
	}

	public void setMaximum(final double n) {
//		if(value2+extent <= n)
		maxVal = n;
	}

	public void setMinimum(final double n) {
//		if (n <= value1)
		minVal = n;
	}

	@Override
	public String toString() {
		String result = "";
		result = result + "Min. Val.:" + minVal + " Value 1:" +value1+ " Value 2:"+value2+ "Extent:"+ extent +  "Max. Val.:"+maxVal;
		return result;
	}

}
