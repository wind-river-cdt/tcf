/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.core.steps;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.launch.core.steps.AbstractLaunchStep;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Abstract TCF launch step implementation.
 */
public abstract class AbstractTcfLaunchStep extends AbstractLaunchStep {

	/**
	 * Returns the active peer model that is currently used.
	 *
	 * @param data The data giving object. Must not be <code>null</code>.
	 * @return The active peer model.
	 */
	protected IPeerModel getActivePeerModel(IFullQualifiedId fullQualifiedId, IPropertiesContainer data) {
		IModelNode node = getActiveLaunchContext(fullQualifiedId, data);
		Assert.isTrue(node instanceof IPeerModel);
		return (IPeerModel)node;
	}
}
