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
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.IRunControl;
import org.eclipse.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.ServiceManager;
import org.eclipse.tcf.te.runtime.services.interfaces.IDebugService;
import org.eclipse.tcf.te.runtime.stepper.StepperAttributeUtil;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.runtime.utils.ProgressHelper;
import org.eclipse.tcf.te.runtime.utils.StatusHelper;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.launch.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.IRemoteAppLaunchAttributes;

/**
 * Launch process step implementation.
 */
public class AttachDebuggerStep extends AbstractTcfLaunchStep {

	/**
	 * Constructor.
	 */
	public AttachDebuggerStep() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#validateExecute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void validateExecute(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException {
		if (StepperAttributeUtil.getProperty(IRemoteAppLaunchAttributes.ATTR_PROCESS_CONTEXT, fullQualifiedId, data) == null) {
			throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "missing process context")); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void execute(IStepContext context, final IPropertiesContainer data, final IFullQualifiedId fullQualifiedId, final IProgressMonitor monitor, final ICallback callback) {
		IDebugService dbgService = ServiceManager.getInstance().getService(getActivePeerModel(data), IDebugService.class, false);
		if (dbgService != null) {
			Callback cb = new Callback();
			dbgService.attach(getActivePeerModel(data), new PropertiesContainer(), cb);
			ExecutorsUtil.waitAndExecute(0, cb.getDoneConditionTester(monitor));

			Tcf.getChannelManager().openChannel(getActivePeerModel(data).getPeer(), null, new IChannelManager.DoneOpenChannel() {
				@Override
				public void doneOpenChannel(final Throwable error, final IChannel channel) {
					if (!ProgressHelper.isCancelOrError(AttachDebuggerStep.this, StatusHelper.getStatus(error), monitor, callback)) {
						IProcesses.ProcessContext processContext = (IProcesses.ProcessContext)StepperAttributeUtil.getProperty(IRemoteAppLaunchAttributes.ATTR_PROCESS_CONTEXT, fullQualifiedId, data);
						IRunControl runControl = channel.getRemoteService(IRunControl.class);
						if (runControl != null) {
							runControl.getContext(processContext.getID(), new IRunControl.DoneGetContext() {
								@Override
								public void doneGetContext(IToken token, Exception error, RunControlContext context) {
									if (error == null) {
										context.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
											@Override
											public void doneCommand(IToken token, Exception error) {
												Tcf.getChannelManager().closeChannel(channel);
												callback.done(AttachDebuggerStep.this, StatusHelper.getStatus(error));
											}
										});
									}
									else {
										Tcf.getChannelManager().closeChannel(channel);
										callback.done(AttachDebuggerStep.this, StatusHelper.getStatus(error));
									}
								}
							});
						}
						else {
							Tcf.getChannelManager().closeChannel(channel);
							callback.done(this, new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "missing run control service")); //$NON-NLS-1$
						}
					}
					else if (channel != null) {
						Tcf.getChannelManager().closeChannel(channel);
					}
				}
			});
		}
		else {
			callback.done(this, new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "missing debugger service")); //$NON-NLS-1$
		}
	}
}
