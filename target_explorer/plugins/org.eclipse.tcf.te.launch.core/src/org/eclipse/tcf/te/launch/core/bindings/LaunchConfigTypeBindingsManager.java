/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.bindings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.tcf.te.launch.core.bindings.internal.LaunchConfigTypeBinding;
import org.eclipse.tcf.te.launch.core.bindings.internal.OverwritableLaunchBinding;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
import org.eclipse.tcf.te.launch.core.lm.internal.ExtensionPointManager;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
import org.eclipse.tcf.te.runtime.extensions.ExtensionPointComparator;
import org.eclipse.tcf.te.runtime.stepper.StepperManager;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup;


/**
 * Manager that controls the launch configuration type bindings.
 */
public class LaunchConfigTypeBindingsManager {
	// Map of all launch configuration type bindings by id
	private final Map<String, LaunchConfigTypeBinding> bindings = new Hashtable<String, LaunchConfigTypeBinding>();

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static LaunchConfigTypeBindingsManager instance = new LaunchConfigTypeBindingsManager();
	}

	/**
	 * Returns the singleton instance.
	 */
	public static LaunchConfigTypeBindingsManager getInstance() {
		return LazyInstanceHolder.instance;
	}

	/**
	 * Constructor.
	 */
	LaunchConfigTypeBindingsManager() {
		// Load the launch configuration type bindings on instantiation.
		loadBindingsExtensions();
	}

	/**
	 * Get all valid launch configuration type id's for the given selection.
	 *
	 * @param selection The selection or <code>null</code>.
	 * @return The list of valid launch configuration type id's for the selection or an empty list.
	 */
	public String[] getValidLaunchConfigTypes(ILaunchSelection selection) {
		Set<String> validLaunchTypes = new HashSet<String>();
		if (selection != null && selection.getSelectedContexts() != null && selection.getSelectedContexts().length > 0) {
			for (String launchConfigTypeId : bindings.keySet()) {
				if (isValidLaunchConfigType(launchConfigTypeId, selection)) {
					validLaunchTypes.add(launchConfigTypeId);
				}
			}
		}
		return validLaunchTypes.toArray(new String[validLaunchTypes.size()]);
	}

	/**
	 * Validates the given launch selection.
	 *
	 * @param typeId The launch configuration type id. Must not be <code>null</code>.
	 * @param selection The selection. Must not be <code>null</code>.
	 */
	public boolean isValidLaunchConfigType(String typeId, ILaunchSelection selection) {
		Assert.isNotNull(typeId);
		Assert.isNotNull(selection);

		LaunchConfigTypeBinding binding = bindings.get(typeId);
		ILaunchConfigurationType launchConfigType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
		return (launchConfigType != null && launchConfigType.isPublic() &&
						(selection.getLaunchMode() == null || launchConfigType.supportsMode(selection.getLaunchMode())) &&
						binding != null && binding.validate(selection) == EvaluationResult.TRUE);
	}

	/**
	 * Validates the given launch selection.
	 *
	 * @param typeId The launch configuration type id. Must not be <code>null</code>.
	 * @param mode The launch mode or <code>null</code>.
	 * @param context The selection context. Must not be <code>null</code>.
	 */
	public boolean isValidLaunchConfigType(String typeId, String mode, ISelectionContext context) {
		Assert.isNotNull(typeId);
		Assert.isNotNull(context);

		LaunchConfigTypeBinding binding = bindings.get(typeId);
		ILaunchConfigurationType launchConfigType = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType(typeId);
		return (launchConfigType != null && launchConfigType.isPublic() &&
						(mode == null || launchConfigType.supportsMode(mode)) &&
						binding != null && binding.validate(mode, context) != EvaluationResult.FALSE);
	}

	/**
	 * Get the registered launch manager delegate for the given launch configuration type and launch mode.
	 *
	 * @param typeId The launch configuration type id. Must not be <code>null</code>.
	 * @param mode The launch mode. Must not be <code>null</code>.
	 *
	 * @return The launch manager delegate, or a default delegate if no delegate is registered for the
	 *         given launch configuration type id and launch mode.
	 */
	public ILaunchManagerDelegate getLaunchManagerDelegate(String typeId, String mode) {
		Assert.isNotNull(typeId);
		Assert.isNotNull(mode);

		LaunchConfigTypeBinding binding = bindings.get(typeId);
		if (binding != null) {
			String id = binding.getLaunchManagerDelegate(mode);
			if (id != null) {
				return ExtensionPointManager.getInstance().getLaunchManagerDelegate(id);
			}
		}
		return ExtensionPointManager.getInstance().getDefaultLaunchManagerDelegate();
	}

	/**
	 * Get the registered step group id for the given launch configuration type and launch mode.
	 *
	 * @param typeId The launch configuration type id. Must not be <code>null</code>.
	 * @param mode The launch mode. Must not be <code>null</code>.
	 *
	 * @return The launch step group id or <code>null</code> if no step group is registered for the
	 *         given launch configuration type id and launch mode.
	 */
	public String getStepGroupId(String typeId, String mode) {
		Assert.isNotNull(typeId);
		Assert.isNotNull(mode);

		LaunchConfigTypeBinding binding = bindings.get(typeId);
		if (binding != null) {
			return binding.getStepGroupId(mode);
		}
		return null;
	}

	/**
	 * Get the registered step group identified by the specified unique id.
	 *
	 * @param id The step group id. Must not be <code>null</code>.
	 * @return The step group or <code>null</code>, if no step group is registered for the specified id.
	 */
	public IStepGroup getStepGroup(String id) {
		Assert.isNotNull(id);
		return StepperManager.getInstance().getStepGroupExtManager().getStepGroup(id, true);
	}

	/**
	 * Get the registered step identified by the specified unique id.
	 *
	 * @param id The launch step id. Must not be <code>null</code>.
	 * @return The launch step or <code>null</code> if no step is registered for the specified id.
	 */
	public IStep getStep(String id) {
		Assert.isNotNull(id);
		return StepperManager.getInstance().getStepExtManager().getStep(id, true);
	}

	/*
	 * Load and register all launch configuration type bindings.
	 */
	private void loadBindingsExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint("org.eclipse.tcf.te.launch.core.launchConfigTypeBindings"); //$NON-NLS-1$
		if (point != null) {
			IExtension[] bindings = point.getExtensions();
			Arrays.sort(bindings, new ExtensionPointComparator());
			for (IExtension binding : bindings) {
				IConfigurationElement[] elements = binding.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					loadBinding(element);
				}
			}
		}
	}

	/**
	 * Load a single launch configuration type binding.
	 *
	 * @param element The configuration element. Must not be <code>null</code>.
	 */
	private void loadBinding(IConfigurationElement element) {
		Assert.isNotNull(element);

		if (!element.getName().equals("launchConfigTypeBinding")) { //$NON-NLS-1$
			return;
		}

		String launchConfigTypeId = element.getAttribute("launchConfigTypeId"); //$NON-NLS-1$
		if (!bindings.containsKey(launchConfigTypeId)) {
			bindings.put(launchConfigTypeId, new LaunchConfigTypeBinding(launchConfigTypeId));
		}
		LaunchConfigTypeBinding binding = bindings.get(launchConfigTypeId);

		IConfigurationElement[] lmDelegateBindings = element.getChildren("launchManagerDelegate"); //$NON-NLS-1$
		for (IConfigurationElement lmDelegateBinding : lmDelegateBindings) {
			String id = lmDelegateBinding.getAttribute("id"); //$NON-NLS-1$
			String overwrites = lmDelegateBinding.getAttribute("overwrites"); //$NON-NLS-1$
			String modes = lmDelegateBinding.getAttribute("modes"); //$NON-NLS-1$

			binding.addLaunchManagerDelegate(new OverwritableLaunchBinding(id, overwrites, modes));
		}

		IConfigurationElement[] stepperBindings = element.getChildren("stepper"); //$NON-NLS-1$
		for (IConfigurationElement stepperBinding : stepperBindings) {
			String id = stepperBinding.getAttribute("id"); //$NON-NLS-1$
			String overwrites = stepperBinding.getAttribute("overwrites"); //$NON-NLS-1$
			String modes = stepperBinding.getAttribute("modes"); //$NON-NLS-1$

			binding.addStepper(new OverwritableLaunchBinding(id, overwrites, modes));
		}

		IConfigurationElement[] stepGroupBindings = element.getChildren("stepGroup"); //$NON-NLS-1$
		for (IConfigurationElement stepGroupBinding : stepGroupBindings) {
			String id = stepGroupBinding.getAttribute("id"); //$NON-NLS-1$
			String overwrites = stepGroupBinding.getAttribute("overwrites"); //$NON-NLS-1$
			String modes = stepGroupBinding.getAttribute("modes"); //$NON-NLS-1$

			binding.addStepGroup(new OverwritableLaunchBinding(id, overwrites, modes));
		}

		IConfigurationElement[] enablements = element.getChildren("enablement"); //$NON-NLS-1$
		for (IConfigurationElement enablement : enablements) {
			Expression expression = null;
			try {
				expression = ExpressionConverter.getDefault().perform(enablement);
			} catch (CoreException e) {
				if (Platform.inDebugMode()) {
					e.printStackTrace();
				}
			}

			if (expression != null) {
				binding.addEnablement(expression);
			}
		}
	}

}
