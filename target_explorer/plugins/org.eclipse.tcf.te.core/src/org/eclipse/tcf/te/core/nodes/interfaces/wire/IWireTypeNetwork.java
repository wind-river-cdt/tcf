/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.core.nodes.interfaces.wire;

/**
 * The properties specific to the wire type &quot;network&quot;.
 */
public interface IWireTypeNetwork {

	/**
	 * The data container.
	 */
	public static String PROPERTY_CONTAINER_NAME = "network"; //$NON-NLS-1$

	/**
	 * The network address.
	 */
	public static final String PROPERTY_NETWORK_ADDRESS = "address"; //$NON-NLS-1$

	/**
	 * The network port.
	 */
	public static final String PROPERTY_NETWORK_PORT = "port"; //$NON-NLS-1$

}
