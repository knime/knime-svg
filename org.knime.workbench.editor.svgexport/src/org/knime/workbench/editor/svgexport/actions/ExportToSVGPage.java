/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 * -------------------------------------------------------------------
 *
 * History
 *   02.07.2006 (sieb): created
 */
package org.knime.workbench.editor.svgexport.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.workbench.ui.KNIMEUIPlugin;

/**
 * Page to enter the destination to export the currently selected Workflow to.
 *
 * @author Christoph Sieb, University of Konstanz
 * @author Fabian Dill, KNIME.com AG, Zurich, Switzerland
 * @author Andreas Burger
 */
public class ExportToSVGPage extends WizardPage {
    private Text m_fileDestination;

    private String m_filename;

    private static final ImageDescriptor ICON = AbstractUIPlugin
            .imageDescriptorFromPlugin(KNIMEUIPlugin.PLUGIN_ID,
                    "icons/knime_export55.png");

    /**
     * Creates a new export-to-SVG page.
     *
     * @param selection the initial selection
     */
    public ExportToSVGPage(final ISelection selection) {
        super("wizardPage");
        setTitle("Export KNIME workflows");
        setDescription("Exports a KNIME workflow.");
        setImageDescriptor(ICON);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(final Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        // place components vertically
        container.setLayout(new GridLayout(1, false));

        Group exportGroup = new Group(container, SWT.NONE);
        exportGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        exportGroup.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 9;
        Label label = new Label(exportGroup, SWT.NULL);
        label.setText("Select File to export to:");

        m_fileDestination = new Text(exportGroup, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        m_fileDestination.setLayoutData(gd);

        Button selectFileButton = new Button(exportGroup, SWT.PUSH);
        selectFileButton.setText("Select...");
        selectFileButton.setToolTipText("Opens a file selection dialog.");
        selectFileButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(final SelectionEvent se) {
                FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
                fileDialog.setText("Specify the SVG-Export file.");
                if (m_filename != null) {
                    fileDialog.setFileName(m_filename);
                }
                String filePath = fileDialog.open();
                if (filePath != null && filePath.trim().length() > 0) {
                    if (filePath.length() < 5
                            || filePath.lastIndexOf('.') < filePath.length() - 4) {
                        // they have no extension - add .svg
                        filePath += ".svg";
                    }

                    m_filename = filePath;
                    m_fileDestination.setText(filePath);
                }
            }

            @Override
            public void widgetDefaultSelected(final SelectionEvent se) {
                widgetSelected(se);
            }
        });

        setControl(container);
    }

    /**
     * Returns the destination SVG file.
     *
     * @return a filename
     */
    public String getFileDestination() {
        return m_fileDestination.getText();
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }
}
