/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.views.editor.pages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.tcf.te.ui.trees.TreeControl;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Tree viewer based editor page implementation.
 */
public class TreeViewerExplorerEditorPage extends AbstractCustomFormToolkitEditorPage {
	// The references to the pages subcontrol's (needed for disposal)
	private TreeControl treeControl;
	private String viewerId;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#dispose()
	 */
	@Override
	public void dispose() {
		if (treeControl != null) { treeControl.dispose(); treeControl = null; }
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
	    super.setInitializationData(config, propertyName, data);
		viewerId = data != null ? data.toString() : null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#configureManagedForm(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
    protected void configureManagedForm(IManagedForm managedForm) {
		Assert.isNotNull(managedForm);

		// Configure main layout
		Composite body = managedForm.getForm().getBody();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 2; layout.marginWidth = 0;
		body.setLayout(layout);
		body.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
	 */
	@Override
    protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		Section section = toolkit.getFormToolkit().createSection(parent, ExpandableComposite.TITLE_BAR);
		String title = getTitle();
		// Stretch to a length of 40 characters to make sure the title can be changed
		// to hold and show text up to this length
		while (title.length() < 40) {
			title += " "; //$NON-NLS-1$
		}
		// Set the title to the section
		section.setText(title);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 0;
		data.heightHint = 0;
		section.setLayoutData(data);

		// Create the client area
		Composite client = toolkit.getFormToolkit().createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0; layout.marginHeight = 0;
		client.setLayout(layout);
		section.setClient(client);

		// Setup the tree control
		treeControl = doCreateTreeControl();
		Assert.isNotNull(treeControl);
		treeControl.setupFormPanel((Composite)section.getClient(), toolkit);

		// Set the initial input
		treeControl.getViewer().setInput(getEditorInputNode());
	}

	/**
	 * Creates and returns a tree control.
	 *
	 * @return The new tree control.
	 */
	protected TreeControl doCreateTreeControl() {
		return new TreeControl(viewerId, this);
	}

	/**
	 * Returns the associated tree control.
	 *
	 * @return The associated tree control or <code>null</code>.
	 */
	protected final TreeControl getTreeControl() {
		return treeControl;
	}
}
