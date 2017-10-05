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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.knime.base.node.image.readimage.ReadImageFromUrlNodeModel.ImageType;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

/**
 * Configuration object to the Read PNG node.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @author Marcel Hanser
 */
final class ReadImageFromUrlConfig {

    private static final String NEW_COLUMN_NAME = "new-column-name";

    private static final String FAIL_IF_INVALID = "fail-on-invalid";

    private static final String URL_COLUMN = "url-column";

    private static final String CONGIGURED_TYPE = "types";

    private static final String READ_TIMEOUT = "read-timeout";

    private String m_urlColName;

    private String m_newColumnName;

    private boolean m_failOnInvalid = true;

    private int m_readTimeout = -1;

    private List<? extends ImageType> m_types;

    /** @return the urlColName */
    public String getUrlColName() {
        return m_urlColName;
    }

    /** @param urlColName the urlColName to set */
    public void setUrlColName(final String urlColName) {
        m_urlColName = urlColName;
    }

    /** @return the failOnInvalid */
    public boolean isFailOnInvalid() {
        return m_failOnInvalid;
    }

    /** @param failOnInvalid the failOnInvalid to set */
    public void setFailOnInvalid(final boolean failOnInvalid) {
        m_failOnInvalid = failOnInvalid;
    }

    /** @return the newColumnName */
    public String getNewColumnName() {
        return m_newColumnName;
    }

    /** @param newColumnName the newColumnName to set */
    public void setNewColumnName(final String newColumnName) {
        m_newColumnName = newColumnName;
    }

    /**
     * @param types the types to set
     */
    public void setTypes(final List<? extends ImageType> types) {
        m_types = new ArrayList<ImageType>(types);
    }

    /**
     * @return the image types
     */
    public List<ImageType> getTypes() {
        return m_types == null ? Collections.<ImageType> emptyList() : Collections.unmodifiableList(m_types);
    }

    /**
     * @return the readTimeout
     */
    public int getReadTimeout() {
        return m_readTimeout;
    }

    /**
     * @param readTimeout the readTimeout to set
     */
    public void setReadTimeout(final int readTimeout) {
        m_readTimeout = readTimeout;
    }

    /**
     * Save current configuration.
     *
     * @param settings To save to.
     */
    void save(final NodeSettingsWO settings) {
        settings.addString(URL_COLUMN, m_urlColName);
        settings.addBoolean(FAIL_IF_INVALID, m_failOnInvalid);
        settings.addString(NEW_COLUMN_NAME, m_newColumnName);
        settings.addStringArray(CONGIGURED_TYPE, toStringArray(m_types.toArray(new ImageType[0])));
        settings.addInt(READ_TIMEOUT, m_readTimeout);
    }

    /**
     * Load config in node model.
     *
     * @param settings To load from.
     * @throws InvalidSettingsException If invalid.
     */
    void loadInModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_urlColName = settings.getString(URL_COLUMN);
        m_failOnInvalid = settings.getBoolean(FAIL_IF_INVALID);
        m_newColumnName = settings.getString(NEW_COLUMN_NAME);
        m_types = toEnum(ImageType.class, settings.getStringArray(CONGIGURED_TYPE));
        m_readTimeout = settings.getInt(READ_TIMEOUT, -1);
    }

    /**
     * Load config in dialog.
     *
     * @param settings To load from
     * @param in Current input spec
     * @throws NotConfigurableException If no configuration possible, e.g.
     */
    void loadInDialog(final NodeSettingsRO settings, final DataTableSpec in) throws NotConfigurableException {
        m_urlColName = settings.getString(URL_COLUMN, null);
        if (m_urlColName == null) {
            try {
                guessDefaults(in);
            } catch (InvalidSettingsException e) {
                throw new NotConfigurableException("No valid input column available");
            }
        }
        m_readTimeout = settings.getInt(READ_TIMEOUT, -1);
        m_failOnInvalid = settings.getBoolean(FAIL_IF_INVALID, m_failOnInvalid);
        m_newColumnName = settings.getString(NEW_COLUMN_NAME, m_newColumnName);
        m_types = toEnum(ImageType.class, settings.getStringArray(CONGIGURED_TYPE, toStringArray(ImageType.values())));
    }

    /**
     * Guesses meaningful default values, e.g. the URL column is a string, whose name possibly contains "file", "url" or
     * so.
     *
     * @param in The input spec.
     * @throws InvalidSettingsException If no auto-configuration is possible.
     */
    void guessDefaults(final DataTableSpec in) throws InvalidSettingsException {
        String lastStringCol = null;
        String prefStringCol = null;
        for (DataColumnSpec col : in) {
            if (col.getType().isCompatible(StringValue.class)) {
                String name = col.getName();
                lastStringCol = name;
                String lowName = name.toLowerCase();
                if (lowName.contains("url") || lowName.contains("file") || lowName.contains("location")) {
                    prefStringCol = name;
                }
            }
        }
        String winColumn;
        if (prefStringCol != null) {
            winColumn = prefStringCol;
        } else if (lastStringCol != null) {
            winColumn = lastStringCol;
        } else {
            throw new InvalidSettingsException("No auto-configuration possible:"
                + " No string compatible column in input");
        }
        m_urlColName = winColumn;
        m_failOnInvalid = true;
        m_newColumnName = DataTableSpec.getUniqueColumnName(in, "Image from " + m_urlColName);
        m_types = Arrays.asList(ImageType.values());
    }

    /**
     * Transforms an array of enums to an array containing their {@link Enum#name()} in the same order.
     *
     * @param enums the enums to transform
     * @return an array of enums to an array containing their {@link Enum#name()} in the same order
     */
    private static final <T extends Enum<T>> String[] toStringArray(final T... enums) {
        String[] toReturn = new String[enums.length];
        for (int i = 0; i < enums.length; i++) {
            toReturn[i] = enums[i].name();
        }
        return toReturn;
    }

    /**
     * Transforms an array of strings to an array of enums using {@link Enum#valueOf(Class, String)} in same order.
     *
     * @param enumClass the enum class
     * @param enums the enum names
     * @return an array of strings to an array of enums using {@link Enum#valueOf(Class, String)} in same order
     */
    private static final <T extends Enum<T>> List<T> toEnum(final Class<T> enumClass, final String... enums) {
        List<T> toReturn = new ArrayList<T>();
        for (int i = 0; i < enums.length; i++) {
            toReturn.add(Enum.valueOf(enumClass, enums[i]));
        }
        return toReturn;
    }

}
