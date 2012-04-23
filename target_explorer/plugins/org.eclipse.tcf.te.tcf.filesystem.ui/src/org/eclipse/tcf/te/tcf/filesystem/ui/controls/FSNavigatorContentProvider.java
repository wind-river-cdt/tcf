/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.controls;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.ui.trees.TreeContentProvider;


/**
 * File system content provider for the common navigator of Target Explorer.
 */
public class FSNavigatorContentProvider extends TreeContentProvider {
	// The pending node constant.
	private static final FSTreeNode PENDING_NODE = FSModel.createPendingNode();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) element;
			return node.parent != null ? node.parent : node.peerNode;
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
    public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
	    super.inputChanged(viewer, oldInput, newInput);
	    UIPlugin.getClipboard().addPropertyChangeListener(commonViewerListener);
	    UIPlugin plugin = UIPlugin.getDefault();
		IPreferenceStore preferenceStore = plugin.getPreferenceStore();
		preferenceStore.addPropertyChangeListener(commonViewerListener);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#dispose()
	 */
	@Override
    public void dispose() {
	    UIPlugin.getClipboard().removePropertyChangeListener(commonViewerListener);
	    UIPlugin plugin = UIPlugin.getDefault();
		IPreferenceStore preferenceStore = plugin.getPreferenceStore();
		preferenceStore.removePropertyChangeListener(commonViewerListener);
	    super.dispose();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		super.getChildren(parentElement);

		// For the file system, we need the peer node
		if (parentElement instanceof IPeerModel) {
			final IPeerModel peerNode = (IPeerModel)parentElement;
			// Get the file system model root node, if already stored
			FSModel model = FSModel.getFSModel(peerNode);

			// If the file system model root node hasn't been created, create
			// and initialize the root node now.
			if (isRootNodeVisible()) {
				return new Object[] { model.getRoot() };
			}
			return getChildren(model.getRoot());
		} else if (parentElement instanceof FSTreeNode) {
			final FSTreeNode node = (FSTreeNode)parentElement;
			if(node.isPendingNode() || node.isFile()) {
				return NO_ELEMENTS;
			}
			List<FSTreeNode> children = node.unsafeGetChildren();
			if(!node.childrenQueried) {
				if(!node.childrenQueryRunning) {
					// Get the file system model root node, if already stored
					node.queryChildren();
				}
				children.add(PENDING_NODE);
			}
			return children.toArray();
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
				if(node.childrenQueryRunning) {
					hasChildren = !super.hasChildren(element);
				}
				else {
					hasChildren = !node.childrenQueried || super.hasChildren(element);
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
