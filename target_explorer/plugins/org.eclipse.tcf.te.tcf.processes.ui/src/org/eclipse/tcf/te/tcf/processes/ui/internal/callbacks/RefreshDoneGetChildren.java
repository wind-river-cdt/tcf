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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.Tcf;
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
	// The process model it is associated with.
	ProcessModel model;

	/**
	 * Create an instance with the field parameters.
	 */
	public RefreshDoneGetChildren(Runnable callback, ProcessModel model, Queue<ProcessTreeNode> queue, IChannel channel, ISysMonitor service, ProcessTreeNode parentNode) {
		this.callback = callback;
		this.model = model;
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
    public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
        if (error == null && context_ids != null && context_ids.length > 0) {
        	Map<String, Boolean> status = createStatusMap(context_ids);
            List<ProcessTreeNode> newNodes = Collections.synchronizedList(new ArrayList<ProcessTreeNode>());	
			for (String contextId : context_ids) {
				service.getContext(contextId, new RefreshDoneGetContext(model, newNodes, callback, service, queue, contextId, channel, status, parentNode));
			}
    	} else {
            parentNode.childrenQueryRunning = false;
            parentNode.childrenQueried = true;
            parentNode.children.clear();
			if (queue.isEmpty()) {
	    		model.fireNodeStateChanged(parentNode);
				Tcf.getChannelManager().closeChannel(channel);
				if(callback != null) {
					callback.run();
				}
			} else {
				ProcessTreeNode node = queue.poll();
				service.getChildren(node.id, new RefreshDoneGetChildren(callback, model, queue, channel, service, node));
			}
    	}
    }

    /**
     * Create and initialize a status map with all the context ids and completion status
     * set to false.
     * 
     * @param context_ids All the context ids.
     * @return A map with initial values
     */
	private Map<String, Boolean> createStatusMap(String[] context_ids) {
        Map<String, Boolean> status = new HashMap<String, Boolean>();
        for (String contextId : context_ids) {
        	status.put(contextId, Boolean.FALSE);
        }
        return status;
    }		
}
