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
 * TCF remote file system exception.
 */
public class TCFFileSystemException extends TCFException {
	private static final long serialVersionUID = -5203855887734608373L;

	/**
	 * Constructor.
	 *
	 * @param severity the severity; one of <code>OK</code>, <code>ERROR</code>, 
	 * <code>INFO</code>, <code>WARNING</code>,  or <code>CANCEL</code>
	 * @param message
	 *            The exception detail message or <code>null</code>.
	 */
	public TCFFileSystemException(int severity, String message) {
		super(severity, message);
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
	public TCFFileSystemException(int severity, String message, Throwable cause) {
		super(severity, message, cause);
	}

}
