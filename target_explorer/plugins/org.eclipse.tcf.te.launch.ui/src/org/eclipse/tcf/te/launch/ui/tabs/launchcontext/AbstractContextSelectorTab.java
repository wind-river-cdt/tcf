/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.tabs.launchcontext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.ui.nls.Messages;
import org.eclipse.tcf.te.launch.ui.tabs.AbstractFormsLaunchConfigurationTab;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Abstract context selector launch configuration tab implementation.
 */
public abstract class AbstractContextSelectorTab extends AbstractFormsLaunchConfigurationTab {
	// References to the tab sub sections
	private AbstractContextSelectorSection selectorSection;

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.AbstractFormsLaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		if (selectorSection != null) { selectorSection.dispose(); selectorSection = null; }
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.AbstractFormsLaunchConfigurationTab#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
	 */
	@Override
	protected final void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		// Setup the main panel (using the table wrap layout)
		Composite panel = toolkit.getFormToolkit().createComposite(parent);
		TableWrapLayout layout = new TableWrapLayout();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 1;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		panel.setBackground(parent.getBackground());

		selectorSection = doCreateContextSelectorSection(getManagedForm(), panel);
		selectorSection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
		getManagedForm().addPart(selectorSection);

		doCreateAdditionalFormContent(getManagedForm(), parent, toolkit);
	}

	/**
	 * Do create additional managed form content.
	 *
	 * @param form The managed form.
	 * @param parent The parent composite. Must not be <code>null</code>
	 * @param toolkit The {@link CustomFormToolkit} instance. Must not be <code>null</code>.
	 */
	protected abstract void doCreateAdditionalFormContent(IManagedForm form, Composite parent, CustomFormToolkit toolkit);

	/**
	 * Create the context selector section
	 * @param form The managed form.
	 * @param panel The panel.
	 * @return The context selector section.
	 */
	protected abstract AbstractContextSelectorSection doCreateContextSelectorSection(IManagedForm form, Composite panel);

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return Messages.LaunchContextSelectorTab_name;
	}
}
