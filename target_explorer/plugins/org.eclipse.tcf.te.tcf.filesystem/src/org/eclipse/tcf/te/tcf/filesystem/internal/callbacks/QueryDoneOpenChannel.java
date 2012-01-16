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
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IFileSystem;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The callback handler that handles the event when the channel opens.
 */
public class QueryDoneOpenChannel implements DoneOpenChannel {
	// The parent node to be queried.
	FSTreeNode parentNode;

	/**
	 * Create an instance with a parent node.
	 * 
	 * @param parentNode The parent node.
	 */
	public QueryDoneOpenChannel(FSTreeNode parentNode) {
		this.parentNode = parentNode;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel#doneOpenChannel(java.lang.Throwable, org.eclipse.tcf.protocol.IChannel)
	 */
	@Override
	public void doneOpenChannel(Throwable error, IChannel channel) {
		Assert.isTrue(Protocol.isDispatchThread());
		if(error == null && channel != null) {
			IFileSystem service = channel.getRemoteService(IFileSystem.class);
			if(service != null) {
				if(parentNode.isSystemRoot()) {
					service.roots(new QueryDoneRoots(channel, parentNode));
				} else {
					String absPath = parentNode.getLocation();
					service.opendir(absPath, new QueryDoneOpen(channel, service, parentNode));
				}
			}
		}
	}
}
