/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.internal.celleditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tcf.te.tcf.filesystem.internal.handlers.RenameFilesHandler;
import org.eclipse.tcf.te.ui.interfaces.IViewerCellEditorFactory;

/**
 * FSViewerCellEditorFactory implements <code>IViewerCellEditorFactory</code> to add 
 * cell editors to Target Explorer for renaming files or folders in the file system tree viewer.
 */
public class FSViewerCellEditorFactory implements IViewerCellEditorFactory, FocusListener {
	// The tree viewer to add cell editing.
	private TreeViewer viewer;
	// The cell editors used to rename a file/folder.
	private TextCellEditor cellEditor;
	// The cell modifier used to modify a file/folder's name.
	private ICellModifier cellModifer;

	/**
	 * Create an instance.
	 */
	public FSViewerCellEditorFactory() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.IViewerCellEditorFactory#init(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	public void init(TreeViewer aViewer) {
		viewer = aViewer;
		cellEditor = new TextCellEditor(aViewer.getTree(), SWT.BORDER);
		cellEditor.setValidator(new FSCellValidator(aViewer));
		cellEditor.addListener(new FSCellListener(cellEditor));
		cellModifer = new FSCellModifier();
		Tree tree = aViewer.getTree();
		tree.addFocusListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.IViewerCellEditorFactory#getCellEditors()
	 */
	@Override
	public CellEditor[] getCellEditors() {
		return new CellEditor[] { cellEditor };
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.IViewerCellEditorFactory#getCellModifier()
	 */
	@Override
	public ICellModifier getCellModifier() {
		return cellModifer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.IViewerCellEditorFactory#getColumnProperties()
	 */
	@Override
	public String[] getColumnProperties() {
		return new String[] { FSCellModifier.PROPERTY_NAME };
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
    public void focusGained(FocusEvent e) {
		// Set the currently focused viewer.
		RenameFilesHandler.setCurrentViewer(viewer);
    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
    public void focusLost(FocusEvent e) {
    }
}
