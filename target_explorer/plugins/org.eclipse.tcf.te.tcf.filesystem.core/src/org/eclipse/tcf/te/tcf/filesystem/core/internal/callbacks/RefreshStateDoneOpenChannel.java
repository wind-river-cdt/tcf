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
import org.eclipse.osgi.util.NLS;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IPeer;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.filesystem.core.activator.CorePlugin;
import org.eclipse.tcf.te.tcf.filesystem.core.internal.exceptions.TCFFileSystemException;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.nls.Messages;

/**
 * The callback to process the channel opened event for refreshing the state of a 
 * file system node.
 */
public class RefreshStateDoneOpenChannel extends CallbackBase implements IChannelManager.DoneOpenChannel{
	// The node to be refreshed.
	FSTreeNode node;
	// The callback after the refreshing is done.
	ICallback callback;
	
	/**
	 * Create an instance.
	 */
	public RefreshStateDoneOpenChannel(FSTreeNode node, ICallback callback) {
		this.node = node;
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel#doneOpenChannel(java.lang.Throwable, org.eclipse.tcf.protocol.IChannel)
	 */
	@Override
	public void doneOpenChannel(Throwable error, IChannel channel) {
		IPeer peer = node.peerNode.getPeer();
		if (error != null) {
			if(channel != null) {
				Tcf.getChannelManager().closeChannel(channel);
			}
			String message = getErrorMessage(error);
			IStatus status = new Status(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), message, error);
			invokeCallback(status);
		}
		else {
			IFileSystem service = channel.getRemoteService(IFileSystem.class);
			if (service != null) {
				String path = node.getLocation(true);
				service.stat(path, new RefreshStateDoneStat(node, channel, callback));
			}
			else {
				Tcf.getChannelManager().closeChannel(channel);
				String message = NLS.bind(Messages.Operation_NoFileSystemError, peer.getID());
				IStatus status = new Status(IStatus.ERROR, CorePlugin.getUniqueIdentifier(), message, new TCFFileSystemException(message));
				invokeCallback(status);
			}
		}
	}

	/**
	 * Invoke the callback using the specified status, if the callback
	 * is not null.
	 *  
	 * @param status The processing result.
	 */
	private void invokeCallback(IStatus status) {
		if(callback != null) {
			callback.done(this, status);
		}
    }
}
