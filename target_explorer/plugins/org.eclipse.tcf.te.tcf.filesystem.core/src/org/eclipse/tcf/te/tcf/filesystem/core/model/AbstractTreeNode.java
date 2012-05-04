/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.core.model;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tcf.te.core.interfaces.IViewerInput;
import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
import org.eclipse.tcf.te.tcf.core.Tcf;
import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProvider;

/**
 * The base class of FSTreeNode and ProcessTreeNode which provides base members and methods.
 */
public abstract class AbstractTreeNode extends PlatformObject implements IPeerModelProvider{
	// The unique id of this node.
	private final UUID uniqueId = UUID.randomUUID();

	/**
	 * The tree node name.
	 */
	public String name = null;

	/**
	 * The tree node type.
	 */
	public String type = null;

	/**
	 * The peer node the file system tree node is associated with.
	 */
	public IPeerModel peerNode = null;

	/**
	 * Flag to mark once the children of the node got queried
	 */
	public boolean childrenQueried = false;

	/**
	 * Flag to mark once the children query is running
	 */
	public boolean childrenQueryRunning = false;

	/**
	 * The tree node parent.
	 */
	protected AbstractTreeNode parent = null;

	/**
	 * The tree node children.
	 */
	protected List<AbstractTreeNode> children = Collections.synchronizedList(new ArrayList<AbstractTreeNode>());

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return uniqueId.hashCode();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		if(this == obj)
			return true;
		if (obj instanceof AbstractTreeNode) {
			return uniqueId.equals(((AbstractTreeNode) obj).uniqueId);
		}
		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
		buffer.append(": name=" + (name != null ? name : super.toString())); //$NON-NLS-1$
		buffer.append(", UUID=" + uniqueId.toString()); //$NON-NLS-1$
		return buffer.toString();
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
	 * Called when the children query is done.
	 */
	public void queryDone() {
		childrenQueryRunning = false;
		childrenQueried = true;
		PropertyChangeEvent event = new PropertyChangeEvent(this, "query_done", Boolean.FALSE, Boolean.TRUE); //$NON-NLS-1$
		firePropertyChange(event);
	}
	
	/**
	 * Called when the children query is started.
	 */
	public void queryStarted() {
		childrenQueryRunning = true;
		PropertyChangeEvent event = new PropertyChangeEvent(this, "query_started", Boolean.FALSE, Boolean.TRUE); //$NON-NLS-1$
		firePropertyChange(event);
	}
	
	
	/**
	 * Fire a property change event to notify one of the node's property has changed.
	 * 
	 * @param event The property change event.
	 */
	public void firePropertyChange(PropertyChangeEvent event) {
		if(peerNode != null) {
			IViewerInput viewerInput = (IViewerInput) peerNode.getAdapter(IViewerInput.class);
			viewerInput.firePropertyChange(event);
		} else if(parent != null) {
			parent.firePropertyChange(event);
		}
    }

	/**
	 * Add the specified nodes to the children list.
	 * 
	 * @param nodes The nodes to be added.
	 */
	public void addChidren(List<? extends AbstractTreeNode> nodes) {
		children.addAll(nodes);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "addChildren", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Remove the specified nodes from the children list.
	 * 
	 * @param nodes The nodes to be removed.
	 */
	public void removeChildren(List<? extends AbstractTreeNode> nodes) {
		children.removeAll(nodes);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "removeChildren", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Add the specified the node to the children list.
	 * 
	 * @param node The child node to be added.
	 */
	public void addChild(AbstractTreeNode node) {
		children.add(node);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "addChild", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Remove the specified child node from its children list.
	 * 
	 * @param node The child node to be removed.
	 */
	public void removeChild(AbstractTreeNode node) {
		children.remove(node);
		PropertyChangeEvent event = new PropertyChangeEvent(this, "removeChild", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * Clear the children of this folder.
	 */
	public void clearChildren() {
		children.clear();
		PropertyChangeEvent event = new PropertyChangeEvent(this, "clearChildren", null, null); //$NON-NLS-1$
		firePropertyChange(event);
    }

	/**
	 * If this node is ancestor of the specified node.
	 * @return true if it is.
	 */
	public boolean isAncestorOf(AbstractTreeNode node) {
		if (node == null) return false;
		if (node.parent == this) return true;
		return isAncestorOf(node.parent);
	}

	/**
	 * Get the parent node of this node.
	 * 
	 * @return The parent node.
	 */
	public AbstractTreeNode getParent() {
		return parent;
	}
	
	/**
	 * Set the parent node of this node.
	 * 
	 * @param parent The parent node.
	 */
	public void setParent(AbstractTreeNode parent) {
		this.parent = parent;
	}

	/**
	 * Recursively refresh the children of the given process context.
	 *
	 * @param parentNode The process context node. Must not be <code>null</code>.
	 */
	public void refresh() {
		refresh(null);
	}

	/**
	 * Recursively refresh the children of the given process context with a callback, which is
	 * called when whole process is finished.
	 *
	 * @param callback The callback object, or <code>null</code> when callback is not needed.
	 */
	public void refresh(ICallback callback) {
		queryStarted();
		Tcf.getChannelManager().openChannel(peerNode.getPeer(), null, doCreateRefreshDoneOpenChannel(callback));
	}

	/**
	 * Create the callback object of opening channel for refreshing itself.
	 * 
	 * @param callback The callback object.
	 * @return The callback object.
	 */
	protected abstract DoneOpenChannel doCreateRefreshDoneOpenChannel(ICallback callback);

	/**
	 * Query the children of this file system node.
	 */
	public void queryChildren() {
		queryStarted();
		Tcf.getChannelManager().openChannel(peerNode.getPeer(), null, doCreateQueryDoneOpenChannel());
	}

	/**
	 * Create the callback object of opening channel for querying children.
	 * 
	 * @return The callback object.
	 */
	protected abstract DoneOpenChannel doCreateQueryDoneOpenChannel();
}
