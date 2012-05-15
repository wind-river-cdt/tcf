/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.tcf.filesystem.core.model.AbstractTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.model.ITreeNodeModel;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.trees.TreeContentProvider;


/**
 * The base navigator content provider for File System and Process Monitor
 */
public abstract class NavigatorContentProvider extends TreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof AbstractTreeNode) {
			AbstractTreeNode node = (AbstractTreeNode) element;
			return node.getParent() != null ? node.getParent() : (isRootNodeVisible()? node.peerNode : null);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		super.getChildren(parentElement);

		if (parentElement instanceof IPeerModel) {
			final IPeerModel peerNode = (IPeerModel)parentElement;
			ITreeNodeModel model = doGetModel(peerNode);
			if (isRootNodeVisible()) {
				return new Object[] { model.getRoot() };
			}
			return getChildren(model.getRoot());
		} else if (parentElement instanceof AbstractTreeNode) {
			AbstractTreeNode node = (AbstractTreeNode)parentElement;
			Object[] children = NO_ELEMENTS;
			List<Object> current = new ArrayList<Object>(node.getChildren());
			if (!node.childrenQueried) {
				current.add(getPending(node));
				if (!node.childrenQueryRunning) {
					node.queryChildren();
				}
			}
			children = current.toArray();
			return children;
		}

		return NO_ELEMENTS;
	}
	
	/**
	 * Get the tree node model for this peer node.
	 * 
	 * @param peerNode The peer node from where to get the model.
	 * @return The tree node model.
	 */
	protected abstract ITreeNodeModel doGetModel(IPeerModel peerNode);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(final Object element) {
		Assert.isNotNull(element);

		boolean hasChildren = false;

		if (element instanceof AbstractTreeNode) {
			AbstractTreeNode node = (AbstractTreeNode)element;
			if(node.isSystemRoot()) {
				hasChildren = true;
			}
			else {
				hasChildren = !node.childrenQueried || super.hasChildren(element);
			}
		}
		else if (element instanceof IPeerModel) {
			IPeerModel peerModel = (IPeerModel) element;
			ITreeNodeModel model = doGetModel(peerModel);
			AbstractTreeNode root = model.getRoot();
			hasChildren = root != null ? hasChildren(root) : true;
		}

		return hasChildren;
	}

	/**
	 * If the root node of the tree is visible.
	 * 
	 * @return true if it is visible.
	 */
	protected boolean isRootNodeVisible() {
		return true;
	}
}
