/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.persistence.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.runtime.persistence.activator.CoreBundleActivator;


/**
 */
public class PersistenceDelegateBindingExtensionPointManager extends AbstractExtensionPointManager<PersistenceDelegateBinding> {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static PersistenceDelegateBindingExtensionPointManager instance = new PersistenceDelegateBindingExtensionPointManager();
	}

	/**
	 * Constructor.
	 */
	PersistenceDelegateBindingExtensionPointManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static PersistenceDelegateBindingExtensionPointManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.runtime.persistence.bindings"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "binding"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#doCreateExtensionProxy(org.eclipse.core.runtime.IConfigurationElement)
	 */
	@Override
	protected ExecutableExtensionProxy<PersistenceDelegateBinding> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
		return new ExecutableExtensionProxy<PersistenceDelegateBinding>(element) {
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy#newInstance()
			 */
			@Override
			public PersistenceDelegateBinding newInstance() {
				PersistenceDelegateBinding instance = new PersistenceDelegateBinding();
				try {
					instance.setInitializationData(getConfigurationElement(), null, null);
				} catch (CoreException e) {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
									e.getLocalizedMessage(), e);
					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
				}
				return instance;
			}
		};
	}

	/**
	 * Returns the applicable persistence delegate bindings for the given delegate context.
	 *
	 * @param context The delegate context or <code>null</code>.
	 * @return The list of applicable editor page bindings or an empty array.
	 */
	public PersistenceDelegateBinding[] getApplicableBindings(Object context, Object container) {
		List<PersistenceDelegateBinding> applicable = new ArrayList<PersistenceDelegateBinding>();

		for (PersistenceDelegateBinding binding : getBindings()) {
			Expression enablement = binding.getEnablement();

			// The binding is applicable by default if no expression is specified.
			boolean isApplicable = enablement == null;

			if (enablement != null) {
				if (context != null) {
					// Set the default variable to the delegate context.
					EvaluationContext evalContext = new EvaluationContext(null, context);
					evalContext.addVariable("context", context); //$NON-NLS-1$
					if (context instanceof Class) {
						evalContext.addVariable("contextClass", ((Class<?>)context).getName()); //$NON-NLS-1$
					}
					evalContext.addVariable("container", container); //$NON-NLS-1$
					if (container instanceof Class) {
						evalContext.addVariable("containerClass", ((Class<?>)container).getName()); //$NON-NLS-1$
					}
					// Allow plugin activation
					evalContext.setAllowPluginActivation(true);
					// Evaluate the expression
					try {
						isApplicable = enablement.evaluate(evalContext).equals(EvaluationResult.TRUE);
					} catch (CoreException e) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
										e.getLocalizedMessage(), e);
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
				} else {
					// The enablement is false by definition if no delegate context is given.
					isApplicable = false;
				}
			}

			// Add the binding if applicable
			if (isApplicable) {
				applicable.add(binding);
			}
		}

		return applicable.toArray(new PersistenceDelegateBinding[applicable.size()]);
	}

	/**
	 * Returns the list of all contributed persistence delegate bindings.
	 *
	 * @return The list of contributed persistence delegate bindings, or an empty array.
	 */
	public PersistenceDelegateBinding[] getBindings() {
		List<PersistenceDelegateBinding> contributions = new ArrayList<PersistenceDelegateBinding>();
		Collection<ExecutableExtensionProxy<PersistenceDelegateBinding>> persistenceDelegateBindings = getExtensions().values();
		for (ExecutableExtensionProxy<PersistenceDelegateBinding> persistenceDelegateBinding : persistenceDelegateBindings) {
			PersistenceDelegateBinding instance = persistenceDelegateBinding.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new PersistenceDelegateBinding[contributions.size()]);
	}

	/**
	 * Returns the persistence delegate binding identified by its unique id. If no persistence
	 * delegate binding with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the persistence delegate binding or <code>null</code>
	 *
	 * @return The persistence delegate binding instance or <code>null</code>.
	 */
	public PersistenceDelegateBinding getBinding(String id) {
		PersistenceDelegateBinding contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<PersistenceDelegateBinding> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = proxy.getInstance();
		}

		return contribution;
	}
}
