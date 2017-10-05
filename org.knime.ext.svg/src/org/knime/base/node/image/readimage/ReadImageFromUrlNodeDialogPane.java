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
 *
 */
package org.knime.base.node.image.readimage;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.apache.commons.lang3.StringUtils;
import org.knime.base.node.image.readimage.ReadImageFromUrlNodeModel.ImageType;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.core.util.Pair;

/**
 * Dialog to Read Image node. It has a column selector and few other controls.
 *
 * @author Marcel Hanser
 */
final class ReadImageFromUrlNodeDialogPane extends NodeDialogPane {
    private static final NodeLogger LOGGER = NodeLogger.getLogger(ReadImageFromUrlNodeDialogPane.class);

    private final ColumnSelectionPanel m_columnPanel;

    private final JCheckBox m_failOnInvalidChecker;

    private final JTextField m_appendColumnField;

    private final Pair<JCheckBox, JTextField> m_readTimeoutInput;

    private final Map<ImageType, JCheckBox> m_typeToCheckBox;

    private DataTableSpec m_dataTableSpec;

    /** Create new dialog. */
    @SuppressWarnings("unchecked")
    ReadImageFromUrlNodeDialogPane() {
        m_columnPanel = new ColumnSelectionPanel((Border)null, StringValue.class);
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
        columnConfig.setBorder(BorderFactory.createTitledBorder("URL Column"));
        columnConfig.add(m_columnPanel, BorderLayout.NORTH);

        JPanel nextPanel = new JPanel(new BorderLayout());
        nextPanel.setLayout(new GridLayout(0, 2));
        nextPanel.add(replaceColumnRadio);
        nextPanel.add(new JLabel(""));
        nextPanel.add(appendColumnRadio);
        nextPanel.add(m_appendColumnField);
        columnConfig.add(nextPanel, BorderLayout.WEST);

        JPanel importPanel = new JPanel(new GridLayout(0, 1));

        m_typeToCheckBox = new LinkedHashMap<ImageType, JCheckBox>();

        m_typeToCheckBox.put(ImageType.SVG, addCheckBox(importPanel, "SVG", null));
        m_typeToCheckBox.put(ImageType.PNG, addCheckBox(importPanel, "PNG", null));

        JPanel lalap = new JPanel(new BorderLayout());
        lalap.add(importPanel, BorderLayout.WEST);
        lalap.add(new JLabel("  "), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(BorderFactory.createTitledBorder("Image types"));
        southPanel.add(lalap, BorderLayout.NORTH);
        southPanel.add(new JLabel("  "), BorderLayout.SOUTH);

        JPanel tabPanel = new JPanel(new BorderLayout());

        tabPanel.add(columnConfig, BorderLayout.NORTH);
        tabPanel.add(southPanel, BorderLayout.CENTER);

        JPanel southernPanel = new JPanel(new GridLayout(2, 2));
        m_failOnInvalidChecker = addCheckBox(southernPanel, "Fail on invalid input", null);
        // dummy label
        southernPanel.add(new JLabel(""));
        final JTextField field = new JTextField(10);

        final JCheckBox timeoutCheckBox = addCheckBox(southernPanel, "Customize image read timeout in seconds", null);

        timeoutCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                field.setEnabled(timeoutCheckBox.isSelected());
            }
        });
        southernPanel.add(field);
        m_readTimeoutInput = Pair.create(timeoutCheckBox, field);
        tabPanel.add(southernPanel, BorderLayout.SOUTH);
        addTab("Settings", tabPanel);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        ReadImageFromUrlConfig config = new ReadImageFromUrlConfig();
        m_dataTableSpec = specs[0];
        config.loadInDialog(settings, specs[0]);
        m_columnPanel.update(specs[0], config.getUrlColName(), false, true);
        m_failOnInvalidChecker.setSelected(config.isFailOnInvalid());

        setText(m_appendColumnField, config.getNewColumnName());

        List<ImageType> types = config.getTypes();
        for (Map.Entry<ImageType, JCheckBox> entry : m_typeToCheckBox.entrySet()) {
            entry.getValue().setSelected(types.contains(entry.getKey()));
        }
        int readTimeout = config.getReadTimeout();
        if (readTimeout > 0) {
            m_readTimeoutInput.getFirst().setSelected(true);
            m_readTimeoutInput.getSecond().setEnabled(true);
            m_readTimeoutInput.getSecond().setText(String.valueOf(readTimeout/1000d));
        } else {
            m_readTimeoutInput.getFirst().setSelected(false);
            m_readTimeoutInput.getSecond().setEnabled(false);
            m_readTimeoutInput.getSecond().setText(
                String.valueOf(getSystemPropertyAsDouble(KNIMEConstants.PROPERTY_URL_TIMEOUT, 1)));
        }
    }

    /**
     * @return trys to format and return the given system property, if it is not set or its not a valid integer
     */
    private double getSystemPropertyAsDouble(final String property, final double defaultDouble) {
        String to = System.getProperty(KNIMEConstants.PROPERTY_URL_TIMEOUT);
        double toReturn = defaultDouble;
        if (to != null) {
            try {
                toReturn = Double.parseDouble(to) / 1000;
            } catch (NumberFormatException ex) {
                LOGGER.error("Illegal value for property " + KNIMEConstants.PROPERTY_URL_TIMEOUT + ": " + to);
            }
        }
        return toReturn;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        ReadImageFromUrlConfig config = new ReadImageFromUrlConfig();

        config.setFailOnInvalid(m_failOnInvalidChecker.isSelected());

        config.setNewColumnName(m_appendColumnField.isEnabled() ? getText(m_appendColumnField,
            "New column name must not be empty.") : null);

        List<ImageType> types = new ArrayList<ImageType>();
        for (Map.Entry<ImageType, JCheckBox> entry : m_typeToCheckBox.entrySet()) {
            if (entry.getValue().isSelected()) {
                types.add(entry.getKey());
            }
        }
        CheckUtils.checkSetting(!types.isEmpty(), "one of %s must be selected.", Arrays.toString(ImageType.values()));
        config.setTypes(types);

        String selectedColumn = m_columnPanel.getSelectedColumn();
        CheckUtils.checkSetting(m_dataTableSpec.containsName(selectedColumn),
            "column: '%s' is not contained in the given input table.", selectedColumn);
        CheckUtils.checkSetting(
            m_dataTableSpec.getColumnSpec(selectedColumn).getType().isCompatible(StringValue.class),
            "column: '%s' is not a string compatible column.", selectedColumn);
        config.setUrlColName(m_columnPanel.getSelectedColumn());

        if (m_readTimeoutInput.getFirst().isSelected()) {
            double readTimeout =
                getDouble(m_readTimeoutInput.getSecond(), "Read timeout must be a valid double number.");
            CheckUtils.checkSetting(readTimeout > 0, "Read timeout must be positive.");
            // convert to milli seconds and set in the configuration
            config.setReadTimeout((int)(readTimeout * 1000));
        }
        config.save(settings);
    }

    private static void setText(final JTextField appendColumnField, final String newColumnName) {
        appendColumnField.setEnabled(false);
        if (newColumnName != null) {
            appendColumnField.setText(newColumnName);
            appendColumnField.setEnabled(true);
        }
    }

    private double getDouble(final JTextField intField, final String exceptionText) throws InvalidSettingsException {
        String text = intField.getText();
        try {
            return Double.valueOf(text);

        } catch (Exception e) {
            throw new InvalidSettingsException(exceptionText);
        }
    }

    private static String getText(final JTextField field, final String messageIfNotExist)
        throws InvalidSettingsException {
        String text = field.getText();
        CheckUtils.checkSetting(StringUtils.isNotEmpty(text), messageIfNotExist);
        return text;
    }

    private static JCheckBox addCheckBox(final JPanel panel, final String string, final String south) {
        final JCheckBox jCheckBox = new JCheckBox(string);
        if (south != null) {
            panel.add(jCheckBox, south);
        } else {
            panel.add(jCheckBox);
        }
        return jCheckBox;
    }
}
