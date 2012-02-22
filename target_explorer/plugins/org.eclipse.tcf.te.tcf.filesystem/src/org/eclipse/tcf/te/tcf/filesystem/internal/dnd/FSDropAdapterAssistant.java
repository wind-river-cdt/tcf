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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.eclipse.ui.navigator.CommonNavigator;

/**
 * The drop assistant used by Target Explorer to extend its DnD support to FSTreeNode elements.
 */
public class FSDropAdapterAssistant extends CommonDropAdapterAssistant {
	// The common dnd operation
	CommonDnD dnd;

	/**
	 * Create an instance.
	 */
	public FSDropAdapterAssistant() {
		dnd = new CommonDnD();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
		boolean valid = false;
		if (target instanceof FSTreeNode) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
				valid = dnd.validateLocalSelectionDrop(target, operation, transferType);
			}
			else if(FileTransfer.getInstance().isSupportedType(transferType)) {
				valid = dnd.validateFilesDrop(target, operation, transferType);
			}
		}
		return valid ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#isSupportedType(org.eclipse.swt.dnd.TransferData)
	 */
	@Override
    public boolean isSupportedType(TransferData aTransferType) {
		if(FileTransfer.getInstance().isSupportedType(aTransferType))
			return true;
	    return super.isSupportedType(aTransferType);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.CommonDropAdapterAssistant#handleDrop(org.eclipse.ui.navigator.CommonDropAdapter, org.eclipse.swt.dnd.DropTargetEvent, java.lang.Object)
	 */
	@Override
	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, Object aTarget) {
		boolean sucess = false;
		TransferData transferType = aDropTargetEvent.currentDataType;
		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType)) {
			IStructuredSelection selection = (IStructuredSelection) aDropTargetEvent.data;
			int operations = aDropAdapter.getCurrentOperation();
			FSTreeNode target = (FSTreeNode) aTarget;
			sucess = dnd.dropLocalSelection(target, operations, selection);
		}
		else if(FileTransfer.getInstance().isSupportedType(transferType)) {
			String[] files = (String[]) aDropTargetEvent.data;
		    int operations = aDropAdapter.getCurrentOperation();
			FSTreeNode target = (FSTreeNode) aTarget;
		    sucess = dnd.dropFiles(getCommonViewer(), files, operations, target);
		}
		return sucess ? Status.OK_STATUS : Status.CANCEL_STATUS;
	}
	
	/**
	 * Get the tree viewer of Target Explorer view.
	 * 
	 * @return The tree viewer of Target Explorer view or null if the view is not found.
	 */
	private TreeViewer getCommonViewer() {
		Assert.isNotNull(Display.getCurrent());
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Assert.isNotNull(window);
		IViewReference[] references = window.getActivePage().getViewReferences();
		for(IViewReference reference : references) {
			if(reference.getId().equals(IUIConstants.ID_EXPLORER)) {
				CommonNavigator navigator =  (CommonNavigator) reference.getPart(true);
				return navigator.getCommonViewer();
			}
		}
	    return null;
    }
}
