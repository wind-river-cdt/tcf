/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.viewer;

import java.util.EventObject;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.launch.ui.model.LaunchModel;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
import org.eclipse.tcf.te.ui.trees.TreeContentProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;

/**
 * Launches content provider for the common navigator of Target Explorer.
 */
public class LaunchNavigatorContentProvider extends TreeContentProvider implements ICommonContentProvider, ITreeViewerListener, IEventListener {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof LaunchNode) {
			LaunchNode node = (LaunchNode)element;
			if (node.getParent() == null || LaunchNode.TYPE_ROOT.equals(node.getType())) {
				return node.getModel().getModelRoot();
			}

			if (!isRootNodeVisible() && LaunchNode.TYPE_LAUNCH_CONFIG_TYPE.equals(node.getType())) {
				return node.getModel().getModelRoot();
			}
			return node.getParent();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
		EventManager.getInstance().addEventListener(this, ChangeEvent.class);
		this.viewer.addTreeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		EventManager.getInstance().removeEventListener(this);
		this.viewer.removeTreeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object element) {
		super.getChildren(element);

		if (element instanceof LaunchNode) {
			return ((LaunchNode)element).getChildren();
		}

		LaunchModel model = LaunchModel.getLaunchModel(element);
		if (model != null) {
			if (isRootNodeVisible()) {
				return new Object[]{model.getRootNode()};
			}
			return model.getRootNode().getChildren();
		}
		return NO_ELEMENTS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof LaunchNode) {
			return ((LaunchNode)element).hasChildren();
		}
		LaunchModel model = LaunchModel.getLaunchModel(element);
		if (model != null) {
			return model.getRootNode().hasChildren();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	@Override
	public void init(ICommonContentExtensionSite config) {
		Assert.isNotNull(config);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void restoreState(IMemento aMemento) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento aMemento) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeViewerListener#treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent)
	 */
	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeViewerListener#treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent)
	 */
	@Override
	public void treeExpanded(TreeExpansionEvent event) {
	}

	/**
	 * If the root node of the tree is visible.
	 * 
	 * @return true if it is visible.
	 */
	protected boolean isRootNodeVisible() {
		return true;
	}

	@Override
	public void eventFired(EventObject event) {
		if (event.getSource() instanceof LaunchModel) {
			this.viewer.refresh(true);
		}
	}
}
