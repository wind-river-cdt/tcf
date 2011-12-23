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

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback handler that handles the result of service.getContext when querying.
 */
public class QueryDoneGetContext implements ISysMonitor.DoneGetContext {
	// The current context id.
	String contextId;
	// The channel used for query.
	IChannel channel;
	// The parent node to be queried.
	ProcessTreeNode parentNode;
	// The status map to mark and check the completion status.
	Map<String, Boolean> status;
	// The process model it is associated with.
	ProcessModel model;
	
	/**
	 * Create an instance with the field parameters.
	 */
	public QueryDoneGetContext(ProcessModel model, String contextId, IChannel channel, Map<String, Boolean> status, ProcessTreeNode parentNode) {
		this.model = model;
		this.contextId = contextId;
		this.channel = channel;
		this.parentNode = parentNode;
		this.status = status;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.services.ISysMonitor.DoneGetContext#doneGetContext(org.eclipse.tcf.protocol.IToken, java.lang.Exception, org.eclipse.tcf.services.ISysMonitor.SysMonitorContext)
	 */
    @Override
	public void doneGetContext(IToken token, Exception error, ISysMonitor.SysMonitorContext context) {
		if (error == null && context != null) {
			ProcessTreeNode childNode = createNodeForContext(context);
			childNode.parent = parentNode;
			childNode.peerNode = parentNode.peerNode;
			parentNode.children.add(childNode);
			model.fireNodeStateChanged(parentNode);
		}
		setAndCheckStatus();
	}

    /**
     * Set the complete flag for this context id and check if
     * all tasks have completed. 
     */
    private void setAndCheckStatus() {
    	synchronized(status) {
    		status.put(contextId, Boolean.TRUE);
    		if(isAllComplete()){
				parentNode.childrenQueryRunning = false;
				parentNode.childrenQueried = true;
				Tcf.getChannelManager().closeChannel(channel);
    		}
    	}
    }
    
    /**
     * Check if all tasks have completed by checking
     * the status entries. 
     * 
     * @return true if all of them are marked finished.
     */
    private boolean isAllComplete() {
		synchronized (status) {
			for (String id : status.keySet()) {
				Boolean bool = status.get(id);
				if (!bool.booleanValue()) {
					return false;
				}
			}
			return true;
		}
    }
    
	/**
	 * Creates a node from the given system monitor context.
	 * 
	 * @param context The system monitor context. Must be not <code>null</code>.
	 * 
	 * @return The node.
	 */
	private ProcessTreeNode createNodeForContext(ISysMonitor.SysMonitorContext context) {
		Assert.isTrue(Protocol.isDispatchThread());
		Assert.isNotNull(context);

		ProcessTreeNode node = new ProcessTreeNode();

		node.childrenQueried = false;
		node.childrenQueryRunning = false;
		node.context = context;
		node.name = context.getFile();
		node.type = "ProcNode"; //$NON-NLS-1$
		node.id = context.getID();
		node.pid = context.getPID();
		node.ppid = context.getPPID();
		node.parentId = context.getParentID();
		node.state = context.getState();
		node.username = context.getUserName();

		return node;
	}        
}