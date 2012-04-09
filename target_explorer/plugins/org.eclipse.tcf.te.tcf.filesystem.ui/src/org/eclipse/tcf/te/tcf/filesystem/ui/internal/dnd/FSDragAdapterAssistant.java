/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.ui.internal.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

/**
 * The drag assistant used by Target Explorer to extend its DnD support to FSTreeNode elements.
 */
public class FSDragAdapterAssistant extends CommonDragAdapterAssistant {
	// The common dnd operation
	CommonDnD dnd;

	/**
	 * Create an instance.
	 */
	public FSDragAdapterAssistant() {
		dnd = new CommonDnD();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#dragStart(org.eclipse.swt.dnd.DragSourceEvent, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void dragStart(DragSourceEvent anEvent, IStructuredSelection aSelection) {
		anEvent.doit = dnd.isDraggable(aSelection);
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
}
