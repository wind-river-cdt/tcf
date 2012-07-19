/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.core.internal;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.tcf.te.launch.core.lm.LaunchManager;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;

/**
 * LaunchConfigurationListenerDelegate
 */
public class LaunchConfigurationListenerDelegate implements ILaunchConfigurationListener {

	/**
	 * Constructor.
	 */
	public LaunchConfigurationListenerDelegate() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		if (configuration instanceof ILaunchConfigurationWorkingCopy) {
			return;
		}
		ILaunchConfigurationListener listener = getListeningDelegate(configuration);
		if (listener != null) {
			listener.launchConfigurationAdded(configuration);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		if (configuration instanceof ILaunchConfigurationWorkingCopy) {
			return;
		}
		ILaunchConfigurationListener listener = getListeningDelegate(configuration);
		if (listener != null) {
			listener.launchConfigurationChanged(configuration);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		if (configuration instanceof ILaunchConfigurationWorkingCopy) {
			return;
		}
		ILaunchConfigurationListener listener = getListeningDelegate(configuration);
		if (listener != null) {
			listener.launchConfigurationRemoved(configuration);
		}
	}

	private ILaunchConfigurationListener getListeningDelegate(ILaunchConfiguration configuration) {
		try {
			ILaunchManagerDelegate delegate = LaunchManager.getInstance().getLaunchManagerDelegate(configuration.getType(), ""); //$NON-NLS-1$
			if (delegate instanceof ILaunchConfigurationListener) {
				return (ILaunchConfigurationListener)delegate;
			}
		}
		catch (Exception e) {
		}
		return null;
	}
}
