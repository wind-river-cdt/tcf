/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.lm.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.launch.core.lm.delegates.DefaultLaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.runtime.interfaces.extensions.IExecutableExtension;

/**
 * Launch manager delegate extension point manager.
 */
public class ExtensionPointManager extends AbstractExtensionPointManager<ILaunchManagerDelegate> {

	private final ILaunchManagerDelegate defaultDelegate = new DefaultLaunchManagerDelegate();

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static ExtensionPointManager instance = new ExtensionPointManager();
	}

	/**
	 * Returns the singleton instance for the manager.
	 */
	public static ExtensionPointManager getInstance() {
		return LazyInstanceHolder.instance;
	}

	/**
	 * Constructor.
	 */
	ExtensionPointManager() {
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
		return "org.eclipse.tcf.te.launch.core.launchManagerDelegates"; //$NON-NLS-1$
	}

	/**
	 * Get the list of all registered launch manager delegates.
	 */
	public ILaunchManagerDelegate[] getLaunchManagerDelegates() {
		List<ILaunchManagerDelegate> delegates = new ArrayList<ILaunchManagerDelegate>();
		for (ExecutableExtensionProxy<ILaunchManagerDelegate> proxy : getExtensions().values()) {
			IExecutableExtension candidate = proxy.getInstance();
			if (candidate instanceof ILaunchManagerDelegate && !delegates.contains(candidate)) {
				delegates.add((ILaunchManagerDelegate)candidate);
			}
		}

		return delegates.toArray(new ILaunchManagerDelegate[delegates.size()]);
	}

	/**
	 * Returns the launch manager delegate with the given id.
	 * @param id The id of the launch manager delegate.
	 */
	public ILaunchManagerDelegate getLaunchManagerDelegate(String id) {
		Assert.isNotNull(id);
		ILaunchManagerDelegate delegate = null;
		if (getExtensions().containsKey(id)) {
			delegate = getExtensions().get(id).getInstance();
		}

		return delegate != null ? delegate : getDefaultLaunchManagerDelegate();
	}

	/**
	 * Returns the default dummy launch manager delegate.
	 */
	public ILaunchManagerDelegate getDefaultLaunchManagerDelegate() {
		return defaultDelegate;
	}
}
