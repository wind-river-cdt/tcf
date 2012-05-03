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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.launch.ui.model.LaunchModel;
import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
import org.eclipse.tcf.te.runtime.events.ChangeEvent;
import org.eclipse.tcf.te.runtime.events.EventManager;
import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
import org.eclipse.tcf.te.ui.trees.TreeContentProvider;
import org.eclipse.ui.PlatformUI;

/**
 * Launches content provider for the common navigator of Target Explorer.
 */
public class LaunchNavigatorContentProvider extends TreeContentProvider implements IEventListener {

	/**
	 * Constructor.
	 */
	public LaunchNavigatorContentProvider() {
		super();
		EventManager.getInstance().addEventListener(this, ChangeEvent.class);
	}

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
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.trees.TreeContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		EventManager.getInstance().removeEventListener(this);
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
			if (isRootNodeVisible() && model.getRootNode().hasChildren()) {
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

	/**
	 * If the root node of the tree is visible.
	 * 
	 * @return true if it is visible.
	 */
	protected boolean isRootNodeVisible() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
	 */
	@Override
	public void eventFired(EventObject event) {
		final TreeViewer viewer = this.viewer;
		if (event.getSource() instanceof LaunchModel) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					viewer.refresh(true);
				}
			});
		}
	}
}
