/*
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
 * ------------------------------------------------------------------------
 */
package org.knime.ext.svg.node.radarplot;

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
	
/** Creates a new Slider
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

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		super.setEnabled(enabled);
		if (this.getModel() instanceof RadarSliderModel)
			((RadarSliderModel)this.getModel()).setEnabled(enabled);
	}
	
	

}
