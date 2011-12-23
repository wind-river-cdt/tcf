/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.runtime.services.interfaces;

import java.util.Map;

/**
 * Properties access service.
 * <p>
 * Allows generic access to properties of a given context without having
 * to know all the details and limitations for accessing the desired properties.
 */
public interface IPropertiesAccessService extends IService {

	/**
	 * Returns a unmodifiable map containing the target addresses and ports for the given context,
	 * if it can be determined.
	 * <p>
	 * A context may return multiple target addresses and ports if the context can be reached using
	 * different connection methods.
	 * <p>
	 * <b>Note:</b>
	 * <ul>
	 * <li>See the constants defined in the properties access constants interface for default
	 * address and port types.</li>
	 * <li>The target address returned must <b>not</b> necessarily be an IP address.</li>
	 * <li>The values of the address or port properties might be <code>null</code>.</li>
	 * </ul>
	 *
	 * @param context The context to get the target addresses and ports from. Must not be <code>null</code>.
	 * @return The unmodifiable map containing the target addresses and ports, or <code>null</code>.
	 */
	public Map<String, String> getTargetAddress(Object context);
}