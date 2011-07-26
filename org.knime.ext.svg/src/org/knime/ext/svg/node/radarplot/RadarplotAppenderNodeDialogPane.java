/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 * -------------------------------------------------------------------
 *
 * History
 *   17.01.2007 (mb): created
 */
package org.knime.ext.svg.node.radarplot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

/**
 * 
 * 
 * @author M. Berthold, University of Konstanz
 * @author Andreas Burger
 */
public class RadarplotAppenderNodeDialogPane extends NodeDialogPane {

	// private ColumnSettingsTable m_myTable;
	private JPanel m_sliders = new JPanel(new GridLayout());

	private LinkedList<RadarSlider> m_sliderList = new LinkedList<RadarSlider>();

	private RadarplotAppenderRowSettings[] m_rowSettings;

	private String[] format;

	private Color m_backgroundColor = new Color(175, 220, 240);

	private Color m_intervalColor = new Color(105, 150, 170);

	private Color m_bendColor = Color.GREEN;

	private Color m_outlyingBendColor = Color.RED;

	private LinkedList<JCheckBox> m_checkboxes = new LinkedList<JCheckBox>();
	
	private LinkedList<Integer[]> m_correspondingColumns = new LinkedList<Integer[]>();

	public RadarplotAppenderNodeDialogPane() {
		JPanel allPanel = new JPanel();
		allPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JScrollPane scrollPane = new JScrollPane(m_sliders);

		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;

		allPanel.add(scrollPane, c);

		c.weightx = 0.3;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 5;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 0, 5, 0);

		allPanel.add(new JPanel(), c);

		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 5;

		JButton selectAll = new JButton("Display all");
		selectAll.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent arg0) {
				for (JCheckBox box : m_checkboxes) {
					box.setSelected(true);
				}

			}

		});

		allPanel.add(selectAll, c);

		c.weightx = 0.3;
		c.weighty = 0;
		c.gridx = 2;
		c.gridy = 5;

		allPanel.add(new JPanel(), c);

		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 3;
		c.gridy = 5;
		c.gridheight = 1;
		c.gridwidth = 1;

		JButton selectNone = new JButton("Display none");

		selectNone.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent arg0) {
				for (JCheckBox box : m_checkboxes) {
					box.setSelected(false);
				}
			}
		});

		allPanel.add(selectNone, c);

		c.weightx = 0.3;
		c.weighty = 0;
		c.gridx = 4;
		c.gridy = 5;
		c.gridwidth = GridBagConstraints.REMAINDER;

		allPanel.add(new JPanel(), c);

		this.addTab("Column Settings", allPanel);
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final DataTableSpec[] specs) throws NotConfigurableException {
		String m_confName = RadarplotAppenderNodeModel.COLUMNRANGEPREFIX;
		// ColumnSettingsTable testTable = new
		// ColumnSettingsTable(m_confName);
		// ColumnSettingsTable testTable2 = new
		// ColumnSettingsTable(m_confName);
		// testTable.setNewSpec(specs[0]);
		// testTable2.loadSettings(settings);
		// boolean equals = testTable.equals(testTable2);

		ColumnSettingsTable incomingValues = new ColumnSettingsTable(m_confName);
		ColumnSettingsTable settingsTable = new ColumnSettingsTable(m_confName);
		incomingValues.setNewSpec(specs[0]);
		settingsTable.loadSettings(settings);

		int tableCompare = compareTables(incomingValues, settingsTable);
		int nrAttr = 0;
		if (incomingValues.isProper() >= 0){
			nrAttr = incomingValues.getRowCount();
			m_rowSettings = new RadarplotAppenderRowSettings[nrAttr];
			format = new String[nrAttr];
			for (int i = 0; i < nrAttr; i++) {
				m_rowSettings[i] = new RadarplotAppenderRowSettings();
			}
		}

		JPanel data = new JPanel();
		m_sliders.removeAll();
		data.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		JPanel buttonPanel = new JPanel();

		final JButton backgroundColor = new JButton("Background color...");
//		 c.gridx = 1;
//		 c.gridy =0;
//		 c.gridheight = 1;
//		 c.gridwidth = 1;
		buttonPanel.add(backgroundColor, c);

		backgroundColor.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				Color barColor = JColorChooser.showDialog(backgroundColor,
						"Choose Background Color", m_backgroundColor);
				if (barColor != null) {
					m_backgroundColor = barColor;
					for (RadarSlider slider : m_sliderList) {
						((RadarSliderUI) slider.getUI())
								.setBackgroundColor(m_backgroundColor);
					}
				}
			}
		});

		final JButton intervalColor = new JButton("Range color...");
