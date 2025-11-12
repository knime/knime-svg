/*
 * ------------------------------------------------------------------------
 *
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
 * ------------------------------------------------------------------------
 */

package org.knime.base.node.image.readimage;

import static org.knime.base.node.image.readimage.ReadImageFromUrlConfig.CONFIGURED_TYPE;
import static org.knime.base.node.image.readimage.ReadImageFromUrlConfig.FAIL_IF_INVALID;
import static org.knime.base.node.image.readimage.ReadImageFromUrlConfig.NEW_COLUMN_NAME;
import static org.knime.base.node.image.readimage.ReadImageFromUrlConfig.READ_TIMEOUT;
import static org.knime.base.node.image.readimage.ReadImageFromUrlConfig.URL_COLUMN;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.OptionalWidget.DefaultValueProvider;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.StringChoicesProvider;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation;
import org.knime.node.parameters.widget.text.TextInputWidget;

/**
 * Node parameters for Read Images.
 *
 * @author Tim Crundall, TNG Technology Consulting GmbH
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
class ReadImageFromUrlNodeParameters implements NodeParameters {

    @Section(title = "Error Handling")
    interface ErrorHandlingSection {
    }

    @Section(title = "Output")
    @After(ErrorHandlingSection.class)
    interface OutputSection {
    }

    @Widget(title = "URL column", description = "Select the input column containing the URLs.")
    @ChoicesProvider(StringColumnsProvider.class)
    @Persist(configKey = URL_COLUMN)
    String m_urlColumn;

    @Widget(title = "Image types",
        description = "Select which image types are valid in the input. "
            + "If a non-valid type is encountered it will handle the error according to the "
            + "\"Fail on invalid input\" option.")
    @ChoicesProvider(ImageTypeChoicesProvider.class)
    @Persist(configKey = CONFIGURED_TYPE)
    String[] m_imageTypes = getAllImageTypes().toArray(String[]::new);

    @Widget(title = "Fail on invalid input",
        description = "If selected, the node will fail during execution if any URL is invalid or points "
            + "to an invalid image file. If unselected, the node will skip these invalid entries and insert "
            + "a missing value instead.")
    @Persist(configKey = FAIL_IF_INVALID)
    @Layout(ErrorHandlingSection.class)
    boolean m_failOnInvalid = true;

    @Widget(title = "Custom image read timeout",
        description = "Defines the connection and read timeout for the used URL connections in seconds. "
            + "The default is 1 second if it is not overwritten by the 'knime.url.timeout' system property.")
    @OptionalWidget(defaultProvider = TimeoutDefaultProvider.class)
    @NumberInputWidget(minValidation = TimeoutMinimumValidation.class)
    @Layout(ErrorHandlingSection.class)
    @Persistor(TimeoutPersistor.class)
    Optional<Double> m_readTimeoutSeconds = Optional.empty();

    @Widget(title = "Append new column",
        description = "If selected, appends a new column with the images. Otherwise, replaces the input column.")
    @Layout(OutputSection.class)
    @TextInputWidget(placeholder = "New column name")
    @Persistor(OutputColumnPersistor.class)
    Optional<String> m_outputColumn = Optional.empty();

    private static final class StringColumnsProvider extends CompatibleColumnsProvider {
        public StringColumnsProvider() {
            super(StringValue.class);
        }
    }

    private static final class TimeoutDefaultProvider implements DefaultValueProvider<Double> {
        @Override
        public Double computeState(final NodeParametersInput parametersInput) {
            return 1.0;
        }
    }

    private static final class ImageTypeChoicesProvider implements StringChoicesProvider {
        @Override
        public List<String> choices(final NodeParametersInput parametersInput) {
            return getAllImageTypes();
        }
    }

    private static List<String> getAllImageTypes() {
        return Arrays.stream(ReadImageFromUrlNodeModel.ImageType.values())
                .map(Enum::name).toList();
    }

    private static final class TimeoutMinimumValidation extends MinValidation {
        // Timeout is stored as integer in milliseconds, hence the sensible minimum is 0.001 seconds
        @Override
        protected double getMin() {
            return 0.001;
        }
    }

    private static final class OutputColumnPersistor implements NodeParametersPersistor<Optional<String>> {

        @Override
        public Optional<String> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return Optional.ofNullable(settings.getString(NEW_COLUMN_NAME, null));
        }

        @Override
        public void save(final Optional<String> newColumnName, final NodeSettingsWO settings) {
            settings.addString(NEW_COLUMN_NAME, newColumnName.orElse(null));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{NEW_COLUMN_NAME}};
        }
    }

    private static final class TimeoutPersistor implements NodeParametersPersistor<Optional<Double>> {

        @Override
        public Optional<Double> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            int millis = settings.getInt(READ_TIMEOUT);
            return millis <= 0 ? Optional.empty() : Optional.of(millis / 1000.0);
        }

        @Override
        public void save(final Optional<Double> timeout, final NodeSettingsWO settings) {
            settings.addInt(READ_TIMEOUT, timeout.map(t -> (int)(t * 1000)).orElse(-1));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{READ_TIMEOUT}};
        }
    }

}
