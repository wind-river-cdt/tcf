/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.handlers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSRefresh;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.internal.utils.StateManager;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;

/**
 * The job to refresh a file system node.
 */
public class RefreshNodeJob extends Job {
	// The file system node to be refreshed.
	private FSTreeNode node;
	/**
	 * Create the job with the node to be refreshed.
	 * 
	 * @param node the node to be refreshed. 
	 */
	public RefreshNodeJob(FSTreeNode node) {
	    super(NLS.bind(Messages.RefreshDirectoryHandler_RefreshJobTitle, node.name));
	    this.node = node;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (node.isSystemRoot() || node.isRoot() || node.isDirectory()) {
			FSRefresh refresh = new FSRefresh(node);
			refresh.doit();
		}
		else if (node.isFile() && !PersistenceManager.getInstance().isAutoSaving()) {
			try {
				StateManager.getInstance().refreshState(node);
			}
			catch (TCFException e) {
				return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), Messages.StateManager_RefreshFailureTitle, e);
			}
		}
		return Status.OK_STATUS;
	}
}
