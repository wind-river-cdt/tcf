/*
 * LaunchProcessStep.java
 * Created on 02.03.2012
 *
 * Copyright 2012 Wind River Systems Inc. All rights reserved.
 */
package org.eclipse.tcf.te.tcf.launch.core.steps;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.core.utils.text.StringUtil;
import org.eclipse.tcf.te.launch.core.persistence.DefaultPersistenceDelegate;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.runtime.stepper.StepperAttributeUtil;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.tcf.launch.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.launch.core.interfaces.ILinuxAppLaunchAttributes;
import org.eclipse.tcf.te.tcf.processes.core.interfaces.launcher.IProcessLauncher;
import org.eclipse.tcf.te.tcf.processes.core.launcher.ProcessLauncher;

/**
 * LaunchProcessStep
 * @author tobias.schwarz@windriver.com
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
		String processImage = DefaultPersistenceDelegate.getAttribute(getLaunchConfiguration(context), ILinuxAppLaunchAttributes.ATTR_PROCESS_IMAGE, (String)null);
		if (processImage != null && processImage.trim().length() > 0) {
			StepperAttributeUtil.setProperty(ILinuxAppLaunchAttributes.ATTR_PROCESS_IMAGE, fullQualifiedId, data, processImage);
		}
		else {
			throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), "missing process image name"));
		}

		String processArguments = DefaultPersistenceDelegate.getAttribute(getLaunchConfiguration(context), ILinuxAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, (String)null);
		StepperAttributeUtil.setProperty(ILinuxAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, fullQualifiedId, data, processArguments);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void execute(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, final ICallback callback) {
		// Construct the launcher object
		ProcessLauncher launcher = new ProcessLauncher();

		Map<String, Object> launchAttributes = new HashMap<String, Object>();

		launchAttributes.put(IProcessLauncher.PROP_PROCESS_PATH, StepperAttributeUtil.getStringProperty(ILinuxAppLaunchAttributes.ATTR_PROCESS_IMAGE, fullQualifiedId, data));

		String arguments = StepperAttributeUtil.getStringProperty(ILinuxAppLaunchAttributes.ATTR_PROCESS_ARGUMENTS, fullQualifiedId, data);
		String[] args = arguments != null && !"".equals(arguments.trim()) ? StringUtil.tokenize(arguments, 0, true) : null; //$NON-NLS-1$
		launchAttributes.put(IProcessLauncher.PROP_PROCESS_ARGS, args);

		launchAttributes.put(ITerminalsConnectorConstants.PROP_LOCAL_ECHO, Boolean.FALSE);
		launchAttributes.put(IProcessLauncher.PROP_PROCESS_ASSOCIATE_CONSOLE, Boolean.TRUE);

		// Launch the process
		IPropertiesContainer container = new PropertiesContainer();
		container.setProperties(launchAttributes);
		launcher.launch(getActivePeerModel(data).getPeer(), container, callback);
	}
}
