/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.core.model;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.protocol.Protocol;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.IProcesses.ProcessContext;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.services.ISysMonitor.SysMonitorContext;
import org.eclipse.tcf.te.core.interfaces.IFilterable;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel;
import org.eclipse.tcf.te.tcf.filesystem.core.model.AbstractTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.model.UserAccount;
import org.eclipse.tcf.te.tcf.processes.core.callbacks.QueryDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.core.callbacks.RefreshChildrenDoneOpenChannel;
import org.eclipse.tcf.te.tcf.processes.core.callbacks.RefreshDoneOpenChannel;

/**
 * Representation of a process tree node.
 */
public final class ProcessTreeNode extends AbstractTreeNode implements IFilterable {
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
	 * Get the children process list.
	 * 
	 * @return The children process list.
	 */
	@Override
    public List<ProcessTreeNode> getChildren() {
		return (List<ProcessTreeNode>) super.getChildren();
    }

	/**
	 * Return if this node is a root node.
	 * 
	 * @return true if it is a root node.
	 */
	@Override
    public boolean isSystemRoot() {
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
	
	public boolean isAgentOwner() {
		UserAccount account = getUserAccount(peerNode);
		if (account != null && context != null) {
			return context.getUID() == account.getEUID();
		}
		return false;
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
	 * Set the system monitor context and fire a property change event.
	 */
	public void setSysMonitorContext(SysMonitorContext sContext) {
		SysMonitorContext oldContext = this.context;
		this.context = sContext;
		if (oldContext != sContext) {
			firePropertyChange(new PropertyChangeEvent(this, "sContext", oldContext, context)); //$NON-NLS-1$
		}
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.model.AbstractTreeNode#doCreateRefreshDoneOpenChannel(org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
	 */
	@Override
    protected DoneOpenChannel doCreateRefreshDoneOpenChannel(ICallback callback) {
	    return new RefreshDoneOpenChannel(this, callback);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.model.AbstractTreeNode#doCreateQueryDoneOpenChannel()
	 */
	@Override
    protected DoneOpenChannel doCreateQueryDoneOpenChannel(ICallback callback) {
	    return new QueryDoneOpenChannel(this, callback);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.core.model.AbstractTreeNode#getParent()
	 */
	@Override
    public ProcessTreeNode getParent() {
		return (ProcessTreeNode) parent;
	}

	@Override
    public void refreshChildren() {
		Tcf.getChannelManager().openChannel(peerNode.getPeer(), null, new RefreshChildrenDoneOpenChannel(this));
    }
}
