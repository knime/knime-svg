/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   21.01.2007 (berthold): created
 */
package org.knime.ext.svg.node.radarplot;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.table.AbstractTableModel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/** Table which holds all the settings for one radarplot node
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class ColumnSettingsTable extends AbstractTableModel {

	private int m_nrAttr = 0;
	private boolean[] m_isSelected;
	private String[] m_attrName;
	private double[] m_min;
	private double[] m_max;
	private double[] m_validMin;
	private double[] m_validMax;
	private String[] m_backgroundColor;
	private String[] m_intervalColor;
	private String[] m_bendColor;
	private String[] m_outlyingBendColor;
	private boolean[] m_isDouble;

	private final String m_confName;

	/**
	 * Flag specifying if execution should fail on missing columns.
	 * @since 2.6
	 */
	private boolean m_failOnMissingCols = false;

    /**
     * Constructor of <code>ColumnSettingsTable</code>.
     * @param confName The name of the configuration to load and save.
     */
    public ColumnSettingsTable(final String confName) {
		m_confName = confName;
		m_nrAttr = 0;
	}

	/** Loads a new spec from given spec
	 * @param spec Spec to load. Invalid values will be replaced with -1
	 * @since 2.6
	 */
	public void setNewSpec(final DataTableSpec spec) {
		initMembers(spec.getNumColumns());
		for (int i = 0; i < m_nrAttr; i++) {
			DataColumnSpec thisSpec = spec.getColumnSpec(i);
			m_attrName[i] = thisSpec.getName();
			if (thisSpec.getType().isCompatible(DoubleValue.class)) {
				m_isSelected[i] = true;
				if ((thisSpec.getDomain().getLowerBound()) != null){
                    m_min[i] = ((DoubleValue)(thisSpec.getDomain()
                            .getLowerBound())).getDoubleValue();
                    m_max[i] = ((DoubleValue)(thisSpec.getDomain()
                            .getUpperBound())).getDoubleValue();
				} else {
					m_min[i] = -1;
					m_max[i] = -1;
				}
				m_backgroundColor[i] = "175.220.240";
				m_intervalColor[i] = "105.150.170";
				m_bendColor[i] = "0.255.0" ;
				m_outlyingBendColor[i] = "255.0.0";
				m_validMin[i] = m_min[i];
				m_validMax[i] = m_max[i];
				m_isDouble[i] = true;
			} else {
				m_isSelected[i] = false;
				m_isDouble[i] = false;
			}
		}
		fireTableDataChanged();
	}

	/**
	 * Creates empty arrays for members.
	 * @param nrAttr Length of arrays.
	 */
	private void initMembers(final int nrAttr) {
		m_nrAttr = nrAttr;
		m_isSelected = new boolean[m_nrAttr];
		m_attrName = new String[m_nrAttr];
		m_min = new double[m_nrAttr];
		m_max = new double[m_nrAttr];
		m_validMin = new double[m_nrAttr];
		m_validMax = new double[m_nrAttr];
		m_backgroundColor = new String[m_nrAttr];
		m_intervalColor = new String[m_nrAttr];
		m_bendColor = new String[m_nrAttr];
		m_outlyingBendColor = new String[m_nrAttr];
		m_isDouble = new boolean[m_nrAttr];
	}

	final static String[] COLNAMES = {"E", "Values"};

	@Override
	public int getColumnCount() {
		return m_attrName.length;
	}

	@Override
	public String getColumnName(final int i) {
		return m_attrName[i];
	}

	/**
	 * @return Creates and returns a color, generated from the given string
	 * array. If no color could be generated the specified default color is
	 * returned.
	 * @since 2.6
	 */
	private Color getColor(final String[] colArr, final Color defaultColor) {
        for (String colStr : colArr) {
            if (colStr != null) {
                String[] result = colStr.split("\\.");
                int red = Integer.parseInt(result[0]);
                int green = Integer.parseInt(result[1]);
                int blue = Integer.parseInt(result[2]);
                return new Color(red, green, blue);
            } else {
                continue;
            }
        }
        return defaultColor;
	}

    /**
     * Generates the background color from the stored values
     *
     * @return The stored background color
     */
    public Color getBackgroundColor() {
        return getColor(m_backgroundColor, new Color(175, 220, 240));
    }

    /**
     * Generates the interval color from the stored values
     *
     * @return The stored interval color
     */
    public Color getIntervalColor() {
        return getColor(m_intervalColor, new Color(105, 150, 170));
    }

    /**
     * Generates the ribbon color from the stored values. This is the colour of
     * the ribbon if all values are within the boundaries
     *
     * @return The ribbon color
     */
    public Color getBendColor() {
        return getColor(m_bendColor, new Color(0, 255, 0));
    }

    /**
     * Generates the ribbon color from the stored values. This is the colour of
     * the ribbon if some values are outside the boundaries
     *
     * @return The ribbon color
     */
    public Color getOutlyingBendColor() {
        return getColor(m_outlyingBendColor, new Color(255, 0, 0));
    }

	public boolean isSelected(final int i) {
		return m_isSelected[i];
	}

	public boolean isDouble(final int i) {
		return m_isDouble[i];
	}

	public double getMin(final int i) {
		return m_min[i];
	}

	public double getValidMin(final int i) {
		return m_validMin[i];
	}

	public double getValidMax(final int i) {
		return m_validMax[i];
	}

	public double getMax(final int i) {
		return m_max[i];
	}

	public int getNrSelected() {
		int s = 0;
		for (int i = 0; i < m_nrAttr; i++) {
			if (m_isSelected[i]) {
				s++;
			}
		}
		return s;
	}

	@Override
	public Class<?> getColumnClass(final int col) {
		switch (col) {
		case 0: return Boolean.class;
		case 1: return String.class;
		case 2:
		case 3:
		case 4:
		case 5: return Double.class;
		}
		assert false;
		return String.class;
	}

	@Override
	public int getRowCount() {
		return m_nrAttr;
	}

	public String getRowName(final int i){
		return m_attrName[i];
	}

	@Override
	public boolean isCellEditable(final int row, final int col) {
		return m_isDouble[row] && ((col == 0) || (col == 3) || (col == 4));
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		switch (col) {
		case 0: return m_isSelected[row];
		case 1: return m_attrName[row];
		}
		if (!m_isDouble[row]) {
			return Double.NaN;
		}
		switch (col) {
		case 2: return m_min[row];
		case 3: return m_validMin[row];
		case 4: return m_validMax[row];
		case 5: return m_max[row];
		}
		assert false;
		return "N/A";
	}

	@Override
	public void setValueAt(final Object obj, final int row, final int col) {
		switch (col) {
		case 0: m_isSelected[row] = ((Boolean)obj).booleanValue(); return;
		case 1: assert false;
		case 2: assert false;
		case 5: assert false;
		}
		double val = ((Double)obj).doubleValue();
		if ((val < m_min[row]) || (val > m_max[row])) {
			throw new IllegalArgumentException();
		}
		switch (col) {
		case 3: m_validMin[row] = val; return;
		case 4: m_validMax[row] = val; return;
		}
		return;
	}


    /**
     * Loads the given settings into this table.
     *
     * @param settings Settings to load
     * @throws InvalidSettingsException If settings could not be loaded.
     * @since 2.6
     */
	void loadSettingsModel(final NodeSettingsRO settings)
	throws InvalidSettingsException {
			NodeSettingsRO mySettings = settings.getNodeSettings(m_confName);
	        if (mySettings == null) {
	            throw new InvalidSettingsException("No settings available");
	        }

			int nrAttr = mySettings.getInt("NRATTR");
	        // check for key before loading to ensure backwards compatibility
            // (old node configurations may have not this setting set already)
            if (mySettings.containsKey("FAILONMISSINGCOLS")) {
                m_failOnMissingCols =
                    mySettings.getBoolean("FAILONMISSINGCOLS");
            } else {
                m_failOnMissingCols = false;
            }

			initMembers(nrAttr);
			for (int i = 0; i < m_nrAttr; i++) {
				NodeSettingsRO thisSettings = mySettings.getNodeSettings(
						"Attribute_" + i);
				String name = thisSettings.getString("NAME");
				if (name == null) {
				    throw new InvalidSettingsException("Name must not be null!");
				} else {
				    m_attrName[i] = name;
				}
				m_isSelected[i] = thisSettings.getBoolean("SELECTED");
				m_isDouble[i] = thisSettings.getBoolean("ISDOUBLE");

				if (m_isDouble[i]) {
				    // non-double columns don't have usable settings
                    String background = thisSettings.getString("BACKGROUND");
                    if (background == null) {
                        throw new InvalidSettingsException(
                                "Background must not be null!");
                    } else {
                        m_backgroundColor[i] = background;
                    }

                    String intervalColor = thisSettings.getString("INTERVAL");
                    if (intervalColor == null) {
                        throw new InvalidSettingsException(
                                "Interval color must not be null!");
                    } else {
                        m_intervalColor[i] = intervalColor;
                    }

                    String bendColor = thisSettings.getString("BEND");
                    if (bendColor == null) {
                        throw new InvalidSettingsException(
                                "Bend color must not be null!");
                    } else {
                        m_bendColor[i] = bendColor;
                    }

                    String outlyingBendColor =
                        thisSettings.getString("OUTLYINGBEND");
                    if (outlyingBendColor == null) {
                        throw new InvalidSettingsException(
                                "Outlying bend color must not be null!");
                    } else {
                        m_outlyingBendColor[i] = outlyingBendColor;
                    }

    				m_validMin[i] = thisSettings.getDouble("VALIDMIN");
    				m_validMax[i] = thisSettings.getDouble("VALIDMAX");
    				m_min[i] = thisSettings.getDouble("MIN");
    				m_max[i] = thisSettings.getDouble("MAX");
				}
			}
		fireTableDataChanged();
	}

	/**
	 * Loads settings for dialog. First all saved settings are loaded and
	 * second the given input spec is matched against the loaded settings.
	 * Only the settings of proper existing columns will be taken over and
	 * handed to the dialog. Settings of non existing or invalid columns will
	 * not be handed to the dialog.
	 * @param settings Settings to load.
	 * @param spec Spec of input data.
	 * @since 2.6
	 */
	void loadSettingsDialog(final NodeSettingsRO settings,
            final DataTableSpec spec) {
	    ColumnSettingsTable origSettings = new ColumnSettingsTable(m_confName);
	    origSettings.loadSettingsDialogInternal(settings);
	    this.setNewSpecAndTakeoverSettings(spec, origSettings);
	}

    /**
     * Internal method to load settings for dialog.
     * @param settings Settings to load for dialog.
     * @since 2.6
     */
    private void loadSettingsDialogInternal(final NodeSettingsRO settings) {
        NodeSettingsRO mySettings = null;
        try {
            mySettings = settings.getNodeSettings(m_confName);
        } catch (InvalidSettingsException ise) {
            // do not throw exception here
        }

        if (mySettings != null) {
            int nrAttr = mySettings.getInt("NRATTR", 0);
            m_failOnMissingCols = mySettings.getBoolean("FAILONMISSINGCOLS",
                    false);

            boolean[] isSelected = new boolean[nrAttr];
            String[] attrName = new String[nrAttr];
            double[] min = new double[nrAttr];
            double[] max = new double[nrAttr];
            double[] validMin = new double[nrAttr];
            double[] validMax = new double[nrAttr];
            String[] backgroundColor = new String[nrAttr];
            String[] intervalColor = new String[nrAttr];
            String[] bendColor = new String[nrAttr];
            String[] outlyingBendColor = new String[nrAttr];
            boolean[] isDouble = new boolean[nrAttr];

            int nrValidAttr = 0;
            boolean[] validSettings = new boolean[nrAttr];
            Arrays.fill(validSettings, true);

            for (int i = 0; i < nrAttr; i++) {
                NodeSettingsRO thisSettings = null;
                try {
                    thisSettings = mySettings.getNodeSettings("Attribute_" + i);
                }  catch (InvalidSettingsException ise) {
                    // do not throw exception here
                }
                if (thisSettings != null) {
                    attrName[i] = thisSettings.getString("NAME", null);
                    if (attrName[i] == null) {
                        validSettings[i] = false;
                    }

                    backgroundColor[i] =
                        thisSettings.getString("BACKGROUND", null);
                    if (backgroundColor[i] == null) {
                        validSettings[i] = false;
                    }

                    intervalColor[i] = thisSettings.getString("INTERVAL", null);
                    if (intervalColor[i] == null) {
                        validSettings[i] = false;
                    }

                    bendColor[i] = thisSettings.getString("BEND", null);
                    if (bendColor[i] == null) {
                        validSettings[i] = false;
                    }

                    outlyingBendColor[i] =
                            thisSettings.getString("OUTLYINGBEND", null);
                    if (outlyingBendColor[i] == null) {
                        validSettings[i] = false;
                    }

                    validMin[i] = thisSettings.getDouble("VALIDMIN", 0);
                    validMax[i] = thisSettings.getDouble("VALIDMAX", 0);
                    min[i] = thisSettings.getDouble("MIN", 0);
                    max[i] = thisSettings.getDouble("MAX", 0);
                    isSelected[i] =
                            thisSettings.getBoolean("SELECTED", false);
                    isDouble[i] = thisSettings.getBoolean("ISDOUBLE", false);

                    if (validSettings[i]) {
                        nrValidAttr++;
                    }
                }
            }

            // load valid settings (that could properly be loaded) into members
            initMembers(nrValidAttr);
            int validIndex = 0;
            for (int i = 0; i < nrAttr; i++) {
                if (validSettings[i]) {
                    m_attrName[validIndex] = attrName[i];
                    m_backgroundColor[validIndex] = backgroundColor[i];
                    m_intervalColor[validIndex] = intervalColor[i];
                    m_bendColor[validIndex] = bendColor[i];
                    m_outlyingBendColor[validIndex] = outlyingBendColor[i];
                    m_validMax[validIndex] = validMax[i];
                    m_validMin[validIndex] = validMin[i];
                    m_min[validIndex] = min[i];
                    m_max[validIndex] = max[i];
                    m_isSelected[validIndex] = isSelected[i];
                    m_isDouble[validIndex] = isDouble[i];
                    validIndex++;
                }
            }
        }
        fireTableDataChanged();
    }

	/** Save settings into a NodeSettingsWO object
	 * @param settings Settings in which to save
	 */
	void saveSettings(final NodeSettingsWO settings) {
		NodeSettingsWO mySettings = settings.addNodeSettings(m_confName);
		mySettings.addInt("NRATTR", m_nrAttr);
		mySettings.addBoolean("FAILONMISSINGCOLS", m_failOnMissingCols);
		for (int i = 0; i < m_nrAttr; i++) {
			NodeSettingsWO thisSettings = mySettings.addNodeSettings(
					"Attribute_" + i);
			thisSettings.addString("NAME", m_attrName[i]);
			thisSettings.addDouble("VALIDMIN", m_validMin[i]);
			thisSettings.addDouble("VALIDMAX", m_validMax[i]);
			thisSettings.addDouble("MIN", m_min[i]);
			thisSettings.addDouble("MAX", m_max[i]);
			thisSettings.addString("BACKGROUND", m_backgroundColor[i]);
			thisSettings.addString("INTERVAL", m_intervalColor[i]);
			thisSettings.addString("BEND", m_bendColor[i]);
			thisSettings.addString("OUTLYINGBEND", m_outlyingBendColor[i]);
			thisSettings.addBoolean("SELECTED", m_isSelected[i]);
			thisSettings.addBoolean("ISDOUBLE", m_isDouble[i]);
		}
	}


    /**
     * Checks if this table is proper (at least one column has valid values).
     *
     * @return 1 if all Double columns are valid, 0 if some are invalid, -1 if
     *         ALL are invalid.
     */
	public int isProper() {
	    return isProper(false);
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_nrAttr;
        for (int i = 0; i < m_nrAttr; i++) {
            result = prime * result + m_attrName[i].hashCode();
        }
        return result;
    }


    /**
     * Checks for equality. This table and another table are equal if their column names, maximum and minimum Values are
     * the same. This requires them to share the same number of rows.
     *
     * @param other Table against which to test this table
     * @return <code>true</code> if the two tables are equal, <code>false</code> otherwise
     */
    public boolean equals(final ColumnSettingsTable other) {
        if (this.getRowCount() != other.getRowCount()) {
            return false;
        }
        for (int i = 0; i < this.getRowCount(); i++) {
            if (!(this.m_attrName[i].equals(other.m_attrName[i]))) {
                return false;
            }
            if (this.getValidMax(i) != other.getValidMax(i)) {
                return false;
            }
            if (this.getValidMin(i) != other.getValidMin(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ColumnSettingsTable other = (ColumnSettingsTable)obj;
        return equals(other);
    }

    /**
     * Checks if this table is proper (at least one column has valid values).
     * If selectedOnly is set <code>true</code> only selected columns are
     * considered for check.
     *
     * @param selectedOnly If set <code>true</code> only selected columns are
     * considered for check, otherwise all double columns are considered.
     * @return 1 if all Double columns are valid, 0 if some are invalid, -1 if
     *         ALL are invalid.
     * @since 2.6
     */
    public int isProper(final boolean selectedOnly) {
        int numCols = 0;
        int numValidCols = 0;
        for (int i = 0; i < this.getRowCount(); i++) {
            if (this.isDouble(i)) {
                // check if selectedOnly = false
                // OR (if selectedOnly = true AND column is selected)
                if (!selectedOnly
                        || (selectedOnly && this.isSelected(i))) {
                    numCols++;
                    if (this.getValidMax(i) != this.getValidMin(i)) {
                        numValidCols++;
                    }
                }
            }
        }
        if (numCols != numValidCols) {
            if (numValidCols == 0) {
                return -1;
            } else {
                return 0;
            }
        }
        return 1;
    }

	/**
	 * Returns the index of the specified attribute or -1 is attribute does not
	 * exist.
	 * @param attr Attribute to search index for.
	 * @return The index of the specified attribute.
	 * @since 2.6
	 */
	int getIndexOfAttr(final String attr) {
	    for (int i = 0; i < m_attrName.length; i++) {
	        if (m_attrName[i].equals(attr)) {
	            return i;
	        }
	    }
	    return -1;
	}


    /**
     * Sets a new spec. All settings for columns are first set to default.
     * Second the given settings will be taken over if possible. If the a column
     * exist in the input spec and the column is a double column, all settings
     * will be taken over. Settings for columns that do not exist in the spec
     * will be ignored. Columns in the spec, for which no settings are specified
     * will be set to default.
     * @param spec The spec of the input data table
     * @param settings The settings to take over (as good as possible)
     * @since 2.6
     */
    void setNewSpecAndTakeoverSettings(final DataTableSpec spec,
            final ColumnSettingsTable settings) {
        // set settings data taken from given spec
        setNewSpec(spec);

        // take over all settings that from given settings that match with the
        // spec settings, and take care of valid domain min/max and
        // ribbon min/max
        int foundAttributes = 0;
        m_failOnMissingCols = settings.getFailOnMissingCols();
        for (int i = 0; i < getRowCount(); i++) {
            boolean foundAttribute = false;

            for (int j = 0; j < settings.getRowCount(); j++) {
                // found column in old settings which is available in new
                // settings as well => take values from new settings
                if (getRowName(i).equals(settings.getRowName(j))
                        && (isDouble(i) == settings.isDouble(j))) {
                    foundAttribute = true;
                    // count selected and found attributes
                    if (settings.isSelected(j)) {
                        foundAttributes++;
                    }

                    m_isSelected[i] = settings.isSelected(j);

                    // if in given settings min == max, this means
                    // default min/max is set, which can be overwritten by the
                    // domain min/max
                    if (settings.getMax(j) == settings.getMin(j)) {
                        m_max[i] = m_validMax[i];
                        m_min[i] = m_validMin[i];

                    // else, take over specified settings but only if in valid
                    // range if out of valid range adjust min/max
                    } else {
                        // take ribbon max from settings unless the domain max
                        // is less than the ribbon max
                        m_max[i] = Math.min(settings.getMax(j), m_validMax[i]);
                        // take ribbon min from settings unless the domain min
                        // is grater than the ribbon min.
                        m_min[i] = Math.max(settings.getMin(j), m_validMin[i]);
                    }

                    // colors can be simply taken over
                    m_backgroundColor[i] = settings.getBackgroundAt(j);
                    m_intervalColor[i] = settings.getIntervalColorAt(j);
                    m_bendColor[i] = settings.getBendColorAt(j);
                    m_outlyingBendColor[i] = settings.getOutlyingBendColorAt(j);

                // if attribute names are equal but one is double attribute and
                // the other not, just take over color settings and mark as non
                // double/selected
                } else if(getRowName(i).equals(settings.getRowName(j))) {
                    m_isSelected[i] = false;
                    m_isDouble[i] = false;
                    m_backgroundColor[i] = settings.getBackgroundAt(j);
                    m_intervalColor[i] = settings.getIntervalColorAt(j);
                    m_bendColor[i] = settings.getBendColorAt(j);
                    m_outlyingBendColor[i] = settings.getOutlyingBendColorAt(j);
                }
            }

            // if attribute is not available in new settings, disable it
            if (!foundAttribute) {
                m_isSelected[i] = false;
            }
        }
    }

    /**
     * Checks if all selected columns from given settings are available and
     * returns <code>true</code> if so, otherwise <code>true</code>.
     * @param settings Settings to check for available columns.
     * @return <code>true</code> is all selected columns in given settings
     * are available, otherwise <code>false</code>.
     * @since 2.6
     */
    boolean allColumnAvailable(final ColumnSettingsTable settings) {
        int foundAttributes = 0;
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < settings.getRowCount(); j++) {
                if (getRowName(i).equals(settings.getRowName(j))
                        && (isDouble(i) == settings.isDouble(j))) {
                    if (settings.isSelected(j)) {
                        foundAttributes++;
                    }
                }
            }
        }
        if (foundAttributes < settings.getNrSelected()) {
            return false;
        }
        return true;
    }

    /**
     * @param i index to return background value for.
     * @return background value at given index.
     * @since 2.6
     */
    String getBackgroundAt(final int i) {
        return m_backgroundColor[i];
    }

    /**
     * @param i index to return interval color value for.
     * @return interval color value at given index.
     * @since 2.6
     */
    String getIntervalColorAt(final int i) {
        return m_intervalColor[i];
    }

    /**
     * @param i index to return bend color value for.
     * @return bend color value at given index.
     * @since 2.6
     */
    String getBendColorAt(final int i) {
        return m_bendColor[i];
    }

    /**
     * @param i index to outlying bend color value for.
     * @return outlying bend color value at given index.
     * @since 2.6
     */
    String getOutlyingBendColorAt(final int i) {
        return m_outlyingBendColor[i];
    }

    /**
     * @return the failOnMissingCols
     * @since 2.6
     */
    public boolean getFailOnMissingCols() {
        return m_failOnMissingCols;
    }

    /**
     * @param failOnMissingCols the failOnMissingCols to set
     * @since 2.6
     */
    public void setFailOnMissingCols(final boolean failOnMissingCols) {
        m_failOnMissingCols = failOnMissingCols;
    }
}
