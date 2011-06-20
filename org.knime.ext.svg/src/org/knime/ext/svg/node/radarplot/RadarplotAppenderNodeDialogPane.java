/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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

import javax.swing.BoxLayout;
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

    private JPanel m_myPanel;

    // private ColumnSettingsTable m_myTable;
    private JPanel m_sliders = new JPanel(new GridLayout());

    private LinkedList<RadarSlider> m_sliderList =
            new LinkedList<RadarSlider>();

    private RadarplotAppenderRowSettings[] m_rowSettings;

    private String format;

    // private JCheckbox m_includeBand;
    private DataTableSpec m_oldSpec = null;

    private ColumnSettingsPanel[] m_ranges;

    private int m_nrDoubleCols;

    private Color m_backgroundColor = new Color(175, 220, 240);

    private Color m_intervalColor = new Color(105, 150, 170);

    private Color m_bendColor = Color.GREEN;

    private Color m_outlyingBendColor = Color.RED;

    public RadarplotAppenderNodeDialogPane() {
        JPanel allPanel = new JPanel();
        allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(m_sliders);
        allPanel.add(scrollPane);
        this.addTab("Column Settings", allPanel);
    }

    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        try {
            String m_confName = RadarplotAppenderNodeModel.COLUMNRANGEPREFIX;
            ColumnSettingsTable testTable = new ColumnSettingsTable(m_confName);
            ColumnSettingsTable testTable2 =
                    new ColumnSettingsTable(m_confName);
            testTable.setNewSpec(specs[0]);
            testTable2.loadSettings(settings);
            boolean equals = testTable.equals(testTable2);

            JPanel data = new JPanel();
            m_sliders.removeAll();
            data.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 0;
            c.weighty = 0;
            c.fill = GridBagConstraints.BOTH;

            NodeSettingsRO mySettings = settings.getNodeSettings(m_confName);
            int nrAttr = mySettings.getInt("NRATTR");
            m_rowSettings = new RadarplotAppenderRowSettings[nrAttr];
            for (int i = 0; i < nrAttr; i++) {
                m_rowSettings[i] = new RadarplotAppenderRowSettings();
            }

            JPanel buttonPanel = new JPanel();

            final JButton backgroundColor = new JButton("Background color...");
            // c.gridx = 1;
            // c.gridy =0;
            // c.gridheight = 1;
            // c.gridwidth = 1;
            buttonPanel.add(backgroundColor, c);

            backgroundColor.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    Color barColor =
                            JColorChooser.showDialog(backgroundColor,
                                    "Choose Background Color",
                                    m_backgroundColor);
                    if (barColor != null) {
                        m_backgroundColor = barColor;
                        for (RadarSlider slider : m_sliderList) {
                            ((RadarSliderUI)slider.getUI())
                                    .setBackgroundColor(m_backgroundColor);
                        }
                    }
                }
            });

            final JButton intervalColor = new JButton("Range color...");
            // c.gridx = 3;
            // c.gridy =0;
            // c.gridheight = 1;
            // c.gridwidth = 1;
            buttonPanel.add(intervalColor, c);

            intervalColor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    Color barColor =
                            JColorChooser.showDialog(intervalColor,
                                    "Choose Range Color", m_intervalColor);
                    if (barColor != null) {
                        m_intervalColor = barColor;
                        for (RadarSlider slider : m_sliderList) {
                            ((RadarSliderUI)slider.getUI())
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
                    Color barColor =
                            JColorChooser.showDialog(bendColor,
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
                    Color outlierColor =
                            JColorChooser.showDialog(outlyingBendColor,
                                    "Choose alternate Ribbon Color",
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
            data.add(buttonPanel, c);

            if (nrAttr != 0) {
                for (int i = 0; i < nrAttr; i++) {
                    NodeSettingsRO thisSettings =
                            mySettings.getNodeSettings("Attribute_" + i);
                    m_rowSettings[i].set(thisSettings.getDouble("VALIDMIN"),
                            thisSettings.getDouble("MIN"),
                            thisSettings.getDouble("MAX"),
                            thisSettings.getDouble("VALIDMAX"),
                            thisSettings.getBoolean("SELECTED"),
                            thisSettings.getString("NAME"),
                            thisSettings.getBoolean("ISDOUBLE"));
                    m_rowSettings[i].setBarColor(new Color(thisSettings.getInt(
                            "RED", 0), thisSettings.getInt("GREEN", 0),
                            thisSettings.getInt("BLUE", 0)));
                    if (m_rowSettings[i].isDouble()) {
                        if (equals == true) {
                            if (m_rowSettings[i].getMaxValue() != m_rowSettings[i]
                                    .getMinValue()) {
                                format = "#";
                                double j =
                                        (m_rowSettings[i].getMaxValue() - m_rowSettings[i]
                                                .getMinValue());
                                if (j <= 10 && j > 1) {
                                    format = format + ".##";
                                }
                                if (j != 0 && j <= 1) {
                                    format = format + ".#";
                                    while (j <= 1) {
                                        format = format + "#";
                                        j = j * 10;
                                    }
                                }
                                DecimalFormat g = new DecimalFormat(format);

                                JLabel space1 = new JLabel();
                                space1.setPreferredSize(new Dimension(25, 15));
                                c.gridx = 0;
                                c.gridy = 3 * i + 1;
                                c.gridheight = 1;
                                c.gridwidth = GridBagConstraints.REMAINDER;
                                c.insets = new Insets(0, 15, 0, 0);
                                data.add(space1, c);

                                JLabel label =
                                        new JLabel(m_rowSettings[i].getName());
                                c.gridx = 0;
                                c.gridy = 3 * i + 2;
                                c.gridheight = 2;
                                c.gridwidth = 1;
                                data.add(label, c);

                                final JCheckBox box =
                                        new JCheckBox("Display? ");
                                box.setSelected(m_rowSettings[i].isEnabled());
                                c.gridx = 1;
                                c.gridy = 3 * i + 2;
                                c.gridheight = 2;
                                c.gridwidth = 1;
                                c.insets = new Insets(0, 10, 0, 0);
                                data.add(box, c);

                                final RadarSlider slider =
                                        new RadarSlider(
                                                m_rowSettings[i]
                                                        .getLowerValue(),
                                                m_rowSettings[i]
                                                        .getUpperValue(), 0,
                                                m_rowSettings[i].getMinValue(),
                                                m_rowSettings[i].getMaxValue());
                                slider.setUI(new RadarSliderUI(slider));
                                ((RadarSliderUI)slider.getUI())
                                        .setBarColor(m_intervalColor);
                                ((RadarSliderUI)slider.getUI())
                                        .setBackgroundColor(m_backgroundColor);
                                slider.setSize(
                                        (int)(slider.getSize().width * 0.75),
                                        slider.getSize().height);
                                c.gridx = 2;
                                c.gridy = 3 * i + 2;
                                c.gridheight = 1;
                                c.gridwidth = 4;
                                c.weightx = 1;
                                c.insets = new Insets(0, 0, 0, 0);
                                data.add(slider, c);
                                m_sliderList.add(slider);

                                final JLabel spacer = new JLabel();
                                c.insets = new Insets(0, 10, 0, 0);
                                c.gridx = 6;
                                c.gridy = 3 * i + 2;
                                c.weightx = 0;
                                c.gridheight = 2;
                                c.gridwidth = GridBagConstraints.REMAINDER;
                                data.add(spacer, c);

                                JLabel ValueLabel1 =
                                        new JLabel("Lower value: ");
                                c.gridx = 2;
                                c.gridy = 3 * (i + 1);
                                c.gridheight = 1;
                                c.weightx = 0;
                                c.gridwidth = 1;
                                c.insets = new Insets(0, 0, 0, 0);
                                data.add(ValueLabel1, c);

                                final JLabel lowerValueLabel =
                                        new JLabel(g.format(slider.getValue(1)));
                                // lowerValueLabel.setPreferredSize(new
                                // Dimension(25,15));
                                c.gridx = 3;
                                c.gridy = 3 * (i + 1);
                                c.gridheight = 1;
                                c.gridwidth = 1;
                                c.weightx = 1;
                                data.add(lowerValueLabel, c);

                                JLabel ValueLabel2 =
                                        new JLabel("Upper value: ");
                                c.gridx = 4;
                                c.gridy = 3 * (i + 1);
                                c.gridheight = 1;
                                c.gridwidth = 1;
                                c.weightx = 0;
                                data.add(ValueLabel2, c);

                                final JLabel upperValueLabel =
                                        new JLabel(g.format(slider.getValue(2)));
                                upperValueLabel.setPreferredSize(new Dimension(
                                        25, 15));
                                c.gridx = 5;
                                c.gridy = 3 * (i + 1);
                                c.gridheight = 1;
                                c.insets = new Insets(0, 0, 0, 15);
                                c.gridwidth = 1;
                                data.add(upperValueLabel, c);

                                final int counter = i;

                                slider.addChangeListener(new ChangeListener() {

                                    String numberformat = format;

                                    @Override
                                    public void stateChanged(final ChangeEvent e) {
                                        DecimalFormat f =
                                                new DecimalFormat(numberformat);
                                        double minimum =
                                                slider.doubleGetMinimum();
                                        double maximum =
                                                slider.doubleGetMaximum();
                                        double lowerValue = slider.getValue(1);
                                        double upperValue = slider.getValue(2);
                                        m_rowSettings[counter]
                                                .setLowerValue(lowerValue);
                                        m_rowSettings[counter]
                                                .setUpperValue(upperValue);
                                        m_rowSettings[counter]
                                                .setMinValue(minimum);
                                        m_rowSettings[counter]
                                                .setMaxValue(maximum);
                                        lowerValueLabel.setText(f
                                                .format(lowerValue));
                                        upperValueLabel.setText(f
                                                .format(upperValue));
                                    }
                                });

                                box.addChangeListener(new ChangeListener() {
                                    @Override
                                    public void stateChanged(final ChangeEvent e) {
                                        m_rowSettings[counter].setEnabled(box
                                                .isSelected());
                                    }
                                });
                            }
                        }
                    }
                }
                data.setVisible(true);
                m_sliders.add(data);
                m_sliders.validate();

            }
        } catch (Exception e) {
        }
    }


    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        NodeSettingsWO mySettings =
                settings.addNodeSettings(RadarplotAppenderNodeModel.COLUMNRANGEPREFIX);
        mySettings.addInt("NRATTR", m_rowSettings.length);
        for (int i = 0; i < m_rowSettings.length; i++) {
            NodeSettingsWO thisSettings =
                    mySettings.addNodeSettings("Attribute_" + i);
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

}
