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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tcf.te.tcf.processes.ui.interfaces.INodeStateListener;
import org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode;

/**
 * The node state listener that listens to the state change event of the process tree
 * and update the viewer.
 */
public class ProcessTreeNodeStateListener implements INodeStateListener {
	// The viewer to display the process tree.
	private TreeViewer viewer;
	
	/**
	 * Create a process tree node listener.
	 * 
	 * @param viewer The tree viewer to display the process tree.
	 */
	public ProcessTreeNodeStateListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.tcf.processes.ui.interfaces.INodeStateListener#stateChanged(org.eclipse.tcf.te.tcf.processes.ui.model.ProcessTreeNode)
	 */
	@Override
	public void stateChanged(final ProcessTreeNode node) {
		// Make sure that this node is inside of this viewer.
		Tree tree = viewer.getTree();
		if (!tree.isDisposed()) {
			Display display = tree.getDisplay();
			if (display.getThread() == Thread.currentThread()) {
				if (node != null && node.id != null) {
					viewer.refresh(node);
				}
				else {
					// Refresh the whole tree.
					viewer.refresh();
				}
			}
			else {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						stateChanged(node);
					}
				});
			}
		}
	}
}
