/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
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
	
	/**
	 * Checkbox to specify whether node should fail on missing columns or not.
	 * @since 2.6
	 */
	private JCheckBox m_failOnMissingCols;

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
            @Override
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
            @Override
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

        allPanel.add(new JPanel(), c);
		
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 5;
        c.gridy = 5;
        c.gridheight = 1;
        c.gridwidth = 1;

        m_failOnMissingCols =
                new JCheckBox("Fail if any column is missing");

        allPanel.add(m_failOnMissingCols, c);

		c.weightx = 0.3;
		c.weighty = 0;
		c.gridx = 6;
		c.gridy = 5;
		c.gridwidth = GridBagConstraints.REMAINDER;

		allPanel.add(new JPanel(), c);

		this.addTab("Column Settings", allPanel);
	}
	
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        String m_confName = RadarplotAppenderNodeModel.COLUMNRANGEPREFIX;
        ColumnSettingsTable colSettings = new ColumnSettingsTable(m_confName);
        colSettings.loadSettingsDialog(settings, specs[0]);

        // load colors
        m_backgroundColor = colSettings.getBackgroundColor();
        m_bendColor = colSettings.getBendColor();
        m_outlyingBendColor = colSettings.getOutlyingBendColor();
        m_intervalColor = colSettings.getIntervalColor();

        int nrAttr = 0;
        if (colSettings.isProper() >= 0) {
            nrAttr = colSettings.getRowCount();
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

        final JButton backgroundColor = createBackgroundButton();
        buttonPanel.add(backgroundColor, c);

        final JButton intervalColor = createIntervalButton();
        buttonPanel.add(intervalColor, c);

        final JButton bendColor = createBendButton();
        buttonPanel.add(bendColor, c);

        final JButton outlyingBendColor = outlyingBendButton();
        buttonPanel.add(outlyingBendColor, c);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.NORTH;
        data.add(buttonPanel, c);
        c.fill = GridBagConstraints.BOTH;

        if (nrAttr != 0 && colSettings.isProper() >= 0) {
            setupRows(colSettings);

            for (int i = 0; i < nrAttr; i++) {
                if (m_rowSettings[i].isDouble()) {
                    createSliderRow(data, c, i);
                }
            }
            data.setVisible(true);
            m_sliders.add(data);
            m_sliders.validate();

        }

        m_failOnMissingCols.setSelected(colSettings.getFailOnMissingCols());
    }

    /**
     * Creates and returns the background color button.
     * @return The background color button.
     * @since 2.6
     */
    private JButton createBackgroundButton() {
        final JButton backgroundColor = new JButton("Background color...");
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
        return backgroundColor;
    }
    
    /**
     * Creates and returns the interval color button.
     * @return The interval color button.
     * @since 2.6
     */
    private JButton createIntervalButton() {
        final JButton intervalColor = new JButton("Range color...");
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
        return intervalColor;
    }
    
    /**
     * Creates and returns the bend color button.
     * @return The bend color button.
     * @since 2.6
     */
    private JButton createBendButton() {
        final JButton bendColor = new JButton("Ribbon color...");
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
        return bendColor;
    }
    
    /**
     * Creates and returns the out lying bend color button.
     * @return The out lying bend color button.
     * @since 2.6
     */
    private JButton outlyingBendButton() {
        final JButton outlyingBendColor = new JButton("Ribbon color 2...");
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
        return outlyingBendColor;
    }    
    
    /**
     * Creates a row containing a checkbox and a jslider to specify the 
     * settings of a column.
     * @param data The panel to add the components
     * @param c A gridbagconstraints specifying the layout settings
     * @param rowIndex The index of the row
     * @since 2.6
     */
    private void createSliderRow(JPanel data, GridBagConstraints c, 
            int rowIndex) {        
        boolean isValid = (m_rowSettings[rowIndex].getMinValue() 
                        != m_rowSettings[rowIndex].getMaxValue());
        DecimalFormat g = null;
        if (isValid) {
            g = new DecimalFormat(format[rowIndex]);
        } else {
            g = new DecimalFormat("#.#");
        }

        JLabel space1 = new JLabel();
        space1.setPreferredSize(new Dimension(25, 15));
        c.gridx = 0;
        c.gridy = 3 * rowIndex + 1;
        c.gridheight = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(0, 15, 0, 0);
        data.add(space1, c);

        String colName = "";

        if (isValid) {
            colName = m_rowSettings[rowIndex].getName();
        } else {
            colName = m_rowSettings[rowIndex].getName() + " (DISABLED)";
        }

        JLabel label = new JLabel(colName);
        c.gridx = 0;
        c.gridy = 3 * rowIndex + 2;
        c.gridheight = 2;
        c.gridwidth = 1;
        data.add(label, c);

        final JCheckBox box = new JCheckBox("Display? ");
        box.setSelected(m_rowSettings[rowIndex].isEnabled());
        c.gridx = 1;
        c.gridy = 3 * rowIndex + 2;
        c.gridheight = 2;
        c.gridwidth = 1;
        c.insets = new Insets(0, 10, 0, 0);
        data.add(box, c);
        if (!isValid) {
            box.setSelected(false);
            m_rowSettings[rowIndex].setEnabled(false);
            box.setEnabled(false);
        }

        double lowerValue = 0;
        double upperValue = 1;
        double minValue = lowerValue;
        double maxValue = upperValue;
        if (isValid) {
            lowerValue = m_rowSettings[rowIndex].getLowerValue();
            upperValue = m_rowSettings[rowIndex].getUpperValue();
            minValue = m_rowSettings[rowIndex].getMinValue();
            maxValue = m_rowSettings[rowIndex].getMaxValue();
        }
        final RadarSlider slider =
                new RadarSlider(lowerValue, upperValue, 0,
                        minValue, maxValue);
        slider.setUI(new RadarSliderUI(slider));
        ((RadarSliderUI)slider.getUI())
                .setBarColor(m_intervalColor);
        ((RadarSliderUI)slider.getUI())
                .setBackgroundColor(m_backgroundColor);
        slider.setSize((int)(slider.getSize().width * 0.75),
                slider.getSize().height);
        c.gridx = 2;
        c.gridy = 3 * rowIndex + 2;
        c.gridheight = 1;
        c.gridwidth = 4;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 0, 0);
        data.add(slider, c);
        m_sliderList.add(slider);

        if (!isValid) {
            slider.setEnabled(false);
        }

        final JLabel spacer = new JLabel();
        c.insets = new Insets(0, 10, 0, 0);
        c.gridx = 6;
        c.gridy = 3 * rowIndex + 2;
        c.weightx = 0;
        c.gridheight = 2;
        c.gridwidth = GridBagConstraints.REMAINDER;
        data.add(spacer, c);

        JLabel ValueLabel1 = new JLabel("Lower value: ");
        c.gridx = 2;
        c.gridy = 3 * (rowIndex + 1);
        c.gridheight = 1;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 0, 0);
        data.add(ValueLabel1, c);

        int labelWidth = 0;
        String lowerLabelText = "";
        String upperLabelText = "";
        if (isValid) {
            labelWidth = format[rowIndex].length() * 6;
            lowerLabelText = g.format(slider.getValue(1));
            upperLabelText = g.format(slider.getValue(2));

        } else {
            labelWidth = 24;
        }

        final JLabel lowerValueLabel = new JLabel(lowerLabelText);
        lowerValueLabel.setPreferredSize(new Dimension(labelWidth,
                15));
        c.gridx = 3;
        c.gridy = 3 * (rowIndex + 1);
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        data.add(lowerValueLabel, c);

        JLabel ValueLabel2 = new JLabel("Upper value: ");
        c.gridx = 4;
        c.gridy = 3 * (rowIndex + 1);
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        data.add(ValueLabel2, c);

        final JLabel upperValueLabel = new JLabel(upperLabelText);
        upperValueLabel.setPreferredSize(new Dimension(labelWidth,
                15));
        c.gridx = 5;
        c.gridy = 3 * (rowIndex + 1);
        c.gridheight = 1;
        c.insets = new Insets(0, 0, 0, 15);
        c.gridwidth = 1;
        c.weightx = 1;
        data.add(upperValueLabel, c);

        final int counter = rowIndex;

        slider.addChangeListener(new ChangeListener() {
            String numberformat = format[counter];

            @Override
            public void stateChanged(final ChangeEvent e) {
                DecimalFormat f = new DecimalFormat(numberformat);
                double minimum = slider.doubleGetMinimum();
                double maximum = slider.doubleGetMaximum();
                double lVal = slider.getValue(1);
                double uVal = slider.getValue(2);
                m_rowSettings[counter].setLowerValue(lVal);
                m_rowSettings[counter].setUpperValue(uVal);
                m_rowSettings[counter].setMinValue(minimum);
                m_rowSettings[counter].setMaxValue(maximum);
                lowerValueLabel.setText(f.format(lVal));
                upperValueLabel.setText(f.format(uVal));
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

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
	    if (m_rowSettings == null) {
	        throw new InvalidSettingsException("No attributes set to save!");
	    }
	    
		NodeSettingsWO mySettings = settings
				.addNodeSettings(RadarplotAppenderNodeModel.COLUMNRANGEPREFIX);
		mySettings.addInt("NRATTR", m_rowSettings.length);
		mySettings.addBoolean("FAILONMISSINGCOLS", 
		        m_failOnMissingCols.isSelected());
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
	
    /**
     * Takes over all loaded and matched settings and prepares them for use
     * on components such as jslider and others. 
     * @param settingsTable The loaded and matched settings to show in dialog.
     */
    private void setupRows(final ColumnSettingsTable settingsTable) {
        for (int i = 0; i < settingsTable.getColumnCount(); i++) {
            m_rowSettings[i].set(settingsTable.getValidMin(i),
                    settingsTable.getMin(i), settingsTable.getMax(i),
                    settingsTable.getValidMax(i), settingsTable.isSelected(i),
                    settingsTable.getColumnName(i), settingsTable.isDouble(i));
            m_rowSettings[i].setBarColor(settingsTable.getBackgroundColor());
            if (m_rowSettings[i].isDouble()) {
                if (m_rowSettings[i].getMaxValue() != m_rowSettings[i]
                        .getMinValue()) {
                    format[i] = "#";
                    double interval =
                            (m_rowSettings[i].getMaxValue() - m_rowSettings[i]
                                    .getMinValue());
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
