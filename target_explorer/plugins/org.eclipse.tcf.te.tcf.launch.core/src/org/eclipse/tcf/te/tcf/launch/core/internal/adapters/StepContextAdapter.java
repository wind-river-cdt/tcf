/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.launch.core.internal.adapters;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Peer model step context adapter implementation.
 */
public class StepContextAdapter extends PlatformObject implements IStepContext {
	// Reference to the wrapped peer model
	/* default */ final IPeerModel peerModel;

	/**
     * Constructor.
     *
     * @param peerModel The peer model. Must not be <code>null</code>.
     */
    public StepContextAdapter(IPeerModel peerModel) {
    	super();
    	Assert.isNotNull(peerModel);
    	this.peerModel = peerModel;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.model.interfaces.IModelNodeProvider#getModelNode()
	 */
	@Override
	public IModelNode getModelNode() {
		return peerModel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getId()
	 */
	@Override
	public String getId() {
		return peerModel.getPeerId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getName()
	 */
	@Override
	public String getName() {
		final AtomicReference<String> name = new AtomicReference<String>();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				name.set(peerModel.getName());
			}
		};

		if (Protocol.isDispatchThread()) runnable.run();
		else Protocol.invokeAndWait(runnable);

		return name.get();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext#getInfo(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
	public String getInfo(IPropertiesContainer data) {
		return ""; //$NON-NLS-1$
	}

}
