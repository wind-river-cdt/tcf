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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback handler that handles the event when the channel opens when refreshing.
 */
public class RefreshDoneOpenChannel implements IChannelManager.DoneOpenChannel {
	// The parent node to be refreshed.
	ProcessTreeNode parentNode;
	// The callback to be called when refresh is done.
	Runnable callback;
	
	/**
	 * Create an instance with the specified field parameters.
	 */
	public RefreshDoneOpenChannel(Runnable callback, ProcessTreeNode parentNode) {
		this.callback = callback;
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
				Queue<ProcessTreeNode> queue = new ConcurrentLinkedQueue<ProcessTreeNode>();
				final ProcessModel model = ProcessModel.getProcessModel(parentNode.peerNode);
				service.getChildren(parentNode.id, new RefreshDoneGetChildren(model, new Runnable(){

					@Override
                    public void run() {
						Tcf.getChannelManager().closeChannel(channel);
						model.firePropertyChanged(parentNode);
						if(callback!=null) {
							callback.run();
						}
                    }}, queue, channel, service, parentNode));
			}
		}
    }
}
