/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.services.interfaces.constants;

/**
 * Defines the properties access service constants.
 */
public interface IPropertiesAccessServiceConstants {

	/**
	 * Target name.
	 * <p>
	 * The target name is not meant to be identical with the targets network name. It can
	 * be the targets network name, but it can be any other string identifying the target
	 * to the user as well. The name is for display only, it is not meant to be used for
	 * communicating with the target.
	 */
	public static String PROP_NAME = "name"; //$NON-NLS-1$

	/**
	 * Target agent address.
	 * <p>
	 * <i>The value is typically the address an agent running at the target.</i>
	 */
	public static String PROP_ADDRESS = "address"; //$NON-NLS-1$

	/**
	 * Target agent port.
	 * <p>
	 * <i>The value is typically the port an agent running at the target.</i>
	 */
	public static String PROP_PORT = "port"; //$NON-NLS-1$

	/**
	 * Target address to be used to construct a SSH connection.
	 * <p>
	 * If not specified, clients should fall back to {@link PROP_ADDRESS}.
	 */
	public static String PROP_SSH_ADDRESS = "ssh.address"; //$NON-NLS-1$

	/**
	 * Target port to be used to construct a SSH connection.
	 * <p>
	 * If not specified, clients should assume the default "22".
	 */
	public static String PROP_SSH_PORT = "ssh.port"; //$NON-NLS-1$

	/**
	 * Target address to be used to construct a telnet connection.
	 * <p>
	 * If not specified, clients should fall back to {@link PROP_ADDRESS}.
	 */
	public static String PROP_TELNET_ADDRESS = "telnet.address"; //$NON-NLS-1$

	/**
	 * Target port to be used to construct a telnet connection.
	 * <p>
	 * If not specified, clients should assume the default "23".
	 */
	public static String PROP_TELNET_PORT = "telnet.port"; //$NON-NLS-1$
}
