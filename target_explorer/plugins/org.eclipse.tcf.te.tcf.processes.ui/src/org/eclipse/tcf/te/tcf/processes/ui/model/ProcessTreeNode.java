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

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.tcf.services.IProcesses;
import org.eclipse.tcf.services.ISysMonitor;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;

/**
 * Representation of a process tree node.
 */
public final class ProcessTreeNode extends PlatformObject {
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
	 * Fire a property change event.
	 */
	public void firePropertyChanged() {
		IViewerInput  provider = (IViewerInput) peerNode.getAdapter(IViewerInput.class);
		PropertyChangeEvent event = new PropertyChangeEvent(parent == null ? peerNode : this, "state", null, null); //$NON-NLS-1$
		provider.firePropertyChange(event);
    }
}
