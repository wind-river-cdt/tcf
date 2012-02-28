/*
 * AbstractTcfLaunchStep.java
 * Created on 22.02.2012
 *
 * Copyright 2012 Wind River Systems Inc. All rights reserved.
 */
package org.eclipse.tcf.te.tcf.launch.core.steps;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.launch.core.steps.AbstractLaunchStep;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * AbstractTcfLaunchStep
 * @author tobias.schwarz@windriver.com
 */
public abstract class AbstractTcfLaunchStep extends AbstractLaunchStep {

	/**
	 * Returns the active peer model that is currently used.
	 * 
	 * @param data The data giving object. Must not be <code>null</code>.
	 * @return The active peer model.
	 */
	protected IPeerModel getActivePeerModel(IPropertiesContainer data) {
		IModelNode node = getActiveLaunchContext(data);
		Assert.isTrue(node instanceof IPeerModel);
		return (IPeerModel)node;
	}
}
