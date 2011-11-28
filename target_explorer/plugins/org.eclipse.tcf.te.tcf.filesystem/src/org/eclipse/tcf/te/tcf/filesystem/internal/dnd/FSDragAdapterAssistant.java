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

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

/**
 * The drag assistant used by Target Explorer to extend its DnD support to FSTreeNode elements.
 */
public class FSDragAdapterAssistant extends CommonDragAdapterAssistant {

	/**
	 * Create an instance.
	 */
	public FSDragAdapterAssistant() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#dragStart(org.eclipse.swt.dnd.DragSourceEvent, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void dragStart(DragSourceEvent anEvent, IStructuredSelection aSelection) {
		anEvent.doit = isDraggable(aSelection);
		LocalSelectionTransfer.getTransfer().setSelection(aSelection);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#getSupportedTransferTypes()
	 */
	@Override
	public Transfer[] getSupportedTransferTypes() {
		return new Transfer[] {};
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#setDragData(org.eclipse.swt.dnd.DragSourceEvent, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public boolean setDragData(DragSourceEvent anEvent, IStructuredSelection aSelection) {
		return false;
	}
	
	/**
	 * If the current selection is draggable.
	 * 
	 * @param selection The currently selected nodes.
	 * @return true if it is draggable.
	 */
	private boolean isDraggable(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		Object[] objects = selection.toArray();
		for (Object object : objects) {
			if (!isDraggableObject(object)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * If the specified object is a draggable element.
	 * 
	 * @param object The object to be dragged.
	 * @return true if it is draggable.
	 */
	private boolean isDraggableObject(Object object) {
		if (object instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode) object;
			return !node.isRoot() && (node.isWindowsNode() && !node.isReadOnly() || !node.isWindowsNode() && node.isWritable());
		}
		return false;
	}	
}
