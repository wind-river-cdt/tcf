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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tcf.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSMove;
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
		if (validateDropping(target, operation, transferType)) {
			return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), Messages.FSTreeNodeDropAdapterAssistant_DragError);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
		Object data = aDropTargetEvent.data;
		FSTreeNode dest = (FSTreeNode) aTarget;
		IStructuredSelection selection = (IStructuredSelection) data;
		List<FSTreeNode> nodes = selection.toList();
		FSMove move = new FSMove(nodes, dest);
		return move.doit() ? Status.OK_STATUS : new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), Messages.FSTreeNodeDropAdapterAssistant_MoveFailure);
	}
	
    /**
     * Validates dropping on the given object. 
     * 
     * @param target the object that the mouse is currently hovering over, or
     *   <code>null</code> if the mouse is hovering over empty space
     * @param operation the current drag operation (copy, move, etc.)
     * @param transferType the current transfer type
     * @return <code>true</code> if the drop is valid, and <code>false</code>
     *   otherwise
     */
	private boolean validateDropping(Object target, int operation, TransferData transferType) {
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
}
