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

import java.text.ParseException;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Model that holds information about one column only: was it selected,
 * which range has been defined.
 * 
 * @author M. Berthold, University of Konstanz
 */
public class ColumnSettingsModel {
    private final String m_confName;
    private boolean m_isSelected = true;
    private double m_minRange;
    private double m_maxRange;
    
    public ColumnSettingsModel(final String confName) {
        m_confName = confName;
    }
    
    public boolean isSelected() {
        return m_isSelected;
    }
    
    public double getMinRange() {
        return m_minRange;
    }
    
    public double getMaxRange() {
        return m_maxRange;
    }
    
    public void setSelected(final boolean sel) {
        m_isSelected = sel;
    }
    
    public void setMinRange(final double min) {
        m_minRange = min;
    }
    
    public void setMaxRange(final double max) {
        m_maxRange = max;
    }
    
    void loadSettings(NodeSettingsRO settings) {
        try {
            NodeSettingsRO mySettings = settings.getNodeSettings(m_confName);
            m_minRange = mySettings.getDouble("MIN");
            m_maxRange = mySettings.getDouble("MAX");
            m_isSelected = mySettings.getBoolean("SELECTED");
        } catch (InvalidSettingsException ise) {
            // ignore - try to set as much as possible in the dialog
        }
    }

    void saveSettings(NodeSettingsWO settings) throws ParseException {
        NodeSettingsWO mySettings = settings.addNodeSettings(m_confName);
        mySettings.addDouble("MIN", m_minRange);
        mySettings.addDouble("MAX", m_maxRange);
        mySettings.addBoolean("SELECTED", m_isSelected);
    }
}
