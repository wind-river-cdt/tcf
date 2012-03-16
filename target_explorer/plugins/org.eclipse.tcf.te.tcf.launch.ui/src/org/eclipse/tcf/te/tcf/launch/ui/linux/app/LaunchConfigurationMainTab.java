/*
 * LaunchConfigurationMainTab.java
 * Created on 02.03.2012
 *
 * Copyright 2012 Wind River Systems Inc. All rights reserved.
 */
package org.eclipse.tcf.te.tcf.launch.ui.linux.app;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.te.launch.ui.tabs.selector.LaunchContextSelectorTab;
import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;

/**
 * LaunchConfigurationMainTab
 * @author tobias.schwarz@windriver.com
 */
public class LaunchConfigurationMainTab extends LaunchContextSelectorTab {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.selector.LaunchContextSelectorTab#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
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
