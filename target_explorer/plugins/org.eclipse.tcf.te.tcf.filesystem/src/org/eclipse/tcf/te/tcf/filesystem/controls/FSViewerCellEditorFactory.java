/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.tcf.filesystem.controls;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tcf.te.tcf.filesystem.internal.celleditor.FSCellListener;
import org.eclipse.tcf.te.tcf.filesystem.internal.celleditor.FSCellModifier;
import org.eclipse.tcf.te.tcf.filesystem.internal.celleditor.FSCellValidator;
import org.eclipse.tcf.te.tcf.filesystem.internal.handlers.RenameFilesHandler;
import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tcf.te.ui.views.interfaces.IViewerCellEditorFactory;

/**
 * FSViewerCellEditorFactory implements <code>IViewerCellEditorFactory</code> to add 
 * cell editors to Target Explorer for renaming files or folders in the file system tree viewer.
 */
public class FSViewerCellEditorFactory implements IViewerCellEditorFactory, FocusListener {
	// The column properties used for cell editing.
	private static String[] COLUMN_PROPERTIES = { FSCellModifier.PROPERTY_NAME };
	// The tree viewer to add cell editing.
	private TreeViewer viewer;
	// The cell editors used to rename a file/folder.
	private CellEditor[] cellEditors;
	// The cell modifier used to modify a file/folder's name.
	private ICellModifier cellModifer;

	/**
	 * Create an instance.
	 */
	public FSViewerCellEditorFactory() {
	}
	
	/**
	 * Add the editing support for the specified tree viewer using the cell editing configuration
	 * from this factory.
	 * 
	 * @param viewer The tree viewer to which the editing support is added.
	 */
	public void addEditingSupport(final TreeViewer viewer) {
		// Initialize the tree viewer.
		init(viewer);
		// Define an editor activation strategy for the common viewer so as to be invoked only programmatically.
		ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy(viewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				// Activated only when a single FSTreeNode is selected and invoked programmatically.
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				boolean singleSelect = selection.size() == 1;
				if(singleSelect && event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC){
					Object object = selection.getFirstElement();
					return object instanceof FSTreeNode;
				}
				return false;
			}
		};
		TreeViewerEditor.create(viewer, null, activationStrategy, ColumnViewerEditor.DEFAULT);

		// Set the column properties, the cell editor, and the modifier.
		viewer.setColumnProperties(getColumnProperties());
		viewer.setCellEditors(getCellEditors());
		viewer.setCellModifier(getCellModifier());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.views.interfaces.IViewerCellEditorFactory#init(org.eclipse.jface.viewers.TreeViewer)
	 */
	@Override
	public void init(TreeViewer aViewer) {
		viewer = aViewer;
		TextCellEditor cellEditor = new TextCellEditor(aViewer.getTree(), SWT.BORDER);
		cellEditor.setValidator(new FSCellValidator(aViewer));
		cellEditor.addListener(new FSCellListener(cellEditor));
		cellEditors = new CellEditor[] { cellEditor };
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
		return cellEditors;
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
		return COLUMN_PROPERTIES;
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
