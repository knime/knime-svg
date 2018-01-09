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
 * ---------------------------------------------------------------------
 *
 * Created on Mar 17, 2013 by wiswedel
 */
package org.knime.base.node.preproc.colconvert.stringtosvg;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.CheckUtils;

/**
 * Config proxy of node.
 *
 * @author Marcel Hanser
 */
final class StringToSvgConfig {

    private static final String FAIL_IF_INVALID = "fail-on-invalid";

    private static final String NEW_COLUMN_NAME = "new-column-name";

    private static final String IMAGE_COLUMN = "image-column";

    private boolean m_failOnInvalid = true;

    private String m_imageColName;

    private String m_newColumnName;

    /**
     * Loads the configuration for the dialog with corresponding default values.
     *
     * @param settings the settings to load
     * @param spec ...
     * @throws NotConfigurableException if there is no way to guess defaults
     */
    void loadConfigurationInDialog(final NodeSettingsRO settings, final DataTableSpec spec)
        throws NotConfigurableException {
        m_imageColName = settings.getString(IMAGE_COLUMN, null);
        if (m_imageColName == null) {
            try {
                guessDefaults(spec);
            } catch (InvalidSettingsException e) {
                throw new NotConfigurableException("No valid input column available");
            }
        }
        m_failOnInvalid = settings.getBoolean(FAIL_IF_INVALID, m_failOnInvalid);
        m_newColumnName = settings.getString(NEW_COLUMN_NAME, m_newColumnName);
    }

    /**
     * @param spec the spec
     * @throws InvalidSettingsException thrown if there is no good default setting
     */
    void guessDefaults(final DataTableSpec spec) throws InvalidSettingsException {
        String winColumn = null;
        for (DataColumnSpec col : spec) {
            DataType type = col.getType();
            if (type.isCompatible(StringValue.class)) {
                if (type.isCompatible(XMLValue.class)) {
                    //we found a xml value so break.
                    winColumn = col.getName();
                    break;
                }
                winColumn = col.getName();
            }

        }
        CheckUtils.checkSetting(winColumn != null, "No valid input column available");
        m_imageColName = winColumn;
        m_failOnInvalid = true;
        m_newColumnName = DataTableSpec.getUniqueColumnName(spec, "SVG from " + m_imageColName);
    }

    /**
     * Loads the configuration for the model.
     *
     * @param settings the settings to load
     * @throws InvalidSettingsException if the settings are invalid
     */
    void loadConfigurationInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_failOnInvalid = settings.getBoolean(FAIL_IF_INVALID);
        m_newColumnName = settings.getString(NEW_COLUMN_NAME);
        m_imageColName = settings.getString(IMAGE_COLUMN);
    }

    /**
     * Save current configuration.
     *
     * @param settings To save to.
     */
    void saveConfiguration(final NodeSettingsWO settings) {
        settings.addString(IMAGE_COLUMN, m_imageColName);
        settings.addBoolean(FAIL_IF_INVALID, m_failOnInvalid);
        settings.addString(NEW_COLUMN_NAME, m_newColumnName);
    }

    /**
     * @return the failOnInvalid
     */
    public boolean isFailOnInvalid() {
        return m_failOnInvalid;
    }

    /**
     * @param failOnInvalid the failOnInvalid to set
     */
    public void setFailOnInvalid(final boolean failOnInvalid) {
        m_failOnInvalid = failOnInvalid;
    }

    /**
     * @return the newColumnName
     */
    public String getNewColumnName() {
        return m_newColumnName;
    }

    /**
     * @param newColumnName the newColumnName to set
     */
    public void setNewColumnName(final String newColumnName) {
        m_newColumnName = newColumnName;
    }

    /**
     * @return the imageColName
     */
    public String getImageColName() {
        return m_imageColName;
    }

    /**
     * @param imageColName the imageColName to set
     */
    public void setImageColName(final String imageColName) {
        m_imageColName = imageColName;
    }
}
