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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.te.tcf.filesystem.controls.CommonViewerListener;
import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessModel;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;
import org.eclipse.tcf.te.ui.interfaces.IViewerInput;
import org.eclipse.tcf.te.ui.nls.Messages;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Process tree control content provider implementation.
 */
public class ProcessTreeContentProvider implements ITreeContentProvider {
	/**
	 * Static reference to the return value representing no elements.
	 */
	protected final static Object[] NO_ELEMENTS = new Object[0];

	// Flag to control if the process root node is visible
	private final boolean rootNodeVisible;
	private IPropertyChangeListener commonViewerListener;

	/**
	 * Create an instance with the rootNodeVisible set to true.
	 */
	public ProcessTreeContentProvider() {
		this(true);
	}

	/**
	 * Create an instance specifying if the rootNodeVisible is true.
	 * 
	 * @param rootVisible true if the root is visible.
	 */
	public ProcessTreeContentProvider(boolean rootVisible) {
		this.rootNodeVisible = rootVisible;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(viewer instanceof CommonViewer) {
			// This content provider is a navigator content extension.
			commonViewerListener = new CommonViewerListener((CommonViewer) viewer);
		} else {
			commonViewerListener = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof ProcessTreeNode) {
			return ((ProcessTreeNode) element).parent;
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
			IViewerInput viewerInput = (IViewerInput) peerModel.getAdapter(IViewerInput.class);
			if(viewerInput != null && commonViewerListener != null) {
				viewerInput.addPropertyChangeListener(commonViewerListener);
			}
			ProcessModel model = ProcessModel.getProcessModel(peerModel);
			if(model.getRoot() == null) {
				model.createRoot(peerModel);
			}
			if (rootNodeVisible) {
				return new Object[] { model.getRoot() };
			}
			return getChildren(model.getRoot());
		}
		else if (parentElement instanceof ProcessTreeNode) {
			ProcessTreeNode node = (ProcessTreeNode) parentElement;
			if (!node.childrenQueried && !node.childrenQueryRunning) {
				ProcessModel model = ProcessModel.getProcessModel(node.peerNode);
				model.queryChildren(node);
			}
			if(!node.childrenQueried && node.children.isEmpty()) {
				ProcessTreeNode pendingNode = new ProcessTreeNode();
				pendingNode.name = Messages.PendingOperation_label;
				pendingNode.type = "ProcPendingNode";  //$NON-NLS-1$
				return new Object[] { pendingNode };
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
			if (!node.childrenQueried || node.childrenQueryRunning) {
				hasChildren = true;
			}
			else if (node.childrenQueried) {
				hasChildren = node.children.size() > 0;
			}
		}

		return hasChildren;
	}
}
