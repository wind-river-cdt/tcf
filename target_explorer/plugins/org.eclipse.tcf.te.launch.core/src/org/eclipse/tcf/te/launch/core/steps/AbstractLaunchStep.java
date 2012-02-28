/*
 * AbstractLaunchStep.java
 * Created on 22.02.2012
 *
 * Copyright 2012 Wind River Systems Inc. All rights reserved.
 */
package org.eclipse.tcf.te.launch.core.steps;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.tcf.te.launch.core.lm.interfaces.IContextSelectorLaunchAttributes;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.stepper.extensions.AbstractStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;

/**
 * AbstractLaunchStep
 * @author tobias.schwarz@windriver.com
 */
public abstract class AbstractLaunchStep extends AbstractStep {

	/**
	 * Rteurns the launch object for the given step context.
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
		Object context = data.getProperty(IContextSelectorLaunchAttributes.ATTR_ACTIVE_LAUNCH_CONTEXT);
		Assert.isTrue(context instanceof IModelNode);
		return (IModelNode)context;
	}
}
