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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSRefresh;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;

/**
 * The abstract base handler to refresh a file system tree node.
 */
public abstract class RefreshNodeHandler extends AbstractHandler {
	
	/**
	 * Schedule a refresh job for the specified tree node.
	 * 
	 * @param node The tree node to be refreshed.
	 */
	protected void scheduleRefreshJob(final FSTreeNode node) {
		Job job = new Job(NLS.bind(Messages.RefreshDirectoryHandler_RefreshJobTitle, node.name)) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				FSRefresh refresh = new FSRefresh(node);
				refresh.doit();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
