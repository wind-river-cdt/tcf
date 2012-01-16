/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.controls;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation;
import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.trees.TreeContentProvider;


/**
 * File system content provider for the common navigator of Target Explorer.
 */
public class FSNavigatorContentProvider extends TreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode parent = ((FSTreeNode)element).parent;
			// If the parent is a root node, return the associated peer node
			if (parent != null) {
				if (parent.isSystemRoot()) {
					return parent.peerNode;
				}
				return parent;
			}
			return ((FSTreeNode) element).peerNode;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		Assert.isNotNull(parentElement);

		// For the file system, we need the peer node
		if (parentElement instanceof IPeerModel) {
			final IPeerModel peerNode = (IPeerModel)parentElement;
			installPropertyChangeListener(peerNode);
			// Get the file system model root node, if already stored
			FSModel model = FSModel.getFSModel(peerNode);

			// If the file system model root node hasn't been created, create
			// and initialize the root node now.
			if (model.getRoot() == null) {
				model.createRoot(peerNode);
			}
			if (isRootNodeVisible()) {
				return new Object[] { model.getRoot() };
			}
			return getChildren(model.getRoot());
		} else if (parentElement instanceof FSTreeNode) {
			final FSTreeNode node = (FSTreeNode)parentElement;
			if(node.isPendingNode() || node.isFile()) {
				return NO_ELEMENTS;
			}
			if(!node.childrenQueried) {
				if(!node.childrenQueryRunning) {
					// Get the file system model root node, if already stored
					FSModel model = FSModel.getFSModel(node.peerNode);
					model.queryChildren(node);
				}
				if(FSOperation.getCurrentChildren(node).isEmpty()) {
					return new Object[] {FSTreeNode.PENDING_NODE};
				}
			}
			return FSOperation.getCurrentChildren(node).toArray();
		}

		return NO_ELEMENTS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(final Object element) {
		Assert.isNotNull(element);

		boolean hasChildren = false;

		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode)element;
			if(node.isPendingNode()) {
				hasChildren = false;
			} else if(node.isFile()) {
				hasChildren = false;
			} else if(node.isSystemRoot()) {
				hasChildren = true;
			} else if (node.isDirectory()) {
				if (!node.childrenQueried || node.childrenQueryRunning) {
					hasChildren = true;
				} 
				else {
					hasChildren = super.hasChildren(element);
				}
			}
		}
		else if (element instanceof IPeerModel) {
			// Get the root node for this peer model object.
			// If null, true is returned as it means that the file system
			// model hasn't been created yet and have to treat is as children
			// not queried yet.
			IPeerModel peerModel = (IPeerModel) element;
			FSModel model = FSModel.getFSModel(peerModel);
			FSTreeNode root = model.getRoot();
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
