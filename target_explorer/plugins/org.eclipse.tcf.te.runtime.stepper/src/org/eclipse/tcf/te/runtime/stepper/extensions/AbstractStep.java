/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper.extensions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.stepper.StepperAttributeUtil;
import org.eclipse.tcf.te.runtime.stepper.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.runtime.stepper.nls.Messages;

/**
 * An abstract step implementation.
 */
public abstract class AbstractStep extends ExecutableExtension implements IExtendedStep {
	// List of string id's of the step dependencies.
	private final List<String> dependencies = new ArrayList<String>();

	/**
	 * The suffix to append to the full qualified step id to
	 * get the delayed status object.
	 */
	public final static String SUFFIX_DELAYED_STATUS = "delayedStatus"; //$NON-NLS-1$

	/**
	 * The suffix to append to the full qualified step id to
	 * get the operational flag.
	 */
	public final static String SUFFIX_OPERATIONAL = "operational"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtension#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
	 */
	@Override
	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		super.doSetInitializationData(config, propertyName, data);

		// Read in the list of required step or step id's if specified.
		dependencies.clear();
		IConfigurationElement[] requires = config.getChildren("requires"); //$NON-NLS-1$
		for (IConfigurationElement require : requires) {
			String value = require.getAttribute("id"); //$NON-NLS-1$
			if (value == null || value.trim().length() == 0) {
				throw new CoreException(new Status(IStatus.ERROR,
								CoreBundleActivator.getUniqueIdentifier(),
								0,
								NLS.bind(Messages.AbstractStep_error_missingRequiredAttribute, "dependency id (requires)",  getLabel()), //$NON-NLS-1$
								null));
			}
			if (!dependencies.contains(value.trim())) {
				dependencies.add(value.trim());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#initializeFrom(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void initializeFrom(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) {
		Assert.isNotNull(context);
		Assert.isNotNull(data);
		Assert.isNotNull(fullQualifiedId);
		Assert.isNotNull(monitor);

		StepperAttributeUtil.setProperty(SUFFIX_DELAYED_STATUS, fullQualifiedId, data, false);
		StepperAttributeUtil.setProperty(SUFFIX_OPERATIONAL, fullQualifiedId, data, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#cleanup(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void cleanup(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) {
		StepperAttributeUtil.setProperty(SUFFIX_DELAYED_STATUS, fullQualifiedId, data, false);
		StepperAttributeUtil.setProperty(SUFFIX_OPERATIONAL, fullQualifiedId, data, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#rollback(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.core.runtime.IStatus, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
	public void rollback(IStepContext context, IPropertiesContainer data, IStatus status, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, ICallback callback) {
		if (callback != null) {
			callback.done(this, Status.OK_STATUS);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#getTotalWork(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
	public int getTotalWork(IStepContext context, IPropertiesContainer data) {
		return 10;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return dependencies.toArray(new String[dependencies.size()]);
	}

	/**
	 * Invoke the specified callback and pass on the status and user defined data object.
	 *
	 * @param stepData
	 * @param fullQualifiedId
	 * @param callback
	 * @param status
	 * @param data
	 */
	public final void callback(IPropertiesContainer stepData, IFullQualifiedId fullQualifiedId, ICallback callback, IStatus status, Object data) {
		Assert.isNotNull(stepData);
		Assert.isNotNull(fullQualifiedId);
		Assert.isNotNull(callback);
		Assert.isNotNull(status);

		// Check if there have been states delayed
		IStatus delayedStatus = (IStatus)StepperAttributeUtil.getProperty(SUFFIX_DELAYED_STATUS, fullQualifiedId, stepData);
		if (status.getSeverity() != IStatus.ERROR && status.getSeverity() != IStatus.CANCEL && delayedStatus != null) {
			if (status.getSeverity() == IStatus.OK) {
				// replace the whole status with the delayed one
				status = delayedStatus;
			} else {
				// Merge the passed in status and the delayed stati together
				IStatus[] delayedStati = delayedStatus instanceof MultiStatus ? ((MultiStatus)delayedStatus).getChildren() : new IStatus[] { delayedStatus };
				if (delayedStati.length > 0) {
					if (!(status instanceof MultiStatus))  {
						status = new MultiStatus(CoreBundleActivator.getUniqueIdentifier(), 0,
										NLS.bind(Messages.AbstractStep_warning_stepFinishedWithWarnings, getLabel()),
										null);

					}
					// At this point the status must be a MultiStatus
					Assert.isTrue(status instanceof MultiStatus);
					for (IStatus delayed : delayedStati) {
						((MultiStatus)status).merge(delayed);
					}
				}

			}
		}

		// Finally invoke the callback
		callback.setProperty(IStep.CALLBACK_PROPERTY_DATA, data);
		callback.done(this, status);
	}

	/**
	 * Delay the reporting of the given status till the associated launch callback is invoked. If
	 * delayed states are available and the callback is invoked not with an error status, the
	 * delayed states will be reported instead.
	 *
	 * @param status The status to delay. Must not be <code>null</code> and either a warning or info status.
	 */
	protected void delayStatus(IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IStatus status) {
		Assert.isNotNull(status);
		Assert.isTrue(status.getSeverity() == IStatus.WARNING || status.getSeverity() == IStatus.INFO);

		IStatus delayedStatus = (IStatus)StepperAttributeUtil.getProperty(SUFFIX_DELAYED_STATUS, fullQualifiedId, data);
		if (delayedStatus == null) {
			StepperAttributeUtil.setProperty(SUFFIX_DELAYED_STATUS, fullQualifiedId, data, status);
		} else if (delayedStatus instanceof MultiStatus) {
			((MultiStatus)delayedStatus).merge(status);
		} else {
			MultiStatus multiStatus = new MultiStatus(CoreBundleActivator.getUniqueIdentifier(), 0,
							new IStatus[] { delayedStatus, status },
							"", //$NON-NLS-1$
							null);
			StepperAttributeUtil.setProperty(SUFFIX_DELAYED_STATUS, fullQualifiedId, data, multiStatus);
		}
	}
}
