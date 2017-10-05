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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.knime.base.data.xml.SvgCell;
import org.knime.base.data.xml.SvgImageContent;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.image.png.PNGImageContent;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.StringFormat;
import org.knime.core.util.FileUtil;

/**
 * Model for the Read Image node.
 *
 * @author Bernd Wiswedel, KNIME.com, Zurich, Switzerland
 * @author Marcel Hanser
 */
final class ReadImageFromUrlNodeModel extends NodeModel {

    /**
     *
     */
    private static final String EXCEPTION_MESSAGE_TEMPLATE =
        "Failed to read image content from row '%s', url='%s': %s";

    private static final NodeLogger LOGGER = NodeLogger.getLogger(ReadImageFromUrlNodeModel.class);

    /**
     * Magic numbers taken from http://www.astro.keele.ac.uk/oldusers/rno/Computing/File_magic.html#Compressed.
     */
    /**
     * First PNG Bytes.
     */
    private static final byte[] FIRST_PNG_BYTES = {(byte)0x89, 'P', 'N', 'G'};

    /**
     * First ZIP Bytes.
     */
    private static final byte[] FIRST_GZIP_BYTES = {(byte)0x1f, (byte)0x8b};

    /**
     * Amount of bytes given to the {@link ImageType} to determine if they are responsible for the URL content.
     */
    private static final int FIRST_BYTES_NUM = 100;

    private ReadImageFromUrlConfig m_config = new ReadImageFromUrlConfig();

    /** One in, one out. */
    public ReadImageFromUrlNodeModel() {
        super(1, 1);
    }

