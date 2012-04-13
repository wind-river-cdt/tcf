/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.va.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.tcf.core.activator.CoreBundleActivator;


/**
 * Value-add bindings extension point manager implementation.
 */
public class BindingExtensionPointManager extends AbstractExtensionPointManager<Binding> {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static BindingExtensionPointManager instance = new BindingExtensionPointManager();
	}

	/**
	 * Constructor.
	 */
	BindingExtensionPointManager() {
		super();
	}

	/**
	 * Returns the singleton instance of the extension point manager.
	 */
	public static BindingExtensionPointManager getInstance() {
		return LazyInstance.instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.tcf.core.valueaddBindings"; //$NON-NLS-1$
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
	protected ExecutableExtensionProxy<Binding> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
		return new ExecutableExtensionProxy<Binding>(element) {
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy#newInstance()
			 */
			@Override
			public Binding newInstance() {
				Binding instance = new Binding();
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
	 * Returns the applicable value-add bindings for the given delegate context.
	 *
	 * @param peer The peer. Must not be <code>null</code>.
	 *
	 * @return The list of applicable value-add bindings or an empty array.
	 */
	public Binding[] getApplicableBindings(IPeer peer) {
		Assert.isNotNull(peer);

		List<Binding> applicable = new ArrayList<Binding>();

		for (Binding binding : getBindings()) {
			Expression enablement = binding.getEnablement();

			// The binding is *not* applicable by default if no expression is specified.
			boolean isApplicable = false;

			if (enablement != null) {
				if (peer != null) {
					// Set the default variable to the peer.
					EvaluationContext evalContext = new EvaluationContext(null, peer);
					evalContext.addVariable("peer", peer); //$NON-NLS-1$
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
					// The enablement is false by definition if no peer is given.
					isApplicable = false;
				}
			}

			// Add the binding if applicable
			if (isApplicable) {
				applicable.add(binding);
			}
		}

		return applicable.toArray(new Binding[applicable.size()]);
	}

	/**
	 * Returns the list of all contributed value-add bindings.
	 *
	 * @return The list of contributed value-add bindings, or an empty array.
	 */
	public Binding[] getBindings() {
		List<Binding> contributions = new ArrayList<Binding>();
		Collection<ExecutableExtensionProxy<Binding>> bindings = getExtensions().values();
		for (ExecutableExtensionProxy<Binding> binding : bindings) {
			Binding instance = binding.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new Binding[contributions.size()]);
	}

	/**
	 * Returns the value-add binding identified by its unique id. If no value
	 * add binding with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the value-add binding or <code>null</code>
	 *
	 * @return The value-add binding instance or <code>null</code>.
	 */
	public Binding getBinding(String id) {
		Binding contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<Binding> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = proxy.getInstance();
		}

		return contribution;
	}
}
