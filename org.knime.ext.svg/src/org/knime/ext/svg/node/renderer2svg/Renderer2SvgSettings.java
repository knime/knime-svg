/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 * ---------------------------------------------------------------------
 *
 * History
 *   18.12.2010 (meinl): created
 */
package org.knime.ext.svg.node.renderer2svg;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This class holds the settings for the renderer-to-svg node.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class Renderer2SvgSettings {
    private String m_columnName;

    private String m_rendererDescription;

    /**
     * Sets the selected column's name.
     *
     * @param colName the column's name
     */
    public void columnName(final String colName) {
        m_columnName = colName;
    }

    /**
     * Returns the selected column's name.
     *
     * @return the column's name
     */
    public String columnName() {
        return m_columnName;
    }

    /**
     * Sets the selected renderer's description.
     *
     * @param rendererDescription the description
     */
    public void rendererDescription(final String rendererDescription) {
        m_rendererDescription = rendererDescription;
    }

    /**
     * Returns the selected renderer's description.
     *
     * @return the description
     */
    public String rendererDescription() {
        return m_rendererDescription;
    }

    /**
     * Saves the settings into the given settings object.
     *
     * @param settings a settings object
     */
    public void saveSettings(final NodeSettingsWO settings) {
        settings.addString("columnName", m_columnName);
        settings.addString("rendererDescription", m_rendererDescription);
    }

    /**
     * Loads the settings from the given settings object.
     *
     * @param settings a settings object
     * @throws InvalidSettingsException if a setting is missing
     */
    public void loadSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_columnName = settings.getString("columnName");
        m_rendererDescription = settings.getString("rendererDescription");
    }

    /**
     * Loads the settings from the given settings object using default values
     * for missing settings.
     *
     * @param settings a settings object
     */
    public void loadSettingsForDialog(final NodeSettingsRO settings) {
        m_columnName = settings.getString("columnName", null);
        m_rendererDescription = settings.getString("rendererDescription", null);
    }
}
