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
 * --------------------------------------------------------------------
 *
 * History
 *   03.07.2007 (cebron): created
 */
package org.knime.base.node.preproc.colconvert.stringtosvg;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.util.ColumnSelectionPanel;

/**
 * Dialog for the Number to String Node. Lets the user choose the columns to use.
 *
 * @author Marcel Hanser
 */
final class StringToSvgNodeDialogPane extends NodeDialogPane {

    private final JCheckBox m_failOnInvalidChecker;

    private final JTextField m_appendColumnField;

    private ColumnSelectionPanel m_columnPanel;

    private DataTableSpec m_dataTableSpec;

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    StringToSvgNodeDialogPane() {
        m_failOnInvalidChecker = new JCheckBox("Fail on invalid input cell");

        m_columnPanel = new ColumnSelectionPanel((Border)null, XMLValue.class, StringValue.class);
        m_columnPanel.setLayout(new GridLayout(1, 0));

        ButtonGroup bg = new ButtonGroup();
        final JRadioButton replaceColumnRadio = new JRadioButton("Replace input column");
        final JRadioButton appendColumnRadio = new JRadioButton("Append new column");
        bg.add(replaceColumnRadio);
        bg.add(appendColumnRadio);

        m_appendColumnField = new JTextField(10);
        appendColumnRadio.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                m_appendColumnField.setEnabled(appendColumnRadio.isSelected());
            }
        });
        m_appendColumnField.addPropertyChangeListener("enabled", new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                appendColumnRadio.setSelected(m_appendColumnField.isEnabled());
            }
        });
        replaceColumnRadio.setSelected(true);
        replaceColumnRadio.doClick();

        JPanel columnConfig = new JPanel(new BorderLayout());
        columnConfig.setBorder(BorderFactory.createTitledBorder("Input Column"));
        columnConfig.add(m_columnPanel, BorderLayout.NORTH);

        JPanel nextPanel = new JPanel(new BorderLayout());
        nextPanel.setLayout(new GridLayout(0, 2));
        nextPanel.add(replaceColumnRadio);
        nextPanel.add(new JLabel(""));
        nextPanel.add(appendColumnRadio);
        nextPanel.add(m_appendColumnField);
        columnConfig.add(nextPanel, BorderLayout.WEST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(columnConfig, BorderLayout.NORTH);
        panel.add(m_failOnInvalidChecker, BorderLayout.SOUTH);

        JPanel lastPanel = new JPanel(new BorderLayout());
        lastPanel.add(panel, BorderLayout.NORTH);
        addTab("Settings", lastPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        StringToSvgConfig config = new StringToSvgConfig();
        m_dataTableSpec = specs[0];
        config.loadConfigurationInDialog(settings, specs[0]);
        m_failOnInvalidChecker.setSelected(config.isFailOnInvalid());
        setText(m_appendColumnField, config.getNewColumnName());
        m_columnPanel.update(specs[0], config.getImageColName(), false, true);
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        StringToSvgConfig config = new StringToSvgConfig();
        config.setFailOnInvalid(m_failOnInvalidChecker.isSelected());
        config.setNewColumnName(m_appendColumnField.isEnabled() ? getText(m_appendColumnField,
            "New column name must not be empty.") : null);
        String selectedColumn = m_columnPanel.getSelectedColumn();
        CheckUtils.checkSetting(m_dataTableSpec.containsName(selectedColumn),
            "column: '%s' is not contained in the given input table", selectedColumn);
        CheckUtils.checkSetting(
            m_dataTableSpec.getColumnSpec(selectedColumn).getType().isCompatible(StringValue.class),
            "column: '%s' is not a valid string/xml column", selectedColumn);
        config.setImageColName(selectedColumn);

        config.saveConfiguration(settings);
    }

    private static void setText(final JTextField appendColumnField, final String newColumnName) {
        appendColumnField.setEnabled(false);
        if (newColumnName != null) {
            appendColumnField.setText(newColumnName);
            appendColumnField.setEnabled(true);
        }
    }

    private static String getText(final JTextField field, final String messageIfNotExist)
        throws InvalidSettingsException {
        String text = field.getText();
        CheckUtils.checkSetting(StringUtils.isNotEmpty(text), messageIfNotExist);
        return text;
    }
}
