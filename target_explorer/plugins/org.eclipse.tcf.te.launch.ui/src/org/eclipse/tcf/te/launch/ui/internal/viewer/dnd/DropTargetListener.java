/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.launch.ui.internal.viewer.dnd;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

/**
 * The drop target listener for the launches of Target Explorer.
 */
public class DropTargetListener extends ViewerDropAdapter {

	private CommonDnD commonDnD = new CommonDnD();

	/**
	 * Create DropTargetListener using the viewer.
	 * 
	 * @param viewer The file system tree viewer.
	 */
	public DropTargetListener(TreeViewer viewer) {
		super(viewer);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		return commonDnD.isValidDnD(null, target != null ? target : getViewer().getInput(), operation, transferType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@Override
	public boolean performDrop(Object data) {
		return commonDnD.doDnD(null, getCurrentTarget() != null ? getCurrentTarget() : getViewer().getInput(), getCurrentOperation(), (IStructuredSelection)data);
	}
}