//		 c.gridx = 2;
//		 c.gridy =0;
//		 c.gridheight = 1;
//		 c.gridwidth = 1;
		buttonPanel.add(intervalColor, c);

		intervalColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Color barColor = JColorChooser.showDialog(intervalColor,
						"Choose Range Color", m_intervalColor);
				if (barColor != null) {
					m_intervalColor = barColor;
					for (RadarSlider slider : m_sliderList) {
						((RadarSliderUI) slider.getUI())
								.setBarColor(m_intervalColor);
					}
				}
			}
		});

		final JButton bendColor = new JButton("Ribbon color...");
		buttonPanel.add(bendColor, c);

		bendColor.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				Color barColor = JColorChooser.showDialog(bendColor,
						"Choose Ribbon Color", m_bendColor);
				if (barColor != null) {
					m_bendColor = barColor;
				}
			}
		});

		final JButton outlyingBendColor = new JButton("Ribbon color 2...");
		buttonPanel.add(outlyingBendColor, c);

		outlyingBendColor.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				Color outlierColor = JColorChooser.showDialog(
						outlyingBendColor, "Choose alternate Ribbon Color",
						m_outlyingBendColor);
				if (outlierColor != null) {
					m_outlyingBendColor = outlierColor;
				}
			}
		});

		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.NORTH;
		data.add(buttonPanel, c);
		c.fill = GridBagConstraints.BOTH;

		if (nrAttr != 0) {
			
			if (incomingValues.isProper()>=0){
				SetupRows(tableCompare, incomingValues, settingsTable);
			
				for (int i = 0; i < nrAttr; i++) {
					if(m_rowSettings[i].isDouble()){
						boolean isValid = (m_rowSettings[i].getMinValue() !=  m_rowSettings[i].getMaxValue());
						DecimalFormat g = null;
						if (isValid){
							g = new DecimalFormat(format[i]);
						}
						else {
							g = new DecimalFormat("#.#");
						}
							
		
						JLabel space1 = new JLabel();
						space1.setPreferredSize(new Dimension(25, 15));
						c.gridx = 0;
						c.gridy = 3 * i + 1;
						c.gridheight = 1;
						c.gridwidth = GridBagConstraints.REMAINDER;
						c.insets = new Insets(0, 15, 0, 0);
						data.add(space1, c);
						
						String colName = "";
						
						if (isValid)
							colName = m_rowSettings[i].getName();
						else
							colName = m_rowSettings[i].getName() + " (DISABLED)";
		
						JLabel label = new JLabel(colName);
						c.gridx = 0;
						c.gridy = 3 * i + 2;
						c.gridheight = 2;
						c.gridwidth = 1;
						data.add(label, c);
		
						final JCheckBox box = new JCheckBox("Display? ");
						box.setSelected(m_rowSettings[i].isEnabled());
						c.gridx = 1;
						c.gridy = 3 * i + 2;
						c.gridheight = 2;
						c.gridwidth = 1;
						c.insets = new Insets(0, 10, 0, 0);
						data.add(box, c);
						if(!isValid){
							box.setSelected(false);
							m_rowSettings[i].setEnabled(false);
							box.setEnabled(false);
						}
						
						double lowerValue = 0;
						double upperValue = 1;
						double minValue = lowerValue; 
						double maxValue = upperValue;
						if(isValid){
							lowerValue = m_rowSettings[i].getLowerValue();
							upperValue = m_rowSettings[i].getUpperValue();
							minValue = m_rowSettings[i].getMinValue(); 
							maxValue = m_rowSettings[i].getMaxValue();
						}
						final RadarSlider slider = new RadarSlider(
								lowerValue,
								upperValue, 0,
								minValue,
								maxValue);
						slider.setUI(new RadarSliderUI(slider));
						((RadarSliderUI) slider.getUI())
								.setBarColor(m_intervalColor);
						((RadarSliderUI) slider.getUI())
								.setBackgroundColor(m_backgroundColor);
						slider.setSize((int) (slider.getSize().width * 0.75),
								slider.getSize().height);
						c.gridx = 2;
						c.gridy = 3 * i + 2;
						c.gridheight = 1;
						c.gridwidth = 4;
						c.weightx = 1;
						c.insets = new Insets(0, 0, 0, 0);
						data.add(slider, c);
						m_sliderList.add(slider);
						
						if(!isValid)
							slider.setEnabled(false);
		
						final JLabel spacer = new JLabel();
						c.insets = new Insets(0, 10, 0, 0);
						c.gridx = 6;
						c.gridy = 3 * i + 2;
						c.weightx = 0;
						c.gridheight = 2;
						c.gridwidth = GridBagConstraints.REMAINDER;
						data.add(spacer, c);
		
						JLabel ValueLabel1 = new JLabel("Lower value: ");
						c.gridx = 2;
						c.gridy = 3 * (i + 1);
						c.gridheight = 1;
						c.weightx = 0;
						c.gridwidth = 1;
						c.insets = new Insets(0, 0, 0, 0);
						data.add(ValueLabel1, c);
						
						int labelWidth = 0;
						String lowerLabelText = "";
						String upperLabelText = "";
						if (isValid){
							labelWidth = format[i].length()*6;
							lowerLabelText = g.format(slider.getValue(1));
							upperLabelText = g.format(slider.getValue(2));
							
						}
						else
							labelWidth = 24;
		
						final JLabel lowerValueLabel = new JLabel(lowerLabelText);
						 lowerValueLabel.setPreferredSize(new
						 Dimension(labelWidth,15));
						c.gridx = 3;
						c.gridy = 3 * (i + 1);
						c.gridheight = 1;
						c.gridwidth = 1;
						c.weightx = 1;
						data.add(lowerValueLabel, c);
		
						JLabel ValueLabel2 = new JLabel("Upper value: ");
						c.gridx = 4;
						c.gridy = 3 * (i + 1);
						c.gridheight = 1;
						c.gridwidth = 1;
						c.weightx = 0;
						data.add(ValueLabel2, c);
		
						final JLabel upperValueLabel = new JLabel(upperLabelText);
						upperValueLabel.setPreferredSize(new Dimension(labelWidth, 15));
						c.gridx = 5;
						c.gridy = 3 * (i + 1);
						c.gridheight = 1;
						c.insets = new Insets(0, 0, 0, 15);
						c.gridwidth = 1;
						c.weightx = 1;
						data.add(upperValueLabel, c);
		
						final int counter = i;
		
						slider.addChangeListener(new ChangeListener() {
		
							String numberformat = format[counter];
		
							@Override
							public void stateChanged(final ChangeEvent e) {
								DecimalFormat f = new DecimalFormat(numberformat);
								double minimum = slider.doubleGetMinimum();
								double maximum = slider.doubleGetMaximum();
								double lowerValue = slider.getValue(1);
								double upperValue = slider.getValue(2);
								m_rowSettings[counter].setLowerValue(lowerValue);
								m_rowSettings[counter].setUpperValue(upperValue);
								m_rowSettings[counter].setMinValue(minimum);
								m_rowSettings[counter].setMaxValue(maximum);
								lowerValueLabel.setText(f.format(lowerValue));
								upperValueLabel.setText(f.format(upperValue));
							}
						});
		
						box.addChangeListener(new ChangeListener() {
							@Override
							public void stateChanged(final ChangeEvent e) {
								m_rowSettings[counter].setEnabled(box.isSelected());
							}
						});
						m_checkboxes.add(box);
					}
				}
			}
			data.setVisible(true);
			m_sliders.add(data);
			m_sliders.validate();

		}
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		NodeSettingsWO mySettings = settings
				.addNodeSettings(RadarplotAppenderNodeModel.COLUMNRANGEPREFIX);
		mySettings.addInt("NRATTR", m_rowSettings.length);
		for (int i = 0; i < m_rowSettings.length; i++) {
			NodeSettingsWO thisSettings = mySettings
					.addNodeSettings("Attribute_" + i);
			thisSettings.addString("NAME", m_rowSettings[i].getName());
			thisSettings.addDouble("VALIDMIN", m_rowSettings[i].getMinValue());
			thisSettings.addDouble("VALIDMAX", m_rowSettings[i].getMaxValue());
			thisSettings.addDouble("MIN", m_rowSettings[i].getLowerValue());
			thisSettings.addDouble("MAX", m_rowSettings[i].getUpperValue());
			thisSettings.addString(
					"BACKGROUND",
					"" + m_backgroundColor.getRed() + "."
							+ m_backgroundColor.getGreen() + "."
							+ m_backgroundColor.getBlue());
			thisSettings.addString(
					"INTERVAL",
					"" + m_intervalColor.getRed() + "."
							+ m_intervalColor.getGreen() + "."
							+ m_intervalColor.getBlue());
			thisSettings.addString("BEND", "" + m_bendColor.getRed() + "."
					+ m_bendColor.getGreen() + "." + m_bendColor.getBlue());
			thisSettings.addString("OUTLYINGBEND",
					"" + m_outlyingBendColor.getRed() + "."
							+ m_outlyingBendColor.getGreen() + "."
							+ m_outlyingBendColor.getBlue());
			thisSettings.addBoolean("SELECTED", m_rowSettings[i].isEnabled());
			thisSettings.addBoolean("ISDOUBLE", m_rowSettings[i].isDouble());
		}
	}
	
	private int compareTables(ColumnSettingsTable incomingTable, ColumnSettingsTable settingsTable){
		if (settingsTable.equals(incomingTable))
			return 0;
		m_correspondingColumns = new LinkedList<Integer[]>();
		if (settingsTable.isSimilarTo(incomingTable, m_correspondingColumns))
			return 1;
		m_correspondingColumns = new LinkedList<Integer[]>();
		if (incomingTable.isSimilarTo(settingsTable, m_correspondingColumns))
			return 2;

		return -1;
		
	}
	
	private void SetupRows(int comparedTableInfo, ColumnSettingsTable incomingTable, ColumnSettingsTable settingsTable){
		if (comparedTableInfo == 0){ // Both tables equal, take Values from Settings
			for( int i = 0; i< settingsTable.getColumnCount(); i++){
				m_rowSettings[i].set(
						settingsTable.getValidMin(i),
						settingsTable.getMin(i),
						settingsTable.getMax(i),
						settingsTable.getValidMax(i),
						settingsTable.isSelected(i),
						settingsTable.getColumnName(i),
						settingsTable.isDouble(i));
				m_rowSettings[i].setBarColor(settingsTable.getBackgroundColor());
				if (m_rowSettings[i].isDouble()) {
					if (m_rowSettings[i].getMaxValue() != m_rowSettings[i].getMinValue()) {
						format[i] = "#";
						double interval = (m_rowSettings[i].getMaxValue() - m_rowSettings[i].getMinValue());
						if (interval <= 10 && interval > 1) {
							format[i] = format[i] + ".##";
						}
						if (interval != 0 && interval <= 1) {
							format[i] = format[i] + ".#";
							while (interval <= 1) {
								format[i] = format[i] + "#";
								interval = interval * 10;
							}
						}
					}
				}
			}
		}
		if (comparedTableInfo == 1){ // All Rows from Settings in Incoming Values (in no particular order).
			for( int i = 0; i< settingsTable.getColumnCount(); i++){
				int j = m_correspondingColumns.get(i)[1];
				m_rowSettings[j].set(
						settingsTable.getValidMin(i),
						settingsTable.getMin(i),
						settingsTable.getMax(i),
						settingsTable.getValidMax(i),
						settingsTable.isSelected(i),
						settingsTable.getColumnName(i),
						settingsTable.isDouble(i));
				m_rowSettings[j].setBarColor(settingsTable.getBackgroundColor());
				if (m_rowSettings[j].isDouble()) {
					if (m_rowSettings[j].getMaxValue() != m_rowSettings[j].getMinValue()) {
						format[j] = "#";
						double interval = (m_rowSettings[j].getMaxValue() - m_rowSettings[j].getMinValue());
						if (interval <= 10 && interval > 1) {
							format[j] = format[j] + ".##";
						}
						if (interval != 0 && interval <= 1) {
							format[j] = format[j] + ".#";
							while (interval <= 1) {
								format[j] = format[j] + "#";
								interval = interval * 10;
							}
						}
					}
				}
			}
			for(int i = 0; i < m_rowSettings.length; i++){  //Add the rows for which no settings exist - simply load from incoming data
				if (!m_rowSettings[i].isSet()){
					m_rowSettings[i].set(
							incomingTable.getValidMin(i),
							incomingTable.getMin(i),
							incomingTable.getMax(i),
							incomingTable.getValidMax(i),
							incomingTable.isSelected(i),
							incomingTable.getColumnName(i),
							incomingTable.isDouble(i));
					m_rowSettings[i].setBarColor(incomingTable.getBackgroundColor());
					if (m_rowSettings[i].isDouble()) {
						if (m_rowSettings[i].getMaxValue() != m_rowSettings[i].getMinValue()) {
							format[i] = "#";
							double interval = (m_rowSettings[i].getMaxValue() - m_rowSettings[i].getMinValue());
							if (interval <= 10 && interval > 1) {
								format[i] = format[i] + ".##";
							}
							if (interval != 0 && interval <= 1) {
								format[i] = format[i] + ".#";
								while (interval <= 1) {
									format[i] = format[i] + "#";
									interval = interval * 10;
								}
							}
						}
					}
				}
			}
		}
		if (comparedTableInfo == 2){ // Settings has more columns than incoming data -> some columns are missing.
			for( int i = 0; i< incomingTable.getColumnCount(); i++){
				int j = m_correspondingColumns.get(i)[1];
				m_rowSettings[i].set(
						settingsTable.getValidMin(j),
						settingsTable.getMin(j),
						settingsTable.getMax(j),
						settingsTable.getValidMax(j),
						settingsTable.isSelected(j),
						settingsTable.getColumnName(j),
						settingsTable.isDouble(j));
				m_rowSettings[i].setBarColor(settingsTable.getBackgroundColor());
				if (m_rowSettings[i].isDouble()) {
					if (m_rowSettings[i].getMaxValue() != m_rowSettings[i].getMinValue()) {
						format[i] = "#";
						double interval = (m_rowSettings[i].getMaxValue() - m_rowSettings[i].getMinValue());
						if (interval <= 10 && interval > 1) {
							format[i] = format[i] + ".##";
						}
						if (interval != 0 && interval <= 1) {
							format[i] = format[i] + ".#";
							while (interval <= 1) {
								format[i] = format[i] + "#";
								interval = interval * 10;
							}
						}
					}
				}
			}
		}
		if (comparedTableInfo == -1){ // The two tables are considered unlinked. Load the incoming data.
			for( int i = 0; i< m_rowSettings.length; i++){
				m_rowSettings[i].set(
						incomingTable.getValidMin(i),
						incomingTable.getMin(i),
						incomingTable.getMax(i),
						incomingTable.getValidMax(i),
						incomingTable.isSelected(i),
						incomingTable.getColumnName(i),
						incomingTable.isDouble(i));
				m_rowSettings[i].setBarColor(incomingTable.getBackgroundColor());
				if (m_rowSettings[i].isDouble()) {
					if (m_rowSettings[i].getMaxValue() != m_rowSettings[i].getMinValue()) {
						format[i] = "#";
						double interval = (m_rowSettings[i].getMaxValue() - m_rowSettings[i].getMinValue());
						if (interval <= 10 && interval > 1) {
							format[i] = format[i] + ".##";
						}
						if (interval != 0 && interval <= 1) {
							format[i] = format[i] + ".#";
							while (interval <= 1) {
								format[i] = format[i] + "#";
								interval = interval * 10;
							}
						}
					}
				}
			}
		}
	}
}
