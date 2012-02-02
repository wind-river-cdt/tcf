/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.core.exceptions;

/**
 * Exception thrown by the launch configuration management service to
 * signal a service failure.
 */
public class LaunchServiceException extends Exception {

    private static final long serialVersionUID = 4847722803200503842L;

	/**
	 * Default type for this exception.
	 */
	public final static int TYPE_NO_DETAILED_REASON = 0;

	/**
	 * Exception type when mandatory attributes in launch configuration is missing.
	 */
	public final static int TYPE_MISSING_LAUNCH_CONFIG_ATTR = 1;

	/**
	 * Exception type when mandatory attributes in launch specification is missing.
	 */
	public final static int TYPE_MISSING_LAUNCH_SPEC_ATTR = 2;

	private int typeId = TYPE_NO_DETAILED_REASON;

	/**
	 * Default Constructor.
	 */
	public LaunchServiceException() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param message Additional detail message describing the cause of the exception.
	 */
	public LaunchServiceException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message Additional detail message describing the cause of the exception.
	 * @param typeId Additional information to distinguish different exceptions thrown from one method.
	 */
	public LaunchServiceException(String message, int typeId) {
		super(message);
		this.typeId = typeId;
	}

	/**
	 * Constructor.
	 *
	 * @param cause Additional <code>Throwable</code> which was the cause of this exception.
	 */
	public LaunchServiceException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 *
	 * @param message Additional detail message describing the cause of the exception.
	 * @param cause Additional <code>Throwable</code> which was the cause of this exception.
	 */
	public LaunchServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Returns the type of this exception.
	 */
	public int getType() {
		return typeId;
	}
}
