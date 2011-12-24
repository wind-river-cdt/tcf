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
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.IChannel;
import org.eclipse.tcf.protocol.IToken;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The callback handler that handles the result of service.getContext when refreshing.
 */
public class RefreshDoneGetContext implements ISysMonitor.DoneGetContext {
	// The current context id.
	String contextId;
	// The channel used for refreshing.
	IChannel channel;
	// The parent node to be refreshed.
	ProcessTreeNode parentNode;
	// The status map to mark and check the completion status.
	Map<String, Boolean> status;
	// The queue to iterate the legitimate node in the whole tree. 
	Queue<ProcessTreeNode> queue;
	// The list to record all new nodes for merging.
	List<ProcessTreeNode> newNodes;
	// The service used for refreshing.
	ISysMonitor service;
	// The callback to be called when refresh is done.
	Runnable callback;
	// The process model it is associated with.
	ProcessModel model;
	
	/**
	 * Create an instance with the field parameters.
	 */
	public RefreshDoneGetContext(ProcessModel model, List<ProcessTreeNode> newNodes, 
					Runnable callback, ISysMonitor service, Queue<ProcessTreeNode>queue, String contextId, 
					IChannel channel, Map<String, Boolean> status, ProcessTreeNode parentNode) {
		this.model = model;
		this.callback = callback;
		this.service = service;
		this.queue = queue;
		this.contextId = contextId;
		this.channel = channel;
		this.parentNode = parentNode;
		this.status = status;
		this.newNodes = newNodes;
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
            int index = searchChild(childNode);
			if (index != -1) {
				ProcessTreeNode node = parentNode.children.get(index);
				updateData(childNode, node);
			}
			else parentNode.children.add(childNode);
			newNodes.add(childNode);
		}
		setAndCheckStatus();
	}

    /**
     * Update the destination node's data with the source node's data. 
     * 
     * @param src The source node.
     * @param dest The destination node.
     */
	private void updateData(ProcessTreeNode src, ProcessTreeNode dest) {
        dest.context = src.context;
        dest.name = src.name;
        dest.pid = src.pid;
        dest.ppid = src.ppid;
        dest.state = src.state;
        dest.type = src.type;
        dest.username = src.username;
    }

	/**
	 * Remove the dead process nodes.
	 */
    private void removeDead() {
		List<ProcessTreeNode> dead = new ArrayList<ProcessTreeNode>();
		for (ProcessTreeNode node : parentNode.children) {
			int index = searchInList(node, newNodes);
			if (index == -1) {
				dead.add(node);
			}
		}
		for (ProcessTreeNode node : dead) {
			parentNode.children.remove(node);
		}
    }

    /**
     * Search the specified child node in the specified list.
     * 
     * @param childNode The child node.
     * @param list The process node list.
     * @return The index of the child node or -1 if no such node.
     */
    private int searchInList(ProcessTreeNode childNode, List<ProcessTreeNode> list) {
		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				ProcessTreeNode node = list.get(i);
				if (childNode.id.equals(node.id)) {
					return i;
				}
			}
			return -1;
		}
    }

    /**
     * Search the specified child node in the children of the parent node.
     * 
     * @param childNode The child node.
     * @return The index of the child node or -1 if no such node.
     */
	private int searchChild(ProcessTreeNode childNode) {
		return searchInList(childNode, parentNode.children);
    }

    /**
     * Set the complete flag for this context id and check if
     * all tasks have completed. If it is all completed, then
     * merge the resulting children and continue with the
     * next node in the queue. 
     */
    private void setAndCheckStatus() {
    	synchronized(status) {
    		status.put(contextId, Boolean.TRUE);
    		if(isAllComplete()){
				parentNode.childrenQueryRunning = false;
				parentNode.childrenQueried = true;
                removeDead();
                for(ProcessTreeNode node:parentNode.children) {
                	if(node.childrenQueried && !node.childrenQueryRunning) {
                		queue.offer(node);
                	}
                }
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
		node.type = "ProcNode";  //$NON-NLS-1$
		node.id = context.getID();
		node.pid = context.getPID();
		node.ppid = context.getPPID();
		node.parentId = context.getParentID();
		node.state = context.getState();
		node.username = context.getUserName();

		return node;
	}        
}
