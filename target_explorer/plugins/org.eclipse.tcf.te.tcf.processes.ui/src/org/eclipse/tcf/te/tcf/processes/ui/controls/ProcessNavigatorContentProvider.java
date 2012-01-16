/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.processes.ui.controls;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;
import org.eclipse.tcf.te.ui.trees.TreeContentProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.NavigatorFilterService;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.INavigatorFilterService;


/**
 * Processes content provider for the common navigator of Target Explorer.
 */
@SuppressWarnings("restriction")
public class ProcessNavigatorContentProvider  extends TreeContentProvider implements ICommonContentProvider {
	// The "Single Thread" filter id
	private final static String SINGLE_THREAD_FILTER_ID = "org.eclipse.tcf.te.tcf.processes.ui.navigator.filter.singleThread"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof ProcessTreeNode) {
			ProcessTreeNode parent = ((ProcessTreeNode) element).parent;
			// If the parent is a root node, return the associated peer node
			if (parent != null) {
				if (parent.type != null && parent.isRootNode()) {
					return parent.peerNode;
				}
				return parent;
			}
			return ((ProcessTreeNode) element).peerNode;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		Assert.isNotNull(parentElement);

		if (parentElement instanceof IPeerModel) {
			IPeerModel peerModel = (IPeerModel) parentElement;
			installPropertyChangeListener(peerModel);
			ProcessModel model = ProcessModel.getProcessModel(peerModel);
			if(model.getRoot() == null) {
				model.createRoot(peerModel);
			}
			if (isRootNodeVisible()) {
				return new Object[] { model.getRoot() };
			}
			return getChildren(model.getRoot());
		}
		else if (parentElement instanceof ProcessTreeNode) {
			ProcessTreeNode node = (ProcessTreeNode) parentElement;
			if(node.isPendingNode()) {
				return NO_ELEMENTS;
			}
			if (!node.childrenQueried) {
				if (!node.childrenQueryRunning) {
					ProcessModel model = ProcessModel.getProcessModel(node.peerNode);
					model.queryChildren(node);
				}
				if(node.children.isEmpty()) {
					return new Object[] {ProcessTreeNode.PENDING_NODE};
				}
			}
			return node.children.toArray();
		}
		return NO_ELEMENTS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		Assert.isNotNull(element);

		boolean hasChildren = false;

		// No children yet and the element is a process node
		if (element instanceof ProcessTreeNode) {
			ProcessTreeNode node = (ProcessTreeNode) element;
			if(node.isPendingNode()) {
				hasChildren = false;
			}else if (!node.childrenQueried || node.childrenQueryRunning) {
				hasChildren = true;
			}
			else {
				hasChildren = super.hasChildren(element);
			}
		}
		else if (element instanceof IPeerModel) {
			// Get the root node for this peer model object.
			// If null, true is returned as it means that the file system
			// model hasn't been created yet and have to treat is as children
			// not queried yet.
			IPeerModel peerModel = (IPeerModel) element;
			ProcessModel model = ProcessModel.getProcessModel(peerModel);
			ProcessTreeNode root = model.getRoot();
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

	/* (non-Javadoc)
     * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
     */
    @Override
    public void init(ICommonContentExtensionSite config) {
    	Assert.isNotNull(config);

    	// Make sure that the hidden "Single Thread" filter is active
    	INavigatorContentService cs = config.getService();
    	INavigatorFilterService fs = cs != null ? cs.getFilterService() : null;
		if (fs != null && !fs.isActive(SINGLE_THREAD_FILTER_ID)) {
			if (fs instanceof NavigatorFilterService) {
				final NavigatorFilterService navFilterService = (NavigatorFilterService)fs;
				navFilterService.addActiveFilterIds(new String[] { SINGLE_THREAD_FILTER_ID });
				Display display = PlatformUI.getWorkbench().getDisplay();
				display.asyncExec(new Runnable(){
					@Override
                    public void run() {
						navFilterService.updateViewer();
                    }});
			}
		}
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
}
