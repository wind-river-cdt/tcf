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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

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
	Runnable callback;
	// The process model attached.
	ProcessModel model;

	/**
	 * Create an instance with the field parameters.
	 */
	public RefreshDoneGetChildren(ProcessModel model, Runnable callback, Queue<ProcessTreeNode> queue, IChannel channel, ISysMonitor service, ProcessTreeNode parentNode) {
		this.model = model;
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
        if (error == null && contextIds != null && contextIds.length > 0) {
			IProcesses pService = channel.getRemoteService(IProcesses.class);
			if(pService != null) {
	            List<ProcessTreeNode> newNodes = Collections.synchronizedList(new ArrayList<ProcessTreeNode>());	
				Runnable monitorCallback = new RefreshDoneMonitorCallback(newNodes, parentNode, queue, callback, service, model, channel);
				CallbackMonitor monitor = new CallbackMonitor(monitorCallback, (Object[])contextIds);
				for (String contextId : contextIds) {
					RefreshDoneGetContext done = new RefreshDoneGetContext(newNodes, contextId, monitor, parentNode);
					service.getContext(contextId, done);
					pService.getContext(contextId, done);
				}
			}
    	} else {
            parentNode.childrenQueryRunning = false;
            parentNode.childrenQueried = true;
            parentNode.children.clear();
			if (queue.isEmpty()) {
				if(callback != null) {
					callback.run();
				}
			} else {
				ProcessTreeNode node = queue.poll();
				service.getChildren(node.id, new RefreshDoneGetChildren(model, callback, queue, channel, service, node));
			}
    	}
    }
}
