/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.ui.navigator.dnd;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

/**
 * Drop assistant implementation.
 */
public class DropAssistant extends CommonDropAdapterAssistant {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		boolean valid = false;

		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			valid = CommonDnD.validateLocalSelectionDrop(target, operation, transferType);
		}

		return valid ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter adapter, DropTargetEvent event, Object target) {
		boolean sucess = false;
		TransferData transferType = event.currentDataType;
		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			IStructuredSelection selection = (IStructuredSelection) event.data;
			int operations = adapter.getCurrentOperation();
			sucess = CommonDnD.dropLocalSelection(target, operations, selection);
		}
		return sucess ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}

}
