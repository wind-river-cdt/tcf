/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River) - [361324] Add more file operations in the file 
 * 												system of Target Explorer.
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.dnd;

import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSMove;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;

/**
 * The drop target listener for the file tree of Target Explorer.
 */
public class FSDropTargetListener extends ViewerDropAdapter {
	/**
	 * Create FSDropTargetListener using the viewer.
	 * 
	 * @param viewer The file system tree viewer.
	 */
	public FSDropTargetListener(TreeViewer viewer) {
		super(viewer);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		if (target != null && transfer.isSupportedType(transferType) && operation == DND.DROP_MOVE && target instanceof FSTreeNode) {
			FSTreeNode hovered = (FSTreeNode) target;
			if (hovered.isDirectory()) {
				IStructuredSelection selection = (IStructuredSelection) transfer.getSelection();
				List<FSTreeNode> nodes = selection.toList();
				FSTreeNode head = nodes.get(0);
				String hid = head.peerNode.getPeer().getID();
				String tid = hovered.peerNode.getPeer().getID();
				if (hid.equals(tid)) {
					for (FSTreeNode node : nodes) {
						if (node == hovered || node.isAncestorOf(hovered)) {
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@Override
	public boolean performDrop(Object data) {
		Object aTarget = getCurrentTarget();
		FSTreeNode dest = (FSTreeNode) aTarget;
		IStructuredSelection selection = (IStructuredSelection) data;
		List<FSTreeNode> nodes = selection.toList();
		FSMove move = new FSMove(nodes, dest);
		return move.doit();
	}
}
