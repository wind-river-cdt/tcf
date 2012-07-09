/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.runtime.persistence.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.persistence.interfaces.IPersistenceDelegate;
import org.eclipse.tcf.te.runtime.persistence.internal.PersistenceDelegateBinding;
import org.eclipse.tcf.te.runtime.persistence.internal.PersistenceDelegateBindingExtensionPointManager;

/**
 * Persistence delegate manager implementation.
 */
public class PersistenceManager extends AbstractExtensionPointManager<IPersistenceDelegate> {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static PersistenceManager instance = new PersistenceManager();
	}

	/**
	 * Constructor.
	 */
	PersistenceManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static PersistenceManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.runtime.persistence.delegates"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "delegate"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed persistence delegates.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed persistence delegate.
	 *
	 * @return The list of contributed persistence delegates, or an empty array.
	 */
	protected IPersistenceDelegate[] getDelegates(boolean unique) {
		List<IPersistenceDelegate> contributions = new ArrayList<IPersistenceDelegate>();
		Collection<ExecutableExtensionProxy<IPersistenceDelegate>> delegates = getExtensions().values();
		for (ExecutableExtensionProxy<IPersistenceDelegate> delegate : delegates) {
			IPersistenceDelegate instance = unique ? delegate.newInstance() : delegate.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new IPersistenceDelegate[contributions.size()]);
	}

	/**
	 * Returns the persistence delegate identified by its unique id. If no persistence
	 * delegate with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the persistence delegate or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the persistence delegate contribution.
	 *
	 * @return The persistence delegate instance or <code>null</code>.
	 */
	protected IPersistenceDelegate getDelegate(String id, boolean unique) {
		IPersistenceDelegate contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IPersistenceDelegate> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}

	/**
	 * Returns the persistence delegate which is enabled for the given
	 * context and persistence container.
	 *
	 * @param context The persistence delegate context. Must not be <code>null</code>.
	 * @param container The persistence container or <code>null</code>.
	 * @return The persistence delegate which is enabled or <code>null</code>.
	 */
	public IPersistenceDelegate getDelegate(Object context, Object container, boolean unique) {
		Assert.isNotNull(context);

		List<IPersistenceDelegate> delegates = new ArrayList<IPersistenceDelegate>();

		// Get the list of applicable bindings
		PersistenceDelegateBinding[] bindings = PersistenceDelegateBindingExtensionPointManager.getInstance().getApplicableBindings(context, container);
		for (PersistenceDelegateBinding binding : bindings) {
			IPersistenceDelegate delegate = getDelegate(binding.getDelegateId(), unique);
			if (delegate != null && !delegates.contains(delegate)) {
				delegates.add(delegate);
			}
		}

		// If no applicable persistence delegate is found, always return null
		if (delegates.isEmpty() || delegates.isEmpty()) {
			return null;
		}

		if (delegates.size() > 1) {
			IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "Found multiple persistence delgates for " + context.getClass().getName() + //$NON-NLS-1$
							" (" + context + ")" + (container != null ? " to store in " + container : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
		}

		return delegates.get(0);
	}
}
