/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.stepper.extensions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IContext;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IContextStep;
import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
import org.eclipse.tcf.te.runtime.stepper.nls.Messages;

/**
 * Context step executor implementation.
 * <p>
 * The step executor is responsible for initiating the execution of a single step. The executor
 * creates and associated the step callback and blocks the execution till the executed step invoked
 * the callback.
 * <p>
 * The step executor is passing any status thrown by the executed step to the parent stepper
 * instance for handling.
 * <p>
 * If the step to execute is of type {@link AbstractContextStep}, the step executor is calling
 * {@link AbstractContextStep#initializeFrom(IContext, IPropertiesContainer, IFullQualifiedId, IProgressMonitor)}
 * and
 * {@link AbstractContextStep#validateExecute(IContext, IPropertiesContainer, IFullQualifiedId, IProgressMonitor)}
 * before calling
 * {@link IContextStep#execute(IContext, IPropertiesContainer, IFullQualifiedId, IProgressMonitor, ICallback)}.
 * <p>
 * The methods will be called within the current step executor thread.
 * <p>
 * The stepper implementation can be traced and profiled by setting the debug options:
 * <ul>
 * <li><i>org.eclipse.tcf.te.runtime.stepper/trace/stepping</i></li>
 * <li><i>org.eclipse.tcf.te.runtime.stepper/profile/stepping</i></li>
 * </ul>
 */
public class ContextStepExecutor extends AbstractContextStepExecutor {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractContextStepExecutor#formatMessage(java.lang.String, int, org.eclipse.tcf.te.runtime.stepper.interfaces.IContextStep, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.tcf.te.runtime.stepper.interfaces.IContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
	protected String formatMessage(String message, int severity, IContextStep step, IFullQualifiedId id, IContext context, IPropertiesContainer data) {
		String template = null;

		switch (severity) {
			case IStatus.INFO:
				template = Messages.ContextStepExecutor_info_stepFailed;
				break;
			case IStatus.WARNING:
				template = Messages.ContextStepExecutor_warning_stepFailed;
				break;
			case IStatus.ERROR:
				template = Messages.ContextStepExecutor_error_stepFailed;
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
										   context.getContextName(),
										   context.getContextInfo(),
										   (step.getLabel() != null && step.getLabel().trim().length() > 0 ? step.getLabel() : step.getId())
		});

		// If we have more information available, append them
		if (splittedMsg.length > 1 && splittedMsg[1] != null && !"".equals(splittedMsg[1])) { //$NON-NLS-1$
			formattedMessage += "\n\n" + splittedMsg[1]; //$NON-NLS-1$
		}

		// In debug mode, there is even more information to add
		if (Platform.inDebugMode()) {
			formattedMessage += "\n\n" + NLS.bind(Messages.ContextStepExecutor_stepFailed_debugInfo, id.toString()); //$NON-NLS-1$
		}

		return formattedMessage;
	}

	/* (non-Javadoc)
	 * @see com.windriver.ide.target.core.stepper.AbstractTargetContextStepExecutor#isExceptionMessageFormatted(java.lang.String)
	 */
	@Override
	protected boolean isExceptionMessageFormatted(String message) {
		assert message != null;
		return message.startsWith(Messages.ContextStepExecutor_checkPoint_normalizationNeeded);
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
