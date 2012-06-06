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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.te.core.utils.text.StringUtil;
import org.eclipse.tcf.te.launch.core.persistence.DefaultPersistenceDelegate;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.runtime.stepper.StepperAttributeUtil;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.tcf.launch.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.ICommonTCFLaunchAttributes;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.IRemoteAppLaunchAttributes;
import org.eclipse.tcf.te.tcf.processes.core.interfaces.launcher.IProcessLauncher;
import org.eclipse.tcf.te.tcf.processes.core.launcher.ProcessLauncher;

/**
 * Launch process step implementation.
 */
public class LaunchProcessStep extends AbstractTcfLaunchStep {

	/**
	 * Constructor.
	 */
	public LaunchProcessStep() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#validateExecute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void validateExecute(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws CoreException {
		String processImage = DefaultPersistenceDelegate.getAttribute(getLaunchConfiguration(context), IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, (String)null);
		if (processImage != null && processImage.trim().length() > 0) {
			StepperAttributeUtil.setProperty(IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, fullQualifiedId, data, processImage);
		}
		else {
			throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "missing process image name")); //$NON-NLS-1$
		}

		IChannel channel = (IChannel)StepperAttributeUtil.getProperty(ICommonTCFLaunchAttributes.ATTR_CHANNEL, fullQualifiedId, data);
		if (channel == null || channel.getState() != IChannel.STATE_OPEN) {
			throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "missing or closed channel")); //$NON-NLS-1$
		}

		String processArguments = DefaultPersistenceDelegate.getAttribute(getLaunchConfiguration(context), IRemoteAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, (String)null);
		StepperAttributeUtil.setProperty(IRemoteAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, fullQualifiedId, data, processArguments);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void execute(IStepContext context, final IPropertiesContainer data, final IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, final ICallback callback) {
		final IChannel channel = (IChannel)StepperAttributeUtil.getProperty(ICommonTCFLaunchAttributes.ATTR_CHANNEL, fullQualifiedId, data);
		Assert.isTrue(channel != null && channel.getState() == IChannel.STATE_OPEN, "channel is missing or closed"); //$NON-NLS-1$
		// Construct the launcher object
		ProcessLauncher launcher = new ProcessLauncher();

		Map<String, Object> launchAttributes = new HashMap<String, Object>();

		launchAttributes.put(IProcessLauncher.PROP_PROCESS_PATH, StepperAttributeUtil.getStringProperty(IRemoteAppLaunchAttributes.ATTR_PROCESS_IMAGE, fullQualifiedId, data));

		String arguments = StepperAttributeUtil.getStringProperty(IRemoteAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, fullQualifiedId, data);
		String[] args = arguments != null && !"".equals(arguments.trim()) ? StringUtil.tokenize(arguments, 0, true) : null; //$NON-NLS-1$
		launchAttributes.put(IProcessLauncher.PROP_PROCESS_ARGS, args);

		launchAttributes.put(ITerminalsConnectorConstants.PROP_LOCAL_ECHO, Boolean.FALSE);

		boolean outputConsole = DefaultPersistenceDelegate.getAttribute(getLaunchConfiguration(context), "org.eclipse.debug.ui.ATTR_CONSOLE_OUTPUT_ON", true); //$NON-NLS-1$
		if (outputConsole) {
			launchAttributes.put(IProcessLauncher.PROP_PROCESS_ASSOCIATE_CONSOLE, Boolean.TRUE);
		}
		String outputFile = DefaultPersistenceDelegate.getAttribute(getLaunchConfiguration(context), "org.eclipse.debug.ui.ATTR_CAPTURE_IN_FILE", (String)null); //$NON-NLS-1$
		if (outputFile != null) {
			launchAttributes.put(IProcessLauncher.PROP_PROCESS_OUTPUT_REDIRECT_TO_FILE, outputFile);
		}

		if (ILaunchManager.DEBUG_MODE.equals(getLaunchMode(context))) {
			launchAttributes.put(IProcessLauncher.PROP_PROCESS_ATTACH, Boolean.TRUE);
		}

		// Launch the process
		IPropertiesContainer container = new PropertiesContainer();
		container.setProperties(launchAttributes);
		launcher.launch(getActivePeerModel(data).getPeer(), container, new Callback(callback) {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				Object result = getResult();
				if (status.isOK() && result instanceof IProcesses.ProcessContext) {
					StepperAttributeUtil.setProperty(IRemoteAppLaunchAttributes.ATTR_PROCESS_CONTEXT, fullQualifiedId.getParentId(), data, result);
				}
				Assert.isTrue(channel.getState() == IChannel.STATE_OPEN, "channel is closed"); //$NON-NLS-1$
				super.internalDone(caller, status);
			}
		});
	}
}
