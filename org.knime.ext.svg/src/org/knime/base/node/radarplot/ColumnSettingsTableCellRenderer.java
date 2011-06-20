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
 *   23.01.2007 (mb): created
 */
package org.knime.base.node.radarplot;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 *
 * @author M. Berthold, University of Konstanz
 */
public class ColumnSettingsTableCellRenderer implements TableCellRenderer {


    /**
     * {@inheritDoc}
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table,
            final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {
        ColumnSettingsTable myTable = (ColumnSettingsTable)table.getModel();
        if (column ==1){
        	JPanel panel = new JPanel(new GridBagLayout());
        	GridBagConstraints c = new GridBagConstraints();
        	DecimalFormat f = new DecimalFormat("#0.00");
        	double min, max, up, low;
        	low = (Double) myTable.getValueAt(row, 2);
        	up = (Double) myTable.getValueAt(row, 5);
        	min = (Double) myTable.getValueAt(row, 3);
        	max = (Double) myTable.getValueAt(row, 4);

        	low = low+0.2*(max-min);
        	up = up - 0.3*(max-min);
        	RadarSlider slider = new RadarSlider(low,up,0,min,max);
    		RadarSliderUI ui = new RadarSliderUI(slider);
    		slider.setUI(ui);
    		c.gridwidth =  GridBagConstraints.REMAINDER;
    		c.gridheight = 1;
    		c.weightx = 0.5;
    		c.gridx = 0;
    		c.gridy = 0;
    		panel.add(slider, c);

    		JLabel label1 = new JLabel();
    		label1.setText("Min.: " + f.format(min));
    		c.gridwidth = 1;
    		c.gridheight = GridBagConstraints.REMAINDER;
    		c.fill = GridBagConstraints.NONE;
    		c.gridx = 0;
    		c.gridy = 1;
    		panel.add(label1,c);

    		JLabel label2 = new JLabel();
    		label2.setText("Low: " + f.format(low));
    		c.fill = GridBagConstraints.NONE;
    		c.gridx = 1;

    		c.gridy = 1;
    		panel.add(label2,c);

    		JLabel label3 = new JLabel();
    		label3.setText("High: " +f.format(up));
    		c.fill = GridBagConstraints.NONE;
    		c.gridx = 2;
    		c.gridy = 1;
    		panel.add(label3,c);

    		JLabel label4 = new JLabel();
    		label4.setText("Max.: " + f.format(max));
    		c.fill = GridBagConstraints.NONE;
    		c.gridx = 3;
    		c.gridy = 1;
    		panel.add(label4,c);


    		panel.setVisible(true);
    		return panel;
        }
        System.out.println(column);
        return new JPanel();
    }
}
