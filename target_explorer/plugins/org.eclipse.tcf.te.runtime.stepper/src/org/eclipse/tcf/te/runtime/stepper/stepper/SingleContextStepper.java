/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper.stepper;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.te.runtime.stepper.StepperManager;
import org.eclipse.tcf.te.runtime.stepper.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.stepper.extensions.AbstractStepper;
import org.eclipse.tcf.te.runtime.stepper.extensions.StepExecutor;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepperProperties;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepExecutor;
import org.eclipse.tcf.te.runtime.stepper.interfaces.tracing.ITraceIds;

/**
 * A single context stepper implementation.
 * <p>
 * The single context stepper implementation applies following assumptions:
 * <ul>
 * <li>The step group to execute needs to be passed to the stepper via the properties container
 * given by the {@link #onInitialize(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)}
 * call.</li>
 * <li>The stepper executes single steps through {@link DefaultStepExecutor}.
 * <li>The stepper stops the step group execution if the progress monitor is set to canceled
 * or a status with severity {@link IStatus#CANCEL} is thrown by a single step.
 * <li>The stepper stops the step group execution on first error.</li>
 * <li>The stepper collect warnings and information thrown by the single steps till the
 * end of the step group execution. Warnings and information are not stopping the execution.</li>
 * <li>The stepper ignores any status thrown by the single steps with severity {@link IStatus#OK}.</li>
 * <li>A single stepper instance can execute only one step group at a time.</li>
 * </ul>
 * <p>
 * The stepper implementation can be traced and profiled by setting the debug options:
 * <ul>
 * <li><i>org.eclipse.tcf.te.runtime.stepper/trace/stepping</i></li>
 * <li><i>org.eclipse.tcf.te.runtime.stepper/profile/stepping</i></li>
 * </ul>
 */
public class SingleContextStepper extends AbstractStepper {

	/**
	 * Constructor.
	 */
	public SingleContextStepper() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractStepper#getName()
	 */
	@Override
	protected String getName() {
		return getData() != null ? getData().getStringProperty(IStepperProperties.PROP_NAME) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractStepper#getContexts()
	 */
	@Override
	protected IStepContext[] getContexts() {
	    return getData() != null && getData().getProperty(IStepperProperties.PROP_CONTEXTS) != null ? (IStepContext[])getData().getProperty(IStepperProperties.PROP_CONTEXTS) : new IStepContext[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractStepper#getStepGroupId()
	 */
	@Override
	protected String getStepGroupId() {
		return getData() != null ? getData().getStringProperty(IStepperProperties.PROP_STEP_GROUP_ID) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractStepper#getStepGroup(java.lang.String)
	 */
	@Override
	protected IStepGroup getStepGroup(String id) {
		Assert.isNotNull(id);

		CoreBundleActivator.getTraceHandler().trace("SingleContextStepper#getStepGroup:" //$NON-NLS-1$
													+ " id = '" + id + "'", //$NON-NLS-1$ //$NON-NLS-2$
													0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);

	    return StepperManager.getInstance().getStepGroupExtManager().getStepGroup(id, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractStepper#doCreateStepExecutor(org.eclipse.tcf.te.runtime.stepper.interfaces.IStep, java.lang.String, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId)
	 */
	@Override
	protected IStepExecutor doCreateStepExecutor(IStep step, String secondaryId, IFullQualifiedId fullQualifiedStepId) {
		Assert.isNotNull(step);
		Assert.isNotNull(secondaryId);
		Assert.isNotNull(fullQualifiedStepId);
	    return new StepExecutor();
	}
}
