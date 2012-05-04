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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSModel;
import org.eclipse.tcf.te.tcf.filesystem.core.model.FSTreeNode;
import org.eclipse.tcf.te.tcf.filesystem.core.model.ITreeNodeModel;
import org.eclipse.tcf.te.tcf.filesystem.ui.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;


/**
 * File system content provider for the common navigator of Target Explorer.
 */
public class FSNavigatorContentProvider extends NavigatorContentProvider {
	
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
		if (parentElement instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode)parentElement;
			if (node.isFile()) return NO_ELEMENTS;
		}
		return super.getChildren(parentElement);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.filesystem.ui.controls.NavigatorContentProvider#doGetModel(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel)
	 */
	@Override
    protected ITreeNodeModel doGetModel(IPeerModel peerNode) {
		return FSModel.getFSModel(peerNode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(final Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode)element;
			if(node.isFile()) {
				return false;
			}
		}
		return super.hasChildren(element);
	}
}
