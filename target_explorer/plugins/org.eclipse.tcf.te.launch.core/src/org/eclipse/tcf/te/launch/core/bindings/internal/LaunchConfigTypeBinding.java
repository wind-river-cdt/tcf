/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.bindings.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.launch.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.launch.core.bindings.interfaces.ILaunchBinding;
import org.eclipse.tcf.te.launch.core.bindings.interfaces.IOverwritableLaunchBinding;
import org.eclipse.tcf.te.launch.core.bindings.interfaces.IVaryableLaunchBinding;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;

/**
 * Launch configuration type binding implementation.
 */
public class LaunchConfigTypeBinding {
	// The launch configuration type id
	private final String typeId;

	// Lists of sub bindings
	private final List<ILaunchBinding> lmDelegateBindings = new ArrayList<ILaunchBinding>();
	private final List<ILaunchBinding> stepperBindings = new ArrayList<ILaunchBinding>();
	private final List<ILaunchBinding> stepGroupBindings = new ArrayList<ILaunchBinding>();
	private final List<ILaunchBinding> variantDelegateBindings = new ArrayList<ILaunchBinding>();

	// The list of enablement expressions
	private final List<Expression> expressions = new ArrayList<Expression>();

	/**
	 * Constructor.
	 *
	 * @param typeId The launch configuration type id the binding applies to. Must not be
	 *            <code>null</code>.
	 */
	public LaunchConfigTypeBinding(String typeId) {
		Assert.isNotNull(typeId);
		this.typeId = typeId;
	}

	/**
	 * Returns the launch configuration type id the binding applies to.
	 *
	 * @return The launch configuration type id.
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * Returns the launch manager delegate id for the given launch mode.
	 *
	 * @param mode The launch mode. Must not be <code>null</code>.
	 * @return The launch manager delegate id or <code>null</code>.
	 */
	public String getLaunchManagerDelegate(String mode) {
		Assert.isNotNull(mode);

		ILaunchBinding binding = getBinding(lmDelegateBindings, mode);
		return binding != null ? binding.getId() : null;
	}

	/**
	 * Adds the given launch manager delegate binding.
	 *
	 * @param binding The binding. Must not be <code>null</code>.
	 */
	public void addLaunchManagerDelegate(IOverwritableLaunchBinding binding) {
		Assert.isNotNull(binding);
		if (!lmDelegateBindings.contains(binding)) {
			lmDelegateBindings.add(binding);
		}
	}

	/**
	 * Returns the launch mode variant delegate id for the given launch mode.
	 *
	 * @param mode The launch mode. Must not be <code>null</code>.
	 * @return The launch mode variant delegate id or <code>null</code>.
	 */
	public String getLaunchModeVariantDelegate(String mode) {
		Assert.isNotNull(mode);

		ILaunchBinding binding = getBinding(variantDelegateBindings, mode);
		return binding != null ? binding.getId() : null;
	}

	/**
	 * Adds the given launch mode variant delegate binding.
	 *
	 * @param binding The binding. Must not be <code>null</code>.
	 */
	public void addLaunchModeVariantDelegate(IOverwritableLaunchBinding binding) {
		Assert.isNotNull(binding);
		if (!variantDelegateBindings.contains(binding)) {
			variantDelegateBindings.add(binding);
		}
	}

	/**
	 * Returns the stepper id for the given launch mode.
	 *
	 * @param mode The launch mode. Must not be <code>null</code>.
	 * @return The stepper id or <code>null</code>.
	 */
	public String getStepper(String mode) {
		Assert.isNotNull(mode);

		ILaunchBinding binding = getBinding(stepperBindings, mode);
		return binding != null ? binding.getId() : null;
	}

	/**
	 * Adds the given stepper binding.
	 *
	 * @param binding The binding. Must not be <code>null</code>.
	 */
	public void addStepper(IOverwritableLaunchBinding binding) {
		Assert.isNotNull(binding);
		if (!stepperBindings.contains(binding)) {
			stepperBindings.add(binding);
		}
	}

	/**
	 * Returns the step group id for the given launch mode and variant.
	 *
	 * @param mode The launch mode. Must not be <code>null</code>.
	 * @param variant The launch mode variant or <code>null</code>.
	 *
	 * @return The step group id or <code>null</code>.
	 */
	public String getStepGroupId(String mode, String variant) {
		ILaunchBinding binding = getBinding(stepGroupBindings, mode, variant);
		return binding != null ? binding.getId() : null;
	}

	/**
	 * Adds the given step group binding.
	 *
	 * @param binding The binding. Must not be <code>null</code>.
	 */
	public void addStepGroup(ILaunchBinding binding) {
		Assert.isNotNull(binding);
		if (!stepGroupBindings.contains(binding)) {
			stepGroupBindings.add(binding);
		}
	}

	/**
	 * Adds the given enablement expression.
	 *
	 * @param enablement The enablement expression. Must not be <code>null</code>.
	 */
	public void addEnablement(Expression expression) {
		Assert.isNotNull(expression);
		if (!expressions.contains(expression)) {
			expressions.add(expression);
		}
	}

