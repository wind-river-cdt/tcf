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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.stepper.StepperAttributeUtil;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.runtime.utils.StatusHelper;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.ICommonTCFLaunchAttributes;

/**
 * Open channel step implementation.
 */
public class OpenChannelStep extends AbstractTcfLaunchStep {

	/**
	 * Constructor.
	 */
	public OpenChannelStep() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#validateExecute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void validateExecute(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void execute(IStepContext context, final IPropertiesContainer data, final IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, final ICallback callback) {
		Tcf.getChannelManager().openChannel(getActivePeerModel(data).getPeer(), null, new IChannelManager.DoneOpenChannel() {
			@Override
			public void doneOpenChannel(final Throwable error, final IChannel channel) {
				StepperAttributeUtil.setProperty(ICommonTCFLaunchAttributes.ATTR_CHANNEL, fullQualifiedId.getParentId(), data, channel);
				callback.done(OpenChannelStep.this, StatusHelper.getStatus(error));
			}
		});
	}

	@Override
	public void rollback(IStepContext context, IPropertiesContainer data, IStatus status, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, ICallback callback) {
		IChannel channel = (IChannel)StepperAttributeUtil.getProperty(ICommonTCFLaunchAttributes.ATTR_CHANNEL, fullQualifiedId, data);
		if (channel != null && channel.getState() != IChannel.STATE_CLOSED) {
			Tcf.getChannelManager().closeChannel(channel);
		}
		super.rollback(context, data, status, fullQualifiedId, monitor, callback);
	}
}
