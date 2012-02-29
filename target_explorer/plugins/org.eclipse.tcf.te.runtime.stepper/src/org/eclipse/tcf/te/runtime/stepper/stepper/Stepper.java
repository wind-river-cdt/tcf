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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tcf.te.runtime.interfaces.ISharedConstants;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.stepper.StepperManager;
import org.eclipse.tcf.te.runtime.stepper.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.stepper.extensions.StepExecutor;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepExecutor;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupIterator;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupable;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepperProperties;
import org.eclipse.tcf.te.runtime.stepper.interfaces.tracing.ITraceIds;
import org.eclipse.tcf.te.runtime.stepper.nls.Messages;
import org.eclipse.tcf.te.runtime.utils.ProgressHelper;
import org.eclipse.tcf.te.runtime.utils.StatusHelper;

/**
 * An abstract stepper implementation.
 */
public class Stepper implements IStepper {

	private boolean initialized = false;
	private boolean finished = false;
	private IPropertiesContainer data = null;
	private IFullQualifiedId fullQualifiedId = null;
	private IProgressMonitor monitor = null;
	private IStepContext context = null;
	private boolean cancelable = true;

	/**
	 * Internal helper describing a fully executed step.
	 */
	protected final class ExecutedContextStep {
		final IFullQualifiedId id;
		final IStep step;

		public ExecutedContextStep(IFullQualifiedId id, IStep step) {
			this.id = id;
			this.step = step;
		}
	}

