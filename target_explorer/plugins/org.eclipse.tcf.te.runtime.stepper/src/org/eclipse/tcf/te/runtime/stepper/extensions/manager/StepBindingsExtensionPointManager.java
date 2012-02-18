/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper.extensions.manager;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.runtime.stepper.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;


/**
 * Step bindings extension point manager implementation.
 */
public final class StepBindingsExtensionPointManager extends AbstractExtensionPointManager<StepBinding> {

	/**
	 * Constructor.
	 */
	public StepBindingsExtensionPointManager() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.runtime.stepper.stepBindings"; //$NON-NLS-1$
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
	protected ExecutableExtensionProxy<StepBinding> doCreateExtensionProxy(IConfigurationElement element) throws CoreException {
		return new ExecutableExtensionProxy<StepBinding>(element) {
			/* (non-Javadoc)
			 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy#newInstance()
			 */
			@Override
			public StepBinding newInstance() {
				StepBinding instance = new StepBinding();
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
	 * Returns if or if not the step identified by the given step id is enabled for the
	 * given context.
	 * <p>
	 * <b>Note:</b> A step is considered enabled if
	 * <ul>
	 * <li>There is no step binding contribution for the given step id. Or</li>
	 * <li>At least one of the bindings has not enablement sub element. Or</li>
	 * <li>At least one of the bindings enablement sub element evaluates to <code>true</code>.</li>
	 * </ul>
	 *
	 * @param stepId The step id. Must not be <code>null</code>.
	 * @param contexts The context objects or <code>null</code>.
	 * @return The list of applicable editor page bindings or an empty array.
	 */
	public boolean isStepEnabled(String stepId, IStepContext[] contexts) {
		Assert.isNotNull(stepId);

		// Flag is set to true at the first binding matching the given step id
		boolean hasStepBinding = false;
		boolean enabled = false;

		for (StepBinding binding : getStepBindings()) {
			// Ignore all bindings not matching the given step id.
			if (!stepId.equals(binding.getStepId())) continue;

			// OK. There is at least one binding contribution for the given step id.
			hasStepBinding = true;

			// Get the enablement.
			Expression enablement = binding.getEnablement();

			// The binding is applicable by default if no expression is specified.
			enabled = enablement == null;

			if (enablement != null) {
				if (contexts != null) {
					// To satisfy the "isIterable" and "isCountable" needs of the expression
					// evaluator, pass on the contexts as collection.
					List<IStepContext> variableValue = Arrays.asList(contexts);
					// Set the default variable to the context.
					EvaluationContext evalContext = new EvaluationContext(null, variableValue);
					// Initialize the evaluation context named variables
					evalContext.addVariable("activeContexts", variableValue); //$NON-NLS-1$
					// Allow plugin activation
					evalContext.setAllowPluginActivation(true);
					// Evaluate the expression
					try {
						enabled = enablement.evaluate(evalContext).equals(EvaluationResult.TRUE);
					} catch (CoreException e) {
						IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
													e.getLocalizedMessage(), e);
						Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
					}
				} else {
					// The enablement is false by definition if no context is given.
					enabled = false;
				}
			}

			// Break the loop if the step is found enabled
			if (enabled) break;
		}

		return !hasStepBinding || enabled;
	}

	/**
	 * Returns the list of all contributed step bindings.
	 *
	 * @return The list of contributed step bindings, or an empty array.
	 */
	public StepBinding[] getStepBindings() {
		List<StepBinding> contributions = new ArrayList<StepBinding>();
		Collection<ExecutableExtensionProxy<StepBinding>> statusHandlerBindings = getExtensions().values();
		for (ExecutableExtensionProxy<StepBinding> statusHandlerBinding : statusHandlerBindings) {
			StepBinding instance = statusHandlerBinding.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new StepBinding[contributions.size()]);
	}

	/**
	 * Returns the step binding identified by its unique id. If no step
	 * binding with the specified id is registered, <code>null</code> is returned.
	 *
	 * @param id The unique id of the step binding or <code>null</code>
	 *
	 * @return The step binding instance or <code>null</code>.
	 */
	public StepBinding getBinding(String id) {
		StepBinding contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<StepBinding> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = proxy.getInstance();
		}

		return contribution;
	}
}
