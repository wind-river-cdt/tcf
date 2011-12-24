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

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback handler that handles the event when the channel opens when querying.
 */
public class QueryDoneOpenChannel implements IChannelManager.DoneOpenChannel {
	// The parent node to be queried.
	ProcessTreeNode parentNode;

	/**
	 * Create an instance with a process model and a parent node.
	 * 
	 * @param model The process model.
	 * @param parentNode The parent node to be queried.
	 */
	public QueryDoneOpenChannel(ProcessTreeNode parentNode) {
		this.parentNode = parentNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel#doneOpenChannel(java.lang.Throwable, org.eclipse.tcf.protocol.IChannel)
	 */
	@Override
    public void doneOpenChannel(Throwable error, final IChannel channel) {
		Assert.isTrue(Protocol.isDispatchThread());
		if (error == null && channel != null) {
			ISysMonitor service = channel.getRemoteService(ISysMonitor.class);
			if (service != null) {
				service.getChildren(parentNode.id, new QueryDoneGetChildren(channel, service, parentNode));
			}
		}
    }
}
