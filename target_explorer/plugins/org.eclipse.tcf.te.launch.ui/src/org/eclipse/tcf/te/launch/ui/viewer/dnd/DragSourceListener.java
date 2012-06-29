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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;

/**
 * The drag source listener for the launches of Target Explorer.
 */
public class DragSourceListener extends DragSourceAdapter {
	// The tree viewer in which the DnD gesture happens.
	private TreeViewer viewer;

	private CommonDnD commonDnD = new CommonDnD();

	/**
	 * Create an DragSourceListener using the specified tree viewer.
	 * 
	 * @param viewer The file system tree viewer.
	 */
	public DragSourceListener(TreeViewer viewer) {
		super();
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		event.doit = commonDnD.isDraggable(selection);
		LocalSelectionTransfer.getTransfer().setSelection(selection);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragSetData(DragSourceEvent event) {
		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
			event.data = LocalSelectionTransfer.getTransfer().getSelection();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragFinished(DragSourceEvent event) {
	}
}
