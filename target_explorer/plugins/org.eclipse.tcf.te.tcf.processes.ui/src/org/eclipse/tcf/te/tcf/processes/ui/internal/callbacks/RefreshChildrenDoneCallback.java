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

import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback invoked after refreshing the children of a process node. 
 */
public class RefreshChildrenDoneCallback implements Runnable {
	// The step to progressively update the tree viewer.
	private static final int PROGRESSIVE_STEP_COUNT = 5;
	// The monitor to unlock the current node.
	private CallbackMonitor monitor;
	// The process model used to fire property change events.
	private ProcessModel model;
	// This process' context id.
	private String contextId;
	// The parent process of this process.
	private ProcessTreeNode parentNode;

	/**
	 * Create an instance with parameters to initialize the fields.
	 */
	public RefreshChildrenDoneCallback(String contextId, ProcessTreeNode parentNode, CallbackMonitor monitor, ProcessModel model) {
		this.contextId = contextId;
		this.model = model;
		this.monitor = monitor;
		this.parentNode = parentNode;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		monitor.unlock(contextId);
		int count = monitor.getUnlockedCount();
		if ((count % PROGRESSIVE_STEP_COUNT) == 0) {
			model.firePropertyChanged(parentNode);
		}
	}
}