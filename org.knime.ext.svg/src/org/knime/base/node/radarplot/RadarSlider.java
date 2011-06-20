package org.knime.base.node.radarplot;

import javax.swing.JSlider;

/**
 * 
 * Basic Slider with two thumbs to allow the input of intervals.
 * 
 * @author Andreas Burger
 *
 */
@SuppressWarnings("serial")
public class RadarSlider extends JSlider {
	
/**
 * 
 * @param lowVal Value of the left thumb
 * @param highVal Value of the right thumb
 * @param extent 
 * @param min Minimum
 * @param max Maximum
 */
	public RadarSlider (double lowVal, double highVal, int extent, double min, double max){
		RadarSliderModel model = new RadarSliderModel();
		model.setValue(lowVal, 1);
		model.setValue(highVal, 2);
		model.setExtent(extent);
		model.setMinimum(min);
		model.setMaximum(max);
		this.setModel(model);
		
	}

	/**
	 * Returns the Value of the selected thumb. NOTE: Thumbs are numbered 1 and 2!
	 * @param n Lower thumb (1) oder higher thumb (2)
	 * @return Current Value of the desired thumb
	 */
	public double getValue(int n){
		return ((RadarSliderModel)getModel()).getValue(n);
	}
	
	/**
	 * @return Maximum value of the slider
	 */
	public double doubleGetMaximum(){
		return ((RadarSliderModel)getModel()).doubleGetMaximum();
	}
	
	/**
	 * @return Minimum value of the slider
	 */
	public double doubleGetMinimum(){
		return ((RadarSliderModel)getModel()).doubleGetMinimum();
	}
	
	/**
	 * Returns the Value of the selected thumb. NOTE: Thumbs are numbered 1 and 2!
	 * @param value The desired value for the thumb
	 * @param thumb Which thumb to set.
	 */
	public void setValue(double value, int thumb){
		((RadarSliderModel)getModel()).setValue(value, thumb);
	}

}
