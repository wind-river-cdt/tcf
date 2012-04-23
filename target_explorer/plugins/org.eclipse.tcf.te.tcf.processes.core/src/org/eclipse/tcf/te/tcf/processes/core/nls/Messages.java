/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.core.nls;

import org.eclipse.osgi.util.NLS;

/**
 * Target Explorer TCF processes extensions core plug-in externalized strings management.
 */
public class Messages extends NLS {

	// The plug-in resource bundle name
	private static final String BUNDLE_NAME = "org.eclipse.tcf.te.tcf.processes.core.nls.Messages"; //$NON-NLS-1$

	/**
	 * Static constructor.
	 */
	static {
		// Load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	// **** Declare externalized string id's down here *****

	public static String ProcessLauncher_error_channelConnectFailed;
	public static String ProcessLauncher_error_channelNotConnected;
	public static String ProcessLauncher_error_missingProcessPath;
	public static String ProcessLauncher_error_missingRequiredService;
	public static String ProcessLauncher_error_illegalNullArgument;
	public static String ProcessLauncher_error_getEnvironmentFailed;
	public static String ProcessLauncher_error_processLaunchFailed;
	public static String ProcessLauncher_error_processTerminateFailed;
	public static String ProcessLauncher_error_processSendSignalFailed;
	public static String ProcessLauncher_error_possibleCause;
	public static String ProcessLauncher_cause_subscribeFailed;
	public static String ProcessLauncher_cause_startFailed;
	public static String ProcessLauncher_cause_ioexception;

	public static String ProcessStreamReaderRunnable_error_readFailed;
	public static String ProcessStreamWriterRunnable_error_writeFailed;
	public static String ProcessStreamReaderRunnable_error_appendFailed;

	public static String CallbackMonitor_AllTasksFinished;
	public static String PendingOperation_label;
	public static String ProcessLabelProvider_RootNodeLabel;
	public static String AttachStep_error_connect;
	public static String AttachStep_error_attach;
	public static String AttachStep_error_getContext;
	public static String AttachStep_error_openChannel;
	public static String AttachStep_error_title;
	public static String DetachStep_error_title;
	public static String DetachStep_error_disconnect;
	public static String DetachStep_error_getContext;
	public static String DetachStep_error_detach;
	public static String DetachStep_error_openChannel;
}
