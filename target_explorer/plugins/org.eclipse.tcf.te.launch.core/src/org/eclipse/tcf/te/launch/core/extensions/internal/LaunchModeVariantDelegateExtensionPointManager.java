/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.extensions.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.runtime.interfaces.extensions.IExecutableExtension;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IVariantDelegate;

/**
 * Launch mode variant delegate extension point manager.
 */
public class LaunchModeVariantDelegateExtensionPointManager extends AbstractExtensionPointManager<IVariantDelegate> {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static LaunchModeVariantDelegateExtensionPointManager instance = new LaunchModeVariantDelegateExtensionPointManager();
	}

	/**
	 * Returns the singleton instance for the manager.
	 */
	public static LaunchModeVariantDelegateExtensionPointManager getInstance() {
		return LazyInstanceHolder.instance;
	}

	/**
	 * Constructor.
	 */
	LaunchModeVariantDelegateExtensionPointManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "delegate"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.launch.core.launchModeVariantDelegates"; //$NON-NLS-1$
	}

	/**
	 * Get the list of all registered launch mode variant delegates.
	 */
	public IVariantDelegate[] getLaunchModeVariantDelegates() {
		List<IVariantDelegate> delegates = new ArrayList<IVariantDelegate>();
		for (ExecutableExtensionProxy<IVariantDelegate> proxy : getExtensions().values()) {
			IExecutableExtension candidate = proxy.getInstance();
			if (candidate instanceof IVariantDelegate && !delegates.contains(candidate)) {
				delegates.add((IVariantDelegate)candidate);
			}
		}

		return delegates.toArray(new IVariantDelegate[delegates.size()]);
	}

	/**
	 * Returns the launch mode variant delegate with the given id.
	 *
	 * @param id The id of the launch mode variant delegate. Must not be <code>null</code>.
	 */
	public IVariantDelegate getLaunchModeVariantDelegate(String id) {
		assert id != null;
		IVariantDelegate delegate = null;
		if (getExtensions().containsKey(id)) {
			delegate = getExtensions().get(id).getInstance();
		}

		return delegate;
	}
}
