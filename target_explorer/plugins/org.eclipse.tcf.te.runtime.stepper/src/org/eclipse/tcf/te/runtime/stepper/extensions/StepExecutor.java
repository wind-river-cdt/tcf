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

import java.util.Date;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
import org.eclipse.tcf.te.runtime.interfaces.ISharedConstants;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.stepper.activator.CoreBundleActivator;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepExecutor;
import org.eclipse.tcf.te.runtime.stepper.interfaces.tracing.ITraceIds;
import org.eclipse.tcf.te.runtime.stepper.nls.Messages;
import org.eclipse.tcf.te.runtime.utils.ProgressHelper;
import org.eclipse.tcf.te.runtime.utils.StatusHelper;

/**
 * Step executor implementation.
 * <p>
 * The step executor is responsible for initiating the execution of a single step. The executor
 * creates and associated the step callback and blocks the execution till the executed step invoked
 * the callback.
 * <p>
 * The step executor is passing any status thrown by the executed step to the parent stepper
 * instance for handling.
 * <p>
 * If the step to execute is of type {@link IExtendedStep}, the step executor is calling
 * {@link IExtendedStep#initializeFrom(IAdaptable, IPropertiesContainer, IFullQualifiedId, IProgressMonitor)} and
 * {@link IExtendedStep#validateExecute(IAdaptable, IPropertiesContainer, IFullQualifiedId, IProgressMonitor)} before calling
 * {@link IStep#execute(IAdaptable, IPropertiesContainer, IFullQualifiedId, IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)}.
 * <p>
 * The methods will be called within the current step executor thread.
 * <p>
 * The stepper implementation can be traced and profiled by setting the debug options:
 * <ul>
 * <li><i>org.eclipse.tcf.te.runtime.stepper/trace/stepping</i></li>
 * <li><i>org.eclipse.tcf.te.runtime.stepper/profile/stepping</i></li>
 * </ul>
 */
public class StepExecutor implements IStepExecutor {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepExecutor#execute(org.eclipse.tcf.te.runtime.stepper.interfaces.IStep, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public final void execute(IStep step, IFullQualifiedId id, final IStepContext context, final IPropertiesContainer data, IProgressMonitor progress) throws CoreException {
		Assert.isNotNull(step);
		Assert.isNotNull(id);
		//		Assert.isNotNull(context);
		Assert.isNotNull(data);
		Assert.isNotNull(progress);

		long startTime = System.currentTimeMillis();

