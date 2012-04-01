/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.callbacks;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.te.runtime.callback.Callback;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.nls.Messages;

/**
 * The callback handler that handles the event when the channel opens.
 */
public class QueryDoneOpenChannel implements DoneOpenChannel {
	// The parent node to be queried.
	FSTreeNode parentNode;
	// Callback object.
	ICallback callback;

	/**
	 * Create an instance with a parent node.
	 * 
	 * @param parentNode The parent node.
	 */
	public QueryDoneOpenChannel(FSTreeNode parentNode) {
		this(parentNode, null);
	}

	/**
	 * Create an instance with a parent node.
	 * 
	 * @param parentNode The parent node.
	 * @param callback Callback object.
	 */
	public QueryDoneOpenChannel(FSTreeNode parentNode, ICallback callback) {
		this.parentNode = parentNode;
		this.callback = callback;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel#doneOpenChannel(java.lang.Throwable, org.eclipse.tcf.protocol.IChannel)
	 */
	@Override
	public void doneOpenChannel(Throwable error, final IChannel channel) {
		Assert.isTrue(Protocol.isDispatchThread());
		ICallback proxy = new Callback(){
			@Override
            protected void internalDone(Object caller, IStatus status) {
				// Reset the children query markers
				parentNode.queryDone();
				Tcf.getChannelManager().closeChannel(channel);
				if(callback != null) {
					callback.done(caller, status);
				}
            }
		};
		if(error == null && channel != null) {
			IFileSystem service = channel.getRemoteService(IFileSystem.class);
			if(service != null) {
				if(parentNode.isSystemRoot()) {
					service.roots(new QueryDoneRoots(proxy, parentNode));
				} else {
					String absPath = parentNode.getLocation();
					service.opendir(absPath, new QueryDoneOpen(proxy, channel, service, parentNode));
				}
			} else {
				Status status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), Messages.QueryDoneOpenChannel_NoFService, null);
				proxy.done(this, status);
			}
		} else {
			IStatus status = error == null ? Status.OK_STATUS : new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), error.getLocalizedMessage(), error);
			proxy.done(this, status);
		}
	}
}
