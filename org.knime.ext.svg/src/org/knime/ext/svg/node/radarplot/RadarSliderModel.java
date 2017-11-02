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
