/**
 * INetworkWireType.java
 * Created on Nov 11, 2011
 *
 * Copyright (c) 2011 Wind River Systems, Inc.
 *
 * The right to copy, distribute, modify, or otherwise make use
 * of this software may be licensed only pursuant to the terms
 * of an applicable Wind River license agreement.
 */
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
