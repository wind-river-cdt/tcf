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
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IContextStepper;

/**
 * Stepper extension point manager implementation.
 */
public final class StepperExtensionPointManager extends AbstractExtensionPointManager<IContextStepper> {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.runtime.stepper.steppers"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "stepper"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed stepper.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed stepper.
	 *
	 * @return The list of contributed stepper, or an empty array.
	 */
	public IContextStepper[] getStepper(boolean unique) {
		List<IContextStepper> contributions = new ArrayList<IContextStepper>();
		Collection<ExecutableExtensionProxy<IContextStepper>> delegates = getExtensions().values();
		for (ExecutableExtensionProxy<IContextStepper> delegate : delegates) {
			IContextStepper instance = unique ? delegate.newInstance() : delegate.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new IContextStepper[contributions.size()]);
	}

	/**
	 * Returns the stepper identified by its unique id. If no stepper with the specified id is registered,
	 * <code>null</code> is returned.
	 *
	 * @param id The unique id of the stepper or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the stepper contribution.
	 *
	 * @return The stepper instance or <code>null</code>.
	 */
	public IContextStepper getStepper(String id, boolean unique) {
		Assert.isNotNull(id);
		IContextStepper contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IContextStepper> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}
}