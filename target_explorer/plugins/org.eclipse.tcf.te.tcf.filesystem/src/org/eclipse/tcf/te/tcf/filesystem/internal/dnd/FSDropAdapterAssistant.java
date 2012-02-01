/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.dnd;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSCopy;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSMove;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSOperation;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

/**
 * The drop assistant used by Target Explorer to extend its DnD support to FSTreeNode elements.
 */
public class FSDropAdapterAssistant extends CommonDropAdapterAssistant {
	/**
	 * Create an instance.
	 */
	public FSDropAdapterAssistant() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		if (transfer.isSupportedType(transferType) && target instanceof FSTreeNode) {
			FSTreeNode hovered = (FSTreeNode) target;
			IStructuredSelection selection = (IStructuredSelection) transfer.getSelection();
			List<FSTreeNode> nodes = selection.toList();
			boolean moving = (operation & DND.DROP_MOVE) != 0;
			boolean copying = (operation & DND.DROP_COPY) != 0;
			if (hovered.isDirectory() && hovered.isWritable() && (moving || copying)) {
				FSTreeNode head = nodes.get(0);
				String hid = head.peerNode.getPeerId();
				String tid = hovered.peerNode.getPeerId();
				if (hid.equals(tid)) {
					for (FSTreeNode node : nodes) {
						if (moving && node == hovered || node.isAncestorOf(hovered)) {
							return Status.CANCEL_STATUS;
						}
					}
					return Status.OK_STATUS;
				}
			}
			else if (hovered.isFile() && copying) {
				hovered = hovered.parent;
				return validateDrop(hovered, operation, transferType);
			}
		}
		return Status.CANCEL_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
		Object data = aDropTargetEvent.data;
		int operations = aDropAdapter.getCurrentOperation();
		IStructuredSelection selection = (IStructuredSelection) data;
		List<FSTreeNode> nodes = selection.toList();
		FSOperation operation = null;
		if ((operations & DND.DROP_MOVE) != 0) {
			FSTreeNode dest = (FSTreeNode) aTarget;
			operation = new FSMove(nodes, dest);
		}
		else if ((operations & DND.DROP_COPY) != 0) {
			FSTreeNode hovered = (FSTreeNode) aTarget;
			FSTreeNode dest = getCopyDestination(hovered, nodes);
			operation = new FSCopy(nodes, dest);
		}
		return operation != null ? operation.doit() : Status.CANCEL_STATUS;
	}

	/**
	 * Return an appropriate destination directory for copying according to
	 * the specified hovered node.  If the hovered node is a file, then return 
	 * its parent directory. If the hovered node is a directory, then return its
	 * self if it is not a node being copied. Return its parent directory if it is
	 * a node being copied.
	 * @param hovered
	 * @param nodes
	 * @return
	 */
	private FSTreeNode getCopyDestination(FSTreeNode hovered, List<FSTreeNode> nodes) {
		if (hovered.isFile()) {
			return hovered.parent;
		}
		else if (hovered.isDirectory()) {
			for (FSTreeNode node : nodes) {
				if (node == hovered) {
					return hovered.parent;
				}
			}
		}
		return hovered;
	}
}
