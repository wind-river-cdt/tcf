/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions;

/**
 * TCF file system implementation base exception.
 */
public class TCFException extends Exception {
	private static final long serialVersionUID = -220092425137980661L;
	
	// The severity code of this exception, could be used in job status handling.
	private int severity;
	
	/**
	 * Constructor.
	 *
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 * @param message
	 *            The exception detail message or <code>null</code>.
	 */
	public TCFException(int severity, String message) {
		super(message);
		this.severity = severity;
	}

	/**
	 * Constructor.
	 *
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 * @param message
	 *            The exception detail message or <code>null</code>.
	 * @param cause
	 *            The exception cause or <code>null</code>.
	 */
	public TCFException(int severity, String message, Throwable cause) {
		super(message, cause);
		this.severity = severity;
	}
	
	/**
	 * Returns the severity. The severities are as follows (in
	 * descending order):
	 * <ul>
	 * <li><code>CANCEL</code> - cancelation occurred</li>
	 * <li><code>ERROR</code> - a serious error (most severe)</li>
	 * <li><code>WARNING</code> - a warning (less severe)</li>
	 * <li><code>INFO</code> - an informational ("fyi") message (least severe)</li>
	 * <li><code>OK</code> - everything is just fine</li>
	 * </ul>
	 * <p>
	 * The severity of a multi-status is defined to be the maximum
	 * severity of any of its children, or <code>OK</code> if it has
	 * no children.
	 * </p>
	 *
	 * @return the severity: one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 * @see #matches(int)
	 */
	public int getSeverity() {
		return severity;
	}
}
