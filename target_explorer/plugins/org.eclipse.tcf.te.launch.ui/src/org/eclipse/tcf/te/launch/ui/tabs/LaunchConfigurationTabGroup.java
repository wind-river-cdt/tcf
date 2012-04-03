/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.tabs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.tcf.te.launch.ui.tabs.launchcontext.LaunchContextSelectorTab;

/**
 * Default launch configuration tab group implementation.
 */
public class LaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {


	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.launch.ui.tabs.AbstractLaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
	 */
	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
	    super.createTabs(dialog, mode);

	    // The list of tabs to be set to the launch tab group
		List<ILaunchConfigurationTab> tabs = new ArrayList<ILaunchConfigurationTab>();

		// Create the default launch tabs
		createContextSelectorTab(dialog, tabs, mode);

		// Create and add any additional launch tabs
		createAdditionalTabs(dialog, tabs, mode);

		// Apply the tabs
		setTabs(tabs.toArray(new ILaunchConfigurationTab[tabs.size()]));
	}

	/**
	 * Create the context selector tab.
	 *
	 * @param dialog The launch configuration dialog this tab group is contained in.
	 * @param tabs The list of launch configuration tabs. Must not be <code>null</code>.
	 * @param mode The mode the launch configuration dialog was opened in.
	 */
	public void createContextSelectorTab(ILaunchConfigurationDialog dialog, List<ILaunchConfigurationTab> tabs, String mode) {
		Assert.isNotNull(tabs);

		ILaunchConfigurationTab tab = new LaunchContextSelectorTab();
		tabs.add(tab);
	}

	/**
	 * Hook for subclasses to overwrite to add additional launch configuration tabs to the given
	 * tab list.
	 * <p>
	 * Called from {@link #createTabs(ILaunchConfigurationDialog, String)} before setting the tabs list
	 * to the launch configuration tab group.
	 *
	 * @param dialog The launch configuration dialog this tab group is contained in.
	 * @param tabs The list of launch configuration tabs. Must not be <code>null</code>.
	 * @param mode The mode the launch configuration dialog was opened in.
	 */
	public void createAdditionalTabs(ILaunchConfigurationDialog dialog, List<ILaunchConfigurationTab> tabs, String mode) {
		Assert.isNotNull(tabs);
	}
}
