/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.internal.callbacks;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IFileSystem.DoneStat;
import org.eclipse.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.FileState;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * The callback to process the stat done event when
 * refreshing the state of a file system node.
 */
public class RefreshStateDoneStat implements DoneStat {
	// The channel used to refresh the node's state.
	IChannel channel;
	// The node whose state is being refreshed.
	FSTreeNode node;
	// The callback to be invoked after refreshing.
	ICallback callback;
	
	/**
	 * Create an instance
	 */
	public RefreshStateDoneStat(FSTreeNode node, IChannel channel, ICallback callback) {
		this.node = node;
		this.channel = channel;
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.IFileSystem.DoneStat#doneStat(org.eclipse.tcf.protocol.IToken, org.eclipse.tcf.services.IFileSystem.FileSystemException, org.eclipse.tcf.services.IFileSystem.FileAttrs)
	 */
	@Override
	public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
		Tcf.getChannelManager().closeChannel(channel);
		if (error == null) {
			FileAttrs oldAttrs = node.attr;
			node.setAttributes(attrs);
			FileState fileDigest = PersistenceManager.getInstance().getFileDigest(node);
			if (fileDigest.getTargetDigest() == null || 
							(oldAttrs == null && attrs != null || 
							oldAttrs != null && attrs == null || 
							oldAttrs != null && attrs != null && oldAttrs.mtime != attrs.mtime)) {
				// Its modification time has changed. Update the digest.
				updateTargetDigest();
			}
			else {
				invokeCallback(Status.OK_STATUS);
			}
		}
		else {
			String message = NLS.bind(Messages.StateManager_CannotGetFileStatMessage, new Object[] { node.name, error });
			IStatus status = new Status(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), message, error);
			invokeCallback(status);
		}
	}

	/**
	 * Invoke the callback handler if it is not null using
	 * the specified status.
	 * 
	 * @param status The refreshing result.
	 */
	protected void invokeCallback(IStatus status) {
		if(callback != null) {
			callback.done(this, status);
		}
    }
	
	/**
	 * Update the node's target digest and invoke the callback
	 * when the job is done.
	 */
	private void updateTargetDigest() {
		Job job = new TargetFileDigestJob(node);
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
            public void done(IJobChangeEvent event) {
				invokeCallback(event.getResult());
			}
		});
		job.schedule();
    }
}
