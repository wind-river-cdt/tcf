/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.ui.linux.app;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/**
 * Remote application launch configuration tab group implementation.
 */
public class LaunchConfigurationTabGroup extends org.eclipse.tcf.te.launch.ui.tabs.LaunchConfigurationTabGroup {

	/**
	 * Create the context processImage tab.
	 *
	 * @param dialog The launch configuration dialog this tab group is contained in.
	 * @param tabs The list of launch configuration tabs. Must not be <code>null</code>.
	 * @param mode The mode the launch configuration dialog was opened in.
	 */
	@Override
	public void createContextSelectorTab(ILaunchConfigurationDialog dialog, List<ILaunchConfigurationTab> tabs, String mode) {
		Assert.isNotNull(tabs);

		ILaunchConfigurationTab tab = new LaunchConfigurationMainTab();

		tabs.add(tab);
	}
}