    /** {@inheritDoc} */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        ColumnRearranger rearranger = createColumnRearranger(inSpecs[0], new AtomicInteger());
        DataTableSpec out = rearranger.createSpec();
        return new DataTableSpec[]{out};
    }

    /** {@inheritDoc} */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        DataTableSpec spec = inData[0].getDataTableSpec();
        AtomicInteger failCount = new AtomicInteger();
        ColumnRearranger rearranger = createColumnRearranger(spec, failCount);
        BufferedDataTable out = exec.createColumnRearrangeTable(inData[0], rearranger, exec);
        int rowCount = out.getRowCount();
        int fail = failCount.get();
        if (fail > 0) {
            setWarningMessage("Failed to read " + fail + " of " + rowCount + " files");
        }
        return new BufferedDataTable[]{out};
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec in, final AtomicInteger failCounter)
        throws InvalidSettingsException {
        String colName = m_config.getUrlColName();
        if (colName == null) {
            m_config.guessDefaults(in); // throws ISE
            colName = m_config.getUrlColName();
            setWarningMessage("Auto-configuration: Guessing column \"" + colName + "\" to contain locations");
        }
        final int colIndex = in.findColumnIndex(colName);
        if (colIndex < 0) {
            throw new InvalidSettingsException("No such column in input: " + colName);
        }
        DataColumnSpec colSpec = in.getColumnSpec(colIndex);
        if (!colSpec.getType().isCompatible(StringValue.class)) {
            throw new InvalidSettingsException("Selected column \"" + colName + "\" is not string-compatible");
        }
        final String newColName = m_config.getNewColumnName();
        DataColumnSpecCreator colSpecCreator;

        DataType type = getCommonDataType();

        if (newColName != null) {
            String newName = DataTableSpec.getUniqueColumnName(in, newColName);
            colSpecCreator = new DataColumnSpecCreator(newName, type);
        } else {
            colSpecCreator = new DataColumnSpecCreator(colSpec);
            colSpecCreator.setType(type);
            colSpecCreator.removeAllHandlers();
            colSpecCreator.setDomain(null);
        }
        DataColumnSpec outColumnSpec = colSpecCreator.createSpec();
        ColumnRearranger rearranger = new ColumnRearranger(in);
        CellFactory fac = new SingleCellFactory(outColumnSpec) {
            @Override
            public DataCell getCell(final DataRow row) {
                DataCell cell = row.getCell(colIndex);
                if (cell.isMissing()) {
                    return DataType.getMissingCell();
                } else {
                    String urlValue = ((StringValue)cell).getStringValue();
                    URL url = null;
                    try {
                        url = new URL(urlValue);
                        return toImageCell(url);
                    } catch (UnknownHostException e) {
                        String message = String.format(EXCEPTION_MESSAGE_TEMPLATE, row.getKey(),
                            StringFormat.formatPath(urlValue, 50), "unknown host: " + e.getMessage());
                        LOGGER.warn(message, e);
                        if (m_config.isFailOnInvalid()) {
                            if ("file".equals(url.getProtocol())) {
                                StringBuilder b = new StringBuilder(message);
                                b.append("\nNote that file URLs should look like ");
                                if (SystemUtils.IS_OS_WINDOWS) {
                                    b.append("'file:/C:\\file.png' on windows.\n");
                                } else {
                                    b.append("'file:/some/path/file.png' on linux and mac.\n");
                                }
                                message = b.toString();
                            }
                            throw new RuntimeException(message, e);
                        } else {
                            failCounter.incrementAndGet();
                            return new MissingCell(e.getMessage());
                        }
                    } catch (Exception e) {
                        String message =
                            String.format(EXCEPTION_MESSAGE_TEMPLATE, row.getKey(),
                                StringFormat.formatPath(urlValue, 50), e.getMessage());
                        LOGGER.warn(message, e);
                        if (m_config.isFailOnInvalid()) {
                            throw new RuntimeException(message, e);
                        } else {
                            failCounter.incrementAndGet();
                            return new MissingCell(e.getMessage());
                        }
                    }
                }
            }
        };
        if (newColName == null) {
            rearranger.replace(fac, colIndex);
        } else {
            rearranger.append(fac);
        }
        return rearranger;
    }

    /**
     * @return the merged DataType of the configured {@link ImageType}s.
     * @throws InvalidSettingsException if no type is configured
     */
    private DataType getCommonDataType() throws InvalidSettingsException {
        List<ImageType> types = m_config.getTypes();
        if (types.isEmpty()) {
            throw new InvalidSettingsException("No types configured");
        }
        DataType current = types.get(0).getDataType();
        for (int i = 1; i < types.size(); i++) {
            current = DataType.getCommonSuperType(current, types.get(i).getDataType());
        }
        return current;
    }

    /**
     * Read image from URL.
     *
     * @param urlValue The URL
     * @return A new image cell
     * @throws IOException
     * @throws IllegalArgumentException If the image is invalid
     * @see ImageType#createImageCell(InputStream)
     */
    private DataCell toImageCell(final URL url) throws IOException {

        final byte[] buffer = new byte[FIRST_BYTES_NUM];

        int readTimeout = m_config.getReadTimeout();
        final InputStream in =
            readTimeout > 0 ? FileUtil.openStreamWithTimeout(url, readTimeout) : FileUtil.openStreamWithTimeout(url);

        try {
            int readData = IOUtils.read(in, buffer);

            // we also check for a compression as SVG files are often shipped/shared in a GZIP format.
            CompressTypee compressState = determineCompressType(buffer, readData);
            InputStream imageStream = in;

            switch (compressState) {
                case GZIP:
                    // overwrite the stream variable with the gzip stream
                    imageStream = new GZIPInputStream(aggregate(buffer, readData, in));
                    readData = IOUtils.read(imageStream, buffer);
                default:
                    final ImageType foundType = determineImageType(buffer, readData);
                    if (foundType != null) {
                        return foundType.createImageCell(aggregate(buffer, readData, imageStream));
                    }
                    break;
            }
        } finally {
            IOUtils.closeQuietly(in);
        }

        EnumSet<ImageType> enumsd = EnumSet.allOf(ImageType.class);
        enumsd.removeAll(m_config.getTypes());

        throw new IllegalArgumentException("no applicable image type" + (enumsd.isEmpty()
                ? "" : "; you may want to reconfigure the node to also accept type(s): " + enumsd.toString()));
    }

    /**
     * @param startingBytes
     * @param trailingStream
     * @return A input stream first returning the <code>startingBytes</code> and afterwards the
     *         <code>trailingStream</code>
     */
    private SequenceInputStream aggregate(final byte[] startingBytes, final int readBytes,
        final InputStream trailingStream) {
        return new SequenceInputStream(new ByteArrayInputStream(startingBytes, 0, readBytes), trailingStream);
    }

    /**
     * @param buffer the buffer
     * @param readData amount of read byte
     * @return returns the {@link ImageType} which claims to be responsible for the given bytes
     */
    private ImageType determineImageType(final byte[] buffer, final int readData) {
        for (ImageType type : m_config.getTypes()) {

            if (type.isMimeType(buffer, readData)) {
                return type;
            }
        }
        return null;
    }

    /**
     * @param buffer the buffer
     * @param readBytes amount of read bytes in the buffer
     * @return {@link CompressTypee#GZIP} if the buffer is compressed in the zip format
     */
    private static CompressTypee determineCompressType(final byte[] buffer, final int readBytes) {
        if (startsWithBytes(buffer, readBytes, FIRST_GZIP_BYTES)) {
            return CompressTypee.GZIP;
        } else {
            return CompressTypee.NONE;
        }
    }

    /**
     * @param bytes the bytes to check.
     * @param startingBytes the bytes the given array should start with
     * @return <code>true</code> if the given byte array starts with the bytes defined by startingBytes
     */
    private static boolean startsWithBytes(final byte[] bytes, final int readBytes, final byte... startingBytes) {
        if (readBytes < startingBytes.length) {
            return false;
        }
        for (int i = 0; i < startingBytes.length; i++) {
            if (bytes[i] != startingBytes[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if a stream is compressed in the defined format.
     *
     * @author Marcel Hanser
     */
    enum CompressTypee {
        /**
         * the stream contains content in the ZIP format.
         */
        GZIP,
        /**
         * the stream is not compressed at all.
         */
        NONE;
    }

    /**
     * Encapsulates the functionality needed for predicting an image type and creating the corresponding cell.
     *
     * @author Marcel Hanser
     */
    enum ImageType {

        /**
         * The PNG image type.
         */
        PNG(PNGImageContent.TYPE) {

            @Override
            public boolean isMimeType(final byte[] firstBytes, final int readBytes) {
                return startsWithBytes(firstBytes, readBytes, FIRST_PNG_BYTES);
            }

            @Override
            public DataCell createImageCell(final InputStream stream) throws IOException {
                PNGImageContent pngImageContent = new PNGImageContent(stream);
                return pngImageContent.toImageCell();
            }
        },
        /**
         * The SVG image type.
         */
        SVG(SvgCell.TYPE) {
            @Override
            public boolean isMimeType(final byte[] firstBytes, final int bytes) {
                String s = new String(firstBytes, 0, bytes, Charsets.UTF_8);
                return s.contains("<?xml ") || s.contains("<svg");
            }

            @Override
            public DataCell createImageCell(final InputStream stream) throws IOException {
                SvgImageContent svgImageContent = new SvgImageContent(stream);
                return svgImageContent.toImageCell();
            }
        };
        private final DataType m_dataType;

        /**
         * @param dataType of the cell implementation returned by {@link #createImageCell(InputStream)}
         **/
        private ImageType(final DataType dataType) {
            // null check
            Validate.notNull(dataType);
            this.m_dataType = dataType;
        }

        /**
         * Checks if the bytes fit to the encoding represented by this {@link ImageType}. E.g. the SVG image type might
         * check if the bytes contain the string "&lt;svg".
         *
         * @param firstBytes the first {@value ReadImageFromUrlNodeModel#FIRST_BYTES_NUM} bytes of the input stream
         * @param readBytes amount of read bytes
         * @return <code>true</code> if the bytes fit to the encoding represented by this {@link ImageType} otherwise
         *         <code>false</code>
         */
        public abstract boolean isMimeType(byte[] firstBytes, int readBytes);

        /**
         * Creates the {@link ImageType} dependent cell containing the image information of the given stream. The stream
         * should <b>not</b>cfg be closed. The method is only called if {@link #isMimeType(byte[], int)} previously
         * returned <code>true</code>.
         *
         * @param stream the image data
         * @return the cell.
         * @throws IOException thrown if some IOException occurs during reading the stream
         */
        public abstract DataCell createImageCell(InputStream stream) throws IOException;

        /**
         * @return dataType of the cell implementation returned by {@link #createImageCell(InputStream)}
         */
        public DataType getDataType() {
            return m_dataType;
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
        // no internals
    }

    /** {@inheritDoc} */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        new ReadImageFromUrlConfig().loadInModel(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        ReadImageFromUrlConfig cfg = new ReadImageFromUrlConfig();
        cfg.loadInModel(settings);
        m_config = cfg;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_config.getUrlColName() != null) {
            m_config.save(settings);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // no internals
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // no internals
    }

}
