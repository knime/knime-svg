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
package org.knime.ext.svg.node.radarplot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 *
 * Basic UI for the RadarSlider class.
 * @author Andreas Burger
 *
 */
public class RadarSliderUI extends BasicSliderUI {

	private RadarSlider slider;
	private Color barColor = new Color(200, 200, 200);
	private Color backgroundColor = new Color(128,128,128);

	public RadarSliderUI(final JSlider b) {
		super(b);
		slider = (RadarSlider) b;

	}

	/**
	 * Allows changing of the bar-color
	 * @param barColor The Color with which to paint the bar
	 */
	public void setBarColor(final Color barColor) {
		this.barColor = barColor;
		slider.repaint();
	}

	/**
	 * Allows changing of the background-color.
	 * The background is the space on the bar outside of the two thumbs.
	 * @param bgColor The Color with which to paint the background
	 */
	public void setBackgroundColor(final Color bgColor) {
		backgroundColor = bgColor;
		slider.repaint();
	}

	private int xPositionForValue(final double val){
		// X Position = Origin + width * |(Value - min)| / |(max - min)|. Should work for numbers smaller than 0.
		int result = (int) ((trackRect.x)+ (trackRect.width) * (Math.abs((val-slider.doubleGetMinimum())) /Math.abs(slider.doubleGetMaximum()- slider.doubleGetMinimum())));
		return result;
	}

	private double getValueForXPosition(final int pos){
		// If its at zero, we're done.
		if (pos - xPositionForValue(slider.doubleGetMinimum()) ==0 ) {
            return slider.doubleGetMinimum();
        }
		// else we get the distance from the minimum and thereby the fraction of the width.
		double result = pos - xPositionForValue(slider.doubleGetMinimum());
		result = result / trackRect.width;
		// minimum + interval*fraction of width = actual value.
		result = slider.doubleGetMinimum() + result * (slider.doubleGetMaximum()-slider.doubleGetMinimum());
		// here be adjustments. Yarr.
		if (result > slider.doubleGetMaximum()) {
            result = slider.doubleGetMaximum();
        }
		if (result < slider.doubleGetMinimum()) {
            result = slider.doubleGetMinimum();
        }
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.swing.plaf.basic.BasicSliderUI#paintTrack(java.awt.Graphics)
	 */
	@Override
	public void paintTrack(final Graphics g) {
		if (slider.isEnabled()){
			if (((RadarSliderModel)slider.getModel()).getValue(1) < ((RadarSliderModel)slider.getModel()).getValue(2)){
				g.setColor(backgroundColor);
				g.fill3DRect(xPositionForValue(slider.doubleGetMinimum()), (trackRect.height>>1)-5, xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(1))-xPositionForValue(slider.doubleGetMinimum()),  10, true);
				g.setColor(barColor);
				g.fill3DRect(xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(1)), (trackRect.height>>1)-5, xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(2))-xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(1)),  10, true);
				g.setColor(backgroundColor);
				g.fill3DRect(xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(2)), (trackRect.height>>1)-5, xPositionForValue(slider.doubleGetMaximum())-xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(2)), 10, true);
			}
			if (((RadarSliderModel)slider.getModel()).getValue(1) == ((RadarSliderModel)slider.getModel()).getValue(2)){
				g.setColor(backgroundColor);
				g.fill3DRect(xPositionForValue(0), (trackRect.height>>1)-5, trackRect.width,  10, true);
			}
		}
		else{
			if (((RadarSliderModel)slider.getModel()).getValue(1) < ((RadarSliderModel)slider.getModel()).getValue(2)){
				g.setColor(Color.DARK_GRAY);
				g.fill3DRect(xPositionForValue(slider.doubleGetMinimum()), (trackRect.height>>1)-5, xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(1))-xPositionForValue(slider.doubleGetMinimum()),  10, true);
				g.setColor(Color.GRAY);
				g.fill3DRect(xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(1)), (trackRect.height>>1)-5, xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(2))-xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(1)),  10, true);
				g.setColor(Color.DARK_GRAY);
				g.fill3DRect(xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(2)), (trackRect.height>>1)-5, xPositionForValue(slider.doubleGetMaximum())-xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(2)), 10, true);
			}
			if (((RadarSliderModel)slider.getModel()).getValue(1) == ((RadarSliderModel)slider.getModel()).getValue(2)){
				g.setColor(Color.DARK_GRAY);
				g.fill3DRect(xPositionForValue(0), (trackRect.height>>1)-5, trackRect.width,  10, true);
			}
		}
	}





	@Override
	protected TrackListener createTrackListener(final JSlider slider) {
		TrackListener result = new TrackListener(){
			private int currentThumb;
			@Override
            public void mousePressed(final MouseEvent e) {
				// Selects the thumbs if clicked within 5 px.
				int x = e.getX();
				if (Math.abs(x - xPositionForValue((getSlider()).getValue(1))) <= 5 ){
					currentThumb = 1;
				}
				if (Math.abs(x - xPositionForValue((getSlider()).getValue(2))) <= 5 ){
					currentThumb = 2;
				}
			}
			@Override
            public void mouseDragged( final MouseEvent e ) {
				// Allows moving the thumbs. If one thumb would pass the other the selected thumb is switched instead.
				((RadarSliderModel)getSlider().getModel()).setValue(getValueForXPosition(e.getX()), currentThumb);
				if ((getSlider().getValue(1) >= getSlider().getValue(2)) || (getSlider().getValue(2) <= getSlider().getValue(1)))
                 {
                    currentThumb = ~currentThumb & 3; // not thumb or 3. For the thumb-values 1 or 2 this switches between the two.
                }
				getSlider().repaint();
			}
			@Override
			public void mouseReleased(final MouseEvent arg0) {
				currentThumb = 0;
			}


		};
		return result;
	}
	@Override
	public void paintThumb(final Graphics g) {
		if(slider.isEnabled()){
			int q = (this.xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(1)) -4);
			g.setColor(new Color(192,192,192));
			g.fill3DRect(q, (int) (trackRect.height*0.125), 8, (int) (trackRect.height*0.75), true);
			q = (this.xPositionForValue(((RadarSliderModel)slider.getModel()).getValue(2)) -4);
			g.fill3DRect(q, (int) (trackRect.height*0.125), 8, (int) (trackRect.height*0.75), true);
	//		horizThumbIcon.paintIcon(slider, g, q, 0);
		}
		else{
			int q = (this.xPositionForValue(((RadarSliderModel)slider.getModel()).getMinimum()) -4);
			g.setColor(new Color(192,192,192));
			g.fill3DRect(q, (int) (trackRect.height*0.125), 8, (int) (trackRect.height*0.75), true);
			q = (this.xPositionForValue(((RadarSliderModel)slider.getModel()).getMaximum()) -4);
			g.fill3DRect(q, (int) (trackRect.height*0.125), 8, (int) (trackRect.height*0.75), true);
		}
	}

	public RadarSlider getSlider(){
		return slider;
	}

}
