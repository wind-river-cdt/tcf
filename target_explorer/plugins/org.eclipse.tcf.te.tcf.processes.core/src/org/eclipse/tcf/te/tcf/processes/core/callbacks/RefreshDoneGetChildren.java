/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.core.callbacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.processes.core.activator.CoreBundleActivator;
import org.eclipse.tcf.te.tcf.processes.core.model.ProcessTreeNode;

/**
 * The callback handler that handles the result of service.getChildren when refreshing.
 */
public class RefreshDoneGetChildren implements ISysMonitor.DoneGetChildren {
	// The channel used for refreshing.
	IChannel channel;
	// The service used for refreshing.
	ISysMonitor service;
	// The parent node to be refreshed.
	ProcessTreeNode parentNode;
	// The queue to iterate the legitimate node in the whole tree. 
	Queue<ProcessTreeNode> queue;
	// The service used for refreshing.
	ICallback callback;

	/**
	 * Create an instance with the field parameters.
	 */
	public RefreshDoneGetChildren(ICallback callback, Queue<ProcessTreeNode> queue, IChannel channel, ISysMonitor service, ProcessTreeNode parentNode) {
		this.callback = callback;
		this.queue = queue;
		this.channel = channel;
		this.service = service;
		this.parentNode = parentNode;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.ISysMonitor.DoneGetChildren#doneGetChildren(org.eclipse.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
	 */
    @Override
    public void doneGetChildren(IToken token, Exception error, String[] contextIds) {
        if (error == null) {
			if (contextIds != null && contextIds.length > 0) {
				IProcesses pService = channel.getRemoteService(IProcesses.class);
				if (pService != null) {
					List<ProcessTreeNode> newNodes = Collections.synchronizedList(new ArrayList<ProcessTreeNode>());
					ICallback monitorCallback = new RefreshDoneMonitorCallback(newNodes, parentNode, queue, callback, service, channel);
					CallbackMonitor monitor = new CallbackMonitor(monitorCallback, (Object[]) contextIds);
					for (String contextId : contextIds) {
						RefreshDoneGetContext done = new RefreshDoneGetContext(channel, newNodes, contextId, monitor, parentNode);
						service.getContext(contextId, done);
						pService.getContext(contextId, done);
					}
				}
			} else {
	            parentNode.queryDone();
	            parentNode.clearChildren();
				if (queue.isEmpty()) {
					if(callback != null) {
						callback.done(this, Status.OK_STATUS);
					}
				} else {
					ProcessTreeNode node = queue.poll();
					service.getChildren(node.id, new RefreshDoneGetChildren(callback, queue, channel, service, node));
				}
			}
    	} else {
            parentNode.queryDone();
			if(callback != null) {
				IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), error.getMessage(), error);
				callback.done(this, status);
			}
    	}
    }
}
