/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

/**
 * The drag source listener for the file tree of Target Explorer.
 */
public class FSDragSourceListener implements DragSourceListener {
	// The tree viewer in which the DnD gesture happens.
	private TreeViewer viewer;
	// The common dnd operation
	CommonDnD dnd;

	/**
	 * Create an FSDragSourceListener using the specified tree viewer.
	 * 
	 * @param viewer The file system tree viewer.
	 */
	public FSDragSourceListener(TreeViewer viewer) {
		this.viewer = viewer;
		dnd = new CommonDnD();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragStart(DragSourceEvent event) {
		IStructuredSelection aSelection = (IStructuredSelection) viewer.getSelection();
		event.doit = dnd.isDraggable(aSelection);
		LocalSelectionTransfer.getTransfer().setSelection(aSelection);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragSetData(DragSourceEvent event) {
		event.doit = dnd.setDragData(event);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragFinished(DragSourceEvent event) {
	}
}
