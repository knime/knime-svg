/*
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
 * --------------------------------------------------------------------- *
 *
 * History
 *   18.01.2007 (mb): created
 */
package org.knime.ext.svg.node.radarplot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Panel that holds controls to change settings of the
 * corresponding model.
 * @see ColumnSettingsModel
 *
 * @author M. Berthold, University of Konstanz
 */
public class ColumnSettingsPanel extends JPanel {
    private JCheckBox m_checkbox;
    private JLabel m_label;
    private JLabel m_labelMin;
    private JLabel m_labelMax;
    private JSpinner m_spinnerMin;
    private JSpinner m_spinnerMax;

    private double m_min = Double.NEGATIVE_INFINITY;
    private double m_max = Double.POSITIVE_INFINITY;

    private ColumnSettingsModel m_myModel;

    public ColumnSettingsPanel(final String confName, final String label) {
        m_myModel = new ColumnSettingsModel(confName);
        m_checkbox = new JCheckBox();
        m_checkbox.setEnabled(true);
        m_checkbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent arg0) {
                m_labelMin.setEnabled(m_checkbox.isSelected());
                m_spinnerMin.setEnabled(m_checkbox.isSelected());
                m_labelMax.setEnabled(m_checkbox.isSelected());
                m_spinnerMax.setEnabled(m_checkbox.isSelected());
            }
        });
        m_label = new JLabel(label + ":  ");
        m_labelMin = new JLabel("min=");
        m_labelMax = new JLabel("max=");
        m_spinnerMin = new JSpinner(new SpinnerNumberModel(0.0, m_min, m_max, 1.0));
        JSpinner.DefaultEditor editor =
            (JSpinner.DefaultEditor)m_spinnerMin.getEditor();
        editor.getTextField().setColumns(6);
        editor.getTextField().setFocusLostBehavior(JFormattedTextField.COMMIT);
        m_spinnerMax = new JSpinner(new SpinnerNumberModel(1.0, m_min, m_max, 1.0));
        editor = (JSpinner.DefaultEditor)m_spinnerMax.getEditor();
        editor.getTextField().setColumns(6);
        editor.getTextField().setFocusLostBehavior(JFormattedTextField.COMMIT);
        this.add(m_checkbox);
        this.add(m_label);
        this.add(m_labelMin);
        this.add(m_spinnerMin);
        this.add(m_labelMax);
        this.add(m_spinnerMax);
    }

    public double getMinRange() throws ParseException {
        m_spinnerMin.commitEdit();
        return ((Double)m_spinnerMin.getValue()).doubleValue();
    }

    public double getMaxRange() throws ParseException {
        m_spinnerMax.commitEdit();
        return ((Double)m_spinnerMax.getValue()).doubleValue();
    }

    public void setDomain(final double min, final double max) {
        m_min = min;
        m_max = max;
        double minRange = (Double)m_spinnerMin.getValue();
        double maxRange = (Double)m_spinnerMax.getValue();
        if (minRange > m_max) {
            minRange = m_max;
        }
        if (minRange < m_min) {
            minRange = m_min;
        }
        if (maxRange < m_min) {
            maxRange = m_min;
        }
        if (maxRange > m_max) {
            maxRange = m_max;
        }
        ((SpinnerNumberModel)(m_spinnerMin.getModel())).setMinimum(m_min);
        ((SpinnerNumberModel)(m_spinnerMin.getModel())).setMaximum(m_max);
        ((SpinnerNumberModel)(m_spinnerMin.getModel())).setStepSize(0.1);
        ((SpinnerNumberModel)(m_spinnerMin.getModel())).setValue(minRange);
        ((SpinnerNumberModel)(m_spinnerMax.getModel())).setMinimum(m_min);
        ((SpinnerNumberModel)(m_spinnerMax.getModel())).setMaximum(m_max);
        ((SpinnerNumberModel)(m_spinnerMax.getModel())).setStepSize(0.1);
        ((SpinnerNumberModel)(m_spinnerMax.getModel())).setValue(maxRange);
    }

    void loadSettings(final NodeSettingsRO settings) {
        m_myModel.loadSettings(settings);
        m_spinnerMin.setValue(m_myModel.getMinRange());
        m_spinnerMax.setValue(m_myModel.getMaxRange());
        m_checkbox.setSelected(m_myModel.isSelected());
        setDomain(m_min, m_max);  // fix values if they are out of range!
    }

    void saveSettings(final NodeSettingsWO settings) throws ParseException {
        m_myModel.setMinRange(getMinRange());
        m_myModel.setMaxRange(getMaxRange());
        m_myModel.setSelected(m_checkbox.isSelected());
        m_myModel.saveSettings(settings);
    }

}
