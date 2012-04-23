/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.core.model;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.IProcesses.ProcessContext;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.services.ISysMonitor.SysMonitorContext;
import org.eclipse.tcf.te.core.interfaces.IViewerInput;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;
import org.eclipse.tcf.te.tcf.processes.core.nls.Messages;

/**
 * Representation of a process tree node.
 */
public final class ProcessTreeNode extends PlatformObject implements IPeerModelProvider{
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
		node.name = Messages.ProcessLabelProvider_RootNodeLabel;
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
	private List<ProcessTreeNode> children = new ArrayList<ProcessTreeNode>();

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
	 * Create process node with its parent node and a process context.
	 * 
	 * @param parentNode The parent node.
	 * @param aContext The process context.
	 */
	public ProcessTreeNode(ProcessTreeNode parentNode, ProcessContext aContext) {
		Assert.isTrue(Protocol.isDispatchThread());
		Assert.isNotNull(aContext);
		context = null;
		pContext = aContext;
		name = aContext.getName();
		type = "ProcNode";  //$NON-NLS-1$
		id = aContext.getID();
		if(id != null) {
			pid = parsePID(id);
		} else {
			pid = -1;
		}
		parentId = aContext.getParentID();
		if(parentId != null) {
			ppid = parsePID(parentId);
		} else {
			ppid = -1;
		}
		parent = parentNode;
		peerNode = parentNode.peerNode;
    }
	
	/**
	 * Parse a process id from string to long.
	 * 
	 * @param string The string expression of the process id.
	 * @return a long process id or -1 if it is not able to be parsed.
	 */
	private long parsePID(String string) {
		if(string.startsWith("P")) { //$NON-NLS-1$
			string = string.substring(1);
		}
		try {
			return Long.parseLong(string);
		} catch(NumberFormatException nfe) {
			return -1;
		}
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
     * Update this process' data with a new system monitor context.
     *
     * @param aContext the new context.
     */
	public void updateSysMonitorContext(SysMonitorContext aContext) {
		Assert.isNotNull(aContext);
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

    /**
     * Update this process' data with a new system monitor context.
     *
     * @param aContext the new context.
     */
	public void updateProcessContext(ProcessContext aContext) {
		Assert.isNotNull(aContext);
		ProcessContext oldContext = this.pContext;
		this.pContext = aContext;
		name = aContext.getName();
		id = aContext.getID();
		parentId = aContext.getParentID();
		if (oldContext != aContext) {
			firePropertyChange(new PropertyChangeEvent(this, "pContext", oldContext, aContext)); //$NON-NLS-1$
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

	/**
	 * Fire a property change event to notify one of the node's property has changed.
	 * 
	 * @param event The property change event.
	 */
	protected void firePropertyChange(PropertyChangeEvent event) {
		if(peerNode != null) {
			IViewerInput viewerInput = (IViewerInput) peerNode.getAdapter(IViewerInput.class);
			viewerInput.firePropertyChange(event);
		} else if(parent != null) {
			parent.firePropertyChange(event);
		}
    }

	/**
	 * Add the specified the node to the children list.
	 * 
	 * @param node The child node to be added.
	 */
	public void addChild(ProcessTreeNode child) {
		children.add(child);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "state", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Remove the specified child node from its children list.
	 * 
	 * @param node The child node to be removed.
	 */
	public void removeChild(ProcessTreeNode child) {
		children.remove(child);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "state", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Clear the children of this folder.
	 */
	public void clearChildren() {
		children.clear();
		PropertyChangeEvent event = new PropertyChangeEvent(this, "state", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }
	
	/**
	 * Get the children process list.
	 * 
	 * @return The children process list.
	 */
	public List<ProcessTreeNode> getChildren() {
	    return children;
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider#getPeerModel()
	 */
	@Override
    public IPeerModel getPeerModel() {
	    return peerNode;
    }

	/**
	 * Set the system monitor context and fire a property change event.
	 */
	public void setSysMonitorContext(SysMonitorContext sContext) {
		SysMonitorContext oldContext = this.context;
		this.context = sContext;
		if (oldContext != sContext) {
			firePropertyChange(new PropertyChangeEvent(this, "sContext", oldContext, context)); //$NON-NLS-1$
		}
    }
}
