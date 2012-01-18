/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.services.ISysMonitor.SysMonitorContext;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.nls.Messages;
import org.eclipse.tcf.te.ui.utils.PropertyChangeProvider;

/**
 * Representation of a process tree node.
 */
public final class ProcessTreeNode extends PropertyChangeProvider {
	public static final ProcessTreeNode PENDING_NODE = createPendingNode();
	
	/**
	 * Create a pending node.
	 * 
	 * @return A pending node.
	 */
	private static ProcessTreeNode createPendingNode() {
		ProcessTreeNode node = new ProcessTreeNode();
		node.name = Messages.PendingOperation_label;
		node.type = "ProcPendingNode"; //$NON-NLS-1$
		return node;
	}
	
	/**
	 * Create a root process node.
	 * 
	 * @param peerModel The peer model which this process belongs to.
	 * @return The root process node.
	 */
	public static ProcessTreeNode createRootNode(IPeerModel peerModel) {
		ProcessTreeNode node = new ProcessTreeNode();
		node.type = "ProcRootNode"; //$NON-NLS-1$
		node.peerNode = peerModel;
		return node;
	}
	
	private final UUID uuid = UUID.randomUUID();

	/**
	 * The tree node name.
	 */
	public String name = null;

	/**
	 * The tree node type.
	 */
	public String type = null;

	/**
	 * The system monitor context object
	 */
	public ISysMonitor.SysMonitorContext context;

	/**
	 * The process context object
	 */
	public IProcesses.ProcessContext pContext;

	/**
	 * The internal process id
	 */
	public String id = null;

	/**
	 * The internal parent process id.
	 */
	public String parentId = null;

	/**
	 * The native process id.
	 */
	public long pid = 0L;

	/**
	 * The native parent process id.
	 */
	public long ppid = 0L;

	/**
	 * The process state
	 */
	public String state = null;

	/**
	 * The process owner/creator
	 */
	public String username = null;

	/**
	 * The tree node parent.
	 */
	public ProcessTreeNode parent = null;

	/**
	 * The tree node children.
	 */
	public List<ProcessTreeNode> children = new ArrayList<ProcessTreeNode>();

	/**
	 * Flag to mark once the children of the node got queried
	 */
	public boolean childrenQueried = false;

	/**
	 * Flag to mark once the children query is running
	 */
	public boolean childrenQueryRunning = false;

	/**
	 * The peer node the process node is associated with.
	 */
	public IPeerModel peerNode;

	/**
	 * Create a pending node.
	 */
	public ProcessTreeNode() {
	}

	/**
	 * Create process node with its parent node and a context.
	 * 
	 * @param parentNode The parent node.
	 * @param aContext The system monitor context.
	 */
	public ProcessTreeNode(ProcessTreeNode parentNode, ISysMonitor.SysMonitorContext aContext) {
		Assert.isTrue(Protocol.isDispatchThread());
		Assert.isNotNull(aContext);
		context = aContext;
		pContext = null;
		name = aContext.getFile();
		type = "ProcNode";  //$NON-NLS-1$
		id = aContext.getID();
		pid = aContext.getPID();
		ppid = aContext.getPPID();
		parentId = aContext.getParentID();
		state = aContext.getState();
		username = aContext.getUserName();
		parent = parentNode;
		peerNode = parentNode.peerNode;
	}
	
	/**
	 * Return if this node is a pending node.
	 * 
	 * @return true if this node is a pending node.
	 */
	public boolean isPendingNode() {
		return type != null && type.equals("ProcPendingNode"); //$NON-NLS-1$
	}

	/**
	 * Return if this node is a root node.
	 * 
	 * @return true if it is a root node.
	 */
	public boolean isRootNode() {
		return type != null && type.equals("ProcRootNode"); //$NON-NLS-1$
	}

    /**
     * Update the destination node's data with the source node's data.
     *
     * @param src The source node.
     * @param dest The destination node.
     */
	public void updateData(SysMonitorContext aContext) {
		SysMonitorContext oldContext = this.context;
		this.context = aContext;
		name = aContext.getFile();
		id = aContext.getID();
		pid = aContext.getPID();
		ppid = aContext.getPPID();
		parentId = aContext.getParentID();
		state = aContext.getState();
		username = aContext.getUserName();
		if (oldContext != aContext) {
			firePropertyChange(new PropertyChangeEvent(this, "context", oldContext, aContext)); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return uuid.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof ProcessTreeNode) {
			return uuid.equals(((ProcessTreeNode) obj).uuid);
		}
		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}

	/**
	 * Set the process context.
	 * 
	 * @param pContext The process context.
	 */
	public void setProcessContext(IProcesses.ProcessContext pContext) {
		IProcesses.ProcessContext oldContext = this.pContext;
		this.pContext = pContext;
		if (oldContext != pContext) {
			firePropertyChange(new PropertyChangeEvent(this, "pContext", oldContext, pContext)); //$NON-NLS-1$
		}
	}
}
