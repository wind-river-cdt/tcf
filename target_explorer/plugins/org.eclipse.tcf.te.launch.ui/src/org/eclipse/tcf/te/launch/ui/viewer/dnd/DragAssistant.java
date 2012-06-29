/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.viewer.dnd;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.navigator.CommonDragAdapterAssistant;

/**
 * Drag assistant implementation.
 */
public class DragAssistant extends CommonDragAdapterAssistant {

	private CommonDnD commonDnD = new CommonDnD();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#dragStart(org.eclipse.swt.dnd.DragSourceEvent, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void dragStart(DragSourceEvent event, IStructuredSelection selection) {
		event.doit = commonDnD.isDraggable(selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#getSupportedTransferTypes()
	 */
	@Override
	public Transfer[] getSupportedTransferTypes() {
		return new Transfer[] {LocalSelectionTransfer.getTransfer()};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDragAdapterAssistant#setDragData(org.eclipse.swt.dnd.DragSourceEvent, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public boolean setDragData(DragSourceEvent event, IStructuredSelection aSelection) {
		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
			event.data = LocalSelectionTransfer.getTransfer().getSelection();
			return true;
		}
		return false;
	}

}