		CoreBundleActivator.getTraceHandler().trace("StepExecutor#execute: *** START (" + step.getLabel() + ")", //$NON-NLS-1$ //$NON-NLS-2$
						0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
		CoreBundleActivator.getTraceHandler().trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(startTime)) + "]" //$NON-NLS-1$ //$NON-NLS-2$
						+ " ***", //$NON-NLS-1$
						0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);

		int ticksToUse = (step instanceof IExtendedStep) ? ((IExtendedStep)step).getTotalWork(context, data) : IProgressMonitor.UNKNOWN;
		progress = ProgressHelper.getProgressMonitor(progress, ticksToUse);
		ProgressHelper.beginTask(progress, step.getLabel(), ticksToUse);

		// Create the handler (and the callback) for the current step
		final Callback callback = new Callback();

		// Catch any exception that might occur during execution.
		// Errors are passed through by definition.
		try {
			// Execute the step. Spawn to the dispatch thread if necessary.
			if (step instanceof IExtendedStep) {
				IExtendedStep extendedStep = (IExtendedStep)step;

				// IExtendedStep provides protocol for initialization and validation.
				extendedStep.initializeFrom(context, data, id, progress);

				// step is initialized -> now validate for execution.
				// If the step if not valid for execution, validateExecute is throwing an exception.
				extendedStep.validateExecute(context, data, id, progress);
			}

			step.execute(context, data, id, progress, callback);

			// Wait till the step finished, an execution occurred or the
			// user hit cancel on the progress monitor.
			ExecutorsUtil.waitAndExecute(0, callback.getDoneConditionTester(null));

			// Check the status of the step
			normalizeStatus(step, id, context, data, callback.getStatus());
		}
		catch (Exception e) {
			CoreBundleActivator.getTraceHandler().trace("StepExecutor#execute: Exception catched: class ='" + e.getClass().getName() + "'" //$NON-NLS-1$ //$NON-NLS-2$
							+ ", message = '" + e.getLocalizedMessage() + "'"  //$NON-NLS-1$ //$NON-NLS-2$
							+ ", cause = "  //$NON-NLS-1$
							+ (e instanceof CoreException ? ((CoreException)e).getStatus().getException() : e.getCause()),
							0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);

			// If the exception is a CoreException by itself, just re-throw
			if (e instanceof CoreException) {
				// Check if the message does need normalization
				if (isExceptionMessageFormatted(e.getLocalizedMessage())) {
					throw (CoreException)e;
				}
				// We have to normalize the status message first
				normalizeStatus(step, id, context, data, ((CoreException)e).getStatus());
			} else {
				// all other exceptions are repackaged within a CoreException
				normalizeStatus(step, id, context, data, StatusHelper.getStatus(e));
			}
		}
		finally {
			if (!progress.isCanceled()) {
				progress.done();
			}

			// Give the step a chance for cleanup
			if (step instanceof IExtendedStep) {
				((IExtendedStep)step).cleanup(context, data, id, progress);
			}

			long endTime = System.currentTimeMillis();
			CoreBundleActivator.getTraceHandler().trace("StepExecutor#execute: *** DONE (" + step.getLabel() + ")", //$NON-NLS-1$ //$NON-NLS-2$
							0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
			CoreBundleActivator.getTraceHandler().trace(" [" + ISharedConstants.TIME_FORMAT.format(new Date(endTime)) //$NON-NLS-1$
							+ " , delay = " + (endTime - startTime) + " ms]" //$NON-NLS-1$ //$NON-NLS-2$
							+ " ***", //$NON-NLS-1$
							0, ITraceIds.PROFILE_STEPPING, IStatus.WARNING, this);
		}
	}

	/**
	 * Normalize the given status.
	 *
	 * @param step The step.
	 * @param id The fully qualified id.
	 * @param context The context.
	 * @param data The step data.
	 * @param status The status.
	 *
	 * @throws CoreException - if the operation fails
	 */
	private void normalizeStatus(IStep step, IFullQualifiedId id, IStepContext context , IPropertiesContainer data, IStatus status) throws CoreException {
		Assert.isNotNull(context);
		Assert.isNotNull(data);
		Assert.isNotNull(id);
		Assert.isNotNull(step);

		if (status == null || status.isOK()) {
			return;
		}

		switch (status.getSeverity()) {
		case IStatus.CANCEL:
			throw new OperationCanceledException(status.getMessage());
		default:
			String message = formatMessage(status.getMessage(), status.getSeverity(), step, id, context, data);
			status = new Status(status.getSeverity(), status.getPlugin(), status.getCode(), message != null ? message : status.getMessage(), status.getException());
			throw new CoreException(status);
		}
	}

	/**
	 * Checks if the given message is already formatted to get displayed to the user.
	 *
	 * @param message The message. Must not be <code>null</code>.
	 * @return <code>True</code> if the message is already formatted to get displayed to the user, <code>false</code> otherwise.
	 */
	protected boolean isExceptionMessageFormatted(String message) {
		Assert.isNotNull(message);
		return message.startsWith(Messages.StepExecutor_checkPoint_normalizationNeeded);
	}

	/**
	 * Format the message depending on the severity.
	 *
	 * @param message The message to format.
	 * @param severity The message severity.
	 * @param step The step.
	 * @param id The full qualified step id.
	 * @param context The target context.
	 * @param data The step data.
	 *
	 * @return Formatted message.
	 */
	protected String formatMessage(String message, int severity, IStep step, IFullQualifiedId id, IStepContext context, IPropertiesContainer data) {
		String template = null;

		switch (severity) {
		case IStatus.INFO:
			template = Messages.StepExecutor_info_stepFailed;
			break;
		case IStatus.WARNING:
			template = Messages.StepExecutor_warning_stepFailed;
			break;
		case IStatus.ERROR:
			template = Messages.StepExecutor_error_stepFailed;
			break;
		}

		// If we cannot determine the formatted message template, just return the message as is
		if (template == null) {
			return message;
		}

		// Check the message for additions
		message = checkMessage(message);

		// Split the message. The first sentence is shown more prominent on the top,
		// the rest as additional information below the step information.
		String[] splittedMsg = message != null ? message.split("[\t\n\r\f]+", 2) : new String[] { null, null }; //$NON-NLS-1$

		// Format the core message
		String formattedMessage = NLS.bind(template,
						new String[] { splittedMsg[0],
						context.getName(),
						context.getInfo(data),
						(step.getLabel() != null && step.getLabel().trim().length() > 0 ? step.getLabel() : step.getId())
		});

		// If we have more information available, append them
		if (splittedMsg.length > 1 && splittedMsg[1] != null && !"".equals(splittedMsg[1])) { //$NON-NLS-1$
			formattedMessage += "\n\n" + splittedMsg[1]; //$NON-NLS-1$
		}

		// In debug mode, there is even more information to add
		if (Platform.inDebugMode()) {
			formattedMessage += "\n\n" + NLS.bind(Messages.StepExecutor_stepFailed_debugInfo, id.toString()); //$NON-NLS-1$
		}

		return formattedMessage;
	}

	/**
	 * Check for additions to add to the message.
	 * <p>
	 * <i>Reserved for future use. Currently returns the message unmodified.</i>
	 *
	 * @param message The message or <code>null</code>.
	 * @return The checked message.
	 */
	protected String checkMessage(String message) {
		return message;
	}
}
