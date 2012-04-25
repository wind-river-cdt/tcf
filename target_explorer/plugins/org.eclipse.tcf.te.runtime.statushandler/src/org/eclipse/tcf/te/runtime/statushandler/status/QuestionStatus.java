/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.statushandler.status;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.te.runtime.statushandler.interfaces.IStatusHandlerConstants;

/**
 * Special status implementation to handle questions.
 * <p>
 * <b>Note:</b>
 * <ul>
 *   <li>{@link #getSeverity()} will always return {@link IStatusHandlerConstants#QUESTION}</li>
 *   <li>{@link #setSeverity(int)} will be ignored</li>
 * </ul>
 */
public class QuestionStatus extends Status {

	/**
	 * Constructor.
	 *
	 * @param pluginId The unique plugin id.
	 * @param message The message.
	 */
	public QuestionStatus(String pluginId, String message) {
		super(IStatus.OK, pluginId, message);

	}

	/**
	 * Constructor.
	 *
	 * @param pluginId The unique plugin id.
	 * @param message The message.
	 * @param exception The exception.
	 */
	public QuestionStatus(String pluginId, String message, Throwable exception) {
		super(IStatus.OK, pluginId, message, exception);

	}

	/**
	 * Constructor.
	 *
	 * @param pluginId The unique plugin id.
	 * @param code The code.
	 * @param message The message.
	 * @param exception The exception.
	 */
	public QuestionStatus(String pluginId, int code, String message, Throwable exception) {
		super(IStatus.OK, pluginId, code, message, exception);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Status#getSeverity()
	 */
	@Override
	public final int getSeverity() {
	    return IStatusHandlerConstants.QUESTION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Status#setSeverity(int)
	 */
	@Override
	protected final void setSeverity(int severity) {
	}
}
