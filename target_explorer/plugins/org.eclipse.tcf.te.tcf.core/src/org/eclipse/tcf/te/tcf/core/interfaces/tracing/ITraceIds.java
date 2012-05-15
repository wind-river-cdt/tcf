/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.core.interfaces.tracing;

/**
 * TCF core plug-in trace slot identifiers.
 */
public interface ITraceIds {

	/**
	 * If activated, tracing information about channel open/close is printed out.
	 */
	public static String TRACE_CHANNELS = "trace/channels"; //$NON-NLS-1$

	/**
	 * If activated, tracing information about the channel manager is printed out.
	 */
	public static String TRACE_CHANNEL_MANAGER = "trace/channelManager"; //$NON-NLS-1$

	/**
	 * If activated, the value-add is launched with logging enabled.
	 */
	public static String VA_LOGGING_ENABLE = "va/logging/enable"; //$NON-NLS-1$

	/**
	 * The value-add log level. Defaults to <code>0x0620</code>.
	 */
	public static String VA_LOGGING_LEVEL = "va/logging/level"; //$NON-NLS-1$
}
