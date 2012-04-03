/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.remote.app;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.ui.tabs.launchcontext.LaunchContextSelectorTab;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;

/**
 * Remote application main launch tab implementation.
 */
public class LaunchConfigurationMainTab extends LaunchContextSelectorTab {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.launchcontext.LaunchContextSelectorTab#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
	 */
	@Override
	protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
		super.doCreateFormContent(parent, toolkit);

		// Setup the main panel (using the table wrap layout)
		Composite panel = toolkit.getFormToolkit().createComposite(parent);
		GridLayout	layout = new GridLayout(1, false);
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		panel.setBackground(parent.getBackground());

		LaunchConfigurationMainTabSection section = new LaunchConfigurationMainTabSection(getManagedForm(), panel);
		section.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		getManagedForm().addPart(section);
	}
}
