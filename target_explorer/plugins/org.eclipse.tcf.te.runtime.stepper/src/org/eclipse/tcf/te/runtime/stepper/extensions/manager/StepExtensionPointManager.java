/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;

/**
 * Step extension point manager implementation.
 */
public final class StepExtensionPointManager extends AbstractExtensionPointManager<IStep> {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getExtensionPointId()
	 */
	@Override
	protected String getExtensionPointId() {
		return "org.eclipse.tcf.te.runtime.stepper.steps"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.AbstractExtensionPointManager#getConfigurationElementName()
	 */
	@Override
	protected String getConfigurationElementName() {
		return "step"; //$NON-NLS-1$
	}

	/**
	 * Returns the list of all contributed steps.
	 *
	 * @param unique If <code>true</code>, the method returns new instances for each
	 *               contributed step.
	 *
	 * @return The list of contributed steps, or an empty array.
	 */
	public IStep[] getSteps(boolean unique) {
		List<IStep> contributions = new ArrayList<IStep>();
		Collection<ExecutableExtensionProxy<IStep>> delegates = getExtensions().values();
		for (ExecutableExtensionProxy<IStep> delegate : delegates) {
			IStep instance = unique ? delegate.newInstance() : delegate.getInstance();
			if (instance != null && !contributions.contains(instance)) {
				contributions.add(instance);
			}
		}

		return contributions.toArray(new IStep[contributions.size()]);
	}

	/**
	 * Returns the step identified by its unique id. If no step with the specified id is registered,
	 * <code>null</code> is returned.
	 *
	 * @param id The unique id of the step or <code>null</code>
	 * @param unique If <code>true</code>, the method returns new instances of the step contribution.
	 *
	 * @return The step instance or <code>null</code>.
	 */
	public IStep getStep(String id, boolean unique) {
		Assert.isNotNull(id);
		IStep contribution = null;
		if (getExtensions().containsKey(id)) {
			ExecutableExtensionProxy<IStep> proxy = getExtensions().get(id);
			// Get the extension instance
			contribution = unique ? proxy.newInstance() : proxy.getInstance();
		}

		return contribution;
	}
}
