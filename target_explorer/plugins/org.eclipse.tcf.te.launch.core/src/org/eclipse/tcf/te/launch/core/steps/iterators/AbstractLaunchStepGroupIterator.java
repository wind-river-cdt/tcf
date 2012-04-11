/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.tcf.te.launch.core.steps.iterators;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchContextLaunchAttributes;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupIterator;

/**
 * Abstract launch stepgroup iterator.
 */
public abstract class AbstractLaunchStepGroupIterator extends ExecutableExtension implements IStepGroupIterator {

	/**
	 * Returns the launch object for the given step context.
	 *
	 * @param context The step context.
	 * @return The launch or <code>null</code>.
	 */
	protected ILaunch getLaunch(IStepContext context) {
		Assert.isNotNull(context);
		return (ILaunch)context.getAdapter(ILaunch.class);
	}

	/**
	 * Returns the active launch context model node that is currently used.
	 *
	 * @param data The data giving object. Must not be <code>null</code>.
	 * @return The active launch context model node.
	 */
	protected IModelNode getActiveLaunchContext(IPropertiesContainer data) {
		Assert.isNotNull(data);
		Object context = data.getProperty(ILaunchContextLaunchAttributes.ATTR_ACTIVE_LAUNCH_CONTEXT);
		Assert.isTrue(context instanceof IModelNode);
		return (IModelNode)context;
	}

	/**
	 * Returns the uses launch configuration.
	 *
	 * @param context The step context.
	 * @return
	 */
	protected ILaunchConfiguration getLaunchConfiguration(IStepContext context) {
		ILaunch launch = getLaunch(context);
		Assert.isNotNull(launch);
		return launch.getLaunchConfiguration();
	}
}