	/**
	 * Constructor.
	 */
	public Stepper() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper#getId()
	 */
	@Override
	public String getId() {
		return getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper#getLabel()
	 */
	@Override
	public String getLabel() {
		return getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper#getDescription()
	 */
	@Override
	public String getDescription() {
		return null;
	}

	/**
	 * Returns the id of the step group to execute by the stepper.
	 * 
	 * @return The step group id.
	 */
	protected String getStepGroupId() {
		return getData() != null ? getData()
						.getStringProperty(IStepperProperties.PROP_STEP_GROUP_ID) : null;
	}

	/**
	 * Returns the step group for the given step group id.
	 * 
	 * @param The step group id. Must not be <code>null</code>:
	 * @return The step group or <code>null</code>.
	 */
	protected IStepGroup getStepGroup(String id) {
		Assert.isNotNull(id);

		CoreBundleActivator.getTraceHandler().trace("SingleContextStepper#getStepGroup:" //$NON-NLS-1$
						+ " id = '" + id + "'", //$NON-NLS-1$ //$NON-NLS-2$
						0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);

		return StepperManager.getInstance().getStepGroupExtManager().getStepGroup(id, false);
	}

	/**
	 * Creates a new instance of the step executor to use for executing a step.
	 * 
	 * @param step The step. Must not be <code>null</code>.
	 * @param secondaryId The secondary id or <code>null</code>.
	 * @param fullQualifiedStepId The fully qualified step id. Must not be <code>null</code>.
	 * 
	 * @return The step executor instance.
	 */
	protected IStepExecutor doCreateStepExecutor(IStep step, String secondaryId, IFullQualifiedId fullQualifiedStepId) {
		Assert.isNotNull(step);
		Assert.isNotNull(fullQualifiedStepId);
		return new StepExecutor();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper#initialize(org.eclipse.tcf.te.runtime
	 * .interfaces.properties.IPropertiesContainer,
	 * org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public final void initialize(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) throws IllegalStateException {
		Assert.isNotNull(context);
		Assert.isNotNull(data);
		Assert.isNotNull(fullQualifiedId);
		Assert.isNotNull(monitor);

		// Assert stepper is not in use
		if (isInitialized()) {
			throw new IllegalStateException("Stepper instance already initialized!"); //$NON-NLS-1$
		}

		// set the initial stepper attributes
		this.context = context;
		this.data = data;
		this.monitor = monitor;
		this.fullQualifiedId = fullQualifiedId;

		// but not finished yet
		this.finished = false;

		// call the hook for the subclasses to initialize themselves
		onInitialize(data, fullQualifiedId, monitor);

		setInitialized();

		CoreBundleActivator.getTraceHandler().trace("Stepper#initialize:" //$NON-NLS-1$
						+ " data = " + data, //$NON-NLS-1$
						0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
	}

	/**
	 * Hook for subclasses to overwrite if subclasses wants to initialize their own state.
	 * 
	 * @param data The data. Must not be <code>null</code>.
	 * @param fullQualifiedId The full qualified id of this stepper.
	 * @param monitor The progress monitor. Must not be <code>null</code>.
	 */
	protected void onInitialize(IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) {
		Assert.isNotNull(data);
		Assert.isNotNull(fullQualifiedId);
		Assert.isNotNull(monitor);
	}

	/**
	 * Marks the stepper to be fully initialized.
	 */
	protected final void setInitialized() {
		initialized = true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper#isInitialized()
	 */
	@Override
	public final boolean isInitialized() {
		return initialized;
	}

	/**
	 * Sets the cancelable state of the stepper.
	 * 
	 * @param cancelable <code>True</code> if the stepper shall be cancelable, <code>false</code> if
	 *            not.
	 */
	protected final void setCancelable(boolean cancelable) {
		this.cancelable = cancelable;
	}

	/**
	 * Returns the cancelable state of the stepper.
	 * 
	 * @return <code>True</code> if the stepper is cancelable, <code>false</code> if not.
	 */
	protected final boolean isCancelable() {
		return cancelable;
	}

	/**
	 * Get the active context.
	 * 
	 * @return The active context or <code>null</code>.
	 */
	protected IStepContext getContext() {
		return context;
	}

	/**
	 * Get the context id.
	 * 
	 * @return The context id or <code>null</code>.
	 */
	protected String getContextId() {
		return context != null ? context.getId() : null;
	}

	/**
	 * Returns the currently associated data. The method returns <code>null</code> if the stepper is
	 * not in initialized state.
	 * 
	 * @return The data or <code>null</code>
	 */
	protected final IPropertiesContainer getData() {
		return isInitialized() ? data : null;
	}

	/**
	 * Returns the full qualified id for this stepper.
	 * 
	 * @return The full qualified stepper id.
	 */
	protected final IFullQualifiedId getFullQualifiedId() {
		return fullQualifiedId;
	}

	/**
	 * Returns the currently associated progress monitor. The method returns <code>null</code> if
	 * the stepper is not in initialized state.
	 * 
	 * @return The progress monitor or <code>null</code>
	 */
	protected final IProgressMonitor getMonitor() {
		return isInitialized() ? monitor : null;
	}

	/**
	 * Marks the stepper to be finished.
	 */
	protected final void setFinished() {
		finished = true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper#isFinished()
	 */
	@Override
	public final boolean isFinished() {
		return finished;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper#cleanup()
	 */
	@Override
	public void cleanup() {
		// Set the progress monitor done here in any case
		if (getMonitor() != null) {
			getMonitor().done();
		}

		// Reset the initial stepper attributes
		context = null;
		data = null;
		monitor = null;
		fullQualifiedId = null;
		finished = false;
		initialized = false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
		buffer.append(" (" + getLabel() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append(": "); //$NON-NLS-1$
		buffer.append("id = " + getId()); //$NON-NLS-1$
		return buffer.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepper#execute()
	 */
	@Override
	public final void execute() throws CoreException {
		long startTime = System.currentTimeMillis();

		CoreBundleActivator.getTraceHandler().trace("Stepper#execute: *** ENTERED", //$NON-NLS-1$
						0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
		CoreBundleActivator
		.getTraceHandler()
		.trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(startTime)) + "]" //$NON-NLS-1$ //$NON-NLS-2$
						+ " ***", //$NON-NLS-1$
						0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);

		try {
			// stepper must be initialized before executing
			if (!isInitialized()) {
				throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), Messages.Stepper_error_initializeNotCalled));
			}

			// Create a container for collecting the non-severe status objects
			// during the step execution. Non-severe status objects will
			// be hold back till the execution completed or stopped with an error.
			// Severe status objects are errors or cancellation.
			List<IStatus> statusContainer = new ArrayList<IStatus>();

			// start execution
			internalExecute(statusContainer);

			// If the warnings container is not empty, create a new status and
			// throw a core exception
			if (!statusContainer.isEmpty()) {
				IStatus status = null;

				// Check if we need a multi status
				if (statusContainer.size() > 1) {
					MultiStatus multiStatus = new MultiStatus(CoreBundleActivator.getUniqueIdentifier(), 0, NLS
									.bind(Messages.Stepper_multiStatus_finishedWithWarnings, getLabel()), null);
					for (IStatus subStatus : statusContainer) {
						multiStatus.merge(subStatus);
					}
					status = multiStatus;
				}
				else {
					status = statusContainer.get(0);
				}

				throw new CoreException(status);
			}
		}
		finally {
			// Mark the stepper finished
			setFinished();

			long endTime = System.currentTimeMillis();
			CoreBundleActivator.getTraceHandler().trace("Stepper#execute: *** DONE", //$NON-NLS-1$
							0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
			CoreBundleActivator.getTraceHandler()
			.trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(endTime)) //$NON-NLS-1$
							+ " , delay = " + (endTime - startTime) + " ms]" //$NON-NLS-1$ //$NON-NLS-2$
							+ " ***", //$NON-NLS-1$
							0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);
		}
	}

	/**
	 * Executes a step or step group.
	 * 
	 * @param statusContainer The status container. Must not be <code>null</code>.
	 * @throws CoreException If the execution fails.
	 */
	protected void internalExecute(List<IStatus> statusContainer) throws CoreException {
		Assert.isNotNull(statusContainer);

		// Get the step group id
		String stepGroupId = getStepGroupId();

		// If no step group id is available, throw an exception
		if (stepGroupId == null) {
			throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), NLS
							.bind(Messages.Stepper_error_missingStepGroupId, getLabel())));
		}

		// Get the step group
		IStepGroup stepGroup = getStepGroup(stepGroupId);

		// If no step group could be found, throw an exception
		if (stepGroup == null) {
			throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), NLS
							.bind(Messages.Stepper_error_missingStepGroup, stepGroupId)));
		}

		// Initialize the progress monitor
		getMonitor().beginTask(stepGroup.getLabel(), calculateTotalWork(stepGroup));

		IFullQualifiedId fullQualifiedId = getFullQualifiedId()
						.createChildId(ID_TYPE_CONTEXT_ID, getContextId(), null);
		fullQualifiedId = fullQualifiedId
						.createChildId(ID_TYPE_STEP_GROUP_ID, stepGroup.getId(), null);
		// Execute the step group
		executeStepGroup(stepGroup, statusContainer, new ArrayList<ExecutedContextStep>(), fullQualifiedId);
	}

	/**
	 * Executes a step group.
	 * 
	 * @param stepGroup The step group. Must not be <code>null</code>.
	 * @param statusContainer A list holding the warnings occurred during the execution. Must not be
	 *            <code>null</code>.
	 * @param executedSteps A list holding the id's of the steps executed before. Must not be
	 *            <code>null</code>.
	 * @param fullQualifiedGroupId The hierarchy of all parent step group id's separated by "::".
	 *            Must not be <code>null</code>.
	 * 
	 * @throws CoreException If the execution fails.
	 */
	private void executeStepGroup(IStepGroup stepGroup, List<IStatus> statusContainer, List<ExecutedContextStep> executedSteps, IFullQualifiedId fullQualifiedGroupId) throws CoreException {
		Assert.isNotNull(stepGroup);
		Assert.isNotNull(statusContainer);
		Assert.isNotNull(executedSteps);
		Assert.isNotNull(fullQualifiedGroupId);

		// Return immediately if the user canceled the monitor in the meanwhile
		if (isCancelable() && getMonitor().isCanceled()) {
			rollback(executedSteps, Status.CANCEL_STATUS, getMonitor());
			throw new CoreException(StatusHelper.getStatus(new OperationCanceledException()));
		}

		CoreBundleActivator
		.getTraceHandler()
		.trace("Stepper#executeStepGroup: step group: '" + stepGroup.getLabel() + "'", //$NON-NLS-1$ //$NON-NLS-2$
						0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);

		// Resolve the steps to execute
		IStepGroupable[] groupables = stepGroup.getSteps(getContext());

		IStepGroupIterator iterator = stepGroup.getStepGroupIterator();
		IFullQualifiedId fullQualifiedIterationId = fullQualifiedGroupId;
		int iteration = 0;

		if (iterator != null) {
			iterator.initialize(getContext(), getData(), fullQualifiedGroupId, getMonitor());
		}
		boolean next = iterator == null || iterator
						.hasNext(getContext(), getData(), fullQualifiedGroupId, getMonitor());

		while (next) {
			if (iterator != null) {
				fullQualifiedIterationId = fullQualifiedGroupId
								.createChildId(ID_TYPE_STEP_GROUP_ITERATION_ID, iterator.getId(), "" + iteration); //$NON-NLS-1$
				iterator.next(getContext(), getData(), fullQualifiedIterationId, getMonitor());
			}
			// Execute the steps or step groups.
			for (IStepGroupable groupable : groupables) {
				executeGroupable(groupable, statusContainer, executedSteps, fullQualifiedIterationId);
			}
			iteration++;
			next = iterator != null && iterator
							.hasNext(getContext(), getData(), fullQualifiedGroupId, getMonitor());
		}
	}

	/**
	 * Executes a step groupable. The groupable might encapsulate a step or a step group.
	 * 
	 * @param step The step groupable. Must not be <code>null</code>.
	 * @param statusContainer A list holding the warnings occurred during the execution. Must not be
	 *            <code>null</code>.
	 * @param executedSteps A list holding the id's of the steps executed before. Must not be
	 *            <code>null</code>.
	 * @param fullQualifiedParentId The hierarchy of all parent step group id's separated by "::".
	 *            Must not be <code>null</code>.
	 * 
	 * @throws CoreException If the execution failed.
	 */
	private void executeGroupable(IStepGroupable groupable, List<IStatus> statusContainer, List<ExecutedContextStep> executedSteps, IFullQualifiedId fullQualifiedParentId) throws CoreException {
		Assert.isNotNull(groupable);
		Assert.isNotNull(statusContainer);
		Assert.isNotNull(executedSteps);
		Assert.isNotNull(fullQualifiedParentId);

		// Return immediately if the user canceled the monitor in the meanwhile
		if (isCancelable() && getMonitor() != null && getMonitor().isCanceled()) {
			rollback(executedSteps, Status.CANCEL_STATUS, getMonitor());
			throw new CoreException(StatusHelper.getStatus(new OperationCanceledException()));
		}

		// If the passed in groupable is disabled -> we are done immediately
		if (groupable.isDisabled()) {
			CoreBundleActivator
			.getTraceHandler()
			.trace("Stepper#executeGroupable: DROPPED DISABLED groupable: id = '" + groupable.getExtension().getId() + "'" //$NON-NLS-1$ //$NON-NLS-2$
							+ ", secondaryId = '" + groupable.getSecondaryId() + "'", //$NON-NLS-1$ //$NON-NLS-2$
							0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
			return;
		}

		// Check if all dependencies of the groupable have been executed before
		checkForDependenciesExecuted(groupable, executedSteps);

		if (groupable.getExtension() instanceof IStepGroup) {
			IFullQualifiedId id = fullQualifiedParentId
							.createChildId(ID_TYPE_STEP_GROUP_ID, groupable.getExtension().getId(), groupable
											.getSecondaryId());
			// If the passed in groupable is associated with a step group
			// -> get the groupable from that group and execute them
			executeStepGroup((IStepGroup) groupable.getExtension(), statusContainer, executedSteps, id);
		}
		else if (groupable.getExtension() instanceof IStep) {
			// If the passed in groupable is associated with a step
			// -> check if the required steps have been executed before,
			// create a step executor and invoke the executor.
			IStep step = (IStep) groupable.getExtension();

			IFullQualifiedId id = fullQualifiedParentId
							.createChildId(ID_TYPE_STEP_ID, step.getId(), groupable
											.getSecondaryId());

			// Create the step executor now
			IStepExecutor executor = doCreateStepExecutor(step, groupable.getSecondaryId(), id);
			Assert.isNotNull(executor);

			try {
				executedSteps.add(new ExecutedContextStep(id, step));
				// Invoke the executor now
				executor.execute(step, id, getContext(), getData(), getMonitor());
			}
			catch (Exception e) {
				// Catch the CoreException first hand as we need to continue the
				// stepping if the step returned with warnings or information only.
				CoreException coreException = normalizeStatus(e, statusContainer);
				// If the exception has been not eaten, rollback previously executed
				// steps and re-throw the exception.
				if (coreException != null) {
					// Rollback everything, if the step(s) are supporting this and
					// the cleanup hasn't been done already.
					if (isInitialized()) {
						rollback(executedSteps, coreException.getStatus(), getMonitor());
					}

					// Re-throw the exception
					throw coreException;
				}
			}
		}
	}

	/**
	 * Checks if all required dependencies have been executed before. If not, the method will throw
	 * an error status.
	 * 
	 * @param groupable The groupable. Must not be <code>null</code>.
	 * @param executedSteps A list holding the id's of the steps executed before. Must not be
	 *            <code>null</code>.
	 * 
	 * @throws CoreException If a dependency has not been executed before.
	 */
	protected void checkForDependenciesExecuted(IStepGroupable groupable, List<ExecutedContextStep> executedSteps) throws CoreException {
		Assert.isNotNull(groupable);
		Assert.isNotNull(executedSteps);

		// Build up the complete list of dependencies.
		List<String> dependencies = new ArrayList<String>(Arrays.asList(groupable.getDependencies()));
		// If the groupable wraps a step, the step can have additional dependencies to check
		if (groupable.getExtension() instanceof IStep) {
			dependencies.addAll(Arrays.asList(((IStep) groupable.getExtension()).getDependencies()));
		}

		// Check each dependency now.
		for (String dependency : dependencies) {
			// The dependencies might be fully qualified. Split out the primary id.
			String[] splitted = dependency.split("##", 2); //$NON-NLS-1$
			String primaryId = splitted.length == 2 ? splitted[0] : dependency;

			// Check if the id is in the list of executed steps. As the list contains
			// the fully qualified id's, we cannot just check for contained
			boolean requiredStepExecuted = false;
			for (ExecutedContextStep step : executedSteps) {
				if (step.step.getId().equals(primaryId)) {
					requiredStepExecuted = true;
					break;
				}
			}

			if (!requiredStepExecuted) {
				throw new CoreException(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), MessageFormat
								.format(Messages.Stepper_error_requiredStepNotExecuted, NLS.bind(groupable
												.getExtension() instanceof IStep ? Messages.Stepper_error_step : Messages.Stepper_error_step, groupable
																.getExtension().getId()), NLS
																.bind(Messages.Stepper_error_requiredStepOrGroup, dependency), ""))); //$NON-NLS-1$
			}

			// Recursive checking is not necessary here as the step or step group
			// id's would have made it the executed steps list of they missed required
			// steps or step groups.
		}

	}

	/**
	 * Rollback the steps previously executed to the failed step. The rollback is executed in
	 * reverse order and the step must be of type {@link IExtendedStep} to participate in the
	 * rollback.
	 * 
	 * @param executedSteps
	 * @param progress
	 */
	protected final void rollback(final List<ExecutedContextStep> executedSteps, final IStatus rollBackStatus, IProgressMonitor progress) {
		Assert.isNotNull(executedSteps);

		final IProgressMonitor rollbackProgress = ProgressHelper.getProgressMonitor(progress, 1);
		ProgressHelper.beginTask(rollbackProgress, "Cancel", executedSteps.size()); //$NON-NLS-1$
		final Callback finalCallback = new Callback();
		final Callback rollbackCallback = new Callback() {
			@Override
			protected void internalDone(Object caller, IStatus status) {
				if (!executedSteps.isEmpty()) {
					setProperty(PROPERTY_IS_DONE, false);
					ExecutedContextStep executedStep = executedSteps
									.remove(executedSteps.size() - 1);
					if (executedStep.step instanceof IExtendedStep) {
						IExtendedStep step = (IExtendedStep) executedStep.step;
						step.rollback(getContext(), getData(), rollBackStatus, executedStep.id, rollbackProgress, this);
					}
					else {
						this.done(this, status);
					}
				}
				else {
					finalCallback.done(this, Status.OK_STATUS);
				}
			}
		};

		rollbackCallback.done(this, rollBackStatus);
		ExecutorsUtil.waitAndExecute(0, finalCallback.getDoneConditionTester(null));
	}

	/**
	 * Calculates the total work required for the step group. The total work is the sum of the total
	 * work of each sub step. If one of the steps returns {@link IProgressMonitor#UNKNOWN}, the
	 * total work will be unknown for the whole step group.
	 * 
	 * @param stepGroup The step group. Must not be <code>null</code>.
	 * @return The total work required or {@link IProgressMonitor#UNKNOWN}.
	 * 
	 * @throws CoreException If the total work of the step group cannot be determined.
	 */
	protected int calculateTotalWork(IStepGroup stepGroup) throws CoreException {
		Assert.isNotNull(stepGroup);

		int totalWork = 0;

		// Loop the group steps and summarize the returned total work
		IStepGroupable[] groupables = stepGroup.getSteps(getContext());
		for (IStepGroupable groupable : groupables) {
			int work = groupable.getExtension() instanceof IStep ? ((IStep) groupable
							.getExtension()).getTotalWork(getContext(), getData()) : groupable
							.getExtension() instanceof IStepGroup ? calculateTotalWork((IStepGroup) groupable
											.getExtension()) : IProgressMonitor.UNKNOWN;

							if (work == IProgressMonitor.UNKNOWN) {
								totalWork = IProgressMonitor.UNKNOWN;
								break;
							}

							totalWork += work;
		}

		return totalWork;
	}

	/**
	 * Normalize the associated status object of the given {@link CoreException}.
	 * <p>
	 * If the associated status contains only WARNING or INFORMATION status objects, the objects are
	 * added to the passed in status container. The passed in exception is dropped and the method
	 * will return <code>null</code>.
	 * <p>
	 * If the associated status contains only OK status objects, the passed in exception and the
	 * associated status are dropped and the method will return <code>null</code>.
	 * <p>
	 * If the associated status contain ERROR status objects, the passed in exception and the
	 * associated status objects are returned if the passed in status container is empty. If the
	 * status container is not empty, a new exception and multi status object is created and
	 * returned. The multi status object will contain all status objects from the status container
	 * and all objects of the originally associated status.
	 * <p>
	 * If the associated status contains a CANCEL status object, the passed in exception and the
	 * associated status objects are returned unmodified.
	 * 
	 * @param e The core exception. Must not be <code>null</code>.
	 * @param statusContainer The list of non-severe status objects. Must not be <code>null</code>.
	 * @return The exception to re-throw or <code>null</code>.
	 */
	private CoreException normalizeStatus(Exception e, List<IStatus> statusContainer) {
		Assert.isNotNull(statusContainer);

		CoreException coreException = null;

		IStatus status = Status.OK_STATUS;
		// Get the associated status from the exception
		if (e instanceof CoreException) {
			status = ((CoreException) e).getStatus();
			coreException = (CoreException) e;
		}
		else if (e instanceof OperationCanceledException) {
			status = new Status(IStatus.CANCEL, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e);
			coreException = new CoreException(status);
		}
		else if (e != null) {
			status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e);
			coreException = new CoreException(status);
		}

		// Check the severity
		// PS: MultiStatus.getSeverity() returns always the highest severity.
		if (status.getSeverity() == IStatus.OK) {
			// OK -> drop completely and return null
			coreException = null;
		}
		else if (status.getSeverity() == IStatus.CANCEL) {
			// CANCEL -> Check monitor to be canceled.
			if (isCancelable()) {
				if (getMonitor() != null && !getMonitor().isCanceled()) {
					getMonitor().setCanceled(true);
				}
			}
		}
		else if (status.getSeverity() == IStatus.WARNING || status.getSeverity() == IStatus.INFO) {
			// WARNING or INFORMATION -> add to the list and return null
			statusContainer.add(status);
			coreException = null;
		}
		else if (status.getSeverity() == IStatus.ERROR) {
			// Error -> If the warnings container not empty, create
			// a new MultiStatus.
			if (!statusContainer.isEmpty()) {
				MultiStatus multiStatus = new MultiStatus(status.getPlugin(), status.getCode(), NLS.bind(Messages.Stepper_multiStatus_finishedWithErrors, getLabel()), null);
				for (IStatus stat : statusContainer) {
					multiStatus.merge(stat);
				}
				// Re-throw via a new CoreException
				coreException = new CoreException(multiStatus);
			}
		}

		return coreException;
	}
}
