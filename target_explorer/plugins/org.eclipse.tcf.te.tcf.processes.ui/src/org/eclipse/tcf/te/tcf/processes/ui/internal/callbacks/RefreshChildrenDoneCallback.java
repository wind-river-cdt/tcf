/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.internal.callbacks;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tcf.te.runtime.callback.Callback;


/**
 * The callback invoked after refreshing the children of a process node. 
 */
public class RefreshChildrenDoneCallback extends Callback {
	// The monitor to unlock the current node.
	private CallbackMonitor monitor;
	// This process' context id.
	private String contextId;

	/**
	 * Create an instance with parameters to initialize the fields.
	 */
	public RefreshChildrenDoneCallback(String contextId, CallbackMonitor monitor) {
		this.contextId = contextId;
		this.monitor = monitor;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	protected void internalDone(Object caller, IStatus status) {
		monitor.unlock(contextId, status);
	}
}