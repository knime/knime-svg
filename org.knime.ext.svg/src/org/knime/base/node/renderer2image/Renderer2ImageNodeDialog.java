/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * ---------------------------------------------------------------------
 *
 * History
 *   18.12.2010 (meinl): created
 */
package org.knime.base.node.renderer2image;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.renderer2image.Renderer2ImageSettings.ImageType;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.renderer.DataValueRendererFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;

/**
 * This is the dialog for the renderer-to-svg node. It lets the user select a column and subsequently one of the
 * registered renderers for this column.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class Renderer2ImageNodeDialog extends NodeDialogPane {
    private final Renderer2ImageSettings m_settings = new Renderer2ImageSettings();

    @SuppressWarnings("unchecked")
    private final ColumnSelectionComboxBox m_column = new ColumnSelectionComboxBox((Border)null, DataValue.class);

    private final DefaultComboBoxModel<String> m_rendererComboModel = new DefaultComboBoxModel<>();

    private final JComboBox<String> m_rendererDescriptions = new JComboBox<>(m_rendererComboModel);

    private final JComboBox<ImageType> m_imageTypes = new JComboBox<>();

    private final JLabel m_pngSize = new JLabel("Image size   ");

    private final JSpinner m_pngWidth = new JSpinner(new SpinnerNumberModel(100, 1, 100000, 1));

    private final JLabel m_x = new JLabel (" x ");

    private final JSpinner m_pngHeight = new JSpinner(new SpinnerNumberModel(100, 1, 100000, 1));

    private final JTextField m_newColumnName = new JTextField(10);

    private final JRadioButton m_replaceColumn = new JRadioButton("Replace input column");

    private final JRadioButton m_appendColumn = new JRadioButton("Append column");


    Renderer2ImageNodeDialog() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.insets = new Insets(2, 1, 2, 1);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(new JLabel("Column   "), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        p.add(m_column, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy++;
        c.weightx = 0;
        p.add(new JLabel("Renderer   "), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        p.add(m_rendererDescriptions, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy++;
        c.weightx = 0;
        p.add(new JLabel("Image type   "), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        p.add(m_imageTypes, c);
        for (ImageType t : ImageType.values()) {
            m_imageTypes.addItem(t);
        }

        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy++;
        c.weightx = 0;
        p.add(new JLabel("Image size   "), c);
        c.gridx = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        p.add(m_pngWidth, c);
        c.gridx = 2;
        p.add(m_x, c);
        c.gridx = 3;
        p.add(m_pngHeight, c);

        m_column.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                m_rendererComboModel.removeAllElements();

                if (m_column.getSelectedItem() != null) {
                    DataColumnSpec cs = (DataColumnSpec)m_column.getSelectedItem();
                    DataType type = cs.getType();
                    for (DataValueRendererFactory rFac : type.getRendererFactories()) {
                        m_rendererComboModel.addElement(rFac.getDescription());
                    }
                }
            }
        });

        m_imageTypes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                imageTypeChanged();
            }
        });

        c.gridx = 0;
        c.gridy++;
        p.add(m_appendColumn, c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(m_newColumnName,c );

        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.weightx = 0;
        p.add(m_replaceColumn, c);

        ButtonGroup bg = new ButtonGroup();
        bg.add(m_appendColumn);
        bg.add(m_replaceColumn);
        m_appendColumn.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                m_newColumnName.setEnabled(m_appendColumn.isSelected());
            }
        });

        addTab("Default Settings", p);
    }


    void imageTypeChanged() {
        boolean isPng = ImageType.Png.equals(m_imageTypes.getSelectedItem());
        m_pngSize.setEnabled(isPng);
        m_pngWidth.setEnabled(isPng);
        m_x.setEnabled(isPng);
        m_pngHeight.setEnabled(isPng);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        m_settings.loadSettingsForDialog(settings);

        m_column.update(specs[0], m_settings.columnName());
        m_rendererDescriptions.setSelectedItem(m_settings.rendererDescription());
        m_imageTypes.setSelectedItem(m_settings.imageType());
        imageTypeChanged();

        m_pngWidth.setValue(m_settings.pngSize().width);
        m_pngHeight.setValue(m_settings.pngSize().height);

        m_replaceColumn.setSelected(m_settings.replaceColumn());
        m_appendColumn.setSelected(!m_settings.replaceColumn());
        m_newColumnName.setEnabled(!m_settings.replaceColumn());
        if (m_settings.newColumnName() != null) {
            m_newColumnName.setText(m_settings.newColumnName());
        } else {
            String colName =
                m_column.getSelectedColumn() + " rendered with " + m_rendererDescriptions.getSelectedItem().toString();
            m_newColumnName.setText(colName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_settings.columnName(m_column.getSelectedColumn());
        if (m_rendererDescriptions.getSelectedItem() == null) {
            throw new InvalidSettingsException("No renderer selected");
        }
        m_settings.rendererDescription(m_rendererDescriptions.getSelectedItem().toString());
        m_settings.imageType((ImageType)m_imageTypes.getSelectedItem());
        m_settings.pngSize(new Dimension((Integer) m_pngWidth.getValue(), (Integer) m_pngHeight.getValue()));
        m_settings.replaceColumn(m_replaceColumn.isSelected());
        if (m_settings.replaceColumn()) {
            m_settings.newColumnName(null);
        } else {
            m_settings.newColumnName(m_newColumnName.getText());
        }

        m_settings.saveSettings(settings);
    }
}