	/**
	 * Evaluates the enablement expressions with the given launch selection.
	 *
	 * @param selection The launch selection. Must not be <code>null</code>.
	 * @return The result of the enablement expression evaluation.
	 */
	public EvaluationResult validate(ILaunchSelection selection) {
		Assert.isNotNull(selection);

		EvaluationResult result = EvaluationResult.NOT_LOADED;

		EvaluationResult valresult;
		for (ISelectionContext context : selection.getSelectedContexts()) {
			if (context.isPreferredContext()) {
				valresult = validate(selection.getLaunchMode(), context);
				if (valresult == EvaluationResult.FALSE) {
					return EvaluationResult.FALSE;
				}
				else if (valresult != EvaluationResult.NOT_LOADED) {
					result = valresult;
				}
			}
		}
		return result;
	}

	/**
	 * Evaluates the enablement expressions with the given launch mode and selection context.
	 *
	 * @param mode The launch mode. Must not be <code>null</code>.
	 * @param context The launch selection context or <code>null</code>.
	 *
	 * @return The result of the enablement expression evaluation.
	 */
	public EvaluationResult validate(String mode, ISelectionContext context) {
		EvaluationResult result = context.isPreferredContext() ? EvaluationResult.FALSE : EvaluationResult.NOT_LOADED;

		if (expressions.isEmpty()) {
			return EvaluationResult.TRUE;
		}
		EvaluationResult valresult;
		for (Expression expression : expressions) {
			// Set the default variable and "selection" is the selection context
			EvaluationContext evalContext = new EvaluationContext(null, context);
			evalContext.addVariable("selection", context); //$NON-NLS-1$
			evalContext.addVariable("mode", mode); //$NON-NLS-1$
			// Allow plugin activation
			evalContext.setAllowPluginActivation(true);
			// Evaluate the expression
			try {
				valresult = expression.evaluate(evalContext);
			} catch (CoreException e) {
				valresult = EvaluationResult.FALSE;

				if (Platform.inDebugMode()) {
					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e);
					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
				}
			}

			if (valresult == EvaluationResult.TRUE) {
				return EvaluationResult.TRUE;
			}
			if (valresult != EvaluationResult.NOT_LOADED) {
				result = valresult;
			}
		}

		return result;
	}

	/**
	 * Returns the list of bindings valid for the given launch mode.
	 *
	 * @param bindings The list of available bindings. Must not be <code>null</code>.
	 * @param mode The launch mode. Must not be <code>null</code>.
	 * @param variant The launch mode variant or <code>null</code>.
	 *
	 * @return The list of valid bindings for the given launch mode or an empty list.
	 */
	private List<ILaunchBinding> getBindings(List<ILaunchBinding> bindings, String mode, String variant) {
		Assert.isNotNull(bindings);
		Assert.isNotNull(mode);

		List<ILaunchBinding> candidates = new ArrayList<ILaunchBinding>();
		for (ILaunchBinding binding : bindings) {
			if (binding instanceof IVaryableLaunchBinding) {
				if (((IVaryableLaunchBinding) binding).isValidLaunchMode(mode, variant)) {
					candidates.add(binding);
				}
			}
			else if (binding.isValidLaunchMode(mode)) {
				candidates.add(binding);
			}
		}

		return candidates;
	}

	/**
	 * Returns the resolved binding in case of overwritable bindings.
	 *
	 * @param bindings The list of available bindings. Must not be <code>null</code>.
	 * @param mode The launch mode. Must not be <code>null</code>.
	 *
	 * @return The resolved binding or <code>null</code>.
	 */
	private ILaunchBinding getBinding(List<ILaunchBinding> bindings, String mode) {
		return getBinding(bindings, mode, null);
	}

	/**
	 * Returns the resolved binding in case of overwritable bindings.
	 *
	 * @param bindings The list of available bindings. Must not be <code>null</code>.
	 * @param mode The launch mode. Must not be <code>null</code>.
	 * @param variant The launch mode variant or <code>null</code>.
	 *
	 * @return The resolved binding or <code>null</code>.
	 */
	private ILaunchBinding getBinding(List<ILaunchBinding> bindings, String mode, String variant) {
		Assert.isNotNull(bindings);
		Assert.isNotNull(mode);

		ILaunchBinding binding = null;

		List<ILaunchBinding> candidates = getBindings(bindings, mode, variant);
		for (int i = 0; i < candidates.size(); i++) {
			if (binding == null) {
				binding = candidates.get(i);
			}
			for (int j = 0; j < candidates.size(); j++) {
				ILaunchBinding cj = candidates.get(j);
				if (cj instanceof IOverwritableLaunchBinding
								&& ((IOverwritableLaunchBinding) cj).overwrites(binding.getId())) {
					binding = cj;
				}
			}
		}

		return binding;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();

		toString.append("LaunchConfigTypeBinding("); //$NON-NLS-1$
		toString.append(typeId);
		toString.append(")"); //$NON-NLS-1$

		return toString.toString();
	}
}
