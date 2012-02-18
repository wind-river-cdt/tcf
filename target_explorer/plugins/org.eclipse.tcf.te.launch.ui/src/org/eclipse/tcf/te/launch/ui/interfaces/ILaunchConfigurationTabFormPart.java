/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.interfaces;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * A form part which can be added to launch configuration tabs and participate at the launch
 * configuration tab life cycle.
 */
public interface ILaunchConfigurationTabFormPart {

	/**
	 * Initializes this form part controls with values from the given launch configuration. This
	 * method is called when a configuration is selected to view or edit, after this form part's
	 * control has been created.
	 *
	 * @param configuration The launch configuration. Must not be <code>null</code>.
	 */
	public void initializeFrom(ILaunchConfiguration configuration);

	/**
	 * Copies values from this form part into the given launch configuration.
	 *
	 * @param configuration The launch configuration. Must not be <code>null</code>.
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration);

	/**
	 * Returns whether this form part is in a valid state in the context of the specified launch
	 * configuration.
	 * <p>
	 * This information is typically used by the launch configuration dialog to decide when it is
	 * okay to launch.
	 * </p>
	 *
	 * @param configuration The launch configuration. Must not be <code>null</code>.
	 * @return <code>True</code> if this form part is in a valid state.
	 */
	public boolean isValid(ILaunchConfiguration configuration);
}
